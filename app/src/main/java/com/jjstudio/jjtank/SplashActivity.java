package com.jjstudio.jjtank;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjstudio.jjtank.adapter.TankAdapter;
import com.jjstudio.jjtank.listener.RecyclerViewClickListener;
import com.jjstudio.jjtank.model.StatusEnum;
import com.jjstudio.jjtank.model.Tank;
import com.jjstudio.jjtank.model.TankControlData;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> tankUUIDs;

    private Button rescanButton;
    private List<Tank> tankList;
    private boolean mScanning;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler delayHandler;
    private long BLE_SCAN_STOP_DELAY = 10000; //10 seconds for blescaner
    private Context context;
    private TextView tankInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        tankUUIDs = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mRecyclerView = findViewById(R.id.tankListView);
        tankInfoTextView = findViewById(R.id.tankInfos);
        rescanButton = findViewById(R.id.rescan);
        rescanButton.setOnClickListener(rescanButtonOnClickListener);
        mRecyclerView.setHasFixedSize(true);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        tankList = new ArrayList<>();


        // specify an adapter (see also next example)
        mAdapter = new TankAdapter(tankList, recyclerViewClickListener);
        mRecyclerView.setAdapter(mAdapter);
        initBluetooth();
    }

    private View.OnClickListener rescanButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.INVISIBLE);
            startScanning();
        }
    };

    private void initBluetooth() {
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (!checkBluetooth()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Bluetooth alert");
            alertDialog.setMessage("Please check your bluetooth setting first.");

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS), 0);
                }
            });

            alertDialog.show();
        } else {
            startScanning();
        }
    }

    private boolean checkBluetooth() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private void startScanning() {
        rescanButton.setVisibility(View.INVISIBLE);
        tankInfoTextView.setText("Scanning tanks...");
        delayHandler = new Handler();
        delayHandler.postDelayed(stopBleRunnable, BLE_SCAN_STOP_DELAY);
        mScanning = true;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.startScan(leScanCallback);
            }
        });
    }

    private void stopScanning() {
        mScanning = false;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
                tankInfoTextView.setText("Scanning stopped.");
                rescanButton.setVisibility(View.VISIBLE);

            }
        });
    }

    private Runnable stopBleRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScanning) {
                Toast.makeText(context, "Stop BLE after 10s to save battery.", Toast.LENGTH_SHORT).show();
                stopScanning();
            }
        }
    };

    private RecyclerViewClickListener recyclerViewClickListener = new RecyclerViewClickListener() {
        @Override
        public void onClick(View view, int position) {
            Tank tank = tankList.get(position);
            final Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRAS_TANK, tank.getTitle());
            intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, tank.getUuid());
            intent.putExtra(MainActivity.EXTRAS_BLUETOOTH_DEVICE, tank.getBluetoothDevice());
            if (mScanning) {
                stopScanning();
            }
            startActivity(intent);
        }
    };
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice bluetoothDevice = result.getDevice();
            String deviceAddress = bluetoothDevice.getAddress();
            String deviceName = bluetoothDevice.getName();
            if (!tankUUIDs.contains(deviceAddress) && (TankControlData.isTest || deviceName != null && deviceName.startsWith("JJtk"))) {
                tankList.add(new Tank(deviceName, deviceAddress, StatusEnum.Disconnected, bluetoothDevice));
                tankInfoTextView.append("\nfound device... " + deviceName + " -  " + deviceAddress);
            }
        }
    };


}
