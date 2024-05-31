package com.example.emergencyapp.utils;

public class PermissionItem {
    private String permissionName;
    private String description;
    private boolean isGranted;

    public PermissionItem(String permissionName, String description, boolean isGranted) {
        this.permissionName = permissionName;
        this.description = description;
        this.isGranted = isGranted;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
    }
}
