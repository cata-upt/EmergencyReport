package com.example.emergencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private User user;
    private static final String PREF_NAME = "UserDetails";

    public UserSessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // To save user login status and username
    public void saveLoginDetails(User user) {
        editor.putBoolean("isLoggedIn", user.isLoggedIn());
        editor.putString("username", user.getUsername());
        editor.putString("phoneNumber", user.getPhoneNumber());
        editor.apply();
    }

    public User getLoginDetails() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", null);
        String phoneNumber = sharedPreferences.getString("phoneNumber", null);
        return new User("", username,"", "", phoneNumber, isLoggedIn);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

}
