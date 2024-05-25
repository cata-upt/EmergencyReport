package com.example.emergencyapp.activities;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.AlertMessage;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.DatabaseCallback;
import com.example.emergencyapp.utils.MessageAdapter;
import com.example.emergencyapp.utils.UserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView nameTextView;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<AlertMessage> alertMessageList;
    private String receiverId;
    private String receiverName;
    private String receiverProfileImageUrl;
    private Boolean isActivityVisible;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        profileImageView = findViewById(R.id.profile_image);
        nameTextView = findViewById(R.id.profile_name);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        alertMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, alertMessageList);
        recyclerViewMessages.setAdapter(messageAdapter);

        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");
        receiverProfileImageUrl = getIntent().getStringExtra("receiverProfileImageUrl");

        if (receiverName == null || receiverProfileImageUrl == null) getReceiverDetails();
        else populateFriendFields();
        loadMessagesForUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    private void populateFriendFields() {

        nameTextView.setText(receiverName);
        Picasso.get().load(receiverProfileImageUrl)
                .placeholder(R.drawable.baseline_person_24)
                .into(profileImageView);
    }

    private void getReceiverDetails() {
        UserHelper.getUserDetails(receiverId, (DatabaseCallback<User>) receiver -> {
            receiverName = receiver.getName();
            receiverProfileImageUrl = receiver.getProfileImageUrl();
            populateFriendFields();
        });
    }

    private void loadMessagesForUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && receiverId != null) {
            String chatRoomId = getChatRoomId(currentUser.getUid(), receiverId);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    alertMessageList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        AlertMessage alertMessage = snapshot.getValue(AlertMessage.class);
                        if (alertMessage != null) {
                            if (!alertMessage.isRead() && !alertMessage.getSenderId().equals(currentUser.getUid())) {
                                alertMessage.setRead(true);
                            }
                            alertMessage.setMessageId(snapshot.getKey());
                            alertMessageList.add(alertMessage);
                            Log.i(TAG, "onDataChange: alert message added:" + alertMessage.getMessage());
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerViewMessages.scrollToPosition(alertMessageList.size() - 1);
                    if (isActivityVisible) markMessagesAsRead();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to retrieve messages for user " + currentUser.getUid());
                }
            });
        }
    }

    private String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    private void markMessagesAsRead() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && receiverId != null) {
            String chatRoomId = getChatRoomId(currentUser.getUid(), receiverId);
            for (AlertMessage alertMessage : alertMessageList) {
                DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId).child(alertMessage.getMessageId());
                if (alertMessage.isRead()) {
                    messageRef.child("read").setValue(true).addOnSuccessListener(aVoid -> {
                        // Successfully updated
                    }).addOnFailureListener(e -> {
                        // Handle the error
                    });
                }
            }
        }
    }

}
