package com.example.emergencyapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.FriendRequestsAdapter;
import com.example.emergencyapp.utils.FriendRequestsViewModel;

import java.util.ArrayList;

public class FriendRequestsFragment extends Fragment {

    private FriendRequestsViewModel friendRequestsViewModel;
    private FriendRequestsAdapter adapter;
    ListView friendRequestsListView;
    TextView messageTextView;

    public FriendRequestsFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        messageTextView = view.findViewById(R.id.emptyListMessageFR);
        friendRequestsListView = view.findViewById(R.id.friend_request_list);
        adapter = new FriendRequestsAdapter(getContext(), new ArrayList<>());
        friendRequestsListView.setAdapter(adapter);
        friendRequestsViewModel = new ViewModelProvider(this).get(FriendRequestsViewModel.class);
        friendRequestsViewModel.getFriendRequests().observe(getViewLifecycleOwner(), users -> {
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
