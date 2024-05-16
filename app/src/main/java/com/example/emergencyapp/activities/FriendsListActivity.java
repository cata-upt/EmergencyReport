package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
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

public class FriendsListActivity extends AppCompatActivity{
    private FriendsAdapter adapter;
    private List<User> friendsList = new ArrayList<>();
    TextView messageTextView;
    Button addFriendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        messageTextView = findViewById(R.id.emptyListMessage);
        addFriendButton = findViewById(R.id.addFriendButton);

        RecyclerView recyclerView = findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter(FriendsListActivity.this, friendsList);
        recyclerView.setAdapter(adapter);

        fetchFriends();
        if(friendsList.size() == 0){
            messageTextView.setVisibility(View.VISIBLE);
            addFriendButton.setVisibility(View.VISIBLE);
        }
        addFriendButton.setOnClickListener(v->showAddFriendsActivity());
    }

    private void showAddFriendsActivity() {
        Intent intent = new Intent(FriendsListActivity.this, AddFriendActivity.class);
        startActivity(intent);
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
}
