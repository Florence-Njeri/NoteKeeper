package com.jwhh.jim.notekeeper.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastReceiver {
    public static final String ACTION_COURSE_EVENT="com.jwhh.jim.notekeeper.action.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID="com.jwhh.jim.notekeeper.extra.COURSE_EVENT";
    public static final String EXTRA_COURSE_MESSAGE="com.jwhh.jim.notekeeper.extra.COURSE_EVENT";

public static void sendEventBroadcast(Context content,String courseId,String message){
    Intent intent=new Intent(ACTION_COURSE_EVENT);
    intent.putExtra(EXTRA_COURSE_ID,courseId);
    intent.putExtra(EXTRA_COURSE_MESSAGE,message);

    content.sendBroadcast(intent);

}


}

