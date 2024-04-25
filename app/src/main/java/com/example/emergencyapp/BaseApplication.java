package com.example.emergencyapp;

import android.app.Application;
import android.util.Log;

import com.example.emergencyapp.utils.UserHelper;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setupFirebaseAuthListener();
    }

    private void setupFirebaseAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            UserSessionManager preferences = new UserSessionManager(getApplicationContext());
            if (user == null) {
                preferences.clearSession();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserHelper userProfile = dataSnapshot.getValue(UserHelper.class);
                        if (userProfile != null) {
                            preferences.saveLoginDetails(userProfile.getUsername());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Database", "loadPost:onCancelled", databaseError.toException());
                    }
                });
            }
        });
    }
}
