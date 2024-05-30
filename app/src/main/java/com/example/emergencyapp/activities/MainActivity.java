package com.example.emergencyapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.ExtraDataNotifications;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.entities.Contact;
import com.example.emergencyapp.entities.AlertMessage;
import com.example.emergencyapp.utils.DatabaseCallback;
import com.example.emergencyapp.utils.LocationStatusHandler;
import com.example.emergencyapp.utils.LocationStatusListener;
import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.ShakeService;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements LocationStatusHandler {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    private LocationManager locationManager;
    private LocationStatusListener locationListener;

    private Button sendTextButton;

    private static String TAG = "EmergencyReport";

    private FirebaseUser user;

    private Queue<Snackbar> snackbarQueue = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        sendTextButton = findViewById(R.id.send_text_button);
        sendTextButton.setOnClickListener(view -> handleSendText());

        if (checkLocationPermissions()) {
            requestLocationPermissions();
        } else {
            startLocationUpdates();
        }
        locationListener = new LocationStatusListener(this, user, this);
        locationListener.requestLocationUpdates();

        if (user == null) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "For a better experience we recommend to sign into your account.", Snackbar.LENGTH_LONG)
                    .setAction("SIGN IN", v -> {
                        Intent i = new Intent(MainActivity.this, SignInActivity.class);
                        startActivity(i);
                        finish();
                    });
            showSnackbar(snackbar);
        }
        boolean areNotificationsEnabled = NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled();
        if (!areNotificationsEnabled) {
            showNotificationSnackBar();
        }

        if (!Settings.canDrawOverlays(this)) {
            showSystemAlertPermissionSnackbar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String emergencyText = sharedPreferences.getString("emergency_text", "");

        String location = getLocation();
        emergencyText += " " + location;

        if (checkSmsPermissions()) {
            sendTextToContacts(emergencyText);
            sendAlertToFriends(emergencyText);
        } else {
            requestSmsPermissions();
            showSmsPermissionSnackbar();
        }

    }

    private String getLocation() {
        String location = locationListener.getAddressFromLocation();

        if (location.equals(getApplicationContext().getString(R.string.address_not_available))) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissions();
                Toast.makeText(getApplicationContext(), "Please give location permission.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Location not available. Try again!", Toast.LENGTH_SHORT).show();
            }
        }

        return location;
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
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "You should have at least a contact in your list.", Snackbar.LENGTH_LONG)
                    .setAction("ADD CONTACT", v -> {
                        Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                        startActivity(intent);
                        finish();
                    });
            showSnackbar(snackbar);
        }
    }

    boolean success;

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
                        Toast.makeText(MainActivity.this, "Direct message sent successfully", Toast.LENGTH_SHORT).show();
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


    private boolean checkLocationPermissions() {
        // Check if the required location permissions are granted
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean checkSmsPermissions() {
        // Check if the required SMS permission is granted
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getApplicationContext().getPackageName()));
        startActivity(intent);
    }

    private void showSmsPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "SMS permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    requestSmsPermissions();
                });

        showSnackbar(snackbar);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                showLocationPermissionSnackbar();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showLocationSnackbar();
            } else {
                locationListener.requestLocationUpdates();
            }
        }
    }

    private void showLocationSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Location services are required for this app. Please enable GPS.", Snackbar.LENGTH_LONG)
                .setAction("ENABLE", v -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                });

        showSnackbar(snackbar);
    }

    private void showLocationPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Location permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    // Request location permissions again
                    requestLocationPermissions();
                });

        showSnackbar(snackbar);
    }

    private void showSystemAlertPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Grant permission for the app to show over other apps", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    // Request location permissions again
                    requestOverlayPermission();
                });

        showSnackbar(snackbar);
    }

    private boolean isShowing = false;

    private void showSnackbar(Snackbar snackbar) {
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (!snackbarQueue.isEmpty()) {
                    Snackbar next = snackbarQueue.poll();
                    next.show();
                }
            }
        });

        snackbarQueue.add(snackbar);
        if (!isShowing) {
            showNext();
        }
    }

    private void showNext() {
        if (!snackbarQueue.isEmpty() && !isShowing) {
            Snackbar next = snackbarQueue.poll();
            next.show();
            isShowing = true;
        }
    }


    @Override
    public void onGPSDisabled() {
        showLocationSnackbar();
    }

    @Override
    public void onGPSEnabled() {
        if (checkLocationPermissions()) {
            startLocationUpdates();
        } else {
            requestLocationPermissions();
        }
    }

    public void showNotificationSnackBar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "For the best experience please allow notifications for this app.", Snackbar.LENGTH_LONG)
                .setAction("Allow", v -> {
                    // Request location permissions again
                    askNotificationPermission();
                });

        showSnackbar(snackbar);
    }

    public void askNotificationPermission() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        } else {
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        }
        startActivity(intent);
    }
}
