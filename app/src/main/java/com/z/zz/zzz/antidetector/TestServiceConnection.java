package com.z.zz.zzz.antidetector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class TestServiceConnection implements ServiceConnection {

    private Context context;
    private boolean isForeground = true;

    public TestServiceConnection(Context context) {
        this.context = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("TestService", "call onServiceConnected(): name=" + name);
        if (isForeground) {
            ContextCompat.startForegroundService(context, new Intent(context, TestService.class));
        } else {
            context.startService(new Intent(context, TestService.class));
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("TestService", "call onServiceDisconnected(): name=" + name);
    }
}
