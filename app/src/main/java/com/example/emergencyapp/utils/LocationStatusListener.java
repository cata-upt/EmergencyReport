package com.example.emergencyapp.utils;

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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationStatusListener implements LocationListener{

    private static final long MIN_TIME = 10; // Minimum time interval for location updates (in milliseconds)
    private static final float MIN_DISTANCE = 2; // Minimum distance for location updates (in meters)

    private Context context;
    private LocationManager locationManager;
    private Location currentLocation;
    private FirebaseUser user;
    private LocationStatusHandler locationStatusHandler;

    private static String TAG="EmergencyCall";

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

    // Request location permission at runtime

    // Check if location services (GPS) are enabled
    private boolean isLocationProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Request location updates
    public void requestLocationUpdates() {
        if (hasLocationPermission() && isLocationProviderEnabled()) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME,
                    MIN_DISTANCE,
                    this,
                    Looper.getMainLooper()
            );
        } else {
            Log.d(TAG, "Location updates not requested. Permission or provider not available.");
        }
    }

    public void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    public String getAddressFromLocation() {
        if (currentLocation != null) {
            return getAddressFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {
            return null;
        }
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
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
                return addressStringBuilder.toString().replaceAll(", $", "");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
        }
        return "Address not available";
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        Log.d(TAG, "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
        updateLocationInFirebase(location);
    }

    private void updateLocationInFirebase(Location location) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(user.getUid()).setValue(location)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location updated in Firebase successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update location in Firebase", e));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changes if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (locationStatusHandler != null) {
                locationStatusHandler.onGPSEnabled();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (locationStatusHandler != null) {
                locationStatusHandler.onGPSDisabled();
            }
        }
    }
}