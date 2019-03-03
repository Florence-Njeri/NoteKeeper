package com.jwhh.jim.notekeeper.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jwhh.jim.notekeeper.Notification.NoteReminderNotification;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_ID = "com.jwhh.jim.notekeeper.extra.NOTE_ID";
    public static final String EXTRA_NOTE_TITLE = "com.jwhh.jim.notekeeper.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.jwhh.jim.notekeeper.extra.NOTE_TEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);


        NoteReminderNotification.notify(context, noteText, noteTitle, noteId);
    }
}
