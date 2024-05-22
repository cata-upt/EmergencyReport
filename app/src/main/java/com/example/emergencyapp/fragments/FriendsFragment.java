package com.example.emergencyapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.FriendsAdapter;
import com.example.emergencyapp.utils.FriendsViewModel;

import java.util.ArrayList;

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
