package com.example.emergencyapp.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class DatabaseConnectionUtils {

    public DatabaseConnectionUtils() {
    }

    public void savePhoneNumberUid(FirebaseUser user, DatabaseReference databaseReference, String phoneNumber) throws RuntimeException{
        databaseReference.child(phoneNumber).setValue(user.getUid())
                .addOnSuccessListener(v-> Log.d("Update phone number", "Phone number updated successfully in phoneUid!"))
                .addOnFailureListener(e-> {throw new RuntimeException(e.getMessage());});
    }

    public void savePhoneNumberUserDetails(FirebaseUser user, DatabaseReference databaseReference, String phoneNumber) throws RuntimeException{
        databaseReference.child(user.getUid()).child("phoneNumber").setValue(phoneNumber)
                .addOnSuccessListener(aVoid -> Log.d("Update phone number", "Phone number updated successfully!"))
                .addOnFailureListener(e-> {throw new RuntimeException(e.getMessage());});
    }

    public void saveUserDetails(User user){

    }
}
