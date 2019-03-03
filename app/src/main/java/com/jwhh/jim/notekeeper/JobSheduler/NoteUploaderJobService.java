package com.jwhh.jim.notekeeper.JobSheduler;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.jwhh.jim.notekeeper.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    /**    Called on Main thread to start work therefore use Async
     * @class JobParameters gets extras passed for the job
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<JobParameters, Void, Void>  task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParams) {
                JobParameters jobParams = backgroundParams[0];
//get persistable bundle
                String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);

                if( ! mNoteUploader.isCanceled())
                    jobFinished(jobParams, false);//true reschedule

                return null;
            }
        };

        mNoteUploader = new NoteUploader(this);
        //Start job running
        task.execute(params);

        return true;//indicated the job should be rescheduled
    }
    //When job doesnt meet criteria eg no network and dont call jobFinished()
    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();//sets mCancel to true therefore dont upload
        return true;//Indicates that the job should restart

    }

}










