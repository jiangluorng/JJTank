package com.jjstudio.jjtank;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRAS_TANK = "TANK";
    public static final String EXTRAS_BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    private BluetoothDevice bluetoothDevice;
    private String tankName;
    private static final String JJCTRL_SERV_UUID = "0000FFB0";
    private static final String JJCTRL_CHNEL1_UUID = "0000FFB1";
    private static final String TAG = SplashActivity.class.getSimpleName();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl1;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl2;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl3;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl4;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl5;

    private TextView statusTextView;
    private byte[] sendValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bluetoothDevice = getIntent().getExtras().getParcelable(EXTRAS_BLUETOOTH_DEVICE);
        tankName = getIntent().getExtras().getString(EXTRAS_TANK);
        statusTextView = findViewById(R.id.statusTextView);
//        statusTextView.setText("Connecting Tank "+tankName);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectDevice();
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
            if (bluetoothGattService==null){
//                statusTextView.setText("No BLE service matching!");
            }
            BluetoothGattCharacteristic characteristic = getJJBluetoothGattCharacteristic(bluetoothGattService);
            if (characteristic==null){
//                statusTextView.setText("No BLE characteristic matching!");
            }
            byte s1 = (byte) 0xC5;
            byte s4 = (byte) 0xAA;
            byte s2 = (byte) 0x50;
            byte s3 = (byte) 0x50;
            sendValue = new byte[]{s1, s2, s3, s4};
            characteristic.setValue(sendValue);
            Log.v(TAG, "Write data: $sendValue on characteristic $characteristic of service $service");
            if (gatt.writeCharacteristic(characteristic)) {
                Log.v(TAG, ("Write data: $sendValue on characteristic $characteristic of service $service success"));
            } else {
                Log.v(TAG, ("Write data: $sendValue on characteristic $characteristic of service $service failed"));

            }
        }

        private BluetoothGattService getJJBluetoothGattService(List<BluetoothGattService> services){
            for (BluetoothGattService bluetoothGattService: services){
                if (bluetoothGattService.getUuid().toString().toUpperCase().startsWith(JJCTRL_SERV_UUID)){
                    return bluetoothGattService;
                }
            }
            return null;
        }

        private BluetoothGattCharacteristic getJJBluetoothGattCharacteristic( BluetoothGattService bluetoothGattService){
            for (BluetoothGattCharacteristic characteristic: bluetoothGattService.getCharacteristics()){
                if (characteristic.getUuid().toString().toUpperCase().startsWith(JJCTRL_CHNEL1_UUID)){
                    return characteristic;
                }
            }
            return null;
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
