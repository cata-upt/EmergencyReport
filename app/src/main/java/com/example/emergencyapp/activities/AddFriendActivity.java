package com.example.emergencyapp.activities;

import android.content.Intent;
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
import com.example.emergencyapp.utils.MessagingService;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddFriendActivity extends AppCompatActivity {
    private EditText editTextPhoneNumber;
    private Button buttonAddFriend;
    MessagingService messagingService;
    FirebaseUser user;
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
            if(user!=null) {
                findUserByPhoneNumber(phoneNumber);
            }else {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "For a better experience we recommend to sign into your account.", Snackbar.LENGTH_LONG)
                        .setAction("SIGN IN", s -> {
                            Intent i =new Intent(AddFriendActivity.this, SignInActivity.class);
                            startActivity(i);
                            finish();
                        });
                showSnackbar(snackbar);
            }
        });


    }

    private void findUserByPhoneNumber(String phoneNumber) {
        DatabaseReference phoneRef = FirebaseDatabase.getInstance().getReference("phone_to_uid").child(phoneNumber);
        phoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.getValue(String.class);
                    sendFriendRequest(userId);
                } else {
                    Toast.makeText(AddFriendActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddFriendActivity.this, "Failed to search for user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean success;
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
                    success = false;
                    String token = dataSnapshot.getValue(String.class);
                    if (token != null) {
                        ExtraDataNotifications extraDataNotifications = new ExtraDataNotifications();
                        extraDataNotifications.addData("targetActivity", "FriendsListActivity");
                        extraDataNotifications.addData("fragment", "FriendRequests");
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
                                    success = true;
                                    Log.d("Notification Service", "Notification sent successfully: token "+token);
                                } else {
                                    Log.e("Notification Service", "Failed to send notification");
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("Notification Service", "Error sending notification", t);
                            }
                        });

                        service.sendNotification(notificationRequestApi).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    if(success) {
                                        Toast.makeText(AddFriendActivity.this, "Friend request sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    Log.d("Notification Service", "Notification sent successfully: token "+token);
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
