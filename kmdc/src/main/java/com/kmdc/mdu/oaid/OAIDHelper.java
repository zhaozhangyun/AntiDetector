package com.kmdc.mdu.oaid;

import android.content.Context;
import android.os.Looper;

import java.util.Map;

import zizzy.zhao.bridgex.l.L;

public class OAIDHelper {

    public static void fetchOAID(Context context, OnFetchListener listener) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(() -> {
                try {
                    Map<String, String> params = Util.getOaidParameters(context);
                    L.d("sdkVersionCode: " + CoreOaid.SDK_VERSION_CODE);
                    L.d(params);
                    if (listener != null) {
                        listener.onResult(params, CoreOaid.SDK_VERSION_CODE);
                    }
                } catch (Throwable t) {
                    L.e("Failed to get oaid parameters: " + t);
                }
            }).start();
        } else {
            try {
                Map<String, String> params = Util.getOaidParameters(context);
                L.d("sdkVersionCode: " + CoreOaid.SDK_VERSION_CODE);
                L.d(params);
                if (listener != null) {
                    listener.onResult(params, CoreOaid.SDK_VERSION_CODE);
                }
            } catch (Throwable t) {
                L.e("Failed to get oaid parameters: " + t);
            }
        }
    }

    public interface OnFetchListener {
        void onResult(Map<String, String> params, int sdkVersionCode);
    }
}
