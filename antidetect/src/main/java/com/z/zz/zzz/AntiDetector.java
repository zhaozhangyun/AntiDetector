package com.z.zz.zzz;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.z.zz.zzz.emu.EmulatorDetector;
import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;
import com.z.zz.zzz.xml.WhiteListEntry;
import com.z.zz.zzz.xml.WhiteListXmlParser;

import java.io.DataOutputStream;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * Created by zizzy on 08/21/18.
 */

public final class AntiDetector {
    public static final String TAG = "AntiDetector";
    private static final String MANUFACTURER_GOOGLE = "Google";
    private static final String BRAND_GOOGLE = "google";
    private static final int FLAG_ANTI_DETECT = 0x1;
    private static final int FLAG_IS_GOOGLE_DEVICE = FLAG_ANTI_DETECT;          // 0
    private static final int FLAG_ENABLE_ADB = FLAG_IS_GOOGLE_DEVICE << 1;      // 1
    private static final int FLAG_IS_DEBUGGABLE = FLAG_ENABLE_ADB << 1;         // 2
    private static final int FLAG_IS_DEBUGGED = FLAG_IS_DEBUGGABLE << 1;        // 3
    private static final int FLAG_IS_ROOTED = FLAG_IS_DEBUGGED << 1;            // 4
    private static final int FLAG_IS_EMULATOR = FLAG_IS_ROOTED << 1;            // 5
    private static final int FLAG_IS_VPN_CONNECTED = FLAG_IS_EMULATOR << 1;     // 6
    private static final int FLAG_IS_WIFI_PROXY = FLAG_IS_VPN_CONNECTED << 1;   // 7
    private static int FLAG_SAFE = 0x0;
    private static AntiDetector antiDetector;
    private Context context;
    private WhiteListXmlParser parser;
    private boolean isSticky;

    private AntiDetector(Context pContext) {
        this.context = pContext;
    }

    public static AntiDetector create(Context pContext) {
        if (pContext == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (antiDetector == null) {
            synchronized (AntiDetector.class) {
                if (antiDetector == null) {
                    antiDetector = new AntiDetector(pContext.getApplicationContext());
                }
            }
        }
        return antiDetector;
    }

    public static boolean getAntiResult() {
        if (antiDetector == null) {
            throw new NullPointerException("The instance of AntiDetector is null");
        }
        return antiDetector.checkAntiDetect();
    }

    private String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            serial = new AndroidID(context).getAndroidID();
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//9.0+
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        serial = Build.getSerial();
                    }
                } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
                    serial = Build.SERIAL;
                } else {//8.0-
                    serial = U.getSystemProperties("ro.serialno");
                }

                L.v(TAG, "Build.SERIAL: " + serial);
                //API>=9 使用serial号
                return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
            } catch (Exception e) {
                serial = new AndroidID(context).getAndroidID();
            }
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    private boolean isRooted() {
        boolean result = false;

        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        L.d(TAG, ">>> isRooted: " + result);
        if (result) {
            FLAG_SAFE |= FLAG_IS_ROOTED;
        }

        return result;
    }

    public AntiDetector setDebug(boolean isDebug) {
        if (isDebug) {
            parser = new WhiteListXmlParser();
            parser.parse(context);
        }
        return this;
    }

    public AntiDetector setSticky(boolean sticky) {
        this.isSticky = sticky;
        return this;
    }

    public void detect(final OnDetectorListener listener) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return checkAntiDetect();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (listener != null) {
                    listener.onResult(result, FLAG_SAFE);
                }
            }
        }.execute();
    }

    /**
     * Check device status to anti detection.
     *
     * @return true in dangerous; false safe
     */
    private boolean checkAntiDetect() {
        synchronized (AntiDetector.class) {
            FLAG_SAFE = 0x0;

            try {
                if (parser != null) {
                    String androidId = new AndroidID(context).getAndroidID();
                    L.v(TAG, "androidId: " + androidId);
                    String serial = getUniquePsuedoID();
                    L.v(TAG, "serial: " + serial);

                    if (!TextUtils.isEmpty(serial)) {
                        Iterator<WhiteListEntry> it = parser.getPluginEntries().iterator();
                        while (it.hasNext()) {
                            String id = it.next().androidId;
                            if (serial.equals(id)) {
                                L.i(TAG, "The device [" + id + "] is in the white list.");
                                return false;
                            }
                        }
                    }

                    if (!TextUtils.isEmpty(androidId)) {
                        Iterator<WhiteListEntry> it = parser.getPluginEntries().iterator();
                        while (it.hasNext()) {
                            String id = it.next().androidId;
                            if (androidId.equals(id)) {
                                L.i(TAG, "The device [" + id + "] is in the white list.");
                                return false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignored
            }

            if (isSticky) {
                boolean inDevelopmentMode = inDevelopmentMode();
                boolean isRooted = isRooted();
                boolean isEmulator = isEmulator();
                boolean isVPNConnected = isVPNConnected();
                boolean isWifiProxy = isWifiProxy();
                boolean isGoogleDevice = isGoogleDevice();

                return inDevelopmentMode
                        || isRooted
                        || isVPNConnected
                        || isWifiProxy
                        || isEmulator
                        || isGoogleDevice;
            } else {
                return inDevelopmentMode()
                        || isRooted()
                        || isVPNConnected()
                        || isWifiProxy()
                        || isEmulator()
                        || isGoogleDevice();
            }
        }
    }

    private boolean isDebugged() {
        L.d(TAG, ">>> Debugger hasTracerPid: " + Debugger.hasTracerPid());
        L.d(TAG, ">>> Debugger isBeingDebugged: " + Debugger.isBeingDebugged());
        L.d(TAG, ">>> Debugger hasAdbInEmulator: " + Debugger.hasAdbInEmulator());
        boolean result = Debugger.hasTracerPid() || Debugger.isBeingDebugged() || Debugger.hasAdbInEmulator();
        if (result) {
            FLAG_SAFE |= FLAG_IS_DEBUGGED;
        }
        return result;
    }

    private boolean inDevelopmentMode() {
        return enableAdb() || isDebuggable() || isDebugged();
    }

    private boolean enableAdb() {
        boolean result = (Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0) > 0);
        L.d(TAG, ">>> enableAdb: " + result);
        if (result) {
            FLAG_SAFE |= FLAG_ENABLE_ADB;
        }
        return result;
    }

    private boolean isDebuggable() {
        boolean result = 0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE);
        L.d(TAG, ">>> isDebuggable: " + result);
        if (result) {
            FLAG_SAFE |= FLAG_IS_DEBUGGABLE;
        }
        return result;
    }

    private boolean isGoogleDevice() {
        boolean result = false;
        try {
            result = MANUFACTURER_GOOGLE.toLowerCase().contains(Build.MANUFACTURER.toLowerCase()) ||
                    BRAND_GOOGLE.toLowerCase().contains(Build.BRAND.toLowerCase());
            L.d(TAG, ">>> isGoogleDevice: " + result);
            if (result) {
                FLAG_SAFE |= FLAG_IS_GOOGLE_DEVICE;
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    private boolean isEmulator() {
        long start = System.currentTimeMillis();
        boolean isEmulator = EmulatorDetector.with(context)
                .addPackageName("com.bluestacks")
                .detect();
        L.d(TAG, ">>> isEmulator cost " + (System.currentTimeMillis() - start) + "ms "
                + ": " + isEmulator + " >>> " + getCheckInfo());
        if (isEmulator) {
            FLAG_SAFE |= FLAG_IS_EMULATOR;
        }
        return isEmulator;
    }

    private String getCheckInfo() {
        return EmulatorDetector.dump();
    }

    /**
     * 是否使用代理(WiFi状态下的,避免被抓包)
     */
    private boolean isWifiProxy() {
        final boolean is_ics_or_later = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress = "";
        int proxyPort = -1;

        try {
            if (is_ics_or_later) {
                proxyAddress = System.getProperty("http.proxyHost");
                String portstr = System.getProperty("http.proxyPort");
                proxyPort = Integer.parseInt((portstr != null ? portstr : "-1"));
            } else {
                proxyAddress = android.net.Proxy.getHost(context);
                proxyPort = android.net.Proxy.getPort(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean result = (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
        L.d(TAG, ">>> isWifiProxy: " + result);
        if (result) {
            FLAG_SAFE |= FLAG_IS_WIFI_PROXY;
        }
        return result;
    }

    /**
     * 是否正在使用VPN
     */
    private boolean isVPNConnected() {
        boolean result = false;

        List<String> networkList = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp()) {
                    networkList.add(ni.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        result = networkList.contains("tun0") || networkList.contains("ppp0");

//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        Network[] networks = cm.getAllNetworks();
//
//        for (int i = 0; i < networks.length; i++) {
//            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);
//            L.i(TAG, "Network " + i + ": " + networks[i].toString());
//            L.i(TAG, "VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
//            L.i(TAG, "NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));
//        }

        L.d(TAG, ">>> isVPNConnected: " + result);
        if (result) {
            FLAG_SAFE |= FLAG_IS_VPN_CONNECTED;
        }
        return result;
    }

    public interface OnDetectorListener {
        void onResult(boolean result, int flag);
    }
}
