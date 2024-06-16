package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emergencyapp.entities.AlertMessage;
import com.example.emergencyapp.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FriendsViewModel extends ViewModel {

    private MutableLiveData<List<FriendItem>> friends;
    private List<FriendItem> friendsList;

    FirebaseUser user;

    public FriendsViewModel() {
        friendsList = new ArrayList<>();
        friends = new MutableLiveData<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        fetchFriends();
    }

    public LiveData<List<FriendItem>> getFriends() {
        return friends;
    }

    public void fetchFriends() {
        if (user != null) {
            UserHelper.getFriendsList(user.getUid(), (DataCallback<List<String>>) friendIds -> {
                if (friendIds != null) {
                    for (String userId : friendIds) {
                        getUserDetails(userId);
                    }
                    Log.i(TAG, "Friend list retrieved successfully.");
                } else {
                    friends.setValue(new ArrayList<>());
                }
            });
        } else {
            friends.setValue(new ArrayList<>());
        }
    }

    private void getUserDetails(String userId) {
        if (user != null) {
            UserHelper.getUserDetails(userId, (DataCallback<User>) user -> {
                fetchLastMessage(user.getUid(), user.getName(), user.getProfileImageUrl());
            });
        }
    }

    private void fetchLastMessage(String friendId, String name, String profileImageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String chatRoomId = getChatRoomId(currentUser.getUid(), friendId);
            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId);

            messagesRef.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            AlertMessage message = snapshot.getValue(AlertMessage.class);
                            if (message != null) {
                                boolean hasUnreadMessages = !message.isRead() && !message.getSenderId().equals(currentUser.getUid());
                                String lastMessage = hasUnreadMessages ? name + ": " + message.getMessage() : "You" + ": " + message.getMessage();
                                FriendItem friendItem = new FriendItem(friendId, name, profileImageUrl, lastMessage, message.getTimestamp(), hasUnreadMessages);
                                updateUserList(friendItem);
                                friends.setValue(new ArrayList<>(friendsList));
                            }
                        }
                    } else {
                        FriendItem friendItem = new FriendItem(friendId, name, profileImageUrl, "No messages yet", 0, false);
                        updateUserList(friendItem);
                        friends.setValue(new ArrayList<>(friendsList));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    public String getChatRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    private void updateUserList(FriendItem friend) {
        boolean userExists = false;
        for (int i = 0; i < friendsList.size(); i++) {
            if (friendsList.get(i).getFriendId().equals(friend.getFriendId())) {
                friendsList.set(i, friend);
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            friendsList.add(friend);
        }
        friendsList.sort(Comparator.comparingLong(FriendItem::getTimestamp).reversed());
    }
}
