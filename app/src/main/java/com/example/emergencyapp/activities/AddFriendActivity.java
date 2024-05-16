package com.example.emergencyapp.activities;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.utils.MessagingService;
import com.example.emergencyapp.utils.User;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddFriendActivity extends AppCompatActivity {
    private EditText editTextPhoneNumber;
    private Button buttonAddFriend;
    MessagingService messagingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        messagingService = new MessagingService();

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);

        buttonAddFriend.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            findUserByPhoneNumber(phoneNumber);
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
                        NotificationRequestApi notificationRequestApi = new NotificationRequestApi(token, title, body);
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(BaseApplication.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        ApiService service = retrofit.create(ApiService.class);
                        service.sendNotification(notificationRequestApi).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(AddFriendActivity.this, "Friend request sent successfully", Toast.LENGTH_SHORT).show();
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
}
