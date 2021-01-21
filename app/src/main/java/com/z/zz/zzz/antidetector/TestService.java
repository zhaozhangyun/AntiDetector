package com.z.zz.zzz.antidetector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class TestService extends Service {

    private NotificationManager notificationManager;
    private String notificationId = "channelId";
    private String notificationName = "channelName";
    private Binder myBinder;

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("测试服务")
                .setContentText("我正在运行");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TestService", "call onCreate(): " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TestService", "call onDestroy(): " + this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.d("TestService", "call onStartCommand(): intent=" + intent
                + ", flags=" + flags + ", startId=" + startId);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, getNotification());
        Utils.sendMessage(0x1000, 0, 0, null, 0);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TestService", "call onBind(): " + intent);
//        Utils.sendMessage(0x1002, 0, 0, null);
        if (myBinder == null) {
            myBinder = new MyBinder();
        }
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("TestService", "call onUnbind(): " + intent);
        return false;
    }

    private class MyBinder extends Binder {
    }
}
