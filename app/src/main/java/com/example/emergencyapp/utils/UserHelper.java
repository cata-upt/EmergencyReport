package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserHelper {

    public static boolean validateUser(User user) throws UserException {
        return validateName(user.getName()) &&
                validateUsername(user.getUsername()) &&
                validateEmail(user.getEmail()) &&
                validatePhoneNumber(user.getPhoneNumber()) &&
                validatePassword(user.getPassword());
    }

    public static boolean validateName(String name) throws UserException {
        if (name.isEmpty()) {
            throw new UserException("The name is not valid!");
        }
        return true;
    }

    public static boolean validateUsername(String username) throws UserException {
        String noWhiteSpace = "^\\S+$";

        if (username.isEmpty() || !username.matches(noWhiteSpace)) {
            throw new UserException("The username is not valid!");
        }
        return true;
    }

    public static boolean validateEmail(String email) throws UserException {
        String emailRegex = "[a-zA-Z0-9+_.-]+@[a-z]+\\.+[a-z]+$";

        if (email.isEmpty() || !email.matches(emailRegex)) {
            throw new UserException("The email is not valid!");
        }
        return true;
    }

    public static boolean validatePassword(String password) throws UserException {

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

    public static boolean validatePhoneNumber(String phoneNumber) throws UserException {
        if (!phoneNumber.matches("^[0-9]+$")) {
            throw new UserException("The phone number can only contain numbers!");
        }
        return true;
    }

    public static void retrieveProfilePictureFromStorage(String userId, DataCallback<String> callback) {
        String imagePath = "images/" + userId + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference userImageRef = storageRef.child(imagePath);
        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            callback.onCallback(uri.toString());
        }).addOnFailureListener(exception -> {
            Log.e("Userhelper", "Failed to load profile picture", exception);
        });
    }

    public static void getUserDetails(String userId, DataCallback<User> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User friend = dataSnapshot.getValue(User.class);
                    if (friend != null) {
                        friend.setUid(userId);
                        callback.onCallback(friend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void getFriendsList(String userId, DataCallback<List<String>> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> friendIds = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        friendIds.add(snapshot.getKey());
                    }
                    callback.onCallback(friendIds);
                } else {
                    callback.onCallback(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static void getLocationSaved(String userId, DataCallback<UserLocation> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference locationRef = databaseReference.child(userId).child("location");
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);

                    if (userLocation != null) {
                        callback.onCallback(userLocation);
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onCallback(null);
                    Log.d(TAG, "Location data not found in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read location from Firebase", databaseError.toException());
            }
        });
    }

    public static void findUserByPhoneNumber(String phoneNumber, Context context, DataCallback<String> callback) {
        DatabaseReference phoneRef = FirebaseDatabase.getInstance().getReference("phone_to_uid").child(phoneNumber);
        phoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.getValue(String.class);
                    callback.onCallback(userId);
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to search for user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void findFriendRequest(String userId, String friendId, DataCallback<String> callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests");
        DatabaseReference userRef = databaseReference.child(userId).child(friendId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null) {
                        callback.onCallback(status);
                    } else {
                        callback.onCallback("unknown");
                    }
                } else {
                    callback.onCallback("not_sent");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onCallback("error");
            }
        });
    }

    public static void sendUserNotification(ApiService service, NotificationRequestApi notificationRequestApi, String token, Context context, String message) {
        service.sendNotification(notificationRequestApi).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    Log.d("Notification Service", "Notification sent successfully: token " + token);
                } else {
                    Log.e("Notification Service", "Failed to send notification");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Notification Service", "Error sending notification", t);
            }
        });
    }
}
