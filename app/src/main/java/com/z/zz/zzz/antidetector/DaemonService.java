package com.z.zz.zzz.antidetector;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DaemonService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DaemonService", "call onCreate(): " + this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DaemonService", "call onDestroy(): " + this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.d("DaemonService", "call onStartCommand(): intent=" + intent
                + ", flags=" + flags + ", startId=" + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("DaemonService", "call onBind(): " + intent);
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("DaemonService", "call onUnbind(): " + intent);
        return super.onUnbind(intent);
    }
}
