package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.credentials.playservices.CredentialProviderMetadataHolder;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.MainActivity;
import com.example.emergencyapp.activities.ShakeServiceActivity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShakeService extends Service {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakePhoneDetector mShakeDetector;
    private WindowManager windowManager;
    private View overlayView;
    FirebaseUser user;
    AlertMessagingUtils alertMessagingUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        user = FirebaseAuth.getInstance().getCurrentUser();
        alertMessagingUtils = new AlertMessagingUtils(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakePhoneDetector();
        mShakeDetector.setOnShakeListener(count -> {
            performAction();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EmergencyReportChannelID", "EmergencyReportChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "EmergencyReportChannelID")
                .setContentTitle("Shake Service")
                .setContentText("Shake detection is running")
                .setSmallIcon(R.drawable.baseline_notification_important_24)
                .setContentIntent(pendingIntent);

        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performAction() {
        showDialog();
    }

    private void showDialog() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        overlayView = inflater.inflate(R.layout.alert_dialog_fragment, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.OPAQUE);
        params.gravity = Gravity.CENTER;

        Button yesButton = overlayView.findViewById(R.id.button_yes);
        Button noButton = overlayView.findViewById(R.id.button_no);
        TextView textView = overlayView.findViewById(R.id.dialog_text);
        textView.setText(R.string.do_you_really_want_to_send_an_emergency_text);

        yesButton.setOnClickListener(v -> {
            handleSendText();
            windowManager.removeView(overlayView);
            overlayView = null;
        });

        noButton.setOnClickListener(v -> {
            windowManager.removeView(overlayView);
            overlayView = null;
        });

        windowManager.addView(overlayView, params);

        new Handler().postDelayed(() -> {
            if (overlayView != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }
        }, 5000); // 5 seconds timer
    }

    public void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");
        alertMessagingUtils.handleSendText();
    }

    private void sendTextToContacts(String emergencyText) {
        ArrayList<Contact> contacts = Contact.loadContactsFromPreferences(this);
        SmsManager smsManager = SmsManager.getDefault();
        if (contacts != null) {
            for (Contact c : contacts) {
                try {
                    PendingIntent sentIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            0,
                            new Intent("SMS_SENT"),
                            PendingIntent.FLAG_IMMUTABLE
                    );
                    smsManager.sendTextMessage(c.getPhoneNumber(), null, emergencyText, sentIntent, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "sendTextToContacts: " + emergencyText);
                } catch (Exception e) {
                    Log.e("EmergencyCall", "sendTextToContacts: ", e);
                    Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            // Message sent successfully
                            Log.i(TAG, "SMS sent successfully");
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Log.e(TAG, "SMS sending failed - Generic failure");
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Log.e(TAG, "SMS sending failed - radio off");
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Log.e(TAG, "SMS sending failed - no PDU defined");
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Log.e(TAG, "SMS sending failed - no service");
                            ;
                            break;
                    }
                }
            }, new IntentFilter("SMS_SENT"));
        }
    }

    private void sendAlertToFriends(String emergencyText) {

        if (user != null) {
            UserSessionManager userSession = new UserSessionManager(getApplicationContext());
            String body = emergencyText;
            String title = userSession.getLoginDetails().getName();
            UserHelper.getFriendsList(user.getUid(), (DatabaseCallback<List<String>>) friendIds -> {
                for (String friend : friendIds) {
                    DatabaseReference userIdToken = FirebaseDatabase.getInstance().getReference("tokens").child(friend);
                    userIdToken.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            success = false;
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
            });
        }
    }

    boolean success;

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
                    success = true;
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

        service.sendNotification(notificationRequestApi).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (success) {
                        Toast.makeText(getApplicationContext(), "Direct message sent successfully", Toast.LENGTH_SHORT).show();
                    }
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
