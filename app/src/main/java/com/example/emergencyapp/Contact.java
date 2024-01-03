package com.example.emergencyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Contact implements Serializable {
    String name;
    String phoneNumber;

    public Contact() {
    }

    public Contact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    protected Contact(Parcel in) {
        name = in.readString();
        phoneNumber = in.readString();
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static void saveContactsToPreferences(Context context, List<Contact> contacts) {
        Gson gson = new Gson();
        String serializedContacts = gson.toJson(contacts);

        SharedPreferences preferences = context.getSharedPreferences("MyContacts", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("contacts", serializedContacts);
        editor.apply();
        Log.d("Contact", "saveContactsToPreferences: "+serializedContacts);
    }

    // Load contacts from SharedPreferences
    public static ArrayList<Contact> loadContactsFromPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyContacts", Context.MODE_PRIVATE);
        String serializedContacts = preferences.getString("contacts", "");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Contact>>() {}.getType();

        Log.d("Contact", "loadContactsFromPreferences: "+serializedContacts);
        return gson.fromJson(serializedContacts, type);
    }

    @Override
    public String toString() {
        return getName()+" : "+getPhoneNumber();
    }
}
