package com.example.alarmapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RepeatAlarmActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Intent intent;
    BluetoothSet.ThreadObject threadObject;
    ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat_alarm);

        /*Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();*/

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.walwal);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        /*
        intent = getIntent();
        threadObject = (BluetoothSet.ThreadObject) intent.getSerializableExtra("thread");
        connectedThread = threadObject.getConnectedThread();

        if(connectedThread!=null){
            connectedThread.write("start");
        }*/
        BluetoothSocket bluetoothSocket = ((MyApplication) this.getApplication()).getBluetoothSocket();

        OutputStream tmpOut = null, mmOutStream;
        try {
            tmpOut = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
        }

        mmOutStream = tmpOut;
        try {
            mmOutStream.write("start".getBytes());
        } catch (IOException e) {
        }
    }
    public void stopAlarm(View view){
        mediaPlayer.stop();
        finish();
    }
}