package com.example.zandut.customcalenderview;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by ZANDUT on 5/20/2017.
 */

public class ServiceReceiver extends BroadcastReceiver
{


    @Override
    public void onReceive(Context context, Intent intent)
    {

        intent= new Intent(context, MyService.class);
        context.startService(intent);


    }
}
