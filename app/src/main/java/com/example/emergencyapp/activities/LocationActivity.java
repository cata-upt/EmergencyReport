package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.UserLocation;
import com.example.emergencyapp.utils.UserSessionManager;
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

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseUser user;
    private UserSessionManager userSession;
    private Marker currentMarker;
    private String userIdToTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userSession = new UserSessionManager(getApplicationContext());

        // Get the userId from the intent
        userIdToTrack = getIntent().getStringExtra("userId");
        if (userIdToTrack == null) {
            userIdToTrack = user.getUid(); // default to the current user if no userId is provided
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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(timisoara));

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
}
