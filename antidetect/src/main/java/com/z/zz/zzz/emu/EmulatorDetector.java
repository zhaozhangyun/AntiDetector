package com.z.zz.zzz.emu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.z.zz.zzz.AntiDetector;
import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.z.zz.zzz.AntiDetector.TAG;

import androidx.core.content.ContextCompat;

public final class EmulatorDetector {

    private static final String EMU_PATTERN_FILE_NAME = "emu_pattern.json";
    public static int MIN_EMU_FLAGS_THRESHOLD = 3;
    private static Property[] PROPERTIES = {};
    private static EmuFeature[] EMU_FEATURES = {};
    private static String[] EMU_FILES = {};
    private static String[] X86_FILES = {};
    private static String[] PIPES = {};
    private static String[] PHONE_NUMBERS = {};
    private static String[] DEVICE_IDS = {};
    private static String[] IMSI_IDS = {};
    private static String[] BLUETOOTH_PATH = {};
    private static String[] QEMU_DRIVERS = {};
    private static String[] IPs = {};
    private static int MIN_PROPERTIES_THRESHOLD = 5;
    private static int MIN_BUILD_THRESHOLD = 4;
    private static EmulatorDetector sEmulatorDetector;
    private static Context sContext;
    private static JSONObject jBuild = new JSONObject();
    private static JSONObject jEmu = new JSONObject();
    private boolean isDebug;
    private List<String> mListPackageName;

    private EmulatorDetector(Context context) {
        sContext = context;
        parseEmuPattern(context);
    }

    public static EmulatorDetector with(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (sEmulatorDetector == null) {
            sEmulatorDetector = new EmulatorDetector(context.getApplicationContext());
        }
        return sEmulatorDetector;
    }

    public static String dump() {
        JSONObject jo = new JSONObject();
        U.putJsonSafed(jo, "build_info", getBuildInfo());
        U.putJsonSafed(jo, "build_dump", jBuild);
        U.putJsonSafed(jo, "emu_dump", jEmu);
        return U.formatJson(jo);
    }

    private static JSONObject getBuildInfo() {
        JSONObject jo = new JSONObject();
        try {
            U.putJsonSafed(jo, "PR", Build.PRODUCT);
            U.putJsonSafed(jo, "MA", Build.MANUFACTURER);
            U.putJsonSafed(jo, "BR", Build.BRAND);
            U.putJsonSafed(jo, "BO", Build.BOARD);
            U.putJsonSafed(jo, "DE", Build.DEVICE);
            U.putJsonSafed(jo, "MO", Build.MODEL);
            U.putJsonSafed(jo, "HW", Build.HARDWARE);
            U.putJsonSafed(jo, "BL", Build.BOOTLOADER);
            U.putJsonSafed(jo, "FP", Build.FINGERPRINT);
            U.putJsonSafed(jo, "SE", U.getBuildSerial(sContext));
        } catch (Exception e) {
        }

        return jo;
    }

    private void parseEmuPattern(Context context) {
        JSONObject jData = null;
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(EMU_PATTERN_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String content = new String(buffer);
            jData = new JSONObject(content);
        } catch (Exception e) {
            if (isDebug) {
                L.e(TAG, "parseEmuPattern error: ", e);
            } else {
                L.w(TAG, "parseEmuPattern error: " + e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        MIN_EMU_FLAGS_THRESHOLD = U.getJsonSafed(jData, "min_emu_flags_threshold");
        MIN_BUILD_THRESHOLD = U.getJsonSafed(jData, "min_build_threshold");
        IPs = convertJsonToArray(jData, "ips");
        QEMU_DRIVERS = convertJsonToArray(jData, "qemu_drivers");
        PIPES = convertJsonToArray(jData, "pipes");
        X86_FILES = convertJsonToArray(jData, "x86_files");
        EMU_FILES = convertJsonToArray(jData, "emu_files");

        JSONArray jProperties = U.getJsonSafed(jData, "properties");
        PROPERTIES = new Property[jProperties.length()];
        for (int i = 0; i < jProperties.length(); i++) {
            JSONObject jo = U.getJsonSafed(jProperties, i);
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = U.getJsonSafed(jo, key);
                Property p = new Property(key, value);
                PROPERTIES[i] = p;
            }
        }

        MIN_PROPERTIES_THRESHOLD = U.getJsonSafed(jData, "min_properties_threshold");

        String[] packages = convertJsonToArray(jData, "packages");
        mListPackageName = Arrays.asList(packages);

        PHONE_NUMBERS = convertJsonToArray(jData, "phone_numbers");
        DEVICE_IDS = convertJsonToArray(jData, "device_id");
        IMSI_IDS = convertJsonToArray(jData, "imsi");
        BLUETOOTH_PATH = convertJsonToArray(jData, "bluetooth_path");

        JSONArray jEmuFeatures = U.getJsonSafed(jData, "emu_features");
        EMU_FEATURES = new EmuFeature[jEmuFeatures.length()];
        for (int i = 0; i < jEmuFeatures.length(); i++) {
            JSONObject jo = U.getJsonSafed(jEmuFeatures, i);
            String name = U.getJsonSafed(jo, "name");
            String[] filePath = convertJsonToArray(jo, "file_path");
            String[] systemProperties = convertJsonToArray(jo, "sys_prop");
            Map<String, String> buildProperties = convertJsonToMap(jo, "build_prop");
            EmuFeature ef = new EmuFeature(name, filePath, systemProperties, buildProperties);
            EMU_FEATURES[i] = ef;
        }
        log("@@@@@@@@@@@ Parse " + EMU_PATTERN_FILE_NAME + " finished.");
    }

    private String[] convertJsonToArray(JSONObject data, String name) {
        JSONArray ja = U.getJsonSafed(data, name);
        if (ja == null) {
            return new String[0];
        }
        String[] content = new String[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            content[i] = U.getJsonSafed(ja, i);
        }
        return content;
    }

    private Map<String, String> convertJsonToMap(JSONObject data, String name) {
        Map<String, String> result = new HashMap<>();
        JSONArray ja = U.getJsonSafed(data, name);
        if (ja == null) {
            return result;
        }
        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = U.getJsonSafed(ja, i);
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                String key = it.next();
                String value = U.getJsonSafed(jo, key);
                result.put(key, value);
            }
        }
        return result;
    }

    private void log(String str) {
        L.v(TAG, "Emu ---> " + str);
    }

    private void logW(String str) {
        L.w(TAG, "Emu ---> " + str);
    }

    // 是否能跳转拨号盘
    private boolean checkResolveDialAction(Context context) {
        String url = "tel:" + "12345678910";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            log("checkResolveDialAction failed --- Failed to resolve dial action");
            U.putJsonSafed(jEmu, "da", 1);
            return true;
        }
        return false;
    }

    // 是否有蓝牙硬件
    private boolean checkBluetoothHardware() {
        // 兼容64位ARM处理器
        for (String path : BLUETOOTH_PATH) {
            if (U.fileExist(path)) {
                return false;
            }
        }
        log("checkBluetoothHardware failed --- Not found libbluetooth_jni.so");
        U.putJsonSafed(jEmu, "bt", 1);
        return true;
    }

    // 是否有GPS硬件
    private boolean checkGPSHardware(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            log("checkGPSHardware failed --- No LocationManager service");
            U.putJsonSafed(jEmu, "gp", 1);
            return true;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            log("checkGPSHardware failed --- No LocationManager providers");
            U.putJsonSafed(jEmu, "gp", 1);
            return true;
        }
        boolean containGPS = providers.contains(LocationManager.GPS_PROVIDER);
        if (!containGPS) {
            log("checkGPSHardware failed --- No GPS provider");
            U.putJsonSafed(jEmu, "gp", 1);
            return true;
        }
        return false;
    }

    // 是否支持多点触控
    private boolean checkMultiTouch(Context context) {
        boolean hasFeature = context.getPackageManager().hasSystemFeature(
                "android.hardware.touchscreen.multitouch");
        if (!hasFeature) {
            log("checkMultiTouch failed --- No multitouch feature");
            U.putJsonSafed(jEmu, "mt", 1);
            return true;
        }
        return false;
    }

    private boolean checkEmuFeature() {
        for (EmuFeature ef : EMU_FEATURES) {
            String name = ef.name;
            String[] filePath = ef.filePath;
            String[] systemProperties = ef.systemProperties;
            Map<String, String> buildProperties = ef.buildProperties;

            for (String path : filePath) {
                if (U.fileExist(path)) {
                    logW("Check (" + name + ") file {" + path + "} is detected");
                    U.putJsonSafed(jEmu, "fe", 1);
                    return true;
                }
            }

            for (String sysProp : systemProperties) {
                if (!Build.UNKNOWN.equals(U.getSystemProperties(sysProp))) {
                    logW("Check (" + name + ") system properties {" + sysProp + "} is detected");
                    U.putJsonSafed(jEmu, "fe", 1);
                    return true;
                }
            }

            Set<String> set = buildProperties.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = buildProperties.get(key);
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    if (U.getSystemProperties(key).toLowerCase().contains(value.toLowerCase())) {
                        logW("Check (" + name + ") build properties {" + key + "} is detected");
                        U.putJsonSafed(jEmu, "fe", 1);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // CPU信息
    private boolean checkCpuInfo() {
        String cpu = getCPUInfo();
        if (!TextUtils.isEmpty(cpu)) {
            if (cpu.toLowerCase().contains("intel") || cpu.toLowerCase().contains("amd")) {
                logW("Check CpuInfo {" + cpu + "} is detected");
                U.putJsonSafed(jEmu, "ci", 1);
                return true;
            }
        }
        return false;
    }

    // 设备版本
    private boolean checkDeviceInfo() {
        String device = getDeviceInfo();
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("qemu")
                    || device.toLowerCase().contains("tencent")
                    || device.toLowerCase().contains("ttvm")
                    || device.toLowerCase().contains("tiantian")) {
                logW("Check DeviceInfo {" + device + "} is detected");
                U.putJsonSafed(jEmu, "di", 1);
                return true;
            }
        }
        return false;
    }

    private String getCPUInfo() {
        String name = "";
        InputStreamReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader("/proc/cpuinfo");
            br = new BufferedReader(fr);
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }

                String[] info = line.split(":\\s+", 2);
                if (info.length >= 2) {
                    String k = info[0].trim();
                    String v = info[1].trim();
                    if ("Hardware".equalsIgnoreCase(k)) {
                        name = v;
                    } else if ("model name".equalsIgnoreCase(k)) {
                        name = v;
                    }
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                L.e(TAG, "getCPUInfo error: ", e);
            } else {
                L.w(TAG, "getCPUInfo error: " + e);
            }
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
        return name;
    }

    private String getDeviceInfo() {
        String result = "";
        ProcessBuilder cmd;
        Process process = null;
        InputStream in = null;

        try {
            String[] args = {"/system/bin/cat", "/proc/version"};
            cmd = new ProcessBuilder(args);
            process = cmd.start();
            in = process.getInputStream();
            byte[] re = new byte[256];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
        } catch (Exception e) {
            if (isDebug) {
                L.e(TAG, "getDeviceInfo error: ", e);
            } else {
                L.w(TAG, "getDeviceInfo error: " + e);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (process != null) {
                process.destroy();
            }
        }
        return result.trim();
    }

    // Build属性
    private boolean checkBuildProperty() {
        jBuild = new JSONObject();
        int flags = 0;

        // FINGERPRINT
        String fingerprint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerprint)) {
            if (fingerprint.toLowerCase().contains("generic")
                    || fingerprint.toLowerCase().contains("test-keys")) {
                U.putJsonSafed(jBuild, "fp", 1);
                flags++;
            }
        }

        // MODEL
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            if (model.toLowerCase().contains("google_sdk")
                    || model.toLowerCase().contains("emulator")
                    || model.contains("Android SDK built for x86")) {
                U.putJsonSafed(jBuild, "mo", 1);
                flags++;
            }
        }

        // BRAND
        String brand = Build.BRAND;
        if (!TextUtils.isEmpty(brand)) {
            if (brand.toLowerCase().contains("generic") || brand.toLowerCase().contains("android")) {
                U.putJsonSafed(jBuild, "br", 1);
                flags++;
            }
        }

        // DEVICE
        String device = Build.DEVICE;
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("generic")) {
                U.putJsonSafed(jBuild, "de", 1);
                flags++;
            }
        }

        // HARDWARE
        String hardware = Build.HARDWARE;
        if (!TextUtils.isEmpty(hardware)) {
            if (hardware.equalsIgnoreCase("goldfish")) {
                U.putJsonSafed(jBuild, "hw", 1);
                flags++;
            }
        }

        // PRODUCT
        String product = Build.PRODUCT;
        if (!TextUtils.isEmpty(product)) {
            if (product.equalsIgnoreCase("google_sdk")
                    || product.equalsIgnoreCase("sdk_x86")) {
                U.putJsonSafed(jBuild, "pr", 1);
                flags++;
            }
        }

        // BOARD
        String board = Build.BOARD;
        if (!TextUtils.isEmpty(board)) {
            if (board.equalsIgnoreCase(Build.UNKNOWN)) {
                U.putJsonSafed(jBuild, "bo", 1);
                flags++;
            }
        }

        // SERIAL
        String serial = U.getBuildSerial(sContext);
        L.i(TAG, ">>> Build.SERIAL: " + serial + ", SDK_INT: " + Build.VERSION.SDK_INT);
        if (!TextUtils.isEmpty(serial)) {
            if (serial.toLowerCase().contains("android") || serial.toLowerCase().contains("emulator")) {
                U.putJsonSafed(jBuild, "se", 1);
                flags++;
            }
        }

        log("checkBuildProperty(): " + flags + " (thresholds: " + MIN_BUILD_THRESHOLD + ")");
        if (flags > 0) {
            U.putJsonSafed(jBuild, "fl", flags);
        }

        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("emu_build", jBuild.toString());
        }

        if (flags >= MIN_BUILD_THRESHOLD) {
            U.putJsonSafed(jEmu, "bd", 1);
            return true;
        }
        return false;
    }

    public boolean detect() {
        jEmu = new JSONObject();

        boolean result = doCheckEmu(sContext);

        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("emu_snapshot", jEmu.toString());
        }

        return result;
    }

    public EmulatorDetector setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    private boolean doCheckEmu(Context context) {
        boolean result = false;

        if (isDebug) {
            JSONObject jo = executeGetProp();
            L.v(TAG, "call executeGetProp(): " + U.formatJson((jo)));
        }

        if (checkEmuFeature()) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }

        if (checkCpuInfo()) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }
        if (checkDeviceInfo()) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }
        if (checkAdvanced()) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }

        int flags = 0;

        if (checkBuildProperty()) {
            flags++;
        }
        if (checkResolveDialAction(context)) {
            flags++;
        }
        if (checkBluetoothHardware()) {
            flags++;
        }
        if (checkGPSHardware(context)) {
            flags++;
        }
        if (checkMultiTouch(context)) {
            flags++;
        }

        log("CheckEmu flags: " + flags + " (thresholds: " + MIN_EMU_FLAGS_THRESHOLD + ")");
        if (flags > 0) {
            U.putJsonSafed(jEmu, "fl", flags);
        }

        if (flags >= MIN_EMU_FLAGS_THRESHOLD) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }

        if (isDebug) {
            return result;
        } else {
            return false;
        }
    }

    private boolean checkAdvanced() {
        if (isDebug) {
            boolean isTelePhony = checkTelephony();
            boolean isIp = checkIp();
            boolean isPackageName = checkPackageName();
            boolean isEmuFile = checkFiles(EMU_FILES);
            boolean isPipe = checkFiles(PIPES);
            boolean isX86File = checkQEmuProps() && checkFiles(X86_FILES);
            boolean isQEmuDrivers = checkQEmuDrivers();
            return isTelePhony || isIp || isPackageName || isEmuFile || isPipe || isX86File || isQEmuDrivers;
        } else {
            return checkTelephony()
                    || checkIp()
                    || checkPackageName()
                    || checkFiles(EMU_FILES)
                    || checkFiles(PIPES)
                    || (checkQEmuProps() && checkFiles(X86_FILES))
                    || checkQEmuDrivers();
        }
    }

    private boolean checkPackageName() {
        if (mListPackageName.isEmpty()) {
            return false;
        }
        PackageManager packageManager = sContext.getPackageManager();
        for (String pkgName : mListPackageName) {
            Intent tryIntent = packageManager.getLaunchIntentForPackage(pkgName);
            if (tryIntent != null) {
                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(tryIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                if (!resolveInfos.isEmpty()) {
                    U.putJsonSafed(jEmu, "pg", 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkTelephony() {
        if (isDebug) {
            boolean isPhoneNumber = checkPhoneNumber();
            boolean isDeviceId = checkDeviceId();
            boolean isImsi = checkImsi();
            boolean isOperatorNameAndroid = checkOperatorNameAndroid();
            return isPhoneNumber || isDeviceId || isImsi || isOperatorNameAndroid;
        } else {
            return checkPhoneNumber()
                    || checkDeviceId()
                    || checkImsi()
                    || checkOperatorNameAndroid();
        }
    }

    @SuppressLint("MissingPermission")
    private boolean checkPhoneNumber() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(
                    Context.TELEPHONY_SERVICE);
            String phoneNumber = telephonyManager.getLine1Number();
            for (String known_number : PHONE_NUMBERS) {
                if (known_number.equalsIgnoreCase(phoneNumber)) {
                    logW("Check PhoneNumber {" + known_number + "} is detected");
                    U.putJsonSafed(jEmu, "pn", 1);
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private boolean checkDeviceId() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(
                    Context.TELEPHONY_SERVICE);
            String deviceId = telephonyManager.getDeviceId();
            for (String known_deviceId : DEVICE_IDS) {
                if (known_deviceId.equalsIgnoreCase(deviceId)) {
                    logW("Check DeviceId {" + known_deviceId + "} is detected");
                    U.putJsonSafed(jEmu, "de", 1);
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private boolean checkImsi() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) sContext.getSystemService(
                    Context.TELEPHONY_SERVICE);
            String imsi = telephonyManager.getSubscriberId();
            for (String known_imsi : IMSI_IDS) {
                if (known_imsi.equalsIgnoreCase(imsi)) {
                    logW("Check IMSI {" + known_imsi + "} is detected");
                    U.putJsonSafed(jEmu, "im", 1);
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    private boolean checkOperatorNameAndroid() {
        String operatorName = ((TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName();
        if (operatorName.equalsIgnoreCase("android")) {
            logW("Check Operator {" + operatorName + "} is detected");
            U.putJsonSafed(jEmu, "no", 1);
            return true;
        }
        return false;
    }

    private boolean checkQEmuDrivers() {
        for (File drivers_file : new File[]{new File("/proc/tty/drivers"),
                new File("/proc/cpuinfo")}) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                byte[] data = new byte[1024];
                InputStream is = null;
                try {
                    is = new FileInputStream(drivers_file);
                    is.read(data);

                    String driver_data = new String(data);
                    for (String known_qemu_driver : QEMU_DRIVERS) {
                        if (driver_data.contains(known_qemu_driver)) {
                            logW(">>> Check QEmu Drivers {" + known_qemu_driver + "} is detected");
                            U.putJsonSafed(jEmu, "qd", known_qemu_driver);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    if (isDebug) {
                        L.e(TAG, "checkQEmuDrivers error: ", e);
                    } else {
                        L.w(TAG, "checkQEmuDrivers error: " + e);
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkFiles(String[] targets) {
        for (String file : targets) {
            if (U.fileExist(file)) {
                logW(">>> Check file {" + file + "} is detected");
                U.putJsonSafed(jEmu, "fd", 1);
                return true;
            }
        }
        return false;
    }

    private boolean checkQEmuProps() {
        int found_props = 0;

        for (Property property : PROPERTIES) {
            String property_value = U.getSystemProperties(property.name);
            if (TextUtils.isEmpty(property.seek_value) && !Build.UNKNOWN.equals(property_value)) {
                logW(">>> Check QEmu Properties {" + property + "} is detected");
                found_props++;
            }
            if (!TextUtils.isEmpty(property.seek_value) && property_value.contains(property.seek_value)) {
                logW(">>> Check QEmu Properties {" + property + "} is detected");
                found_props++;
            }
        }
        log("checkQEmuProps(): " + found_props + " (thresholds: " + MIN_PROPERTIES_THRESHOLD + ")");

        if (found_props >= MIN_PROPERTIES_THRESHOLD) {
            U.putJsonSafed(jEmu, "qp", found_props);
            return true;
        }
        return false;
    }

    private boolean checkIp() {
        String[] args = {"/system/bin/netcfg"};
        InputStream in = null;
        Process process = null;
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File("/system/bin/"));
            pb.redirectErrorStream(true);
            process = pb.start();
            in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                sb.append(new String(re));
            }
        } catch (Exception e) {
            if (isDebug) {
                L.e(TAG, "checkIp error: ", e);
            } else {
                L.w(TAG, "checkIp error: " + e);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }

            if (process != null) {
                process.destroy();
            }
        }

        String netData = sb.toString();

        if (!TextUtils.isEmpty(netData)) {
            log(">>> netcfg data -> " + netData);
            String[] array = netData.split("\n");

            for (String ip : IPs) {
                for (String lan : array) {
                    if ((lan.contains("wlan0") || lan.contains("tunl0") || lan.contains("eth0"))
                            && lan.contains(ip)) {
                        logW(">>> Check IP {" + ip + "} is detected");
                        U.putJsonSafed(jEmu, "ip", 1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private JSONObject executeGetProp() {
        JSONObject jProps = new JSONObject();
        try {
            String regs = "([\\]\\[])";
            Pattern pattern = Pattern.compile(regs);

            String[] strCmd = new String[]{"getprop"};
            List<String> execResult = U.executeCommand(strCmd);

            JSONArray ja = new JSONArray();
            U.putJsonSafed(jProps, "props", ja);

            int i = 0;
            for (String cmd : execResult) {
                String[] line = cmd.split(":");
                Matcher matcher0 = pattern.matcher(line[0]);
                line[0] = matcher0.replaceAll("").trim();
                Matcher matcher1 = pattern.matcher(line[1]);
                line[1] = matcher1.replaceAll("").trim();

                JSONObject jo = new JSONObject();
                U.putJsonSafed(jo, "name", line[0]);
                U.putJsonSafed(jo, "value", line[1]);
                U.putJsonSafed(ja, i++, jo);
            }
            return jProps;
        } catch (Exception e) {
            if (isDebug) {
                L.e(TAG, "executeGetProp error: ", e);
            } else {
                L.w(TAG, "executeGetProp error: " + e);
            }
        }
        return jProps;
    }

    private boolean isSupportTelePhony() {
        PackageManager packageManager = sContext.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    class Property {
        String name;
        String seek_value;

        Property(String name, String seek_value) {
            this.name = name;
            this.seek_value = seek_value;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("[")
                    .append(name)
                    .append(": ")
                    .append(seek_value)
                    .append("]")
                    .toString();
        }
    }

    class EmuFeature {
        String name;
        String[] filePath;
        String[] systemProperties;
        Map<String, String> buildProperties;

        EmuFeature(String name, String[] filePath, String[] systemProperties,
                   Map<String, String> buildProperties) {
            this.name = name;
            this.filePath = filePath;
            this.systemProperties = systemProperties;
            this.buildProperties = buildProperties;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("[")
                    .append(name)
                    .append(" | ")
                    .append(Arrays.asList(filePath))
                    .append(" | ")
                    .append(Arrays.asList(systemProperties))
                    .append(" | ")
                    .append(buildProperties)
                    .append("]")
                    .toString();
        }
    }
}

