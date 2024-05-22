package com.example.emergencyapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.AddFriendActivity;
import com.example.emergencyapp.activities.FriendsAdapter;
import com.example.emergencyapp.utils.FriendsViewModel;
import com.example.emergencyapp.utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsFragment extends Fragment {

    private FriendsViewModel friendsViewModel;
    private ListView friendsListView;
    TextView messageTextView;
    Button addFriendButton;
    private FriendsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        messageTextView = view.findViewById(R.id.emptyListMessage);
        addFriendButton = view.findViewById(R.id.addFriendButton);
        friendsListView = view.findViewById(R.id.friends_list);
        adapter = new FriendsAdapter(getContext(), new ArrayList<>());
        friendsListView.setAdapter(adapter);
        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
        friendsViewModel.getFriends().observe(getViewLifecycleOwner(), users -> {
            adapter.clear();
            adapter.addAll(users);
            adapter.notifyDataSetChanged();
            if (users.isEmpty()) {
                messageTextView.setVisibility(View.VISIBLE);
            } else {
                messageTextView.setVisibility(View.GONE);
            }
        });
        return view;
    }
}
