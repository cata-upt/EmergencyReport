package com.example.emergencyapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.UserLocation;
import com.example.emergencyapp.utils.LocationStatusHandler;
import com.example.emergencyapp.utils.LocationStatusListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback, LocationStatusHandler {

    private GoogleMap mMap;
    private FirebaseUser user;
    private Marker currentMarker;
    private String userIdToTrack;
    private LocationManager locationManager;
    private LocationStatusListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        user = FirebaseAuth.getInstance().getCurrentUser();

        userIdToTrack = getIntent().getStringExtra("userId");
        if (userIdToTrack == null && user != null) {
            userIdToTrack = user.getUid();
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationStatusListener(this, user, this);
        startLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu) {
            Intent i = new Intent(LocationActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng timisoara = new LatLng(45.753540, 21.229172);
        currentMarker = mMap.addMarker(new MarkerOptions().position(timisoara).title("Marker in Timisoara"));
        float zoomLevel = 17.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(timisoara, zoomLevel));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference locationRef = databaseReference.child(userIdToTrack).child("location");

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserLocation location = dataSnapshot.getValue(UserLocation.class);
                updateMap(location);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LocationActivity", "Failed to read location", error.toException());
            }
        });
    }

    private void updateMap(UserLocation location) {
        if (location == null) {
            Log.d("MapActivity", "Skipping null location update");
            return;
        }

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("User Location"));

        float zoomLevel = 17.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationListener.requestLocationUpdates();
            }
        }
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
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
