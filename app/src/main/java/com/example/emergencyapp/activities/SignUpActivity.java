package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
                hideErrorMessage();
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
        emailField = findViewById(R.id.emailEditText);
        passwordField = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        nameLabel = findViewById(R.id.nameLabel);
        usernameLabel = findViewById(R.id.usernameLabel);
        emailLabel = findViewById(R.id.emailLabel);
        passwordLabel = findViewById(R.id.passwordLabel);
        signInTextView = findViewById(R.id.signInTextView);
    }

    private void setFocusChangeListener(EditText editText, TextView label) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                label.setVisibility(View.VISIBLE);
                hideErrorMessage();
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
            hideErrorMessage();
            helperClass.setPassword(PasswordCryptUtils.encryptPassword(password));
            reference.child(username).setValue(helperClass);
        }catch (UserException exception){
            displayErrorMessage(exception.getMessage());
        }catch (Exception e){
            displayErrorMessage("Something went wrong! Try again!");
        }

    }

    private void displayErrorMessage(String message) {
        TextView errorTextView = findViewById(R.id.errorTextView);
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        TextView errorTextView = findViewById(R.id.errorTextView);
        if (errorTextView.getVisibility() == View.VISIBLE) {
            errorTextView.setVisibility(View.GONE);
        }
    }
}
