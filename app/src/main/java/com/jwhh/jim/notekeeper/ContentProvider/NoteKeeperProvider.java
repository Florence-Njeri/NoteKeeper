package com.jwhh.jim.notekeeper.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Note;

import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperOpenHelper;

//TODO handle all database related work and connection
public class NoteKeeperProvider extends ContentProvider {
    private NoteKeeperOpenHelper mDbOpenHelper;
    //todo URI matcher
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);//for uri without authority

    private static final int COURSES = 0;

    private static final int NOTES = 1;

    private static final int NOTES_EXPANDED = 2;

    //add valid URIs using a static initializer
    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        //join courseand notes tables
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);

    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Instantiate open helper and create a content provider
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        //return true if provider gets successfully created
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients. ie DB
        Cursor cursor = null;
        //Connect to DB
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                //Query database for course titles thus this query can be used to populate spinner
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES:
                //Query database for note to open in NoteActivity
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case NOTES_EXPANDED:
                //Query database for notes and courses
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);

        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//ID to use whether from the course or notse database
        String[] columns = new String[projection.length];
        for (int index = 0; index < projection.length; index++) {
            //Loop for each projection ie columns array
            columns[index] = projection[index].equals(BaseColumns._ID) ||
                    projection[index].equals(NoteKeeperProviderContract.CourseIdColumn.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[index]) : projection[index];
        }
        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        return db.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
