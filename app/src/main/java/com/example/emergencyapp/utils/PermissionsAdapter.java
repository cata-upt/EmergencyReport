package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class PermissionsAdapter extends RecyclerView.Adapter<PermissionsAdapter.PermissionViewHolder> {
    private List<PermissionItem> permissionList;
    private Context context;
    private Activity activity;

    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_READ_CONTACTS_PERMISSION = 0;

    public PermissionsAdapter(List<PermissionItem> permissionList, Context context, Activity activity) {
        this.permissionList = permissionList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.permission_item, parent, false);
        return new PermissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        PermissionItem permissionItem = permissionList.get(position);
        holder.permissionName.setText(permissionItem.getPermissionName());
        holder.description.setText(permissionItem.getDescription());
        holder.permissionSwitch.setChecked(permissionItem.isGranted());

        holder.permissionSwitch.setOnClickListener((buttonView) -> {
            if (!permissionItem.isGranted()) {
                requestPermission(permissionItem.getPermissionName());
            } else {
                revokePermissions(permissionItem.getPermissionName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return permissionList.size();
    }

    private void requestPermission(String permission) {
        switch (permission) {
            case "Location":
                requestLocationPermissions();
                break;
            case "Notifications":
                askNotificationPermission();
                break;
            case "SMS":
                requestSmsPermissions();
                break;
            case "Contacts":
                requestContactsPermissions();
                break;
            case "Overlay":
                requestOverlayPermission();
                break;
            case "Send SOS automatically":
                saveBooleanSetting(context, "autoSOS", true);
                break;
        }
    }

    static class PermissionViewHolder extends RecyclerView.ViewHolder {
        TextView permissionName;
        TextView description;
        SwitchMaterial permissionSwitch;

        PermissionViewHolder(@NonNull View itemView) {
            super(itemView);
            permissionName = itemView.findViewById(R.id.permission_name);
            description = itemView.findViewById(R.id.permission_description);
            permissionSwitch = itemView.findViewById(R.id.permission_switch);
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void requestSmsPermissions() {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    private void requestContactsPermissions() {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS_PERMISSION);
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else {
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    public void askNotificationPermission() {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    private void revokePermissions(String permission) {
        switch (permission) {
            case "Notifications":
                askNotificationPermission();
                break;
            case "Overlay":
                requestOverlayPermission();
                break;
            case "Location":
            case "SMS":
            case "Contacts":
                showSettingsActivity();
                break;
                case "Send SOS automatically":
                    saveBooleanSetting(context, "autoSOS", false);
                    break;
        }
    }

    public void saveBooleanSetting(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences("EmergencyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void showSettingsActivity() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + context.getPackageName()));

        context.startActivity(intent);
    }
}
