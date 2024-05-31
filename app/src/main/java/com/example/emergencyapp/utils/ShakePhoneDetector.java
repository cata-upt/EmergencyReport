package com.example.emergencyapp.utils;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakePhoneDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 5.0F;
    private static final int SHAKE_SLOP_TIME_MS = 15000;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 15000;

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;

    public interface OnShakeListener {
        void onShake(int count);
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuacy) {
        // Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mListener.onShake(mShakeCount);
            }
        }
    }
}
