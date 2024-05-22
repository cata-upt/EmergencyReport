package com.example.emergencyapp.activities;

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
import com.example.emergencyapp.utils.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<User> {

    public FriendsAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_item_layout, parent, false);
        }

        ImageView profileImageView = convertView.findViewById(R.id.imageViewProfileFriend);
        TextView userNameTextView = convertView.findViewById(R.id.textViewNameFriend);

        userNameTextView.setText(user.getName());
        Picasso.get().load(user.getProfileImageUrl())
                .placeholder(R.drawable.baseline_person_24)
                .into(profileImageView);

        return convertView;
    }
}
