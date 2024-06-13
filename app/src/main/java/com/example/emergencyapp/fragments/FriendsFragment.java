package com.example.emergencyapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.emergencyapp.R;
import com.example.emergencyapp.activities.AddContactActivity;
import com.example.emergencyapp.activities.AddFriendActivity;
import com.example.emergencyapp.activities.ChatActivity;
import com.example.emergencyapp.activities.FriendsAdapter;
import com.example.emergencyapp.activities.FriendsListActivity;
import com.example.emergencyapp.entities.AlertMessage;
import com.example.emergencyapp.entities.User;
import com.example.emergencyapp.utils.FriendItem;
import com.example.emergencyapp.utils.FriendsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private FriendsViewModel friendsViewModel;
    private ListView friendsListView;
    private List<FriendItem> friends = new ArrayList<>();
    private TextView messageTextView;
    private Button addFriendButton;
    private FriendsAdapter adapter;
    private DatabaseReference chatRoomsRef;
    private ValueEventListener chatRoomsListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        messageTextView = view.findViewById(R.id.emptyListMessage);
        addFriendButton = view.findViewById(R.id.addFriendButton);
        friendsListView = view.findViewById(R.id.friends_list);
        adapter = new FriendsAdapter(getContext(), friends);
        friendsListView.setAdapter(adapter);
        friendsViewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
        updateFriendsList();
        friendsListView.setOnItemClickListener((parent, v, position, id) -> {
            FriendItem selectedUser = friends.get(position);
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("receiverId", selectedUser.getFriendId());
            intent.putExtra("receiverName", selectedUser.getName());
            intent.putExtra("receiverProfileImageUrl", selectedUser.getProfileImageUrl());
            startActivity(intent);
        });

        addFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddFriendActivity.class);
            startActivity(intent);
        });

        listenForMessages();
        return view;
    }

    @Override
    public void onResume() {
        friendsViewModel.fetchFriends();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatRoomsRef != null && chatRoomsListener != null) {
            chatRoomsRef.removeEventListener(chatRoomsListener);
        }
    }

    private void updateFriendsList(){
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
    }

    private void listenForMessages() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (chatRoomsRef != null && chatRoomsListener != null) {
                chatRoomsRef.removeEventListener(chatRoomsListener);
            }

            chatRoomsRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(currentUser.getUid());
            chatRoomsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                        String chatRoomId = chatRoomSnapshot.getKey();
                        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Messages").child(chatRoomId);

                        ValueEventListener messageListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                friendsViewModel.fetchFriends();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                            }
                        };

                        messagesRef.addValueEventListener(messageListener);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error
                }
            };

            chatRoomsRef.addValueEventListener(chatRoomsListener);
        }
    }
}
