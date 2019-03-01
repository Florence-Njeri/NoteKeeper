package com.jwhh.jim.notekeeper;

import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;

import org.junit.Test;

import static org.junit.Assert.*;


public class DataManagerTest {

    @Test
    public void createNewNote() throws Exception {
        //To create a new note we need course, note title and note text

        DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note Title";
        final String noteText = "Body text of note test";

        int noteIndex = dm.createNewNote();
        //Get the note associated with the index
        NoteInfo newNote = dm.getNotes().get(noteIndex);

        //Now set the title,text and course
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        //Now verify if note will get created by getting the index from the datat manager
        NoteInfo compareNote = dm.getNotes().get(noteIndex);
        //Are they both having equal data
        assertEquals(compareNote.getCourse(),course);
        assertEquals(compareNote.getTitle(),noteTitle);
        assertEquals(compareNote.getText(),noteText);
    }
}