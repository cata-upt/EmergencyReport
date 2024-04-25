package com.example.emergencyapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    TextView personName, changeNameTextView, changePictureTextView, changePhoneNumberTextView;

    Button logoutButton;

    UserSessionManager userSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        userSession = new UserSessionManager(this);
        if(!userSession.getLoginDetails().isLoggedIn){
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_profile);
        personName = findViewById(R.id.person_name);
        changeNameTextView = findViewById(R.id.changeNameTextView);
        changePictureTextView = findViewById(R.id.changePictureTextView);
        changePhoneNumberTextView = findViewById(R.id.changePhoneNumberTextView);
        logoutButton = findViewById(R.id.logout_button);

        try {
            personName.setText(userSession.getLoginDetails().username);
        }catch (Exception e){
            Log.e("ProfileActivity", e.getMessage());
        }

        logoutButton.setOnClickListener(v->logOut());
        changeNameTextView.setOnClickListener(v->showChangeNameDialog());
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();

        userSession.clearSession();

        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    public void showChangeNameDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_change_name, null);
            builder.setView(dialogView);

            EditText editNewUsername = dialogView.findViewById(R.id.edit_new_name);
            Button submitButton = dialogView.findViewById(R.id.button_submit);
            AlertDialog dialog = builder.create();

            submitButton.setOnClickListener(v -> {
                String newName = editNewUsername.getText().toString();
                if (!newName.isEmpty()) {
                    updateUserName(user, newName);
                    dialog.dismiss(); // Close the dialog
                } else {
                    editNewUsername.setError("Name cannot be empty");
                }
            });

            dialog.show();
        }
    }

    private void updateUserName(FirebaseUser user, String newUsername) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.child(user.getUid()).child("username").setValue(newUsername)
                .addOnSuccessListener(aVoid -> Log.d("Update Username", "Username updated successfully!"))
                .addOnFailureListener(e -> Log.d("Update Username", "Failed to update username", e));
        userSession.saveLoginDetails(newUsername);
        personName.setText(userSession.getLoginDetails().username);
    }

}
