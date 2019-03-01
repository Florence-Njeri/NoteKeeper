package com.jwhh.jim.notekeeper;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.jwhh.jim.notekeeper.Activities.NoteListActivity;
import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.*;

//So as to run with androidJUNITRunner
@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception {
        //Get reference to our course selected on the spinner
        sDataManager = DataManager.getInstance();

    }

    //Activity test rule for the Activity where the testing will take place
    //Clicking new note button happens in the NoteListActivity
    @Rule
    public ActivityTestRule <NoteListActivity> mNoteListActivityActivityRule =
            new ActivityTestRule <>(NoteListActivity.class);

    //Now write the UI test for what will happen when the new note is being created

    /*@method onView specifies what should happen to a specific view <ViewMatchers>
@method onData specifies the target adapter data eg selecting data from adapter <Hamcrest matchers>
@method onBack pushes the back button
@method check assertions against the view
@method matches confirms that the view actually exists
@doesNoeExist confirms that the view doesnt exist
 */
    @Test
    public void createNewNote() {
        final CourseInfo course = sDataManager.getCourse("java_lang");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of our test note";
        //Get to the specific view and perform an action on it
        onView(withId(R.id.fab)).perform(click());
        //Select course from spinner,Type title and text to the new note
        onView(withId(R.id.spinner_courses)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class), equalTo(course))).perform(click());
        //Verify spinner
        onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(
                containsString(course.getTitle())
        )));

        onView(withId(R.id.text_note_title)).perform(typeText(noteTitle))
        .check(matches(withText(containsString(noteTitle))));
        onView(withId(R.id.text_note_text)).perform(typeText(noteText),
                closeSoftKeyboard());

//Verify the expected
        onView(withId(R.id.text_note_title )).check(matches(withText(containsString(noteTitle))));
        onView(withId(R.id.text_note_text)).check(matches(withText(containsString(noteText))));

        pressBack();
        int noteIndex=sDataManager.getNotes().size()-1;
        NoteInfo note= sDataManager.getNotes().get(noteIndex);
        assertEquals(course,note.getCourse());
        assertEquals(noteTitle,note.getTitle());
        assertEquals(noteText,note.getText());
    }
}