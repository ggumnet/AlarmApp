package com.example.alarmapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;




public class BluetoothSet extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "";
    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    TextView textStatus;
    Button btnParied, btnSearch, btnSend;
    ListView listView;  // listview는 pairing된 기기들을 세로로 정렬

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_set);
        // Enable bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
        // 블루투스 화면에서 기능들을 수행하기 위한 변수들
        textStatus = (TextView) findViewById(R.id.text_status);
        btnParied = (Button) findViewById(R.id.btn_paired);
        btnSearch = (Button) findViewById(R.id.btn_search);
        listView = (ListView) findViewById(R.id.listview);
        // Show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);

        listView.setOnItemClickListener(new myOnItemClickListener());
    }

    public void onClickButtonPaired(View view){
        try {
            btArrayAdapter.clear();
            if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                deviceAddressArray.clear();
            }
            pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceHardwareAddress);
                }
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    public void onClickButtonSearch(View view){
        // Check if the device is already discovering
        try {
            if (btAdapter.isDiscovering()) {
                btAdapter.cancelDiscovery();
            } else {
                if (btAdapter.isEnabled()) {
                    btAdapter.startDiscovery();
                    btArrayAdapter.clear();
                    if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                        deviceAddressArray.clear();
                    }
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(receiver, filter);
                } else {
                    Toast.makeText(getApplicationContext(), "bluetooth not on", Toast.LENGTH_SHORT).show();
                }
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceHardwareAddress);
                    btArrayAdapter.notifyDataSetChanged();
                }
            }catch(SecurityException e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }


    //클릭했을 때 실행 되는 listener
    public class myOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplicationContext(), btArrayAdapter.getItem(position), Toast.LENGTH_SHORT).show();

            textStatus.setText("연결중");

            final String name = btArrayAdapter.getItem(position); // get name
            final String address = deviceAddressArray.get(position); // get address
            boolean flag = true;

            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            //device 까지는 얻어짐.

            // create & connect socket
            try {
                //btSocket = createBluetoothSocket(device);
                btSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                btSocket.connect();  //btSocket의 mPort가 -1인 문제.
            } catch (IOException e) {
                flag = false;
                textStatus.setText("connection failed!");
                e.printStackTrace();
            } catch(SecurityException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }

            // start bluetooth communication
            if(flag){
                textStatus.setText("connected to "+name);

                //TODO: 얘를 RepeatAlarmActivity에 전달해야한다.

                setGlobalSocket(btSocket);
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
            }

            ThreadObject threadObject = new ThreadObject("alarm", "1111", "test" , connectedThread);
            Intent intent = new Intent(getApplicationContext(), AlarmSetActivity.class);
            //intent.putExtra("thread", threadObject); //intent 사이에 값을 key-value로 전달
            try {
                startActivity(intent);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    //처음에 여기서 connection 오류 발생
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        try {
            return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
        }catch(SecurityException e){
            e.printStackTrace();
        }
        return null;
    }
    public void setGlobalSocket(BluetoothSocket bluetoothSocket){
        ((MyApplication) this.getApplication()).setBluetoothSocket(bluetoothSocket);
    }

    public class ThreadObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String id;
        private String dev;
        private ConnectedThread connectedThread;
        public ThreadObject(String name, String id, String dev, ConnectedThread connectedThread) {
            this.name = name;
            this.id = id;
            this.dev = dev;
            this.connectedThread = connectedThread;
        }
        public ConnectedThread getConnectedThread(){
            return connectedThread;
        }
    }
}