package com.example.emergencyapp.activities;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendRequestsAdapter extends ArrayAdapter<User> {

    public FriendRequestsAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_request_item, parent, false);
        }

        TextView userNameTextView = convertView.findViewById(R.id.textViewNameFriendRequest);
        Button acceptButton = convertView.findViewById(R.id.btnAcceptRequest);
        Button declineButton = convertView.findViewById(R.id.btnDeleteRequest);
        ImageView imageView = convertView.findViewById(R.id.imageViewProfileFriendRequest);

        userNameTextView.setText(user.getName());
        Picasso.get()
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.baseline_person_24)
                .into(imageView);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        acceptButton.setOnClickListener(v -> {
            if (firebaseUser != null) {
                acceptFriendRequest(firebaseUser, user.getUid());
                deleteFriendRequest(firebaseUser, user.getUid());
            }
        });

        declineButton.setOnClickListener(v -> {
            if(firebaseUser!=null){
                deleteFriendRequest(firebaseUser, user.getUid());
            }
        });

        return convertView;
    }

    private void acceptFriendRequest(FirebaseUser user, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends");
        databaseReference.child(user.getUid()).child(userId).setValue(true);
        databaseReference.child(userId).child(user.getUid()).setValue(true);
    }

    private void deleteFriendRequest(FirebaseUser user, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests");
        DatabaseReference userRef = databaseReference.child(user.getUid());
        userRef.child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Friend request deleted successfully.");
                    } else {
                        Log.e(TAG, "Failed to delete friend request.");
                    }
                });

    }
}
