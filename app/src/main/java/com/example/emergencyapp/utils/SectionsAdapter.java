package com.example.emergencyapp.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.emergencyapp.fragments.FriendRequestsFragment;
import com.example.emergencyapp.fragments.FriendsFragment;

public class SectionsAdapter extends FragmentStateAdapter{

    public SectionsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FriendsFragment();
            case 1:
                return new FriendRequestsFragment();
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}