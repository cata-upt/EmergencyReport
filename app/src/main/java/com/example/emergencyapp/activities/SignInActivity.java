package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.PasswordCryptUtils;
import com.example.emergencyapp.utils.UserHelper;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    EditText usernameField, passwordField;
    Button loginButton;

    TextView usernameLabel, passwordLabel, loginTextView;

    FirebaseAuth auth;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    UserSessionManager userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        usernameField = findViewById(R.id.usernameEditTextLogin);
        passwordField = findViewById(R.id.loginPasswordEditText);
        usernameLabel = findViewById(R.id.loginUsernameLabel);
        passwordLabel = findViewById(R.id.loginPasswordLabel);
        loginTextView= findViewById(R.id.loginMessageTextView);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v->logIn());

        TextView signUpTextView = findViewById(R.id.signUpTextView);
        signUpTextView.setOnClickListener(v -> {
            signUpActivity();
        });

        setFocusChangeListener(usernameField, usernameLabel);
        setFocusChangeListener(passwordField, passwordLabel);
    }

    private void signUpActivity() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void logIn(){
        auth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users");
        userSession = new UserSessionManager(this);

        String username = usernameField.getEditableText().toString();
        String password = passwordField.getEditableText().toString();


        loginUser(username, password);
    }

    private void loginUser(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            saveUsernameInSavedPreferences(user);
            startActivity(new Intent(SignInActivity.this, ProfileActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUsernameInSavedPreferences(FirebaseUser user){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(user.getUid()).child("username");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.getValue(String.class);
                    userSession.saveLoginDetails(username);
                    Log.d("FirebaseData", "User" + user);
                } else {
                    Log.d("FirebaseData", "User does not exist.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FirebaseData", "Failed to read user data.", databaseError.toException());
            }
        });
    }

    private void setFocusChangeListener(EditText editText, TextView label) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                label.setVisibility(View.VISIBLE);
                hideMessageTextView();
            } else{
                label.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void displayMessageTextView(String message) {
        loginTextView.setText(message);
        loginTextView.setVisibility(View.VISIBLE);
    }

    private void hideMessageTextView() {
        if (loginTextView.getVisibility() == View.VISIBLE) {
            loginTextView.setVisibility(View.GONE);
        }
    }
}
