package com.jwhh.jim.notekeeper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jwhh.jim.notekeeper.Activities.NoteActivity;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.R;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter <NoteRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List <NoteInfo> mNotes;
    private final LayoutInflater mMLayoutInflater;
    public static String COURSE_TITLE = "com.jwhh.jim.notekeeper.COURSE_TITLE";

    public NoteRecyclerAdapter(Context context, List <NoteInfo> notes) {
        mContext = context;
        mMLayoutInflater = LayoutInflater.from(context);
        mNotes = notes;
    }

    @Override
    //Create view and instances of views
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*
        Points to the root of the view being created
        The item view will contain the entire content of the CardView
         */
        View itemView = mMLayoutInflater.inflate(R.layout.item_note, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
    Bind data <4rm database> to views and data handling to the views
     based on position of the data item
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NoteInfo note = mNotes.get(position);
        holder.mTextCourse.setText(note.getCourse().getTitle());//Get Course title
        holder.mTextTitle.setText(note.getTitle());//Get Note title

        //Position of view
        holder.mCurrentPosition = position;

    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //So NoteRecyclerAdapter can reference them
        public final TextView mTextCourse;
        public final TextView mTextTitle;
        public int mCurrentPosition;

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
                    noteIntent.putExtra(NoteActivity.NOTE_POSITION, mCurrentPosition);
                    mContext.startActivity(noteIntent);
                }
            });


        }
    }
}
