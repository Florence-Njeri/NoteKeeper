package com.jwhh.jim.notekeeper.Activities;
// TODO: Open the note from the list

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.DataManager;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperOpenHelper;
import com.jwhh.jim.notekeeper.R;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks <Cursor> {
    public static final String NOTE_ID = "com.jwhh.jim.notekeeper.NOTE_ID";
    private static final int LOADER_NOTES = 0;
    private static final int LOADER_COURSES = 1;
    private final String TAG = getClass().getSimpleName();
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbOpenhelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mNoteId;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNoteQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDbOpenhelper = new NoteKeeperOpenHelper(this);
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
//Populate with cursor based data from the database
        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        //Use CursorLoader to query for Courses Data
        getLoaderManager().initLoader(LOADER_COURSES, null, this);
        readDisplayStateValues();
        //Making data persistent<Doesn't change>
        if (savedInstanceState == null) {
            //When the activity is being newly created ie opening note from list first time
            saveOriginalNoteValues();
        } else {
            //For an existing activity <being recreated> ie left noteActivity to email
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if (!mIsNewNote)
//            load note data on a background thread
            getLoaderManager().initLoader(LOADER_NOTES, null, this);
        Log.d(TAG, "onCreate");
    }


    @Override
    protected void onDestroy() {
        mDbOpenhelper.close();
        super.onDestroy();
    }


    //Restore the original note value ,values before user left activity>
// from the savedInstance state when user resumes activity
    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    //TODO: Save the original state of the note when the user starts a NoteActivity
    private void saveOriginalNoteValues() {
        if (mIsNewNote) {
            return;//Dont do anything if its a new note
        } else {//Save the data contained as the original
            mOriginalNoteCourseId = mNote.getCourse().getCourseId();
            mOriginalNoteTitle = mNote.getTitle();
            mOriginalNoteText = mNote.getText();
        }
    }

    //TODO: What happen s to the note when user is leaving the note activity
    @Override
    protected void onPause() {
        super.onPause();
        //Handle user cancellation
        if (mIsCancelling) {
            Log.i(TAG, " Cancelling note at position" + mNoteId);

            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNoteId);
            } else {
                //When its not a new note handle discarding if there's new data entered when user hits cancel
                storePreviousNoteValues();
            }

        }
        //Save note edited if its not a new note when leaving activity
        else {
            saveNote();
        }
        Log.d(TAG, "onPause");

    }

    //TODO: Discard any changes made to the existing note if user is cancelling
    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);//Get the id of original course then ...
        mNote.setCourse(course);//Set the course to display to original
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    //TODO:Save the note if it gets edited/a new note added
    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());//Set spinner
        mNote.setTitle(mTextNoteTitle.getText().toString());//Set the title to:
        mNote.setText(mTextNoteText.getText().toString());//Set the text to:
    }

    private void displayNote() {
        //Fetching data of a selected note from the cursor returned

        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);
//Use values returned from cursor opposed to the mNote list from the cursor

        int courseIndex = getIndexOfCourseId(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseId) {
        //Access the cursor populating the spinner to get appropriate course for the current note
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;
        boolean more = cursor.moveToFirst();
        while (more) {
            //Get the value of the first row in the cursor
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId)) {
                //Found correct row
                break;
            } else {
                courseRowIndex++;
                more = cursor.moveToNext();
            }
        }
        return courseRowIndex;
    }

    /*
    TODO:read the content of the selected note from list passed from intent extras
     */
    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
        //Get note of the item selected from the database

    }

    //TODO: Handle the creation of a new note
    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNoteId = dm.createNewNote();
//        mNote = dm.getNotes().get(mNotePosition);//Get the position off the new note in the array
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle bundle) {
        //Called to create the loader for the notes and it returns cursor
        //Query data using CursorLoader ,must override loadInBackground
        //Issue database Query
        CursorLoader loader = null;
        //Query note data
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
            //Query courses to Populate the spinner
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();
        return loader;
            /*
            However to avoid program crashing due to both queries running at the same time
                     we will validate to ensure you only display the note when both queries are finished
             */
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        //Load courses data
        return new CursorLoader(this) {
            @Override
            protected Cursor onLoadInBackground() {
                SQLiteDatabase db = mDbOpenhelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };

                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        mNoteQueryFinished = false;
        //Load notes data using CursorLoader's loadInBackground interface
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                //        Query db
                SQLiteDatabase db = mDbOpenhelper.getReadableDatabase();
                //Query criteria

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mNoteId)};
                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null, null, null);
            }
        };


    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {
        //When data is ready receivers the loader from the onCreate method's return value
        //Verify loader
        if (loader.getId() == LOADER_NOTES)
            //Use the notes returned by cursor after the query to display the notes
            loadFinishedNotes(data);

        else if (loader.getId() == LOADER_COURSES)
            loadFinishedCourses(data);
    }

    private void loadFinishedCourses(Cursor data) {
        mAdapterCourses.changeCursor(data);
        mCoursesQueryFinished = true;
        displaynoteWhenQueryIsFinished();

    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        //Display the notes in the Note Activity
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
//Move to the first row in the results
        mNoteCursor.moveToNext();
        mNoteQueryFinished = true;
        displaynoteWhenQueryIsFinished();
    }

    private void displaynoteWhenQueryIsFinished() {
        if (mNoteQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
//Clean up the cursor when returning back to the MainActivity from NoteActivity
        if (loader.getId() == LOADER_NOTES)
            if (mNoteCursor != null)
                mNoteCursor.close();
            else if (loader.getId() == LOADER_COURSES)
                mAdapterCourses.changeCursor(null);
    }


}












