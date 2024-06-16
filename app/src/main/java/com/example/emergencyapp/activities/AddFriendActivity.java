package com.example.emergencyapp.activities;

import android.os.Bundle;
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
    private MessagingService messagingService;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        messagingService = new MessagingService();

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
        UserHelper.findUserByPhoneNumber(phoneNumber, AddFriendActivity.this, (DataCallback<String>) this::isPersonFriend);
    }

    private void isPersonFriend(String friendId) {
        UserHelper.getFriendsList(user.getUid(), (DataCallback<List<String>>) friendsList -> {
            if (friendsList != null) {
                if (friendsList.contains(friendId)) {
                    Toast.makeText(AddFriendActivity.this, "This person is already your friend", Toast.LENGTH_SHORT).show();
                } else {
                    findUniqueFriendRequest(friendId);
                }
            }
        });
    }

    private void findUniqueFriendRequest(String friendId) {
        UserHelper.findFriendRequest(String.valueOf(friendId), user.getUid(), (DataCallback<String>) status -> {
            switch (status) {
                case "pending":
                    Toast.makeText(AddFriendActivity.this, "The friend request is pending.", Toast.LENGTH_SHORT).show();
                    break;
                case "accepted":
                    Toast.makeText(AddFriendActivity.this, "This person is already your friend", Toast.LENGTH_SHORT).show();
                    break;
                case "rejected":
                    Toast.makeText(AddFriendActivity.this, "Friend request was rejected.", Toast.LENGTH_SHORT).show();
                    break;
                case "not_sent":
                    sendFriendRequest(friendId);
                    break;
                case "unknown":
                    sendFriendRequest(friendId);
                    Toast.makeText(AddFriendActivity.this, "Unknown friend request status.", Toast.LENGTH_SHORT).show();
                    break;
                case "error":
                    Toast.makeText(AddFriendActivity.this, "Something went wrong. Try again later!", Toast.LENGTH_SHORT).show();
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

    private void showSnackbar(Snackbar snackbar) {
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.show();
    }
}
