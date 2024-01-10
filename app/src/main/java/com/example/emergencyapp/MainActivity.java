package com.example.emergencyapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;

    private LocationManager locationManager;
    private MyLocationListener locationListener;
    Button sendTextButton;

    private static String TAG = "EmergencyCall";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        sendTextButton = findViewById(R.id.send_text_button);
        sendTextButton.setOnClickListener(view -> sendTextToContacts());

        if (checkLocationPermissions()) {
            startLocationUpdates();
        } else {
            requestLocationPermissions();
        }

        locationListener = new MyLocationListener(this);
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

    private void sendTextToContacts() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");
        ArrayList<Contact> contacts = Contact.loadContactsFromPreferences(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String emergencyText = sharedPreferences.getString("emergency_text", "");
        String location = locationListener.getAddressFromLocation();
        if (location.equals("Address not available")) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissions();
                Toast.makeText(getApplicationContext(), "Please give address permission.", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "Address not available. Try again!", Toast.LENGTH_LONG).show();
            return;
        }
        emergencyText+=" "+location;
        SmsManager smsManager = SmsManager.getDefault();
        if (checkSmsPermissions()) {
            for (Contact c : contacts) {
                try {
                    PendingIntent sentIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            0,
                            new Intent("SMS_SENT"),
                            PendingIntent.FLAG_IMMUTABLE
                    );
                    smsManager.sendTextMessage(c.phoneNumber, null, emergencyText, sentIntent, null);
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
        } else {
            requestSmsPermissions();
            showSmsPermissionSnackbar();
        }

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
        // Request SMS permissions at runtime
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    private void showSmsPermissionSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), "SMS permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    // Request SMS permissions again
                    requestSmsPermissions();
                })
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                showPermissionSnackbar();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationListener.requestLocationUpdates();
            }
        }else {
            showLocationSnackbar();
        }
    }

    private void showLocationSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), "Location services are required for this app. Please enable GPS.", Snackbar.LENGTH_LONG)
                .setAction("ENABLE", v -> {
                    // Redirect the user to the device settings to enable location services
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .show();
    }

    private void showPermissionSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), "Location permission is required for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    // Request location permissions again
                    requestLocationPermissions();
                })
                .show();
    }
}
