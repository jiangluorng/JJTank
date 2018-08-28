package com.jjstudio.jjtank;

import android.Manifest;
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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class SplashActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> tankUUIDs;

    private Button rescanButton;
    private Button scanQRButton;
    private List<Tank> tankList;
    private boolean mScanning;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler delayHandler;
    private long BLE_SCAN_STOP_DELAY = 10000; //10 seconds for blescaner
    private Context context;
    private TextView tankInfoTextView;
    private LocationManager locationManager;
    private String lastConnectedTankName;
    private String lastConnectedTankAddress;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        tankUUIDs = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mRecyclerView = findViewById(R.id.tankListView);
        tankInfoTextView = findViewById(R.id.tankInfos);

        rescanButton = findViewById(R.id.rescan);
        scanQRButton = findViewById(R.id.scanQRButton);
        rescanButton.setOnClickListener(rescanButtonOnClickListener);
        scanQRButton.setOnClickListener(scanQrButtonOnClickListener);
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();
        initBluetooth();
        initLastConnecttedTank();
    }


    private void initLastConnecttedTank() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE);
        lastConnectedTankName = prefs.getString(MainActivity.EXTRAS_TANK, "");
        lastConnectedTankAddress = prefs.getString(MainActivity.EXTRAS_DEVICE_ADDRESS, "");

        if (!lastConnectedTankName.equals("") && !lastConnectedTankAddress.equals("")) {
            tankUUIDs.add(lastConnectedTankAddress);
            tankList.add(new Tank(lastConnectedTankName, lastConnectedTankAddress, StatusEnum.LastConnected, null));
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();

        Log.i("Location info: Lat", lat.toString());
        Log.i("Location info: Lng", lng.toString());

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private View.OnClickListener rescanButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setEnabled(false);
            startScanning();
        }
    };

    private View.OnClickListener scanQrButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stopScanning();
            final Intent intent = new Intent(SplashActivity.this, ScanActivity.class);
            startActivity(intent);
        }
    };

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SplashActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
//                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

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
        }
    }

    private boolean checkBluetooth() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private void startScanning() {
        rescanButton.setEnabled(false);
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
                rescanButton.setEnabled(true);
                mScanning = false;

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
            if (deviceName != null && !tankUUIDs.contains(deviceAddress) && (TankControlData.isTest || deviceName != null && deviceName.startsWith("JJtk"))) {
                tankUUIDs.add(deviceAddress);
                tankList.add(new Tank(deviceName, deviceAddress, StatusEnum.Disconnected, bluetoothDevice));
                tankInfoTextView.append("Found device  " + deviceName + " -  " + deviceAddress + "\n");
                mAdapter.notifyDataSetChanged();
            }
        }
    };


}
