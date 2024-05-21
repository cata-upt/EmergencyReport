package com.example.emergencyapp.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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

    private MutableLiveData<List<User>> friendRequests;
    private List<User> friendRequestsList;

    public FriendsViewModel() {
        friendRequestsList = new ArrayList<>();
        friendRequests = new MutableLiveData<>();
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
                            String userId = snapshot.getValue(String.class);
                            if (userId != null) {
                                getUserDetails(userId);
                            }
                        }
                    } else {
                        friendRequests.setValue(new ArrayList<>());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle database error
                }
            });
        }
    }

    private void getUserDetails(String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend != null) {
                        friendRequestsList.add(friend);
                        friendRequests.setValue(new ArrayList<>(friendRequestsList));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
