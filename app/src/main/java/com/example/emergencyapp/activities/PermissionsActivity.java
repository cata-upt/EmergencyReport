package com.example.emergencyapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.PermissionItem;
import com.example.emergencyapp.utils.PermissionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class PermissionsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PermissionsAdapter adapter;
    private List<PermissionItem> permissionList;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissions_activity);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        permissionList = new ArrayList<>();

        adapter = new PermissionsAdapter(permissionList, this, this);
        recyclerView.setAdapter(adapter);

        sharedPref = this.getSharedPreferences("EmergencyPreferences", Context.MODE_PRIVATE);

        addToPermissionsList();
    }

    @Override
    public void onResume() {
        super.onResume();
        addToPermissionsList();
    }

    private void addToPermissionsList() {
        permissionList.clear();
        permissionList.add(new PermissionItem(
                "Location",
                "Allows the app to access your location.",
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        permissionList.add(new PermissionItem(
                "SMS",
                "Allows the app to send your messages.",
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED));
        permissionList.add(new PermissionItem(
                "Contacts",
                "Allows the app to get access to your contacts.",
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED));
        permissionList.add(new PermissionItem(
                "Overlay",
                "Allows the app to show over other apps.",
                Settings.canDrawOverlays(this)));
        permissionList.add(new PermissionItem(
                "Notifications",
                "Allows the app to send you notifications.",
                NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()));
        permissionList.add(new PermissionItem(
                "Send SOS automatically",
                "Allows the app to send SOS texts automatically after timer expires",
                sharedPref.getBoolean("autoSOS", false)));
        adapter.notifyDataSetChanged();
    }


}
