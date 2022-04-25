package com.kmdc.mdu.oaid;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.kmdc.mdu.oaid.OpenDeviceIdentifierClient.Info;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import zizzy.zhao.bridgex.l.L;

class Util {
    synchronized static Map<String, String> getOaidParameters(Context context) {
        if (!CoreOaid.isOaidToBeRead) {
            return null;
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Oops!!! You can't call getOaidParameters on Main thread.");
        }

        Map<String, String> oaidParameters;

        // IMPORTANT:
        // if manufacturer is huawei then try reading the oaid with hms (huawei mobile service)
        // approach first, as it can read both oaid and limit tracking status
        // otherwise use the msa sdk which only gives the oaid currently

        if (isManufacturerHuawei()) {
            oaidParameters = getOaidParametersUsingHMS(context);
            if (oaidParameters != null) {
                return oaidParameters;
            }

            return getOaidParametersUsingMSA(context);
        } else {
            oaidParameters = getOaidParametersUsingMSA(context);
            if (oaidParameters != null) {
                return oaidParameters;
            }

            return getOaidParametersUsingHMS(context);
        }
    }

    private static boolean isManufacturerHuawei() {
        try {
            String manufacturer = android.os.Build.MANUFACTURER;
            if (manufacturer != null && manufacturer.equalsIgnoreCase("huawei")) {
                return true;
            }
        } catch (Exception e) {
            L.e("Manufacturer not available");
        }
        return false;
    }

    private static Map<String, String> getOaidParametersUsingHMS(Context context) {
        for (int attempt = 1; attempt <= 3; attempt += 1) {
            Info oaidInfo = OpenDeviceIdentifierClient.getOaidInfo(context,
                    3000 * attempt);
            if (oaidInfo != null) {
                Map<String, String> parameters = new HashMap<>();
                addString(parameters, "oaid", oaidInfo.getOaid());
                addBoolean(parameters, "oaid_tracking_enabled", !oaidInfo.isOaidTrackLimited());
                addString(parameters, "oaid_src", "hms");
                addLong(parameters, "oaid_attempt", attempt);
                return parameters;
            }
        }
        L.e("Fail to read the OAID using HMS");
        return null;
    }

    private static Map<String, String> getOaidParametersUsingMSA(Context context) {
        if (!CoreOaid.isMsaSdkAvailable) {
            return null;
        }

        for (int attempt = 1; attempt <= 3; attempt += 1) {
            String oaid = MsaSdkClient2.getOaid(context, 3000 * attempt);
            if (oaid != null && !oaid.isEmpty()) {
                Map<String, String> parameters = new HashMap<>();
                addString(parameters, "oaid", oaid);
                addString(parameters, "oaid_src", "msa");
                addLong(parameters, "oaid_attempt", attempt);
                return parameters;
            }
        }

        L.e("Fail to read the OAID using MSA");
        return null;
    }

    static void addLong(Map<String, String> parameters, String key, long value) {
        if (value < 0) {
            return;
        }
        String valueString = Long.toString(value);
        addString(parameters, key, valueString);
    }

    static void addBoolean(Map<String, String> parameters, String key, Boolean value) {
        if (value == null) {
            return;
        }
        int intValue = value ? 1 : 0;
        addLong(parameters, key, intValue);
    }

    static void addString(Map<String, String> parameters, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        parameters.put(key, value);
    }

    static String readCertFromAssetFile(Context context) {
        try {
            String assetFileName = context.getPackageName() + ".cert.pem";
            InputStream is = context.getAssets().open(assetFileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
            return builder.toString();
        } catch (Exception e) {
            L.e("readCertFromAssetFile failed");
            return "";
        }
    }
}
