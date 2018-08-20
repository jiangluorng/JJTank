package com.jjstudio.jjtank;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton backButton;
    private ImageButton saveButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        backButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view==backButton){
            onBackPressed();
        }
        if (view==saveButton){

        }
    }
}
