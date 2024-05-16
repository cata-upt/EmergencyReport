package com.example.emergencyapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.TokenRequestApi;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApplication extends Application {

    public static final String BASE_URL = "https://fcmserver-985737cafdfc.herokuapp.com/api/";

    @Override
    public void onCreate() {
        super.onCreate();
        setupFirebaseAuthListener();
        obtainFirebaseToken();
        createNotificationChannel();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EmergencyReportChannelID", "EmergencyReportChannel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("EmergencyReport");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupFirebaseAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            UserSessionManager preferences = new UserSessionManager(getApplicationContext());
            if (user == null) {
                preferences.clearSession();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User userProfile = dataSnapshot.getValue(User.class);
                        if (userProfile != null) {
                            userProfile.setLoggedIn(preferences.getLoginDetails().isLoggedIn());
                            preferences.saveLoginDetails(userProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("Database", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }
        });
    }

    public void obtainFirebaseToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);
                    sendRegistrationToServer(token);
                });
    }

    public void sendRegistrationToServer(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService service = retrofit.create(ApiService.class);
            TokenRequestApi tokenRequestApi = new TokenRequestApi(token, user.getUid());
            Call<Void> call = service.registerToken(tokenRequestApi);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("Token Registration", "Token sent successfully");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("Token Registration", "Failed to send token", t);
                }
            });
        }
    }
}
