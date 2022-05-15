package com.z.zz.zzz.antidetector;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Application;

import zizzy.zhao.bridgex.l.L;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (me.weishu.reflection.Reflection.unseal(this) != 0) {
                L.e("Oops!!! Failed to unseal on " + SDK_INT);
            } else {
                L.i("Success to unseal on " + SDK_INT);
            }
        } catch (Throwable t) {
            L.e("Oops!!! Failed to unseal on " + SDK_INT);
        }
    }
}
