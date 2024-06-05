package com.example.emergencyapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.emergencyapp.R;
import com.example.emergencyapp.adapters.SectionsAdapter;
import com.google.android.material.tabs.TabLayout;

public class FriendsListActivity extends AppCompatActivity{
    TabLayout tabLayout;
    ViewPager2 viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        viewPager.setAdapter(new SectionsAdapter(this));
    }

}
