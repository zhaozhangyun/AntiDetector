package com.kmdc.mdu.oaid;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import zizzy.zhao.bridgex.l.L;

public class OpenDeviceIdentifierConnector implements ServiceConnection, IBinder.DeathRecipient {
    private static volatile OpenDeviceIdentifierConnector instance = null;
    private static final Object lockObject = new Object();
    private BlockingQueue<IBinder> binders = null;
    private Context context;
    private boolean shouldUnbind = false;

    private OpenDeviceIdentifierConnector(Context context) {
        binders = new LinkedBlockingQueue<>(1);
        this.context = context;
    }

    // Lazy-initialized singleton
    public static OpenDeviceIdentifierConnector getInstance(Context context) {
        if (instance == null) {
            synchronized (OpenDeviceIdentifierConnector.class) {
                if (instance == null) {
                    instance = new OpenDeviceIdentifierConnector(context);
                }
            }
        }
        return instance;
    }

    public boolean isServiceConnected() {
        return !binders.isEmpty();
    }

    public OpenDeviceIdentifierService getOpenDeviceIdentifierService(long timeOut, TimeUnit timeUnit) {
        // poll in order to wait & retrieve the service
        IBinder service;
        try {
            service = binders.poll(timeOut, timeUnit);
        } catch (InterruptedException e) {
            L.e("Waiting for OpenDeviceIdentifier Service interrupted: " +
                    e.getMessage());
            return null;
        }
        if (service == null) {
            L.w("Timed out waiting for OpenDeviceIdentifier service connection");
            return null;
        }

        // set back for next poll
        set(service);

        return OpenDeviceIdentifierService.Stub.asInterface(service);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //lets use the latest instance
        set(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        reset();
    }

    @Override
    public void onBindingDied(ComponentName name) {
        unbindAndReset();
    }

    @Override
    public void onNullBinding(ComponentName name) {
        unbindAndReset();
    }

    @Override
    public void binderDied() {
        unbindAndReset();
    }

    public synchronized void unbindAndReset() {
        if (shouldUnbind) {
            try {
                shouldUnbind = false;
                reset();
                context.unbindService(this);
            } catch (Exception e) {
                L.e("Fail to unbind " + e.getMessage());
            }
        }
    }

    public void shouldUnbind() {
        shouldUnbind = true;
    }

    private void reset() {
        try {
            synchronized (lockObject) {
                binders.clear();
            }
        } catch (Exception e) {
            L.e("Fail to reset queue " + e.getMessage());
        }
    }

    private void set(IBinder service) {
        try {
            synchronized (lockObject) {
                binders.clear();
                binders.add(service);
            }
        } catch (Exception e) {
            L.e("Fail to add in queue " + e.getMessage());
        }
    }
}

