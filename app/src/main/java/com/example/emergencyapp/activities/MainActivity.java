package com.example.emergencyapp.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.emergencyapp.utils.Contact;
import com.example.emergencyapp.utils.LocationStatusHandler;
import com.example.emergencyapp.utils.LocationStatusListener;
import com.example.emergencyapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationStatusHandler {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;

    private LocationManager locationManager;
    private LocationStatusListener locationListener;

    private Button sendTextButton;

    private static String TAG = "EmergencyReport";

    private FirebaseUser user;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermissions()) {
            locationListener.requestLocationUpdates();
        } else {
            requestLocationPermissions();
        }
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

    private void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String emergencyText = sharedPreferences.getString("emergency_text", "");
        String location = getLocation();
        if (location!=null) {
            emergencyText += " " + location;

            if (checkSmsPermissions()) {
                sendTextToContacts(emergencyText);
            } else {
                requestSmsPermissions();
                showSmsPermissionSnackbar();
            }
        }
    }

    private String getLocation(){
        String location = locationListener.getAddressFromLocation();

        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissions();
                Toast.makeText(getApplicationContext(), "Please give location permission.", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Location not available. Try again!", Toast.LENGTH_SHORT).show();
            }
        }

        return location;
    }

    private void sendTextToContacts(String emergencyText){
        ArrayList<Contact> contacts = Contact.loadContactsFromPreferences(this);
        SmsManager smsManager = SmsManager.getDefault();
        for (Contact c : contacts) {
            try {
                PendingIntent sentIntent = PendingIntent.getBroadcast(
                        getApplicationContext(),
                        0,
                        new Intent("SMS_SENT"),
                        PendingIntent.FLAG_IMMUTABLE
                );
                smsManager.sendTextMessage(c.getPhoneNumber(), null, emergencyText, sentIntent, null);
                Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                Log.i(TAG, "sendTextToContacts: "+emergencyText);
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
                        Log.e(TAG, "SMS sending failed - no service");;
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT"));
    }


    private boolean checkLocationPermissions() {
        // Check if the required location permissions are granted
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        // Request location permissions at runtime
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean checkSmsPermissions() {
        // Check if the required SMS permission is granted
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    private void showSmsPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "SMS permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    requestSmsPermissions();
                });
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                showPermissionSnackbar();
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

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.show();
    }

    private void showPermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Location permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    // Request location permissions again
                    requestLocationPermissions();
                });

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.show();
    }

    @Override
    public void onGPSDisabled() {
        showLocationSnackbar();
    }

    @Override
    public void onGPSEnabled() {
        if (checkLocationPermissions()) {
            requestLocationPermissions();
        } else {
            startLocationUpdates();
        }
    }
}
