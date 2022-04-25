package com.kmdc.mdu.oaid;

import android.content.Context;

import com.bun.miitmdid.core.InfoCode;
import com.bun.miitmdid.core.MdidSdkHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import zizzy.zhao.bridgex.l.L;

@Deprecated
public class MsaSdkClient {
    public static String getOaid(Context context, long maxWaitTimeInMilli) {
        final BlockingQueue<String> oaidHolder = new LinkedBlockingQueue<>(1);

        try {
            boolean msaInternalLogging = false;
            int result = MdidSdkHelper.InitSdk(context, msaInternalLogging, idSupplier -> {
                try {
                    if (idSupplier == null || idSupplier.getOAID() == null) {
                        // so to avoid waiting for timeout
                        oaidHolder.offer("");
                    } else {
                        oaidHolder.offer(idSupplier.getOAID());
                    }
                } catch (Exception e) {
                    L.e("Fail to add: " + e.getMessage());
                }
            });

            if (!isError(result)) {
                return oaidHolder.poll(maxWaitTimeInMilli, TimeUnit.MILLISECONDS);
            }
        } catch (NoClassDefFoundError ex) {
            L.e("Couldn't find msa sdk " + ex.getMessage());
        } catch (InterruptedException e) {
            L.e("Waiting to read oaid from callback interrupted: " + e.getMessage());
        } catch (Throwable t) {
            L.e("Oaid reading process failed: " + t.getMessage());
        }

        return null;
    }

    private static boolean isError(int result) {
        switch (result) {
            case InfoCode.INIT_ERROR_CERT_ERROR:
                L.e("msa sdk error - INIT_ERROR_CERT_ERROR");
                return true;
            case InfoCode.INIT_ERROR_DEVICE_NOSUPPORT:
                L.e("msa sdk error - INIT_ERROR_DEVICE_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_LOAD_CONFIGFILE:
                L.e("msa sdk error - INIT_ERROR_LOAD_CONFIGFILE");
                return true;
            case InfoCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
                L.e("msa sdk error - INIT_ERROR_MANUFACTURER_NOSUPPORT");
                return true;
            case InfoCode.INIT_ERROR_SDK_CALL_ERROR:
                L.e("msa sdk error - INIT_ERROR_SDK_CALL_ERROR");
                return true;
            default:
                return false;
        }
    }
}
