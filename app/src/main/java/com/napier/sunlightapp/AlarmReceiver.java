package com.napier.sunlightapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Class to display notifications at a specified time - called at the set time
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // set intent to show notification and open if the user clicks on the notification:
        Intent alarmIntent = new Intent(context, MainActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // set the notification text:
        String contentStr=context.getString(R.string.dailyReminderContentStr);
        String titleStr = context.getString(R.string.dailyReminderStr);

        // create a notification using the Sunlight notification channel created in Main:
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "SunlightChannel")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(titleStr)
                .setContentText(contentStr)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification:
                .setContentIntent(pendingIntent)
                // make the notification expand so that all the text can be visible:
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentStr))
                .setAutoCancel(true);

        // build notification:
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
