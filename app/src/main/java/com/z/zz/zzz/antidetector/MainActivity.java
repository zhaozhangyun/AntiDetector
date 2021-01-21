package com.z.zz.zzz.antidetector;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.z.zz.zzz.AntiDetector;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String f8342a = "\u53bb\u9886\u94b1";
    private static HandlerThread sWorkerThread;
    private static Handler sWorker;

    static {
        sWorkerThread = new HandlerThread("background-worker", Process.THREAD_PRIORITY_BACKGROUND);
        sWorkerThread.start();
    }

    public TestServiceConnection mTestServiceConnection;
    private DaemonServiceConnection mDaemonServiceConnection;
    private boolean isTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AntiDetector.create(this)
                .setDebug(BuildConfig.DEBUG)
                .setSticky(true)
                .setMinEmuFlagsThresholds(3)
                .detect(new AntiDetector.OnDetectorListener() {
                    @Override
                    public void onResult(boolean result, Map<String, String> data) {
                        Log.i("Main", "AntiDetector result: " + result + ", data: " + data);
                    }
                });

        sWorker = new WorkThreadHanlder(sWorkerThread.getLooper());
        Utils.registerHandler(sWorker, 0x1000);
        Utils.registerHandler(sWorker, 0x1001);
        Utils.registerHandler(sWorker, 0x1002);

        if (isTest) {
            mDaemonServiceConnection = new DaemonServiceConnection();
            bindService(new Intent(this, DaemonService.class),
                    mDaemonServiceConnection, BIND_AUTO_CREATE);
            mTestServiceConnection = new TestServiceConnection(this);
            bindService(new Intent(this, TestService.class),
                    mTestServiceConnection, BIND_AUTO_CREATE);
        } else {
            getApplicationContext().bindService(new Intent(this, StepService.class),
                    new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                            StepService a;
                            int step = 1;
                            if ((iBinder instanceof C2263b) && (a = ((C2263b) iBinder).mo8058a()) != null) {
                                a.showNotification(f8342a, step);
                            }
                            getApplicationContext().unbindService(this);
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName componentName) {
                        }
                    }, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTest) {
            unbindService(mDaemonServiceConnection);
        }
        Utils.unregisterHandler(sWorker);
        if (isTest) {
            stopService(new Intent(this, TestService.class));
        }
    }

    private class WorkThreadHanlder extends Handler {
        public WorkThreadHanlder(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.v("TestService", "call handleMessage(): " + msg);
            switch (msg.what) {
                case 0x1000:
                    unbindService(mTestServiceConnection);
                    Utils.sendMessage(0x1001, 0, 0, null, 1000);
                    break;
                case 0x1001:
                    mTestServiceConnection = new TestServiceConnection(MainActivity.this);
                    bindService(new Intent(MainActivity.this, TestService.class),
                            mTestServiceConnection, BIND_AUTO_CREATE);
                    break;
                case 0x1002:
                    startService(new Intent(MainActivity.this, TestService.class));
                    break;
            }
        }
    }

    private class DaemonServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
