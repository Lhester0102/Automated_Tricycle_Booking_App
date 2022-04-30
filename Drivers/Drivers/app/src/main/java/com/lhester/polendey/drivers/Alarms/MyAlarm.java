package com.lhester.polendey.drivers.Alarms;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.lhester.polendey.drivers.LogInDriver;
import com.lhester.polendey.drivers.R;

public class MyAlarm extends BroadcastReceiver {
    public static int broadcastCode=0;
    String SID="";
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        SID = bundle.getString("SID");
        Log.d("MyAlarmBelal", "Alarm just fired");
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(5000);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context);
        }
        showNotification(context,"Scheduled Arkila","5 minutes before the accepted Arkila "+SID + " "+broadcastCode);


    }
    private void showNotification(Context context, String title, String body) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, LogInDriver.class);
        intent.putExtra("SID",SID);

        //pass the same channel_id which we created in previous method
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), "channel_id")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.driver_icon)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        notificationManager.notify(1, builder.build());

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(Context context) {
        //channel_id should be unique for every notification channel
        NotificationChannel notificationChannel = new NotificationChannel("channel_id", "Test Notification Channel",
                NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription("My test notification channel");

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }

}