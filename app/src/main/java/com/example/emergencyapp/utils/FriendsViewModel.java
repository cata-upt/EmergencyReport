package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.util.Log;

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

public class FriendsViewModel extends ViewModel {

    private MutableLiveData<List<User>> friends;
    private List<User> friendsList;

    public FriendsViewModel() {
        friendsList = new ArrayList<>();
        friends = new MutableLiveData<>();
        fetchFriends();
    }

    public LiveData<List<User>> getFriends() {
        return friends;
    }

    private void fetchFriends() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends");
            DatabaseReference friendsRef = databaseReference.child(user.getUid());

            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendsList.clear();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String userId = snapshot.getKey();
                            if (userId != null) {
                                getUserDetails(userId);
                            }
                        }
                    } else {
                        friends.setValue(new ArrayList<>());
                    }
                    Log.i(TAG, "Friend list retrieved successfully.");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to fetch friends list" );
                }
            });
        }
    }

    private void getUserDetails(String userId) {
        UserHelper.getUserDetails(userId, (DatabaseCallback<User>) user->{
            updateUserList(user);
            friends.setValue(new ArrayList<>(friendsList));
        });
    }

    private void updateUserList(User friend) {
        boolean userExists = false;
        for (int i = 0; i < friendsList.size(); i++) {
            if (friendsList.get(i).getUid().equals(friend.getUid())) {
                friendsList.set(i, friend);
                userExists = true;
                break;
            }
        }
        if (!userExists) {
            friendsList.add(friend);
        }
    }
}
