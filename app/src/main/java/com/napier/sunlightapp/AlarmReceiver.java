package com.napier.sunlightapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Class to display notifications at a specified time
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        String contentStr=context.getString(R.string.dailyReminderContentStr);
        String titleStr = context.getString(R.string.dailyReminderStr);

        // create a notification using the Sunlight notification channel created in Main:
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "SunlightChannel")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(titleStr)
                .setContentText(contentStr)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentStr))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        // build notification:
        notificationManager.notify(1, builder.build());
    }
}
