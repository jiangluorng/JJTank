package com.jjstudio.jjtank;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.sumimakito.awesomeqr.AwesomeQRCode;
import com.jjstudio.jjtank.model.TankControlData;
import com.jjstudio.jjtank.service.BluetoothLeService;
import com.jjstudio.jjtank.util.DataUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRAS_TANK = "TANK";
    public static final String TANK_SPEED_DIRECTION_OFFSET = "SPEED_DIRECTION_OFFSET";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    private String tankName;
    private static final String JJCTRL_SERV_UUID = "0000FFF0";
    private static final String JJCTRL_CHNEL1_UUID = "0000FFF2";
    private String[] speedDirectionOffset;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothGattService;
    private boolean isRunning;
    private boolean isConnectted = true;
    private String mDeviceAddress;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl1;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl2;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl3;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl4;
//    private BluetoothGattCharacteristic bluetoothGattCharacteristicChl5;

    private ImageButton exitButton;
    private ImageButton switchButton;
    private ImageButton lightSwitchButton;
    private ImageButton mgSwitchButton;
    private ImageButton soundSwitchButton;
    private ImageButton gyroSwitchButton;
    private ImageButton fireButton;
    private ImageButton settingButton;
    private ImageButton qrButton;
    private ImageButton reconnectButton;
    private ImageButton bluetoothIndicator;
    private ImageButton bluetoothTxIndicator;
    private ImageButton bluetoothRxIndicator;
    private ImageButton turrentLeftButton;
    private ImageButton turrentUpButton;
    private ImageButton turrentRightButton;
    private ImageButton turrentDownButton;
    private ProgressBar throttleProgressBar;
    private TextView dataDisplayTextView;
    private TextView speedDirectionDataText;
    private TextView  receivedData;

    private TextView statusTextView;
    private byte[] speedDirectionData;
    private int blueBlinkInterval = 500; // 0.5 seconds by default
    private int txBlinkInterval = 200; // 0.5 seconds by default


    private Handler sendingDataHandler = new Handler();
    private Handler blueHandler = new Handler();
    private Handler blueTxHandler = new Handler();
    private Handler blueRxHandler = new Handler();

    private boolean isBluetoothBlinking;
    private Runnable bluetoothBlinking = new Runnable() {
        @Override
        public void run() {
            if (isBluetoothBlinking) {
                bluetoothIndicator.setImageResource(R.drawable.indilightonblue);
                isBluetoothBlinking = false;
            } else {
                bluetoothIndicator.setImageResource(R.drawable.indilightoff);
                isBluetoothBlinking = true;
            }
            // Repeat this the same runnable code block again another 1 seconds
            blueHandler.postDelayed(bluetoothBlinking, blueBlinkInterval);
        }
    };

    private boolean isTxBlinking;
    private int txTimer = 0;
    private Runnable txBlinking = new Runnable() {
        @Override
        public void run() {
            txTimer++;
            if (isTxBlinking) {
                bluetoothTxIndicator.setImageResource(R.drawable.indilightonblue);
                isTxBlinking = false;
            } else {
                bluetoothTxIndicator.setImageResource(R.drawable.indilightoff);
                isTxBlinking = true;
            }
            if (txTimer < 4) {
                // Repeat this the same runnable code block again another 1 seconds
                blueTxHandler.postDelayed(txBlinking, txBlinkInterval);
            } else {
                txTimer = 0;
            }
        }
    };

    private boolean isRxBlinking;

    private Runnable rxBlinking = new Runnable() {
        @Override
        public void run() {
            if (isRxBlinking) {
                bluetoothRxIndicator.setImageResource(R.drawable.indilightonblue);
                isRxBlinking = false;
            } else {
                bluetoothRxIndicator.setImageResource(R.drawable.indilightoff);
                isRxBlinking = true;
            }
            blueRxHandler.postDelayed(rxBlinking, txBlinkInterval);
        }
    };

    private Runnable sendingDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (speedDirectionData != null) {
                writeToCharacteristic(speedDirectionData, false);
                speedDirectionDataText.setText("Speed and direction: "+bytesToHex(speedDirectionData));

            }
            sendingDataHandler.postDelayed(sendingDataRunnable, blueBlinkInterval);
        }
    };

    private View loadingLayout;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDeviceAddress = getIntent().getExtras().getString(EXTRAS_DEVICE_ADDRESS);
        tankName = getIntent().getExtras().getString(EXTRAS_TANK);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingLayout = findViewById(R.id.loadingLayout);
        statusTextView = findViewById(R.id.statusTextView);
        dataDisplayTextView = findViewById(R.id.dataDisplayTextView);
        receivedData = findViewById(R.id.receivedData);
        speedDirectionDataText= findViewById(R.id.speedDirectionDataText);
        throttleProgressBar = findViewById(R.id.throttleProgressBar);
        statusTextView.setText("Connecting Tank " + tankName);
        switchButton = findViewById(R.id.startupButton);
        lightSwitchButton = findViewById(R.id.switch1);
        mgSwitchButton = findViewById(R.id.switch2);
        soundSwitchButton = findViewById(R.id.switch3);
        gyroSwitchButton = findViewById(R.id.switch4);
        fireButton = findViewById(R.id.fireButton);
        exitButton = findViewById(R.id.exitButton);
        settingButton = findViewById(R.id.settingButton);
        reconnectButton = findViewById(R.id.reconnectButton);
        bluetoothIndicator = findViewById(R.id.bluetoothIndicator);
        bluetoothTxIndicator = findViewById(R.id.bluetoothTxIndicator);
        bluetoothRxIndicator = findViewById(R.id.bluetoothRxIndicator);
        turrentLeftButton = findViewById(R.id.turrentLeftButton);
        turrentUpButton = findViewById(R.id.turrentUpButton);
        turrentRightButton = findViewById(R.id.turrentRightButton);
        turrentDownButton = findViewById(R.id.turrentDownButton);
        qrButton = findViewById(R.id.qrButton);


        fireButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        turrentLeftButton.setOnClickListener(this);
        turrentUpButton.setOnClickListener(this);
        turrentRightButton.setOnClickListener(this);
        turrentDownButton.setOnClickListener(this);
        lightSwitchButton.setOnClickListener(this);
        mgSwitchButton.setOnClickListener(this);
        soundSwitchButton.setOnClickListener(this);
        gyroSwitchButton.setOnClickListener(this);
        switchButton.setOnClickListener(this);
        qrButton.setOnClickListener(this);
        reconnectButton.setOnClickListener(this);
        connectTank();
        loadOffset();
    }

    private void loadOffset() {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE);
        speedDirectionOffset = prefs.getString(MainActivity.TANK_SPEED_DIRECTION_OFFSET, "0|0").split("\\|");
    }

    private void saveTank(String tankName, String mDeviceAddress) {
        SharedPreferences prefs = this.getSharedPreferences(
                "com.jjstudio.jjtank", Context.MODE_PRIVATE);
        prefs.edit().putString(MainActivity.EXTRAS_TANK, tankName).apply();
        prefs.edit().putString(MainActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress).apply();
    }

    private void checkSensorManager() {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            statusTextView.setText("Phone doesn't support Gypo");
        } else {
            sensorManager.registerListener(new SensorEventListener() {
                public void onSensorChanged(SensorEvent event) {
                    if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                        return;
                    }
                    float[] values = event.values;
                    float ax = values[0];
                    float ay = values[1];
                    float az = values[2];
                    statusTextView.setText(moveAndReturnValue(ax, ay, az));
                }

                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), txBlinkInterval);
        }
    }

    private void calculateSpeedDirectionData(int speed, int direction) {
        speedDirectionData = new byte[5];
        speedDirectionData[0] = (byte) 0xA5;
        speedDirectionData[1] = (byte) 0x00;
        speedDirectionData[2] = (byte) 0x00;
        speedDirectionData[3] = (byte) 0xA5;
        speedDirectionData[4] = (byte) 0xAA;
        if (speed != 0 && direction == 0) {
            if (speed>0){
                speedDirectionData[1] = (byte) speed;
                speedDirectionData[2] = (byte) speed;
            }else{
                speedDirectionData[1] = (byte) (40-speed);
                speedDirectionData[2] = (byte) (40-speed);

            }
        } else if (speed == 0 && direction != 0) {
            //右边原地打转
            if (direction<-25){
                speedDirectionData[1] = (byte) 0x32;
                speedDirectionData[2] = (byte) 0x72;
            }else if (direction>25){//左边原地
                speedDirectionData[1] = (byte) 0x72;
                speedDirectionData[2] = (byte) 0x32;
            }
            else if (direction>0){//左转
                speedDirectionData[1] = (byte) 0x00;
                speedDirectionData[2] = (byte) direction;
            }else {//右转
                speedDirectionData[1] = (byte) (-direction);
                speedDirectionData[2] = (byte) 0x00;
            }
        } else {
            if (direction > 0) {//左转行进
                speedDirectionData[1] = (byte) 0x32;
                speedDirectionData[2] = (byte) (32-direction);
            } else {//右转前进
                speedDirectionData[1] = (byte) (32+direction);
                speedDirectionData[2] = (byte) 0x32;
            }
        }
    }

    private String moveAndReturnValue(float ax, float ay, float az) {
        //x = speed, -10 means backwards full speed, 10 means forwards full speed
        // y= direction, -15 means left full, 5 = right full
//        writeToCharacteristic();
        String movement = "";

        int speed = getSpeed(ax);
        int direction = getDirection(ay);

        speed = speed+ Integer.parseInt(speedDirectionOffset[0]);
        direction = direction + Integer.parseInt(speedDirectionOffset[1]);

        if (speed > 0) {
            movement = "Forward, ";
        } else if (speed < 0) {
            movement = "Backward, ";

        }
        // adding 50 to display backwards speed
        throttleProgressBar.setProgress(speed + 50);
        if (direction != 0) {
            if (ay > -5) {
                movement += " Right, ";
            } else {
                movement += " Left, ";
            }
        }
        if (speed != 0 || direction != 0) {
            calculateSpeedDirectionData(speed, direction);
        } else {
            speedDirectionData = null;
        }
        movement = movement + "Speed " + speed;
        movement = movement + " Direction " + direction;
        return movement;
    }

    private int getSpeed(float ax) {
        return (int) (0 - ax) * 5;
    }

    private int getDirection(float ay) {
        return (int) ay  * 5;
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
        disconnectTank();
    }

    private void disconnectTank() {
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void connectTank() {
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        blueHandler.post(bluetoothBlinking);
        sendingDataHandler.post(sendingDataRunnable);
    }

    @Override
    public void onClick(View view) {
        if (view == exitButton) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Exit controller");
            alertDialog.setMessage("Do you want to exit controller?");

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    disconnectTank();
                    final Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                    startActivityForResult(intent, 0);
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Good choice", Toast.LENGTH_SHORT).show();
                }
            });

            alertDialog.show();
        }
        if (isConnectted) {
            if (view == settingButton) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Go to setting");
                alertDialog.setMessage("Do you want to go to  settings?");

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        disconnectTank();
                        final Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                        intent.putExtra(MainActivity.EXTRAS_TANK, tankName);
                        startActivity(intent);
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.show();
            }
            byte[] sendValue;
            if (view == fireButton) {
                if (isConnectted) {
                    sendValue = TankControlData.FIRE;
                    writeToCharacteristic(sendValue, true);
                }
            }
            if (view == lightSwitchButton || view == mgSwitchButton || view == soundSwitchButton || view == gyroSwitchButton) {
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
                writeToCharacteristic(sendValue, true);
            }
            if (view == switchButton) {
                if (isRunning) {
                    sendValue = TankControlData.STOP;
                    Toast.makeText(getApplicationContext(), "Tank stopped", Toast.LENGTH_SHORT).show();
                    isRunning = false;
                } else {
                    sendValue = TankControlData.GO;
                    Toast.makeText(getApplicationContext(), "Tank start", Toast.LENGTH_SHORT).show();
                    isRunning = true;
                }
                writeToCharacteristic(sendValue, true);
            }
            if (view == turrentLeftButton) {
                sendValue = TankControlData.TURRENT_LEFT;
                writeToCharacteristic(sendValue, true);
            }
            if (view == turrentRightButton) {
                sendValue = TankControlData.TURRENT_RIGHT;
                writeToCharacteristic(sendValue, true);
            }
            if (view == turrentUpButton) {
                sendValue = TankControlData.TURRENT_UP;
                writeToCharacteristic(sendValue, true);
            }
            if (view == turrentDownButton) {
                sendValue = TankControlData.TURRENT_DOWN;
                writeToCharacteristic(sendValue, true);
            }
            if (view == qrButton) {
                Dialog qrDialog = new Dialog(this);
                qrDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                qrDialog.setContentView(getLayoutInflater().inflate(R.layout.qr_image_layout
                        , null));
                ImageView qrImage = qrDialog.findViewById(R.id.generatedQRImageView);
                Bitmap backgroundBitmap = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.merkava4);
                Bitmap qrCode = AwesomeQRCode.create(tankName + "|" + mDeviceAddress, 800, 20, 0.3f, Color.BLACK, Color.WHITE, backgroundBitmap, true, true);
                qrImage.setImageBitmap(qrCode);
                qrDialog.show();
            }
        }
        if (view == reconnectButton) {
            connectTank();
        }

    }


    private void writeToCharacteristic(byte[] data, boolean updateUI) {
        bluetoothGattCharacteristicChl1.setValue(data);
        if (updateUI) {
            dataDisplayTextView.setText(bytesToHex(data));
        }
        if (bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicChl1)) {
            Log.v(TAG, ("Write data:" + DataUtils.bytesToHex(data) + " on characteristic" + bluetoothGattCharacteristicChl1.getUuid() + " of service $service success"));
        } else {
            Log.v(TAG, ("Write data:" + DataUtils.bytesToHex(data) + " on characteristic" + bluetoothGattCharacteristicChl1.getUuid() + " of service $service failed"));
        }
        blueTxHandler.post(txBlinking);
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

    private void displayReceivedData(String data) {
        if (data != null) {
            receivedData.setText(data);
        }
    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {
        statusTextView.setText("Tank " + tankName + " successfully connected, now you can control it!");
        bluetoothGattService = getJJBluetoothGattService(gattServices);
        if (bluetoothGattService == null) {
            statusTextView.setText("No BLE service matching!");
            loadingLayout.setVisibility(View.GONE);
            return;
        }
        bluetoothGattCharacteristicChl1 = getJJBluetoothGattCharacteristic(bluetoothGattService);
        if (bluetoothGattCharacteristicChl1 == null) {
            statusTextView.setText("No BLE characteristic matching!");
            loadingLayout.setVisibility(View.GONE);
            return;
        }
        statusTextView.setText("Tank connected!");
        loadingLayout.setVisibility(View.GONE);
        checkSensorManager();
        isConnectted = true;
        saveTank(tankName, mDeviceAddress);
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
                loadingLayout.setVisibility(View.GONE);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                bluetoothGatt = mBluetoothLeService.getmBluetoothGatt();
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayReceivedData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
