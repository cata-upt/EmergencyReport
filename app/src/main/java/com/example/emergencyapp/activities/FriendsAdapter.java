package com.example.emergencyapp.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emergencyapp.R;
import com.example.emergencyapp.utils.User;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private Context context;
    private List<User> friends;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProfile;
        private final TextView textViewName;

        public ViewHolder(View view) {
            super(view);
            imageViewProfile = view.findViewById(R.id.imageViewProfile);
            textViewName = view.findViewById(R.id.textViewName);
        }

        public void bind(Context context, final User user) {
            textViewName.setText(user.getName());
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .into(imageViewProfile);
        }
    }

    public FriendsAdapter(Context context, List<User> friends) {
        this.context = context;
        this.friends = friends;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(context, friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }
}
