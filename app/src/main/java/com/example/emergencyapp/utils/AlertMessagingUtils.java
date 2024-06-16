package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.ExtraDataNotifications;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.entities.AlertMessage;
import com.example.emergencyapp.entities.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlertMessagingUtils implements LocationStatusHandler {

    private Context context;
    private FirebaseUser user;
    private LocationManager locationManager;
    private LocationStatusListener locationListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int tryCount = 0;

    public AlertMessagingUtils(Context context) {
        this.context = context;

        user = FirebaseAuth.getInstance().getCurrentUser();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationStatusListener(context, user, this);
        startLocationUpdates();
    }

    public void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String emergencyText = sharedPreferences.getString("emergency_text", "");

        String location = getLocation();
        if(location.equals(context.getString(R.string.address_not_available)) && tryCount<3) {
            tryToGetLocationAgain();
        }else {
            emergencyText += location;

            if (checkSmsPermissions()) {
                sendTextToContacts(emergencyText);
            }
            sendAlertToFriends(emergencyText);
        }
    }

    private String getLocation() {
        String location = locationListener.getAddressFromLocation();

        if (location.equals(context.getString(R.string.address_not_available))) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Please give location permission.", Toast.LENGTH_SHORT).show();
            }
        }

        return location;
    }

    public void tryToGetLocationAgain() {
        tryCount++;
        Log.i(TAG, "tryToGetLocationAgain: "+tryCount);
        handler.postDelayed(this::handleSendText, 5000);
    }

    public void sendTextToContacts(String emergencyText) {
        ArrayList<Contact> contacts = Contact.loadContactsFromPreferences(context);
        SmsManager smsManager = SmsManager.getDefault();
        if (contacts != null) {
            for (Contact c : contacts) {
                try {
                    PendingIntent sentIntent = PendingIntent.getBroadcast(
                            context,
                            0,
                            new Intent("SMS_SENT"),
                            PendingIntent.FLAG_IMMUTABLE
                    );
                    smsManager.sendTextMessage(c.getPhoneNumber(), null, emergencyText, sentIntent, null);
                    Toast.makeText(context, "SMS Sent", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "sendTextToContacts: " + emergencyText);
                } catch (Exception e) {
                    Log.e("EmergencyCall", "sendTextToContacts: ", e);
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void sendAlertToFriends(String emergencyText) {
        if (user != null) {
            UserSessionManager userSession = new UserSessionManager(context);
            String body = emergencyText;
            String title = userSession.getLoginDetails().getName();
            UserHelper.getFriendsList(user.getUid(), (DataCallback<List<String>>) friendIds -> {
                if (friendIds != null) {
                    for (String friend : friendIds) {
                        DatabaseReference userIdToken = FirebaseDatabase.getInstance().getReference("tokens").child(friend);
                        userIdToken.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String token = dataSnapshot.getValue(String.class);
                                if (token != null) {
                                    sendAlertMessage(friend, token, title, body);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("Messaging Service", "Failed to get token for notification", databaseError.toException());
                            }
                        });
                    }
                }
            });
        }
    }

    private void sendAlertMessage(String friend, String token, String title, String body) {
        ExtraDataNotifications extraDataNotifications = new ExtraDataNotifications();
        extraDataNotifications.addData("targetActivity", "ChatActivity");
        extraDataNotifications.addData("senderId", user.getUid());
        NotificationRequestApi notificationRequestApi = new NotificationRequestApi(token, title, body);
        notificationRequestApi.setExtraDataNotifications(extraDataNotifications);
        AlertMessage alertMessage = new AlertMessage(user.getUid(), friend, title, body, System.currentTimeMillis(), false);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        service.sendAlertMessage(alertMessage).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    UserHelper.sendUserNotification(service, notificationRequestApi, token, context, "Direct message sent successfully");
                    Log.d("Notification Service", "Message alert sent successfully");
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

    @Override
    public void onGPSDisabled() {
    }

    @Override
    public void onGPSEnabled() {
        if (checkLocationPermissions()) {
            startLocationUpdates();
        }
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkSmsPermissions() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationListener.requestLocationUpdates();
            }
        }
    }
}
