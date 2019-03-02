package com.jwhh.jim.notekeeper.Service;

import android.app.IntentService;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NoteBackupService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    public static final String EXTRA_COURSE_ID= "com.jwhh.jim.notekeeper.Service.extra.COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }
//REceives ref to intent used to start the service
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
//Get value of course_id extra
            String backUpCourseId=intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this,backUpCourseId);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

}
