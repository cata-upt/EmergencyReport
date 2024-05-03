package com.example.emergencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private UserDetails userDetails;
    private static final String PREF_NAME = "UserDetails";

    public UserSessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // To save user login status and username
    public void saveLoginDetails(UserDetails userDetails) {
        editor.putBoolean("isLoggedIn", userDetails.isLoggedIn());
        editor.putString("username", userDetails.getUsername());
        editor.putString("phoneNumber", userDetails.getPhoneNumber());
        editor.apply();
    }

    public UserDetails getLoginDetails() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", null);
        String phoneNumber = sharedPreferences.getString("phoneNumber", null);
        return new UserDetails("", username,"", "", phoneNumber, isLoggedIn);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

}
