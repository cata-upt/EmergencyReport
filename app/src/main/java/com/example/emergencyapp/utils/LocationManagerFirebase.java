package com.example.emergencyapp.utils;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationManagerFirebase {

    private final Context context;
    private final FusedLocationProviderClient locationClient;
    private final DatabaseReference databaseReference;

    public LocationManagerFirebase(Context context) {
        this.context = context;
        this.locationClient = LocationServices.getFusedLocationProviderClient(context);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("locations");
    }

    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        sendLocationToFirebase(location);
                    }
                }
            }
        };

        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void sendLocationToFirebase(Location location) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child(userId).setValue(location)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    public void stopLocationUpdates(LocationCallback locationCallback) {
        locationClient.removeLocationUpdates(locationCallback);
    }
}

