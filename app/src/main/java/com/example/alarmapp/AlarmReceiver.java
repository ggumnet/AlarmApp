package com.example.alarmapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.widget.Toast;
import android.net.ConnectivityManager;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


//alarm 신호를 receive해서 RepeatAlarmActivity를 실행 시킴.
public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    private static PowerManager.WakeLock sCpuWakeLock;
    private static WifiManager.WifiLock sWifiLock;
    private static ConnectivityManager manager;

    @Override
    public void onReceive(Context context, Intent intent) { //onReceive: 방송 수신되면 자동 호출되는 함수. context: 어플리케이션 context

        if (sCpuWakeLock != null) {
            return;
        }

        if (sWifiLock != null) {
            return;
        }

        // 절전모드로 와이파이 꺼지는것을 방지
        WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        sWifiLock = wifiManager.createWifiLock("wifilock");
        sWifiLock.setReferenceCounted(true);
        sWifiLock.acquire();

        // 시스템에서 powermanager 받아옴
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // 객체의 제어레벨 설정
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "app:alarm");

        //acquire 함수를 실행하여 앱을 깨운다. cpu 를 획득한다
        sCpuWakeLock.acquire();


        manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        this.context = context;

        // 작동할 액티비티를 설정한다
        //Intent alarmIntent = new Intent("android.intent.action.sec");


        /*
        Intent i = new Intent();
        i.setClassName("com.example.alarmapp", "com.example.alarmapp.RepeatAlarmActivity");
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(i.addFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_MULTIPLE_TASK));

         */


        // acquire 함수를 사용하였으면 꼭 release 를 해주어야 한다.
        // cpu를 점유하게 되어 배터리 소모나 메모리 소모에 영향을 미칠 수 있다
        if(sWifiLock != null) {
            sWifiLock.release();
            sWifiLock = null;
        }

        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }

        Calendar nextNotifyTime = Calendar.getInstance();

        // 내일 같은 시간으로 알람시간 결정
        nextNotifyTime.add(Calendar.DATE, 1);

        //  Preference에 설정한 값 저장
        SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
        editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
        editor.apply();

        Date currentDateTime = nextNotifyTime.getTime();
        String date_text = new SimpleDateFormat("yyyy/MM/dd/EE a hh:mm ", Locale.getDefault()).format(currentDateTime);
        Toast.makeText(context.getApplicationContext(),"Next alarm is " + date_text + "!", Toast.LENGTH_SHORT).show();
    }
}
