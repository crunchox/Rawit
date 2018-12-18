package com.example.albert.rawit;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MyJobService extends JobService {
    BackgroundTask backgroundTask;
    @Override
    public boolean onStartJob(final JobParameters job) {
        backgroundTask=new BackgroundTask(){
            @Override
            protected void onPostExecute(String s) {
                Log.i("asdasd",s);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.logo_temp)
                                .setContentTitle(s)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText("Dont forget to drink water or eat fruit for your healthy inside, fresh outside!"));
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(001, mBuilder.build());
                jobFinished(job,false);
            }
        };
        backgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
    public static class BackgroundTask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {
            return "You Need Water!";
        }
    }
}
