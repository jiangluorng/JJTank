package com.jjstudio.jjtank.model;

import android.bluetooth.BluetoothDevice;

import lombok.Data;

@Data
public class Tank {
    private String title;
    private String uuid;
    private StatusEnum status;
    private BluetoothDevice bluetoothDevice;
}
