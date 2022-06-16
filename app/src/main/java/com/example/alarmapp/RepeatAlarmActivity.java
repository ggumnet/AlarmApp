package com.example.alarmapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import java.io.IOException;
import java.io.OutputStream;

public class RepeatAlarmActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_alarm);


        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.walwal);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        setContentView(R.layout.activity_repeat_alarm);
        sendStart();

    }
    public void stopAlarm(View view){
        mediaPlayer.stop();
        finish();
    }
    public void sendStart(){
        BluetoothSocket bluetoothSocket = ((MyApplication) this.getApplication()).getBluetoothSocket();

        OutputStream tmpOut = null, mmOutStream;
        try {
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        mmOutStream = tmpOut;
        try {
            mmOutStream.write("start".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}