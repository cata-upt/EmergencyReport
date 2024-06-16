package com.example.emergencyapp.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emergencyapp.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsViewModel extends ViewModel {
    private MutableLiveData<List<User>> friendRequests;
    private List<User> friendRequestsList;

    public FriendRequestsViewModel() {
        friendRequestsList = new ArrayList<>();
        friendRequests = new MutableLiveData<>();
        friendRequests.setValue(new ArrayList<>());
        fetchFriendRequests();
    }

    public LiveData<List<User>> getFriendRequests() {
        return friendRequests;
    }

    private void fetchFriendRequests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests");
            DatabaseReference friendsRef = databaseReference.child(user.getUid());
            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendRequestsList.clear();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userId = snapshot.getKey();
                            if (userId != null) {
                                getPendingFriendRequests(user.getUid(), userId);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            friendRequests.setValue(new ArrayList<>());
        }
    }

    private void getPendingFriendRequests(String userId, String friendId) {
        UserHelper.findFriendRequest(userId, friendId, (DataCallback<String>) status -> {
            switch (status) {
                case "pending":
                    getUserDetails(friendId);
                    break;
                case "accepted":
                case "rejected":
                case "not_sent":
                case "unknown":
                case "error":
                    break;
            }
        });
    }

    private void getUserDetails(String userId) {
        UserHelper.getUserDetails(userId, (DataCallback<User>) user -> {
            updateUserList(user);
            friendRequests.setValue(new ArrayList<>(friendRequestsList));
        });
    }

    private void updateUserList(User friend) {
        boolean userExists = false;
        for (int i = 0; i < friendRequestsList.size(); i++) {
            if (friendRequestsList.get(i).getUid().equals(friend.getUid())) {
                friendRequestsList.set(i, friend);
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            friendRequestsList.add(friend);
        }
    }
}

