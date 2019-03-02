package com.jwhh.jim.notekeeper.ContentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Courses;
import com.jwhh.jim.notekeeper.ContentProvider.NoteKeeperProviderContract.Notes;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.jim.notekeeper.Database.NoteKeeperOpenHelper;

//TODO handle all database related work and connection
public class NoteKeeperProvider extends ContentProvider {
    private static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    private NoteKeeperOpenHelper mDbOpenHelper;
    //todo URI matcher
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);//for uri without authority

    private static final int COURSES = 0;

    private static final int NOTES = 1;

    private static final int NOTES_EXPANDED = 2;

    private static final int NOTES_ROW = 3;
    private static final int COURSES_ROW = 4;
    private static final int NOTES_EXPANDED_ROW = 5;

    //add valid URIs using a static initializer
    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        //join courseand notes tables
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH + "/#", COURSES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED + "/#", NOTES_EXPANDED_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
String mimeType=null;

int uriMatch=sUriMatcher.match(uri);
switch(uriMatch){
    case COURSES:
//vnd.android.cursor.dir/vnd.com.jwhh.jim.notekeeper.provider.courses
        mimeType= ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                MIME_VENDOR_TYPE + Courses.PATH;
        break;
        case NOTES:
//vnd.android.cursor.dir/vnd.com.jwhh.jim.notekeeper.provider.courses
            mimeType= ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                    MIME_VENDOR_TYPE + Notes.PATH;
        break;
        case NOTES_EXPANDED:
//vnd.android.cursor.dir/vnd.com.jwhh.jim.notekeeper.provider.courses
            mimeType= ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                    MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;

        break;
        case NOTES_ROW:
//vnd.android.cursor.item/item.com.jwhh.jim.notekeeper.provider.courses
            mimeType= ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                    MIME_VENDOR_TYPE + Notes.PATH;

        break;
}
    return mimeType;
    }
/*
Receives uri and ContentValues from ContentResolver insert method
Use UriMatcher to determine target db table
Insert into SQLITE DB
Return URI of newly created row
 */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);

                //content://com.jwhh.jim.notekeeper.provider/notes/1
                rowUri=ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                    break;
                case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);

                //content://com.jwhh.jim.notekeeper.provider/courses/1
                    //@method ContentUris.withAppendedId helps create row URI
                rowUri=ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                     break;
                case NOTES_EXPANDED:
                //Throw read only exception
                     break;
        }
        return rowUri;
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
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
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
 break;
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);

                break;
                case NOTES_ROW:
               rowId=  ContentUris.parseId(uri);
                rowSelection=NoteInfoEntry._ID + " = ?";
                 rowSelectionArgs=new String[] {Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,
                        null,null,null);
                break;
            case NOTES_EXPANDED_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry.getQName(NoteInfoEntry._ID) + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = notesExpandedQuery(db, projection, rowSelection, rowSelectionArgs, null);
                break;
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
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }
}
