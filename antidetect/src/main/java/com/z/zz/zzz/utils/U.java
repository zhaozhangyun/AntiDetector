package com.z.zz.zzz.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class U {
    private static final String TAG = "U";

    public static boolean fileExist(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists()) {
            result = true;
        }
        return result;
    }

    public static String getSystemProperties(String key) {
        String value = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) get.invoke(c, key, Build.UNKNOWN);
            L.v(TAG, "getSystemProperties(" + key + "): " + value);
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

    public static <O> O getJsonSafed(JSONObject json, String name) {
        try {
            if (json.has(name) && !json.isNull(name)) {
                return (O) json.get(name);
            }
        } catch (JSONException e) {
        }

        return null;
    }

    public static JSONArray putJsonSafed(JSONArray json, int index, Object value) {
        try {
            return json.put(index, value);
        } catch (JSONException e) {
        }

        return json;
    }

    public static <O> O getJsonSafed(JSONArray json, int index) {
        try {
            return (O) json.get(index);
        } catch (JSONException e) {
        }

        return null;
    }

    public static String formatJson(Object source) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(getSplitter(100));
        builder.append("\n");
        builder.append(formatJsonBody(source));
        builder.append("\n");
        builder.append(getSplitter(100));
        return builder.toString();
    }

    private static String getSplitter(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("-");
        }
        return builder.toString();
    }

    private static String formatJsonBody(Object source) {
        Object o = getJsonObjFromStr(source);
        if (o != null) {
            try {
                if (o instanceof JSONObject) {
                    return ((JSONObject) o).toString(2);
                } else if (o instanceof JSONArray) {
                    return ((JSONArray) o).toString(2);
                } else {
                    return source.toString();
                }
            } catch (JSONException e) {
                return source.toString();
            }
        } else {
            return source.toString();
        }
    }

    private static Object getJsonObjFromStr(Object test) {
        Object o = null;
        try {
            o = new JSONObject(test.toString());
        } catch (JSONException ex) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    o = new JSONArray(test);
                }
            } catch (JSONException ex1) {
                return null;
            }
        }
        return o;
    }

    @SuppressLint("MissingPermission")
    public static String getBuildSerial(Context context) {
        String serial = Build.UNKNOWN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//9.0+
            try {
                serial = Build.getSerial();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
            serial = Build.SERIAL;
        } else {//8.0-
            serial = getSystemProperties("ro.serialno");
        }

        return TextUtils.isEmpty(serial) ? Build.UNKNOWN : serial;
    }

    public static List<String> executeCommand(String[] shellCmd) {
        long start = System.currentTimeMillis();
        SystemCommandExecutor executor = new SystemCommandExecutor(shellCmd);
        try {
            int result = executor.executeCommand();
            L.v(TAG, "call executeCommand " + Arrays.asList(shellCmd) + ": " + result
                    + ", cost: " + (System.currentTimeMillis() - start) + "ms");
        } catch (Exception e) {
            L.e(TAG, "call SystemCommandExecutor error: ", e);
        }
        return executor.getStandardOutputStream();
    }

    /**
     * str 原字符串
     * strLength 字符串总长
     */
    public static String addZeroToNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }
}
