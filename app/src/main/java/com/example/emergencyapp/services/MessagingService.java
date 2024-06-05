package com.example.emergencyapp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.AddFriendActivity;
import com.example.emergencyapp.activities.ChatActivity;
import com.example.emergencyapp.activities.FriendsListActivity;
import com.example.emergencyapp.activities.MainActivity;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.DatabaseCallback;
import com.example.emergencyapp.utils.UserHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage.getData());
        }
    }

    private void sendNotification(Map<String, String> data) {
        String title = data.get("title");
        String messageBody = data.get("body");
        String targetActivity = data.get("targetActivity");
        Intent intent;
        if ("ChatActivity".equals(targetActivity)) {
            intent = new Intent(this, ChatActivity.class);
            String senderId = data.get("senderId");
            intent.putExtra("receiverId", senderId);
            intent = populateSenderDetails(senderId, intent);
        } else if ("FriendRequests".equals(targetActivity)) {
            intent = new Intent(this, FriendsListActivity.class);
            intent.putExtra("fragment", data.get("fragment"));
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        String channelId = "EmergencyReportChannelID";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.baseline_notification_important_24)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    private Intent populateSenderDetails(String senderId, Intent intent) {
        UserHelper.getUserDetails(senderId, (DatabaseCallback<User>) user -> {
            intent.putExtra("receiverName", user.getName());
            intent.putExtra("receiverProfileImageUrl", user.getProfileImageUrl());
        });

        return intent;
    }
}
