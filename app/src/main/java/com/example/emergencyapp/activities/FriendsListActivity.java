package com.example.emergencyapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.emergencyapp.R;
import com.example.emergencyapp.fragments.FriendRequestsFragment;
import com.example.emergencyapp.fragments.FriendsFragment;
import com.example.emergencyapp.utils.SectionsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class FriendsListActivity extends AppCompatActivity{
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        SectionsAdapter sectionsAdapter = new SectionsAdapter(this);
        viewPager.setAdapter(sectionsAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(sectionsAdapter.getTabTitle(position))
        ).attach();

        Intent intent = getIntent();
        if( intent !=null){
            String fragmentPosition = intent.getStringExtra("fragment");
            if (fragmentPosition!=null && fragmentPosition.equals("FriendRequestsFragment")) {
                viewPager.setCurrentItem(1);
            }else {
                viewPager.setCurrentItem(0);
            }
        }
    }

}
