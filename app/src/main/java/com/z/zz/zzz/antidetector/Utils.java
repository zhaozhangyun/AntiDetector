package com.z.zz.zzz.antidetector;

import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    private static SparseArray<Integer> sparseArray = new SparseArray<>();
    private static AtomicInteger atomicInteger = new AtomicInteger();
    private static Map<Handler, SparseArray<Integer>> handlers = new LinkedHashMap<>();

    public static void registerHandler(Handler target, int what) {
        sparseArray.put(atomicInteger.incrementAndGet(), what);
        handlers.put(target, sparseArray);
    }

    public static void unregisterHandler(Handler target) {
        handlers.remove(target);
    }

    public static void sendMessageAll(int arg1, int arg2, Object obj) {
        try {
            Iterator<Handler> it = handlers.keySet().iterator();
            while (it.hasNext()) {
                Handler target = it.next();
                SparseArray<Integer> sa = handlers.get(target);
                for (int i = 0; i < sa.size(); i++) {
                    Message message = Message.obtain(target, sa.get(i + 1), arg1, arg2, obj);
                    message.sendToTarget();
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void sendMessage(int what, int arg1, int arg2, Object obj) {
        try {
            Iterator<Handler> it = handlers.keySet().iterator();
            while (it.hasNext()) {
                Handler target = it.next();
                SparseArray<Integer> sa = handlers.get(target);
                for (int i = 0; i < sa.size(); i++) {
                    if (sa.get(i + 1) != what) {
                        continue;
                    }
                    Message message = Message.obtain(target, what, arg1, arg2, obj);
                    message.sendToTarget();
                    return;
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void sendMessage(int what, int arg1, int arg2, Object obj, long delayMillis) {
        try {
            Iterator<Handler> it = handlers.keySet().iterator();
            while (it.hasNext()) {
                Handler target = it.next();
                SparseArray<Integer> sa = handlers.get(target);
                for (int i = 0; i < sa.size(); i++) {
                    if (sa.get(i + 1) != what) {
                        continue;
                    }
                    Message message = Message.obtain(target, what, arg1, arg2, obj);
                    target.sendMessageDelayed(message, delayMillis);
                    return;
                }
            }
        } catch (Throwable th) {
        }
    }
}
