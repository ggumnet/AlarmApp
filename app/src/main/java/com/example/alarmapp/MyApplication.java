package com.example.alarmapp;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class MyApplication extends Application {
    private BluetoothSocket bluetoothSocket;

    public BluetoothSocket getBluetoothSocket(){
        return this.bluetoothSocket;
    }
    public void setBluetoothSocket(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket = bluetoothSocket;
    }
}
