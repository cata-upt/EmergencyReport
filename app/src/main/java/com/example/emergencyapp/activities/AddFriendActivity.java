package com.example.emergencyapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.ExtraDataNotifications;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.entities.FriendRequest;
import com.example.emergencyapp.utils.DataCallback;
import com.example.emergencyapp.utils.MessagingService;
import com.example.emergencyapp.utils.UserHelper;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddFriendActivity extends AppCompatActivity {
    private EditText editTextPhoneNumber;
    private Button buttonAddFriend;
    private FirebaseUser user;
    private boolean isActivityActive = true;
    private boolean isToastShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);

        user = FirebaseAuth.getInstance().getCurrentUser();

        buttonAddFriend.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            if (user != null) {
                findUserByPhoneNumber(phoneNumber);
            } else {
                Toast.makeText(AddFriendActivity.this, "To complete this action you need to sign into your account.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void findUserByPhoneNumber(String phoneNumber) {
        UserHelper.findUserByPhoneNumber(phoneNumber, AddFriendActivity.this, (DataCallback<String>) friendID-> isPersonFriend(friendID));
    }

    private void isPersonFriend(String friendId) {
        UserHelper.getFriendsList(user.getUid(), (DataCallback<List<String>>) friendsList -> {
            if (!isActivityActive) return;
            if (friendsList != null) {
                if (friendsList.contains(friendId)) {
                    showToast( "This person is already your friend");
                } else {
                    findUniqueFriendRequest(friendId);
                }
            }else{
                sendFriendRequest(friendId);
            }
        });
    }

    private void findUniqueFriendRequest(String friendId) {
        UserHelper.findFriendRequest(String.valueOf(friendId), user.getUid(), (DataCallback<String>) status -> {
            if (!isActivityActive) return;
            switch (status) {
                case "pending":
                    showToast("The friend request is pending.");
                    break;
                case "accepted":
                    showToast("This person is already your friend.");
                    break;
                case "rejected":
                    showToast("Friend request was rejected.");
                    break;
                case "not_sent":
                    sendFriendRequest(friendId);
                    break;
                case "unknown":
                    sendFriendRequest(friendId);
                    showToast("Unknown friend request status.");
                    break;
                case "error":
                    showToast("Something went wrong. Try again later!");
                    break;
            }
        });
    }

    private void sendFriendRequest(String userId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserSessionManager userSession = new UserSessionManager(getApplicationContext());
        String body = "You have a new friend request from " + userSession.getLoginDetails().getName();
        String title = "New friend request!";

        if (user != null) {
            DatabaseReference userIdToken = FirebaseDatabase.getInstance().getReference("tokens").child(userId);

            userIdToken.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String token = dataSnapshot.getValue(String.class);
                    if (token != null) {
                        ExtraDataNotifications extraDataNotifications = new ExtraDataNotifications();
                        extraDataNotifications.addData("targetActivity", "FriendRequests");
                        extraDataNotifications.addData("fragment", "FriendRequestsFragment");
                        NotificationRequestApi notificationRequestApi = new NotificationRequestApi(token, title, body);
                        notificationRequestApi.setExtraDataNotifications(extraDataNotifications);
                        FriendRequest friendRequest = new FriendRequest(user.getUid(), userId);
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(BaseApplication.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        ApiService service = retrofit.create(ApiService.class);
                        service.sendFriendRequest(friendRequest).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    UserHelper.sendUserNotification(service, notificationRequestApi, token, AddFriendActivity.this, "Friend request sent successfully");
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

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w("Messaging Service", "Failed to get token for notification", databaseError.toException());
                }
            });
        }
    }

    private void showToast(String message) {
        if (isActivityActive) {
            Toast.makeText(AddFriendActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        isActivityActive = false;
        super.onDestroy();
    }

}
