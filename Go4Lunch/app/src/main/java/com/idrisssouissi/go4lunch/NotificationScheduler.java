package com.idrisssouissi.go4lunch;

import android.content.Context;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    public static void scheduleNotification(Context context) {
       Calendar now = Calendar.getInstance();
       Calendar targetTime = Calendar.getInstance();

       //TODO: Changer l'heure de la notif ici
       targetTime.set(Calendar.HOUR_OF_DAY, 12);
       targetTime.set(Calendar.MINUTE, 0);
       targetTime.set(Calendar.SECOND, 0);

       if (now.get(Calendar.HOUR_OF_DAY) >= 15) {
           targetTime.add(Calendar.DAY_OF_YEAR, 1);
       }

       long delay = targetTime.getTimeInMillis() - now.getTimeInMillis();

       OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
               .setInitialDelay(delay, TimeUnit.MILLISECONDS)
               .build();

       WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelNotification(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }
}