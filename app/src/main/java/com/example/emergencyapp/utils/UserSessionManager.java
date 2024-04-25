package com.example.emergencyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "MyAppPreferences";

    public UserSessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    // To save user login status and username
    public void saveLoginDetails(String username) {
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    // To check if user is logged in and get the username
    public Pair<Boolean, String> getLoginDetails() {
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", null);
        return new Pair<>(isLoggedIn, username);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    // Helper class to handle Pair since Java doesn't have a built-in Pair class
    public static class Pair<T, U> {
        public final T isLoggedIn;
        public final U username;

        public Pair(T isLoggedIn, U username) {
            this.isLoggedIn = isLoggedIn;
            this.username = username;
        }
    }
}
