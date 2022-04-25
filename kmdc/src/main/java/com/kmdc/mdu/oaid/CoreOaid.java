package com.kmdc.mdu.oaid;

import static android.os.Build.VERSION.SDK_INT;

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
        if (me.weishu.reflection.Reflection.unseal(context) != 0) {
            L.e("Oops!!! Failed to unseal on " + SDK_INT);
        } else {
            L.i("Success to unseal on " + SDK_INT);
        }

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
            } catch (Throwable th) {
                isMsaSdkAvailable = false;
                L.w("Error during msa sdk initialization: " + th.getMessage());
                try {
                    isMsaSdkAvailable = Reflection.forName(
                            "com.bun.miitmdid.core.MdidSdkHelper") != null;
                } catch (Throwable th1) {
                    isMsaSdkAvailable = false;
                    L.w("Error during msa sdk initialization: " + th1.getMessage());
                }
            }
        }

        try {
            SDK_VERSION_CODE = MdidSdkHelper.SDK_VERSION_CODE;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        L.i("isMsaSdkAvailable: " + isMsaSdkAvailable + ", sdkVersionCode: " + SDK_VERSION_CODE);
    }

    public static void doNotReadOaid() {
        isOaidToBeRead = false;
    }
}
