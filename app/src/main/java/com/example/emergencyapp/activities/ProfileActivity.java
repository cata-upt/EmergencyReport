package com.example.emergencyapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.exceptions.UserException;
import com.example.emergencyapp.utils.DatabaseCallback;
import com.example.emergencyapp.utils.DatabaseConnectionUtils;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.UserHelper;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView personName,changeEmailTextView, changeNameTextView, changePictureTextView, changePhoneNumberTextView, locationTextView;

    private Button logoutButton;

    private UserSessionManager userSession;
    private FirebaseUser user;
    private User userDetails;
    private DatabaseConnectionUtils databaseConnectionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        databaseConnectionUtils = new DatabaseConnectionUtils();
        user = FirebaseAuth.getInstance().getCurrentUser();
        userSession = new UserSessionManager(getApplicationContext());
        userDetails = userSession.getLoginDetails();
        if (userDetails == null || !userDetails.isLoggedIn()) {
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        personName = findViewById(R.id.person_name);
        changeEmailTextView = findViewById(R.id.changeEmailTextView);
        changeNameTextView = findViewById(R.id.changeNameTextView);
        changePictureTextView = findViewById(R.id.changePictureTextView);
        changePhoneNumberTextView = findViewById(R.id.changePhoneNumberTextView);
        locationTextView = findViewById(R.id.locationTextView);
        logoutButton = findViewById(R.id.logout_button);

        try {
            personName.setText(userSession.getLoginDetails().getUsername());
            retrieveProfilePictureFromStorage();
        } catch (Exception e) {
            Log.e("ProfileActivity", Objects.requireNonNull(e.getMessage()));
        }

        logoutButton.setOnClickListener(v -> logOut());
        changeEmailTextView.setOnClickListener(v->showChangeEmailDialog());
        changeNameTextView.setOnClickListener(v -> showChangeNameDialog());
        changePictureTextView.setOnClickListener(v -> pickImage());
        changePhoneNumberTextView.setOnClickListener(v -> showChangePhoneNumberDialog());
        locationTextView.setOnClickListener(v -> showMapActivity());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu) {
            Intent i = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (user != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference profileImageRef = storageRef.child("images/" + user.getUid() + ".jpg");

            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Uri downloadUrl = uri;
                        //Bitmap bmp = ImageUtils.getResizedBitmap(downloadUrl.toString(), 50, 50);
                        //todo: resolve bug
                        //byte[] imgByte = ImageUtils.compressBitmap(bmp, 250);
                        //downloadUrl = ImageUtils.byteArrayToTempUri(getApplicationContext(), imgByte, user.getUid());
                        updateUserProfilePicture(user, uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();

        userSession.clearSession();

        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    public void showChangeEmailDialog() {
        if (user != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_change_attributes, null);
            builder.setView(dialogView);

            EditText editNewEmail = dialogView.findViewById(R.id.edit_text);
            Button submitButton = dialogView.findViewById(R.id.button_submit);
            AlertDialog dialog = builder.create();

            editNewEmail.setText(userSession.getLoginDetails().getEmail());

            submitButton.setOnClickListener(v -> {
                String newEmail = editNewEmail.getText().toString();
                if (!newEmail.isEmpty()) {
                    updateEmail(user, newEmail);
                    dialog.dismiss(); // Close the dialog
                } else {
                    editNewEmail.setError("Email cannot be empty");
                }
            });

            dialog.show();
        }
    }

    public void showChangeNameDialog() {
        if (user != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_change_attributes, null);
            builder.setView(dialogView);

            EditText editNewUsername = dialogView.findViewById(R.id.edit_text);
            Button submitButton = dialogView.findViewById(R.id.button_submit);
            AlertDialog dialog = builder.create();

            editNewUsername.setText(userSession.getLoginDetails().getUsername());

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

    public void showChangePhoneNumberDialog() {
        if (user != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_change_attributes, null);
            builder.setView(dialogView);

            EditText editNewPhoneNumber = dialogView.findViewById(R.id.edit_text);
            editNewPhoneNumber.setText(R.string.change_phone_number_field);
            Button submitButton = dialogView.findViewById(R.id.button_submit);
            submitButton.setText(R.string.change_phone_number_submit_button);
            AlertDialog dialog = builder.create();

            editNewPhoneNumber.setText(userSession.getLoginDetails().getPhoneNumber());
            editNewPhoneNumber.setHint(R.string.change_phone_number_hint);

            submitButton.setOnClickListener(v -> {
                String newPhoneNumber = editNewPhoneNumber.getText().toString();
                if (!newPhoneNumber.isEmpty()) {
                    updatePhoneNumber(user, newPhoneNumber);
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    private void updateEmail(FirebaseUser user, String newEmail) {
        try {
            UserHelper.validateEmail(newEmail);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            databaseReference.child(user.getUid()).child("email").setValue(newEmail)
                    .addOnSuccessListener(aVoid -> Log.d("Update email", "Email updated successfully!"))
                    .addOnFailureListener(e -> Log.d("Update email", "Failed to update email", e));
            this.userDetails.setEmail(newEmail);
            userSession.saveLoginDetails(this.userDetails);
            Toast.makeText(getApplicationContext(), "Email changed successfully!", Toast.LENGTH_SHORT).show();
        }catch (UserException userException){
            Toast.makeText(this, userException.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void updateUserName(FirebaseUser user, String newUsername) {
        try {
            UserHelper.validateUsername(newUsername);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            databaseReference.child(user.getUid()).child("username").setValue(newUsername)
                    .addOnSuccessListener(aVoid -> Log.d("Update Username", "Username updated successfully!"))
                    .addOnFailureListener(e -> Log.d("Update Username", "Failed to update username", e));
            this.userDetails.setUsername(newUsername);
            userSession.saveLoginDetails(this.userDetails);
            Toast.makeText(getApplicationContext(), "Username changed successfully!", Toast.LENGTH_SHORT).show();
            personName.setText(userSession.getLoginDetails().getUsername());
        }catch (UserException userException){
            Toast.makeText(this, userException.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updatePhoneNumber(FirebaseUser user, String newPhoneNumber) {
        try {
            UserHelper.validatePhoneNumber(newPhoneNumber);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            databaseConnectionUtils.savePhoneNumberUserDetails(user, firebaseDatabase.getReference("Users"), newPhoneNumber);
            databaseConnectionUtils.savePhoneNumberUid(user, firebaseDatabase.getReference("phone_to_uid"), newPhoneNumber);
            this.userDetails.setPhoneNumber(newPhoneNumber);
            userSession.saveLoginDetails(this.userDetails);
        } catch (RuntimeException e) {
            Log.e("Update phone number", "Failed to update the phone number in the database", e);
        }catch (UserException userException){
            Toast.makeText(this, userException.getMessage(), Toast.LENGTH_LONG).show();
        }

        Toast.makeText(getApplicationContext(), "Phone number changed successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateUserProfilePicture(FirebaseUser user, String imageUrl) {
        Log.i("EmergencyApp", "updateUserProfilePicture: saving image url");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        databaseReference.child("profileImageUrl").setValue(imageUrl);
        retrieveProfilePictureFromStorage();
    }

    private void showMapActivity() {
        Intent i = new Intent(ProfileActivity.this, LocationActivity.class);
        startActivity(i);
    }

    private void retrieveProfilePictureFromStorage() {
        UserHelper.retrieveProfilePictureFromStorage(user.getUid(), (DatabaseCallback<String>) uri -> {
            Log.d("Profile", "Profile picture URL: " + uri);
            loadImageIntoView(uri);
        });
    }

    private void loadImageIntoView(String imageUrl) {
        ImageView imageView = findViewById(R.id.profile_image);
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.baseline_person_24)
                .into(imageView);
    }

}
