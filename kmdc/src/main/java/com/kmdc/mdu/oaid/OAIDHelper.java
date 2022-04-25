package com.kmdc.mdu.oaid;

import android.content.Context;

import java.util.Map;

import zizzy.zhao.bridgex.l.L;

public class OAIDHelper {

    public static void fetchOAID(Context context, OnFetchListener listener) {
        new Thread(() -> {
            try {
                Map<String, String> params = Util.getOaidParameters(context);
                L.d("sdkVersionCode: " + CoreOaid.SDK_VERSION_CODE);
                L.d(params);
                if (listener != null) {
                    listener.onResult(params, CoreOaid.SDK_VERSION_CODE);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }).start();
    }

    public interface OnFetchListener {
        void onResult(Map<String, String> params, int sdkVersionCode);
    }
}
