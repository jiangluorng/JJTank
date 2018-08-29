package com.jjstudio.jjtank;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    private ImageButton backButton;
    private ImageButton saveButton;
    private String[] speedDirectionOffset;
    private EditText directionOffset;
    private EditText speedOffset;
    private String mDeviceAddress;
    private String tankName;
    private Spinner bluetoothIntervalSpinner;
    private static final Integer[] interval = {100, 200, 500,800};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mDeviceAddress = getIntent().getExtras().getString(MainActivity.EXTRAS_DEVICE_ADDRESS);
        tankName = getIntent().getExtras().getString(MainActivity.EXTRAS_TANK);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        directionOffset = findViewById(R.id.directionOffset);
        speedOffset = findViewById(R.id.speedOffset);
        bluetoothIntervalSpinner = findViewById(R.id.bluetoothIntervalSpinner);
        backButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        bluetoothIntervalSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<Integer>adapter = new ArrayAdapter<Integer>(SettingActivity.this,
                android.R.layout.simple_spinner_item,interval);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bluetoothIntervalSpinner.setAdapter(adapter);
        bluetoothIntervalSpinner.setOnItemSelectedListener(this);
        loadSetting();
    }

    private void loadSetting() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE);
        speedDirectionOffset = prefs.getString(MainActivity.TANK_SPEED_DIRECTION_OFFSET, "0|0").split("\\|");
        int savedInterval = prefs.getInt(MainActivity.EXTRAS_BLUETOOTH_INTERVAL,100);
        if (savedInterval==100){
            bluetoothIntervalSpinner.setSelection(0);
        }else if (savedInterval==200){
            bluetoothIntervalSpinner.setSelection(1);
        }else if (savedInterval==500){
            bluetoothIntervalSpinner.setSelection(2);
        }else {
            bluetoothIntervalSpinner.setSelection(3);
        }
        directionOffset.setText(speedDirectionOffset[0]);
        directionOffset.setText(speedDirectionOffset[1]);
    }

    @Override
    public void onClick(View view) {
        if (view == backButton) {
            onBackPressed();
        }
        if (view == saveButton) {
            SharedPreferences prefs = this.getSharedPreferences(
                    "com.jjstudio.jjtank", Context.MODE_PRIVATE);
            prefs.edit().putString(MainActivity.TANK_SPEED_DIRECTION_OFFSET, speedOffset.getText().toString() + "|" + directionOffset.getText().toString()).apply();
            Toast.makeText(getApplicationContext(), "Setting saved!", Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            intent.putExtra(MainActivity.EXTRAS_TANK, tankName);
            startActivity(intent);
        }
    }
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE);
        prefs.edit().putInt(MainActivity.EXTRAS_BLUETOOTH_INTERVAL, (Integer)parent.getItemAtPosition(position)).apply();
        }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE); prefs.edit().putInt(MainActivity.EXTRAS_BLUETOOTH_INTERVAL, 200).apply();

    }
}
