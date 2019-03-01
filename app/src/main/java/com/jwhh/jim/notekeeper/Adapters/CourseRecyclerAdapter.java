package com.jwhh.jim.notekeeper.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jwhh.jim.notekeeper.Activities.NoteActivity;
import com.jwhh.jim.notekeeper.DataClasses.CourseInfo;
import com.jwhh.jim.notekeeper.DataClasses.NoteInfo;
import com.jwhh.jim.notekeeper.R;

import java.util.List;

public class CourseRecyclerAdapter extends RecyclerView.Adapter <CourseRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List <CourseInfo> mCourses;
    private final LayoutInflater mMLayoutInflater;

    public CourseRecyclerAdapter(Context context, List <CourseInfo> courses) {
        mContext = context;
        mMLayoutInflater = LayoutInflater.from(context);
        mCourses = courses;
    }

    @Override
    //Create view and instances of views
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*
        Points to the root of the view being created
        The item view will contain the entire content of the CardView
         */
        View itemView = mMLayoutInflater.inflate(R.layout.item_course, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
    Bind data <4rm database> to views and data handling to the views
     based on position of the data item
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CourseInfo course = mCourses.get(position);
        holder.mTextCourse.setText(course.getTitle());//Get Course title


        //Position of view
        holder.mCurrentPosition = position;

    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //So NoteRecyclerAdapter can reference them
        public final TextView mTextCourse;
        public int mCurrentPosition;

        //Contains ref. to views to be set at run time
        public MyViewHolder(View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);

            //Handle click events based on position
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view,mCourses.get(mCurrentPosition).getTitle(),Snackbar.LENGTH_SHORT).show();
                }
            });


        }
    }
}
