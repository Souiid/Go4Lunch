package com.idrisssouissi.go4lunch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.idrisssouissi.go4lunch.data.FirebaseApiService;
import com.idrisssouissi.go4lunch.data.RestaurantApiService;

import java.io.IOException;
import java.util.concurrent.Executors;

import kotlin.Triple;

public class NotificationWorker extends Worker {

    RestaurantApiService restaurantApiService;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

       SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
       String restaurantID = sharedPreferences.getString("restaurantID", "");

     if (restaurantID.isEmpty()) {
         return Result.success();
     }

     new FirebaseApiService().getUserNamesInRestaurant(restaurantID, names -> {
         Log.d("ppp", "UserNames: " + names);
         Executors.newSingleThreadExecutor().execute(() -> {
             try {
                 Triple<String, String, String> restaurantInfo = new RestaurantApiService().getRestaurantDetailsFromId(restaurantID, true);
                 String restaurantName = restaurantInfo.component1();
                 String address = restaurantInfo.component2();
                 String joinedNames = String.join(", ", names);
                 showNotification(getApplicationContext().getString(R.string.time_to_lunch), getApplicationContext().getString(R.string.dont_forget_to, restaurantName, joinedNames, address));
                Log.d("ppp", "Notification sent: " + getApplicationContext().getString(R.string.time_to_lunch) + getApplicationContext().getString(R.string.dont_forget_to, restaurantName, joinedNames, address));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         });
     });

     return Result.success();
    }


    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "reminder_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_check)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));


        notificationManager.notify(1, notification.build());
    }
}