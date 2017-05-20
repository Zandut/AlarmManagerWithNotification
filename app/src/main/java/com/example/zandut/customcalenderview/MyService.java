package com.example.zandut.customcalenderview;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by ZANDUT on 5/20/2017.
 */

public class MyService extends IntentService
{

    public MyService()
    {
        super("Custom Calender View Service");
    }


    Handler handler = new Handler();

    @Override
    protected void onHandleIntent(@Nullable final Intent intent)
    {

        long[] pattern = {0, 300, 0};
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.putExtra("waktu", SystemClock.elapsedRealtime() + 5 * 1000);
        PendingIntent pi = PendingIntent.getActivity(MyService.this, 01234, intent1, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MyService.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(intent.getExtras().getString("title"))
                .setContentText("Take questionnaire for Duke Mood Study.")
                .setVibrate(pattern)
                .setAutoCancel(true);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());





    }
}
