package com.napier.sunlightapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Class to set the alarm for notifications after the device reboots.
 */
public class DeviceBootReceiver extends BroadcastReceiver {
    // store user settings:
    public static boolean notificationEnabled=true;
    public static int hour=11;
    @Override
    public void onReceive(Context context, Intent intent) {
        // check if boot completed and notifications are enabled:
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") && notificationEnabled) {
            // on device boot complete, reset the alarm
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}
