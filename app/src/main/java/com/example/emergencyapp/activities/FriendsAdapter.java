package com.example.emergencyapp.activities;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.emergencyapp.R;
import com.example.emergencyapp.entities.FriendItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<FriendItem> {

    public FriendsAdapter(Context context, List<FriendItem> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FriendItem user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_item_layout, parent, false);
        }

        ImageView profileImageView, unreadMessageIndicator;
        TextView userNameTextView, lastMessageTextView;
        profileImageView = convertView.findViewById(R.id.imageViewProfileFriend);
        userNameTextView = convertView.findViewById(R.id.textViewNameFriend);
        lastMessageTextView = convertView.findViewById(R.id.lastMessageText);
        unreadMessageIndicator = convertView.findViewById(R.id.unreadMessageIndicator);

        userNameTextView.setText(user.getName());
        lastMessageTextView.setText(user.getMessage());
        unreadMessageIndicator.setVisibility(user.getUnreadMessages() ? View.VISIBLE : View.GONE);

        Picasso.get().load(user.getProfileImageUrl())
                .placeholder(R.drawable.baseline_person_24)
                .into(profileImageView);

        return convertView;
    }


}
