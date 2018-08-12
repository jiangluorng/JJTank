package com.jjstudio.jjtank.model;

import android.bluetooth.BluetoothDevice;

import lombok.Data;

@Data
public class Tank {
    private String title;
    private String uuid;
    private StatusEnum status;
    private BluetoothDevice bluetoothDevice;

    public Tank(String title, String uuid, StatusEnum status,BluetoothDevice bluetoothDevice) {
        this.title = title;
        this.uuid = uuid;
        this.status = status;
        this.bluetoothDevice = bluetoothDevice;
    }
}
