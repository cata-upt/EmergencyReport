package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ShakeService extends Service {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakePhoneDetector mShakeDetector;
    private WindowManager windowManager;
    private View overlayView;
    private FirebaseUser user;
    private AlertMessagingUtils alertMessagingUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        user = FirebaseAuth.getInstance().getCurrentUser();
        alertMessagingUtils = new AlertMessagingUtils(getApplicationContext());
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakePhoneDetector();
        mShakeDetector.setOnShakeListener(count -> {
            performAction();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EmergencyReportChannelID", "EmergencyReportChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "EmergencyReportChannelID")
                .setContentTitle("Shake Service")
                .setContentText("Shake detection is running")
                .setSmallIcon(R.drawable.baseline_notification_important_24)
                .setContentIntent(pendingIntent);

        startForeground(1, notificationBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performAction() {
        showDialog();
    }

    private void showDialog() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        overlayView = inflater.inflate(R.layout.alert_dialog_fragment, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.OPAQUE);
        params.gravity = Gravity.CENTER;

        Button yesButton = overlayView.findViewById(R.id.button_yes);
        Button noButton = overlayView.findViewById(R.id.button_no);
        TextView textView = overlayView.findViewById(R.id.dialog_text);
        textView.setText(R.string.do_you_really_want_to_send_an_emergency_text);

        yesButton.setOnClickListener(v -> {
            handleSendText();
            windowManager.removeView(overlayView);
            overlayView = null;
        });

        noButton.setOnClickListener(v -> {
            windowManager.removeView(overlayView);
            overlayView = null;
        });

        windowManager.addView(overlayView, params);

        new Handler().postDelayed(() -> {
            if (overlayView != null) {
                windowManager.removeView(overlayView);
                overlayView = null;
            }
        }, 5000); // 5 seconds timer
    }

    public void handleSendText() {
        Log.d(TAG, "Button clicked, initiating SMS sending process");
        alertMessagingUtils.handleSendText();
    }
}
