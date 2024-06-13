package com.example.emergencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.emergencyapp.entities.User;

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
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.apply();
    }

    public User getLoginDetails() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", null);
        String phoneNumber = sharedPreferences.getString("phoneNumber", null);
        String name = sharedPreferences.getString("name", null);
        String email = sharedPreferences.getString("email", null);
        return new User(name, username,email, "", phoneNumber, isLoggedIn);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

}
