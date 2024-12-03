package com.idrisssouissi.go4lunch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "notification_channel_id";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Créer le canal de notification (nécessaire pour Android 8+)`

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Notification",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        // Construire la notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Icône de la notification
                .setContentTitle("Titre de la notification")
                .setContentText("Ceci est une notification locale.") // Contenu
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Envoyer la notification
        notificationManager.notify(0, notificationBuilder.build());
    }
}
