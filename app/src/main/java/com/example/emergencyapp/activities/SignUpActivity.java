package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.emergencyapp.R;
import com.example.emergencyapp.exceptions.UserException;
import com.example.emergencyapp.utils.DatabaseConnectionUtils;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.UserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameField, usernameField, emailField, passwordField, phoneNumberField;
    private Button registerButton;
    private TextView signInTextView, nameLabel, usernameLabel, emailLabel, passwordLabel, phoneNumberLabel;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private DatabaseConnectionUtils databaseConnectionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        findViews();
        databaseConnectionUtils = new DatabaseConnectionUtils();

        signInTextView.setOnClickListener(v -> {
            signInActivity();
        });

        registerButton.setOnClickListener(view -> register());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hideMessageTextView();
            }
        };

        nameField.addTextChangedListener(textWatcher);
        usernameField.addTextChangedListener(textWatcher);
        emailField.addTextChangedListener(textWatcher);
        passwordField.addTextChangedListener(textWatcher);
        phoneNumberField.addTextChangedListener(textWatcher);
        setFocusChangeListener(nameField, nameLabel);
        setFocusChangeListener(usernameField, usernameLabel);
        setFocusChangeListener(emailField, emailLabel);
        setFocusChangeListener(passwordField, passwordLabel);
        setFocusChangeListener(phoneNumberField, phoneNumberLabel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu) {
            Intent i = new Intent(SignUpActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void findViews() {
        nameField = findViewById(R.id.nameEditText);
        usernameField = findViewById(R.id.usernameEditTextRegister);
        emailField = findViewById(R.id.emailEditText);
        passwordField = findViewById(R.id.signupPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        nameLabel = findViewById(R.id.signupNameLabel);
        usernameLabel = findViewById(R.id.signupUsernameLabel);
        emailLabel = findViewById(R.id.sigupEmailLabel);
        passwordLabel = findViewById(R.id.signupPasswordLabel);
        phoneNumberField = findViewById(R.id.phoneNumberEditText);
        phoneNumberLabel = findViewById(R.id.signupPhoneNumberLabel);
        signInTextView = findViewById(R.id.signInTextView);
    }

    private void setFocusChangeListener(EditText editText, TextView label) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                label.setVisibility(View.VISIBLE);
                hideMessageTextView();
            } else {
                label.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void signInActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    private void register() {
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users");

        String name = nameField.getEditableText().toString();
        String username = usernameField.getEditableText().toString();
        String email = emailField.getEditableText().toString();
        String password = passwordField.getEditableText().toString();
        String phoneNumber = phoneNumberField.getEditableText().toString();

        User user = new User(name, username, email, password, phoneNumber, true);

        try {
            UserHelper.validateUser(user);
            hideMessageTextView();
            registerUser(user);
            accountCreatedSuccessfully();
        } catch (UserException exception) {
            displayMessageTextView(exception.getMessage());
        } catch (Exception e) {
            displayMessageTextView("Something went wrong! Try again!");
        }

    }

    private void registerUser(User user) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            firebaseDatabase.getReference("Users").child(userId).setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            try {
                                                databaseConnectionUtils.savePhoneNumberUid(firebaseUser, firebaseDatabase.getReference("phone_to_uid"), user.getPhoneNumber());
                                                Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                                finish();
                                            }catch (RuntimeException e){
                                                Log.e("Update phone number", "Failed to update phone number to uid", e);
                                            }

                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Failed to register", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void displayMessageTextView(String message) {
        TextView errorTextView = findViewById(R.id.registerMessageTextView);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void hideMessageTextView() {
        TextView errorTextView = findViewById(R.id.registerMessageTextView);
        if (errorTextView.getVisibility() == View.VISIBLE) {
            errorTextView.setVisibility(View.GONE);
        }
    }

    private void accountCreatedSuccessfully() {
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        passwordField.setText("");
        displayMessageTextView("Account created successfully!");
    }
}
