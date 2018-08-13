package com.jjstudio.jjtank;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjstudio.jjtank.model.TankControlData;
import com.jjstudio.jjtank.service.BluetoothLeService;
import com.jjstudio.jjtank.util.DataUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRAS_TANK = "TANK";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    private BluetoothDevice bluetoothDevice;
    private String tankName;
    private static final String JJCTRL_SERV_UUID = "0000FFB0";
    private static final String JJCTRL_CHNEL1_UUID = "0000FFB1";
    private static final String TAG = SplashActivity.class.getSimpleName();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private boolean isRunning;
    private boolean isConnectted;
    private String mDeviceAddress;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl1;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl2;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl3;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl4;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl5;

    private ImageButton startupButton;
    private ImageButton switch1Button;
    private ImageButton switch2Button;
    private ImageButton switch3Button;
    private ImageButton switch4Button;
    private ImageButton fireButton;


    private TextView statusTextView;
    private byte[] sendValue;
    private LinearLayout loading;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bluetoothDevice = getIntent().getExtras().getParcelable(EXTRAS_BLUETOOTH_DEVICE);
        mDeviceAddress = getIntent().getExtras().getString(EXTRAS_DEVICE_ADDRESS);
        tankName = getIntent().getExtras().getString(EXTRAS_TANK);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = findViewById(R.id.statusTextView);
        statusTextView.setText("Connecting Tank " + tankName);
        startupButton = findViewById(R.id.startupButton);
        loading = findViewById(R.id.loading);
        switch1Button = findViewById(R.id.switch1);
        switch2Button = findViewById(R.id.switch2);
        switch3Button = findViewById(R.id.switch3);
        switch4Button = findViewById(R.id.switch4);
        fireButton = findViewById(R.id.fireButton);
        fireButton.setOnClickListener(this);
        switch1Button.setOnClickListener(this);
        switch2Button.setOnClickListener(this);
        switch3Button.setOnClickListener(this);
        switch4Button.setOnClickListener(this);
        startupButton.setOnClickListener(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onClick(View view) {
        if (isConnectted) {
            if (view == fireButton) {
                if (isConnectted) {
                    sendValue = TankControlData.FIRE;
                    writeToCharacteristic();
                }
            }
            if (view == switch1Button || view == switch2Button || view == switch3Button || view == switch4Button) {
                ImageButton btn = (ImageButton) view;
                boolean isOn = (btn.getTag() != null && (boolean) btn.getTag());
                if (!isOn) {
                    btn.setImageResource(R.drawable.swithfieldon);
                    btn.setTag(true);
                    sendValue = TankControlData.SWT_1_ON;
                } else {
                    btn.setImageResource(R.drawable.swithfieldoff);
                    sendValue = TankControlData.SWT_1_OFF;
                    btn.setTag(false);
                }
                writeToCharacteristic();
            }
            if (view == startupButton) {
                if (isRunning) {
                    sendValue = TankControlData.STOP;
                    isRunning = false;
                } else {
                    sendValue = TankControlData.GO;
                    isRunning = true;
                }
                writeToCharacteristic();
            }
        }
    }


    private void writeToCharacteristic() {
        bluetoothGattCharacteristicChl1.setValue(sendValue);
        Log.v(TAG, "Write data: $sendValue on characteristic $characteristic of service $service");
        if (bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicChl1)) {
            Log.v(TAG, ("Write data:" + DataUtils.bytesToHex(sendValue) + " on characteristic" + bluetoothGattCharacteristicChl1.getUuid() + " of service $service success"));
        } else {
            Log.v(TAG, ("Write data:" + DataUtils.bytesToHex(sendValue) + " on characteristic" + bluetoothGattCharacteristicChl1.getUuid() + " of service $service failed"));
        }
    }

    private BluetoothGattService getJJBluetoothGattService(List<BluetoothGattService> services) {
        for (BluetoothGattService bluetoothGattService : services) {
            if (bluetoothGattService.getUuid().toString().toUpperCase().startsWith(JJCTRL_SERV_UUID)) {
                return bluetoothGattService;
            }
        }
        return null;
    }

    private BluetoothGattCharacteristic getJJBluetoothGattCharacteristic(BluetoothGattService bluetoothGattService) {
        for (BluetoothGattCharacteristic characteristic : bluetoothGattService.getCharacteristics()) {
            if (characteristic.getUuid().toString().toUpperCase().startsWith(JJCTRL_CHNEL1_UUID)) {
                return characteristic;
            }
        }
        return null;
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            statusTextView.setText(data);
        }
    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        statusTextView.setText("Tank " + tankName + " successfully connected, now you can control it!");
        bluetoothGattService = getJJBluetoothGattService(gattServices);
        if (bluetoothGattService == null) {
            statusTextView.setText("No BLE service matching!");
        }
        bluetoothGattCharacteristicChl1 = getJJBluetoothGattCharacteristic(bluetoothGattService);
        if (bluetoothGattCharacteristicChl1 == null) {
            statusTextView.setText("No BLE characteristic matching!");
        }
        statusTextView.setText("Tank connected!");
        loading.setVisibility(View.GONE);
        isConnectted = true;
    }

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                bluetoothGatt = mBluetoothLeService.getmBluetoothGatt();
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };
}
