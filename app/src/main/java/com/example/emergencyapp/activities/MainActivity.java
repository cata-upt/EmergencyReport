package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.ContextCompat;

import com.example.emergencyapp.utils.AlertMessagingUtils;
import com.example.emergencyapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private Button sendTextButton;

    private static String TAG = "EmergencyReport";

    private FirebaseUser user;

    private Queue<Snackbar> snackbarQueue = new LinkedList<>();
    AlertMessagingUtils alertMessagingUtils;
    private boolean isBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = FirebaseAuth.getInstance().getCurrentUser();

        alertMessagingUtils = new AlertMessagingUtils(getApplicationContext());

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        sendTextButton = findViewById(R.id.send_text_button);
        sendTextButton.setOnClickListener(view -> handleSendText());

        if (user == null) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "For a better experience we recommend to sign into your account.", Snackbar.LENGTH_LONG)
                    .setAction("SIGN IN", v -> {
                        Intent i = new Intent(MainActivity.this, SignInActivity.class);
                        startActivity(i);
                        finish();
                    });
            showSnackbar(snackbar);
        }

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "For a better experience we recommend to grant permissions for this app.", Snackbar.LENGTH_LONG)
                .setAction("GRANT", v -> {
                    Intent i = new Intent(MainActivity.this, PermissionsActivity.class);
                    startActivity(i);
                    finish();
                });
        showSnackbar(snackbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");
        alertMessagingUtils.handleSendText();
    }

    private boolean isShowing = false;

    private void showSnackbar(Snackbar snackbar) {
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color));

        TextView textView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        Button actionButton = snackBarView.findViewById(com.google.android.material.R.id.snackbar_action);
        actionButton.setTextColor(ContextCompat.getColor(this, R.color.black));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (!snackbarQueue.isEmpty()) {
                    Snackbar next = snackbarQueue.poll();
                    next.show();
                }
            }
        });

        snackbarQueue.add(snackbar);
        if (!isShowing) {
            showNext();
        }
    }

    private void showNext() {
        if (!snackbarQueue.isEmpty() && !isShowing) {
            Snackbar next = snackbarQueue.poll();
            next.show();
            isShowing = true;
        }
    }
}
