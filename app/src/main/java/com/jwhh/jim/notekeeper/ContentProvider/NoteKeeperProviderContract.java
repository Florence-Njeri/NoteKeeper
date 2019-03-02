package com.jwhh.jim.notekeeper.ContentProvider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract;

public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {
    }


    //TODO content provider authority constants and declare other constants used to access database
    public static final String AUTHORITY = "com.jwhh.jim.notekeeper.provider";
    //Content provider base uri
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * R protected since they are only used in CP tables implementation
     * Identify columns exposed by each table
     * Column constants managed in interfaces--it interfaces groups related columns
     * to access eg -->>Courses.COLUMN_COURSE_TITLE
     */
    protected interface CourseIdColumn {
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CourseColumns {
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NoteColumns {
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";


    }

    //TODO CLASSES TO BE ACCESSED and implement interface to columns they expose
    public static final class Courses implements BaseColumns, CourseColumns, CourseIdColumn {
        public static final String PATH = "courses";
        //content://com.jwhh.jim.notekeeper.provider/courses ie table URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements BaseColumns, NoteColumns, CourseIdColumn,CourseColumns {
        public static final String PATH = "notes";
        //content://com.jwhh.jim.notekeeper.provider/notes
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

        //Table with a list of notes plus the corresponding course title used to populate MainActivity recyclerView
        public static String PATH_EXPANDED = "notes_expanded";
        public static Uri CONTENT_EXPANDED_URI=Uri.withAppendedPath(AUTHORITY_URI,PATH_EXPANDED);

    }
}