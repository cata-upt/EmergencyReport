package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.entities.UserLocation;
import com.example.emergencyapp.exceptions.UserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserHelper {

    public static boolean validateUser(User user) throws UserException {
        return validateName(user.getName()) &&
                validateUsername(user.getUsername()) &&
                validateEmail(user.getEmail()) &&
                validatePassword(user.getPassword());
    }

    private static boolean validateName(String name) throws UserException {
        if (name.isEmpty()) {
            throw new UserException("The name is not valid!");
        }
        return true;
    }

    private static boolean validateUsername(String username) throws UserException {
        String noWhiteSpace = "(?=\\s+$)";

        if (username.isEmpty() || username.matches(noWhiteSpace)) {
            throw new UserException("The username is not valid!");
        }
        return true;
    }

    private static boolean validateEmail(String email) throws UserException {
        String emailRegex = "[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+$";

        if (email.isEmpty() || !email.matches(emailRegex)) {
            throw new UserException("The email is not valid!");
        }
        return true;
    }

    private static boolean validatePassword(String password) throws UserException {

        if (password.isEmpty()) {
            throw new UserException("The password cannot be null!");
        } else {
            boolean validPass = true;

            if (password.length() < 8) validPass = false;
            if (!password.matches(".*[0-9].*")) validPass = false;
            if (!password.matches(".*[a-z].*")) validPass = false;
            if (!password.matches(".*[A-Z].*")) validPass = false;
            if (!password.matches(".*[!#&*~%@$^].*")) validPass = false;
            if (password.matches(".*\\s.*")) validPass = false;

            if (!validPass) {
                throw new UserException("The password needs to have 8 characters, at least a number, a capital letter and a special character!");
            }
        }
        return true;
    }

    public static void retrieveProfilePictureFromStorage(String userId, DatabaseCallback callback) {
        String imagePath = "images/" + userId + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(imagePath);
        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onCallback(uri.toString());
        }).addOnFailureListener(exception -> {
            Log.e("Userhelper", "Failed to load profile picture", exception);
        });
    }

    public static void getUserDetails(String userId, DatabaseCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend != null) {
                        friend.setUid(userId);
                        callback.onCallback(friend);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    public static void getFriendsList(String userId, DatabaseCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> friendIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    friendIds.add(snapshot.getKey());
                }
                callback.onCallback(friendIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    public static void getLocationSaved(String userId, DatabaseCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference locationRef = databaseReference.child(userId).child("location");

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);

                    if (userLocation != null) {
                        callback.onCallback(userLocation);
                    } else {
                        Log.d(TAG, "Location data incomplete");
                    }
                } else {
                    Log.d(TAG, "Location data not found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read location from Firebase", databaseError.toException());
            }
        });
    }
}
