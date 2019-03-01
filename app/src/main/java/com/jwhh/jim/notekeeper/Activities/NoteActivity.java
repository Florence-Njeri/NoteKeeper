package com.jwhh.jim.notekeeper.Activities;
// TODO: Open the note from the list
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.DataManager;
import com.jwhh.jim.notekeeper.R;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.jwhh.jim.notekeeper.NOTE_POSITION";
    private  final String TAG = getClass().getSimpleName();
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValues();
        //Making data persistent<Doesn't change>
        if(savedInstanceState == null) {
            //When the activity is being newly created ie opening note from list first time
            saveOriginalNoteValues();
        } else {
            //For an existing activity <being recreated> ie left noteActivity to email
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        if(!mIsNewNote)
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        Log.d(TAG,"onCreate" );
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
        if(mIsNewNote) {
            return;//Dont do anything if its a new note
        }
        else {//Save the data contained as the original
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
        if(mIsCancelling) {
            Log.i(TAG," Cancelling note at position" + mNotePosition);

            if(mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            }
            else {
                //When its not a new note handle discarding if there's new data entered when user hits cancel
                storePreviousNoteValues();
            }

        }
        //Save note edited if its not a new note when leaving activity
        else {
            saveNote();
        }
        Log.d(TAG,"onPause" );

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

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }
/*
TODO:read the content of the selected note from list passed from intent extras
 */
    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = mNotePosition== POSITION_NOT_SET;
        if(mIsNewNote) {
            createNewNote();
        }
            //Get position of the item selected
            mNote = DataManager.getInstance().getNotes().get(mNotePosition);

    }
//TODO: Handle the creation of a new note
    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
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

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() +"\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}











