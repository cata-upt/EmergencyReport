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

    private List<User> friendsList = new ArrayList<>();
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
        adapter = new FriendsAdapter(getContext(), friendsList);
        friendsListView.setAdapter(adapter);

        fetchFriends();
        if(friendsList.size() == 0){
            messageTextView.setVisibility(View.VISIBLE);
        }
        addFriendButton.setOnClickListener(v->showAddFriendsActivity());
        return view;
    }

    private void fetchFriends() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference friendsRef = databaseReference.child(user.getUid()).child("friends");

            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    friendsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User friend = snapshot.getValue(User.class);
                        friendsList.add(friend);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FriendsListActivity", "Failed to read friends", databaseError.toException());
                }
            });
        }
    }

    private void showAddFriendsActivity() {
        Intent intent = new Intent(getContext(), AddFriendActivity.class);
        startActivity(intent);
    }
}
