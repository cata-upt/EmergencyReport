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

import com.example.emergencyapp.BaseApplication;
import com.example.emergencyapp.R;
import com.example.emergencyapp.api.ApiService;
import com.example.emergencyapp.api.utils.ExtraDataNotifications;
import com.example.emergencyapp.api.utils.NotificationRequestApi;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.UserHelper;
import com.example.emergencyapp.utils.UserSessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FriendRequestsAdapter extends ArrayAdapter<User> {

    private User user;

    public FriendRequestsAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        user = getItem(position);
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
                updateStatusFriendRequest(firebaseUser, user.getUid(), "accepted");
            }
        });

        declineButton.setOnClickListener(v -> {
            if (firebaseUser != null) {
                updateStatusFriendRequest(firebaseUser, user.getUid(), "rejected");
            }
        });

        return convertView;
    }

    private void acceptFriendRequest(FirebaseUser user, String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Friends");
        databaseReference.child(user.getUid()).child(userId).setValue(true);
        databaseReference.child(userId).child(user.getUid()).setValue(true);
        sendAcceptNotification(userId);
    }

    private void sendAcceptNotification(String userId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        UserSessionManager userSession = new UserSessionManager(this.getContext());

        String title = userSession.getLoginDetails().getName();
        String body = "Your friend request was accepted.";

        ExtraDataNotifications extraDataNotifications = new ExtraDataNotifications();
        extraDataNotifications.addData("targetActivity", "FriendRequests");
        extraDataNotifications.addData("fragment", "FriendsFragment");

        DatabaseReference userIdToken = FirebaseDatabase.getInstance().getReference("tokens").child(userId);
        userIdToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String token = dataSnapshot.getValue(String.class);
                if (token != null) {
                    NotificationRequestApi notificationRequestApi = new NotificationRequestApi(token, title, body);
                    notificationRequestApi.setExtraDataNotifications(extraDataNotifications);
                    UserHelper.sendUserNotification(service, notificationRequestApi, token, getContext(), "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Messaging Service", "Failed to get token for notification", databaseError.toException());
            }
        });
    }

    private void updateStatusFriendRequest(FirebaseUser firebaseUser, String friendId, String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("FriendRequests");
        DatabaseReference userRef = databaseReference.child(firebaseUser.getUid());
        userRef.child(friendId).child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Friend request status updated to false successfully.");
                    } else {
                        Log.e(TAG, "Failed to update friend request status.");
                    }
                });
        remove(user);
        notifyDataSetChanged();
    }
}
