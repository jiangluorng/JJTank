package com.jjstudio.jjtank;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jjstudio.jjtank.model.TankControl;
import com.jjstudio.jjtank.model.TankControlData;
import com.jjstudio.jjtank.util.DataUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRAS_TANK = "TANK";
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
    private ProgressBar progressBar;


    private TextView statusTextView;
    private TankControl tankControl;
    private byte[] sendValue;
    private LinearLayout loading;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bluetoothDevice = getIntent().getExtras().getParcelable(EXTRAS_BLUETOOTH_DEVICE);
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
        connectDevice();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.GONE);
            }
        }, 3000);

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


    private void connectDevice() {
        bluetoothDevice.connectGatt(this, true, leConnectCallback);
    }

    private BluetoothGattCallback leConnectCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Tank " + tankName + " disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            super.onServicesDiscovered(gatt, status);
            bluetoothGatt = gatt;
//            mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
            List<BluetoothGattService> services = gatt.getServices();
            bluetoothGattService = getJJBluetoothGattService(services);
            if (bluetoothGattService == null) {
//                MainActivity.this.statusTextView.setText("No BLE service matching!");
            }
            bluetoothGattCharacteristicChl1 = getJJBluetoothGattCharacteristic(bluetoothGattService);
            if (bluetoothGattCharacteristicChl1 == null) {
//                MainActivity.this.statusTextView.setText("No BLE characteristic matching!");
            }
//            MainActivity.this.statusTextView.setText("Tank connected!");
            isConnectted = true;
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            //rewrite data
            if (!characteristic.getValue().equals(sendValue)) {
                gatt.writeCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}
