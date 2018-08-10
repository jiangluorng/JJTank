package com.jjstudio.jjtank;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jjstudio.jjtank.adapter.TankAdapter;
import com.jjstudio.jjtank.model.Tank;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mRecyclerView = findViewById(R.id.tankListView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        List<Tank> tankList = new ArrayList<>();
        // specify an adapter (see also next example)
        mAdapter = new TankAdapter(tankList);
        mRecyclerView.setAdapter(mAdapter);
    }
}
