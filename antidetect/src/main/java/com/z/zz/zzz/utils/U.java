package com.z.zz.zzz.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;

public class U {
    private static final String TAG = "U";

    public static boolean fileExist(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists()) {
            L.v(TAG, "Oops!!! File exist: " + filePath);
            result = true;
        }
        return result;
    }

    public static String getSystemProperties(String key) {
        String value = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            value = (String) (get.invoke(c, key));
            L.v(TAG, "getSystemProperties(" + key + "): " +
                    (TextUtils.isEmpty(value) ? "n/a" : value));
        } catch (Exception e) {
            L.e(TAG, "getSystemProperties error: ", e);
        }
        return value;
    }

    public static String tempToStr(float temp, int tempSetting) {
        if (temp <= 0.0f) {
            return "";
        }
        if (tempSetting == 2) {
            return String.format("%.1f°F",
                    new Object[]{Float.valueOf(((9.0f * temp) + 160.0f) / 5.0f)});
        }
        return String.format("%.1f°C", new Object[]{Float.valueOf(temp)});
    }

    public static JSONObject putJsonSafed(JSONObject json, String name, Object value) {
        try {
            return json.put(name, value);
        } catch (JSONException e) {
        }

        return json;
    }

    public static String getBuildSerial(Context context) {
        String serial = Build.UNKNOWN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//9.0+
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                serial = Build.getSerial();
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
            serial = Build.SERIAL;
        } else {//8.0-
            serial = getSystemProperties("ro.serialno");
        }

        return TextUtils.isEmpty(serial) ? Build.UNKNOWN : serial;
    }
}
