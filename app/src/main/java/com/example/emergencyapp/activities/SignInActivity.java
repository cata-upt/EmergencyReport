package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.User;
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
        userSession = new UserSessionManager(getApplicationContext());

        setContentView(R.layout.activity_signin);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(SignInActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void signUpActivity() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void logIn(){
        auth = FirebaseAuth.getInstance();
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users");


        String username = usernameField.getEditableText().toString();
        String password = passwordField.getEditableText().toString();


        loginUser(username, password);
    }

    private void loginUser(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        assert user != null;
                        saveUserDetailsInSavedPreferences(user);
                    } else {
                        Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateUI(User user) {
        if (user != null) {
            Intent intent = new Intent(SignInActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserDetailsInSavedPreferences(FirebaseUser user){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference userRef = databaseReference.child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setLoggedIn(true);
                        userSession.saveLoginDetails(user);
                        registerUserDeviceToken();
                        updateUI(user);
                        Log.d("FirebaseData", "User" + user);
                    } else {
                        Log.d("FirebaseData", "User does not exist.");
                    }
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

    private void registerUserDeviceToken(){
        ((BaseApplication) getApplicationContext()).obtainFirebaseToken();
    }
}
