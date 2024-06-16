package com.example.emergencyapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.Password;
import com.example.emergencyapp.entities.UserLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LocationStatusListener implements LocationListener {

    private static final long MIN_TIME = 10; // Minimum time interval for location updates (in milliseconds)
    private static final float MIN_DISTANCE = 2; // Minimum distance for location updates (in meters)

    private Context context;
    private LocationManager locationManager;
    private UserLocation currentLocation;
    private FirebaseUser user;
    private LocationStatusHandler locationStatusHandler;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private static String TAG = "EmergencyCall";

    public LocationStatusListener(Context context, FirebaseUser user, LocationStatusHandler locationStatusHandler) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.user = user;
        this.locationStatusHandler = locationStatusHandler;
    }

    // Check if the app has location permission
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationUpdates() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getLastKnownLocation();

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this, Looper.getMainLooper());
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this, Looper.getMainLooper());
            }
        } else {
            Log.d(TAG, "Location updates not requested. Permission or provider not available.");
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastKnownLocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location bestLastKnownLocation = null;

        if (lastKnownLocationGPS != null && lastKnownLocationNetwork != null) {
            bestLastKnownLocation = lastKnownLocationGPS.getTime() > lastKnownLocationNetwork.getTime() ? lastKnownLocationGPS : lastKnownLocationNetwork;
        } else if (lastKnownLocationGPS != null) {
            bestLastKnownLocation = lastKnownLocationGPS;
        } else if (lastKnownLocationNetwork != null) {
            bestLastKnownLocation = lastKnownLocationNetwork;
        }

        if (bestLastKnownLocation != null) {
            updateLocationInFirebase(bestLastKnownLocation);
        }
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    public String getAddressFromLocation() {
        if (currentLocation == null && user != null) {
            retrieveLocationFromFirebase();
        }
        return getAddressFromLocation(currentLocation);
    }

    public void retrieveLocationFromFirebase() {
        UserHelper.getLocationSaved(user.getUid(), (DataCallback<UserLocation>) userLocation -> {
            if(userLocation !=null) {
                currentLocation = userLocation;
            }
        });
    }

    private String getAddressFromLocation(UserLocation location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder addressStringBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    // Remove non-alphanumeric characters using regex
                    String cleanedLine = address.getAddressLine(i)
                            .replaceAll("ă", "a")
                            .replaceAll("â", "a")
                            .replaceAll("ș", "s")
                            .replaceAll("ț", "t")
                            .replaceAll("î", "i");
                    addressStringBuilder.append(cleanedLine).append(", ");
                }
                // Remove the trailing comma and space
                Date date = new Date(location.getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return addressStringBuilder.toString().replaceAll(", $", "") + " at " + sdf.format(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting address from location", e);
        }
        return context.getString(R.string.address_not_available);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new UserLocation(location.getLatitude(), location.getLongitude(), location.getTime(), location.getAccuracy(), location.getAltitude(), location.getSpeed());
        Log.d(TAG, "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
        updateLocationInFirebase(location);
    }

    private void updateLocationInFirebase(Location location) {
        if (user == null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (user != null && user.getUid() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            UserLocation userLocation = new UserLocation();
            userLocation.setLatitude(location.getLatitude());
            userLocation.setLongitude(location.getLongitude());
            userLocation.setAltitude(location.getAltitude());
            userLocation.setTime(location.getTime());
            userLocation.setSpeed(location.hasSpeed() ? location.getSpeed() : null);
            userLocation.setAccuracy(location.hasAccuracy() ? location.getAccuracy() : null);

            databaseReference.child(user.getUid()).child("location").setValue(userLocation)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Location updated in Firebase successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update location in Firebase", e));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changes if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER) || provider.equals(LocationManager.NETWORK_PROVIDER)) {
            if (locationStatusHandler != null) {
                locationStatusHandler.onGPSEnabled();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER) || provider.equals(LocationManager.NETWORK_PROVIDER)) {
            if (locationStatusHandler != null) {
                locationStatusHandler.onGPSDisabled();
            }
        }
    }
}