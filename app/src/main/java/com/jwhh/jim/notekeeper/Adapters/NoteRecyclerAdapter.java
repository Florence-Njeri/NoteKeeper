package com.jwhh.jim.notekeeper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jwhh.jim.notekeeper.Activities.NoteActivity;
import com.jwhh.jim.notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.jwhh.jim.notekeeper.R;

public class NoteRecyclerAdapter extends RecyclerView.Adapter <NoteRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;
    /*
    Use a cursor to populate main Activity recycler view instead of using DataManager ArrayList
     Now pass cursor to constructor
     */

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (mCursor == null)
            return;


        //Get column index from mCursor and display data coming from cursor
        //Populate views from our adapter
        mCoursePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);

    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();//close the old cursor
            mCursor=cursor;//New cursor
            populateColumnPositions();
            notifyDataSetChanged();
        }

    @Override
    //Create view and instances of views
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*
        Points to the root of the view being created
        The item view will contain the entire content of the CardView
         */
        View itemView = mLayoutInflater.inflate(R.layout.item_note, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
    Bind data <4rm database> to views and data handling to the views
     based on position of the data item
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String course=mCursor.getString(mCoursePos);
        String noteTitle=mCursor.getString(mNoteTitlePos);
        int id=mCursor.getInt(mIdPos);

        holder.mTextCourse.setText(course);//Get Course title
        holder.mTextTitle.setText(noteTitle);//Get Note title
        holder.mId=id;//Get Note id



    }

    @Override
    public int getItemCount() {
        return mCursor==null? 0 :mCursor.getCount();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //So NoteRecyclerAdapter can reference them
        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mId;

        //Contains ref. to views to be set at run time
        public MyViewHolder(View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);

            //Handle click events based on position
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent noteIntent = new Intent(mContext, NoteActivity.class);
                    noteIntent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(noteIntent);
                }
            });


        }
    }
}
