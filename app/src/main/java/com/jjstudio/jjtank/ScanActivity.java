package com.jjstudio.jjtank;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

public class ScanActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener, View.OnClickListener {

    private QRCodeReaderView qrCodeReaderView;

    private TextView tankDeviceAddress;

    private String tankUUID;

    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        tankUUID ="";
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        connectButton = findViewById(R.id.connectButton);
        tankDeviceAddress= findViewById(R.id.tankDeviceAddress);
        connectButton.setOnClickListener(this);
        initQRScanner();
    }

    @Override
    public void onClick(View view) {
        final Intent intent = new Intent(ScanActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, tankUUID);
        startActivity(intent);
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        tankUUID = text;
        connectButton.setVisibility(View.VISIBLE);
        tankDeviceAddress.setText("Found tank -> " +text);
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void initQRScanner(){
        qrCodeReaderView.setOnQRCodeReadListener(this);
        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);
        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);
        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);
        // Use this function to set front camera preview
        qrCodeReaderView.setFrontCamera();
        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();
    }
}
