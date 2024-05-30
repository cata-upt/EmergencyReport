package com.example.emergencyapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.emergencyapp.R;

public class AlertDialogFragment extends DialogFragment {
    private static final int TIMER_DURATION = 5000; // 5 seconds
    private Handler handler;
    private Runnable runnable;

    public interface AlertDialogFragmentListener {
        void onYesClicked();
    }

    private AlertDialogFragmentListener listener;

    public void setListener(AlertDialogFragmentListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alert_dialog_fragment, container, false);

        TextView textView = view.findViewById(R.id.dialog_text);
        textView.setText("Do you want to perform the action?");

        Button yesButton = view.findViewById(R.id.button_yes);
        Button noButton = view.findViewById(R.id.button_no);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onYesClicked();
                }
                dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onYesClicked();
                }
                dismiss();
            }
        };

        handler.postDelayed(runnable, TIMER_DURATION);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
