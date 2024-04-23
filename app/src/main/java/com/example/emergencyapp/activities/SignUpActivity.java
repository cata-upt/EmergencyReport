package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.exceptions.UserException;
import com.example.emergencyapp.utils.PasswordCryptUtils;
import com.example.emergencyapp.utils.UserHelperClass;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText nameField, usernameField, emailField, passwordField;
    Button registerButton;
    TextView signInTextView, nameLabel, usernameLabel, emailLabel, passwordLabel;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        findViews();

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
        setFocusChangeListener(nameField, nameLabel);
        setFocusChangeListener(usernameField, usernameLabel);
        setFocusChangeListener(emailField, emailLabel);
        setFocusChangeListener(passwordField, passwordLabel);
    }

    private void findViews(){
        nameField=findViewById(R.id.nameEditText);
        usernameField =findViewById(R.id.usernameEditText);
        emailField = findViewById(R.id.usernameEditText);
        passwordField = findViewById(R.id.signupPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        nameLabel = findViewById(R.id.signupNameLabel);
        usernameLabel = findViewById(R.id.signupUsernameLabel);
        emailLabel = findViewById(R.id.sigupEmailLabel);
        passwordLabel = findViewById(R.id.signupPasswordLabel);
        signInTextView = findViewById(R.id.signInTextView);
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

    private void signInActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    private void register(){
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users");

        String name = nameField.getEditableText().toString();
        String username = usernameField.getEditableText().toString();
        String email = emailField.getEditableText().toString();
        String password = passwordField.getEditableText().toString();

        UserHelperClass helperClass = new UserHelperClass(name, username, email, password);

        try {
            helperClass.validateUser();
            hideMessageTextView();

            byte[] salt = PasswordCryptUtils.getNextSalt();
            String saltString = Base64.encodeToString(salt, Base64.DEFAULT);
            helperClass.setSalt(saltString);

            helperClass.setPassword(PasswordCryptUtils.encryptPassword(password,salt));

            reference.child(username).setValue(helperClass);
            accountCreatedSuccessfully();
        } catch (UserException exception) {
            displayMessageTextView(exception.getMessage());
        } catch (Exception e) {
            displayMessageTextView("Something went wrong! Try again!");
        }

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

    private void accountCreatedSuccessfully(){
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        displayMessageTextView("Account created successfully!");
    }
}
