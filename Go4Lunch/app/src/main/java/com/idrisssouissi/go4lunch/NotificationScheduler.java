package com.idrisssouissi.go4lunch;

import android.content.Context;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {

    private static final String WORK_ID = "notification_work";

    public static void scheduleNotification(Context context) {
        Calendar now = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, 11);
        targetTime.set(Calendar.MINUTE, 35);
        targetTime.set(Calendar.SECOND, 0);

        if (targetTime.before(now)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = targetTime.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
    }

    public static void cancelNotification(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
    }
}