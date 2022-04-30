package com.lhester.polendey.drivers.Alarms;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class SetAlarms {
    public static ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();
    public static void AddAlarm(String SID, Context context, int year,int month,int day, int hour, int min){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month,day,hour,min, 0);
        MyAlarm.broadcastCode++;
        Log.e("Set",String.valueOf (calendar.getTimeInMillis()));
        Log.e("YEAR", String.valueOf(calendar.get(Calendar.YEAR)));
        Log.e("MONTH",String.valueOf (calendar.get(Calendar.MONTH)));
        Log.e("DAY",String.valueOf (calendar.get(Calendar.DAY_OF_MONTH)));
        Log.e("HOUR",String.valueOf (hour));
        Log.e("MIN",String.valueOf (min));
        setAlarm(context, calendar.getTimeInMillis(), SID);
    }
    public static void setAlarm(Context context, long time,String SID) {
        MyAlarm.broadcastCode++;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MyAlarm.class);
        i.putExtra("SID",SID);
        PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(), MyAlarm.broadcastCode, i, 0);

        am.setRepeating(AlarmManager.RTC, time, AlarmManager.RTC_WAKEUP, pi);
        intentArray.add(pi);
        Toast.makeText(context.getApplicationContext(), "Arkila Alarm is set", Toast.LENGTH_SHORT).show();
    }
}
