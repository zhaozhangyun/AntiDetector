package com.kmdc.mdu.oaid;

import android.content.Context;

import com.bun.miitmdid.core.MdidSdkHelper;

import zizzy.zhao.bridgex.l.L;

public class CoreOaid {
    static boolean isOaidToBeRead = false;
    static boolean isMsaSdkAvailable = false;
    static int SDK_VERSION_CODE = -1;

    public static void readOaid() {
        isOaidToBeRead = true;
    }

    public static void readOaid(Context context) {
        readOaid();

        try {
            System.loadLibrary("msaoaidsec");
            String certificate = Util.readCertFromAssetFile(context);
            isMsaSdkAvailable = MdidSdkHelper.InitCert(context, certificate);
            if (isMsaSdkAvailable) {
                L.i("Success to init cert");
            }
        } catch (Throwable t) {
            isMsaSdkAvailable = false;
            L.w("Error during msa sdk initialization: " + t.getMessage());
            try {
                Class jLibrary = Reflection.forName("com.bun.miitmdid.core.JLibrary");
                Reflection.invokeMethod(
                        jLibrary,
                        "InitEntry",
                        null,
                        new Class[]{Context.class},
                        context);
                isMsaSdkAvailable = true;
                if (isMsaSdkAvailable) {
                    L.i("Success to call JLibrary.InitEntry");
                }
            } catch (Throwable t1) {
                isMsaSdkAvailable = false;
                L.w("Error during msa sdk initialization: " + t1);
                try {
                    isMsaSdkAvailable = Reflection.forName(
                            "com.bun.miitmdid.core.MdidSdkHelper") != null;
                } catch (Throwable t2) {
                    isMsaSdkAvailable = false;
                    L.w("Error during msa sdk initialization: " + t2);
                }
            }
        }

        try {
            SDK_VERSION_CODE = MdidSdkHelper.SDK_VERSION_CODE;
        } catch (Throwable t) {
            L.w("Failed to read sdk version code: " + t);
        }

        L.i("isMsaSdkAvailable: " + isMsaSdkAvailable + ", sdkVersionCode: " + SDK_VERSION_CODE);
    }

    public static void doNotReadOaid() {
        isOaidToBeRead = false;
    }
}
