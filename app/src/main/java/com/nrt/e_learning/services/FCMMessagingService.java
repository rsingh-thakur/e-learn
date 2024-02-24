package com.nrt.e_learning.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import com.nrt.e_learning.PlayerActivity;
import com.nrt.e_learning.R;

public class FCMMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.

        if (remoteMessage.getNotification()!=null){
            // Show the notification
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationTitle = remoteMessage.getNotification().getTitle();
            Uri imageUrl = remoteMessage.getNotification().getImageUrl();
            String videoUrl = remoteMessage.getData().get("videoUrl");
//            Log.i("vidoex",videoUrl);
            sendNotification (notificationTitle, notificationBody, imageUrl, videoUrl);

        }
    }


    public void sendNotification(String title, String body, Uri url, String videoUrl) {
        // Send a notification to all devices
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully subscribed to the "all" topic

                        sendNotificationToTopic("all", title, body,url,videoUrl);
                    } else {
                        // Subscription to "all" topic failed
                        // Handle the error
                    }
                });
    }

    private void sendNotificationToTopic(String topic, String title, String body, Uri imageUrl, String videoURL) {

        Intent intent = new Intent(this.getApplicationContext(), PlayerActivity.class);
        intent.putExtra("videoUrl", videoURL); // Pass the video URL to PlayerActivity
        intent.putExtra("videoTitle", title) ;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_MUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Load the image using Glide
        Bitmap bitmap = null;
        try {
            FutureTarget<Bitmap> futureTarget = Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .submit();
            bitmap = futureTarget.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.bell)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);


        // Set the large icon (image) for the notification
        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since Android Oreo, notification channel is needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Send the notification to the specified topic
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(topic)
                .setMessageId(Integer.toString(0))
                .addData("title", title)
                .addData("body", body)
                .addData("image", String.valueOf(imageUrl))
                .addData("videoUrl", videoURL)
                .build());

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());

    }
    public interface NotificationSender {
        void sendNotification(String title, String body ,Uri imageURl);
    }

}
