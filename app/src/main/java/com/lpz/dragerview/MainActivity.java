package com.lpz.dragerview;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DragLayerLayout dragLayerLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dragLayerLayout = findViewById(R.id.drag_layer_layout);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        MyViewPagerAdapter adapter = new MyViewPagerAdapter(getSupportFragmentManager(), dragLayerLayout);
        dragLayerLayout.setDragCallback(adapter);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());
        tabLayout.setupWithViewPager(viewPager);
    }
}
