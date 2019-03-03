package com.jwhh.jim.notekeeper.Activities;
// TODO: Open the note from the list

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.jwhh.jim.notekeeper.BroadcastReceiver.CourseEventBroadcastReceiver;
import com.jwhh.jim.notekeeper.BroadcastReceiver.NoteReminderReceiver;
import com.jwhh.jim.notekeeper.BuildConfig;
import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.DataManager;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperOpenHelper;
import com.jwhh.jim.notekeeper.Notification.NoteReminderNotification;
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
    private Uri mNoteUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //StrictMode
        enableStrictMode();
        mDbOpenhelper = new NoteKeeperOpenHelper(this);
        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
//Populate with cursor based data from the content provider
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

    private void enableStrictMode() {
        //Only run when debugging or testing
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
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
                //Delete the note that was being created
                deleteNoteFromDatabase();
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

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};
        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenhelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                return null;
            }
        };
        task.execute();
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
        //Update our DB
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();//Set the title to:
        String noteText = mTextNoteText.getText().toString();//Set the text to:
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        //Get ref to cursor associated to the spinner
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }


    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {

//Identify the columns and their values
        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, courseId);
        values.put(Notes.COLUMN_NOTE_TITLE, noteTitle);
        values.put(Notes.COLUMN_NOTE_TEXT, noteText);
        //Connect to content provider and parse uri to the note

        getContentResolver().update(mNoteUri, values, null, null);

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

        CourseEventBroadcastReceiver.sendEventBroadcast(this,courseId,"Editing Note");
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
//   Background work to avoid ANR
        @SuppressLint("StaticFieldLeak")
        AsyncTask <ContentValues, Integer, Uri> task = new AsyncTask <ContentValues, Integer, Uri>() {

            ProgressBar mProgressBar;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(1);
                super.onPreExecute();
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues insertValues = contentValues[0];
                //Perform insert using the content resolver which returns back a uri ie the ContentProvider's
//   insert() receives the uri and ContentValues passed into this ContentResolver
                Uri rowUri = getContentResolver().insert(Notes.CONTENT_URI, insertValues);//this insert() leads to insert() of C.P getting called
              simulateLongRunningWork();
               publishProgress(2);
               simulateLongRunningWork();
               publishProgress(3);
                return rowUri;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue=0;
                mProgressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mNoteUri = uri;
                displaySnackbar(mNoteUri.toString());
                mProgressBar.setVisibility(View.GONE);
            }
        };
        //Insert a new row into the database

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");
//Instantiate background work
        task.execute(values);

    }
    private void displaySnackbar(String message) {
        View view = findViewById(R.id.spinner_courses);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(Exception ex) {}
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
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
//Instantiate a notification
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        int noteId = (int) ContentUris.parseId(mNoteUri);
        //Use Alarm Manager to fire after some time elapses
        Intent intent=new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE,noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT,noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID,noteId);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);

      long currentTimeInMilliseconds=  SystemClock.elapsedRealtime();
      long ONE_HOUR=  5*1000;//i hour in milliseconds
      long alarmTime=currentTimeInMilliseconds+ONE_HOUR;//Fire notification after one hour
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,alarmTime,pendingIntent);
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
        //Uri to content provider
        Uri uri = Courses.CONTENT_URI;
        //Load courses title data for populating the spinner

        String[] courseColumns = {
                Courses.COLUMN_COURSE_ID,
                Courses.COLUMN_COURSE_TITLE,
                Courses._ID
        };
        //Find the cursor loader and perform query the results in curser will be received in loadFinished()
        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);

    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        //Access notes from the Content provider
        mNoteQueryFinished = false;
        //Load notes data using CursorLoader's loadInBackground interface

        String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT};
//URI to query for the row Uri
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);


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












