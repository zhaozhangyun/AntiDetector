package com.z.zz.zzz.emu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.z.zz.zzz.AntiDetector.TAG;

public final class EmulatorDetector {

    //    private static final String[] GENY_FILES = {
//            "/dev/socket/genyd",
//            "/dev/socket/baseband_genyd"
//    };
//    private static final String[] ANDY_FILES = {
//            "fstab.andy",
//            "ueventd.andy.rc"
//    };
//    private static final String[] NOX_FILES = {
//            "fstab.nox",
//            "init.nox.rc",
//            "ueventd.nox.rc"
//    };
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
    private static int MIN_PROPERTIES_THRESHOLD = 0x5;
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
        return jo.toString();
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
        try {
            InputStream is = context.getResources().getAssets().open(EMU_PATTERN_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String content = new String(buffer);
            jData = new JSONObject(content);
            log("jdata: " + jData);
        } catch (Exception e) {
            L.e(TAG, "parse emu_pattern.json error", e);
        }

        MIN_EMU_FLAGS_THRESHOLD = U.getJsonSafed(jData, "min_emu_flags_threshold");
        MIN_BUILD_THRESHOLD = U.getJsonSafed(jData, "min_build_threshold");
        IPs = parseJson(jData, "ips");
        QEMU_DRIVERS = parseJson(jData, "qemu_drivers");
        PIPES = parseJson(jData, "pipes");
        X86_FILES = parseJson(jData, "x86_files");
        EMU_FILES = parseJson(jData, "emu_files");

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

        String[] packages = parseJson(jData, "packages");
        mListPackageName = Arrays.asList(packages);

        PHONE_NUMBERS = parseJson(jData, "phone_numbers");
        DEVICE_IDS = parseJson(jData, "device_id");
        IMSI_IDS = parseJson(jData, "imsi");
        BLUETOOTH_PATH = parseJson(jData, "bluetooth_path");

        JSONArray jEmuFeatures = U.getJsonSafed(jData, "emu_features");
        EMU_FEATURES = new EmuFeature[jEmuFeatures.length()];
        for (int i = 0; i < jEmuFeatures.length(); i++) {
            JSONObject jo = U.getJsonSafed(jEmuFeatures, i);
            String name = U.getJsonSafed(jo, "name");
            String[] filePath = parseJson(jo, "file_path");
            String[] systemProperties = parseJson(jo, "sys_prop");
            EmuFeature ef = new EmuFeature(name, filePath, systemProperties);
            EMU_FEATURES[i] = ef;
        }
        log("@@@@@@@@@@@ Parse emu_pattern.json finished.");
    }

    private String[] parseJson(JSONObject jo, String name) {
        log("call parseJson(): name=" + name);
        JSONArray ja = U.getJsonSafed(jo, name);
        String[] content = new String[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            content[i] = U.getJsonSafed(ja, i);
            log("   -- value: " + content[i]);
        }
        return content;
    }

    private void log(String str) {
        L.v(TAG, "Emu ---> " + str);
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

            for (String path : filePath) {
                if (U.fileExist(path)) {
                    U.putJsonSafed(jEmu, name, 1);
                    return true;
                }
            }

            for (String sysProp : systemProperties) {
                if (!TextUtils.isEmpty(U.getSystemProperties(sysProp))) {
                    U.putJsonSafed(jEmu, name, 1);
                    return true;
                }
            }
        }

        return false;
    }

    // 海马模拟器特征文件
//    private boolean checkHaimaEmuFeature() {
//        if (U.fileExist("/system/lib/libdroid4x.so")
//                || U.fileExist("/system/bin/droid4x-prop")
//                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.droid4x"))) {
//            U.putJsonSafed(jEmu, "hm", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // 文卓爷模拟器特征文件
//    private boolean checkWenzhuoEmuFeature() {
//        if (U.fileExist("/system/bin/windroyed")) {
//            U.putJsonSafed(jEmu, "wz", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // 逍遥模拟器特征文件
//    private boolean checkXiaoyaoEmuFeature() {
//        if (U.fileExist("/system/bin/microvirt-prop")
//                || U.fileExist("/system/bin/microvirtd")
//                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.microvirtd"))) {
//            U.putJsonSafed(jEmu, "xy", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // BlueStack模拟器特征文件
//    private boolean checkBlueStackEmuFeature() {
//        if (U.fileExist("/data/.bluestacks.prop")) {
//            U.putJsonSafed(jEmu, "bs", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // 夜神模拟器特征文件
//    private boolean checkYeshenEmuFeature() {
//        if (U.fileExist("/system/bin/nox-prop")
//                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.noxd"))) {
//            U.putJsonSafed(jEmu, "ys", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // 天天模拟器特征文件
//    private boolean checkTiantianEmuFeature() {
//        if (U.fileExist("/system/bin/ttVM-prop")
//                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.ttVM_x86-setup"))) {
//            U.putJsonSafed(jEmu, "tt", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // Vbox特征
//    private boolean checkVboxFeature() {
//        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.vbox86-setup"))
//                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_dpi"))
//                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_graph_mode"))) {
//            U.putJsonSafed(jEmu, "vb", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // Genymotion特征
//    private boolean checkGenymotionFeature() {
//        if (U.getSystemProperties("ro.product.manufacturer").contains("Genymotion")) {
//            U.putJsonSafed(jEmu, "gy", 1);
//            return true;
//        }
//        return false;
//    }
//
//    // Qemu特征
//    private boolean checkQemuFeature() {
//        String[] known_files = {"/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
//                "/system/bin/qemu-props", "/system/bin/qemu_props"};
//        for (String pipe : known_files) {
//            if (U.fileExist(pipe)) {
//                log("checkQemuFeature: " + pipe);
//                U.putJsonSafed(jEmu, "qe", 1);
//                return true;
//            }
//        }
//        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.qemud"))
//                || !TextUtils.isEmpty(U.getSystemProperties("ro.kernel.android.qemud"))) {
//            U.putJsonSafed(jEmu, "qe", 1);
//            return true;
//        }
//        return false;
//    }

    // CPU信息
    private boolean checkCpuInfo() {
        String cpu = getCPUInfo();
        if (!TextUtils.isEmpty(cpu)) {
            if (cpu.toLowerCase().contains("intel") || cpu.toLowerCase().contains("amd")) {
                log(" Check [" + cpu + "] is detected");
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
                log(" Check [" + device + "] is detected");
                U.putJsonSafed(jEmu, "di", 1);
                return true;
            }
        }
        return false;
    }

    private String getCPUInfo() {
        String name = "";
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String line = null;

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
            L.e(TAG, "getCPUInfo error: ", e);
        }
        return name;
    }

    private String getDeviceInfo() {
        String result = "";
        ProcessBuilder cmd;

        try {
            String[] args = {"/system/bin/cat", "/proc/version"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[256];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (Exception e) {
            L.e(TAG, "getDeviceInfo error: ", e);
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
                    || fingerprint.toLowerCase().contains("x86")
                    || fingerprint.toLowerCase().contains("vbox")
                    || fingerprint.toLowerCase().contains("test-keys")) {
                U.putJsonSafed(jBuild, "fp", 1);
                flags++;
            }
        }

        // MODEL
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            if (model.toLowerCase().contains("google_sdk")
                    || model.equalsIgnoreCase("google_sdk")
                    || model.equalsIgnoreCase("sdk")
                    || model.toLowerCase().contains("droid4x")
                    || model.toLowerCase().contains("emulator")
                    || model.contains("Android SDK built for x86")) {
                U.putJsonSafed(jBuild, "mo", 1);
                flags++;
            }
        }

        // MANUFACTURER
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.toLowerCase().contains("genymotion")) {
                U.putJsonSafed(jBuild, "ma", 1);
                flags++;
            }
        }

        // BRAND
        String brand = Build.BRAND;
        if (!TextUtils.isEmpty(brand)) {
            if (brand.toLowerCase().contains("generic")
                    || brand.toLowerCase().contains("android")) {
                U.putJsonSafed(jBuild, "br", 1);
                flags++;
            }
        }

        // DEVICE
        String device = Build.DEVICE;
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("generic")
                    || device.toLowerCase().contains("vbox")) {
                U.putJsonSafed(jBuild, "de", 1);
                flags++;
            }
        }

        // HARDWARE
        String hardware = Build.HARDWARE;
        if (!TextUtils.isEmpty(hardware)) {
            if (hardware.equalsIgnoreCase("goldfish")
                    || hardware.equalsIgnoreCase("vbox86")
                    || hardware.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "hw", 1);
                flags++;
            }
        }

        // PRODUCT
        String product = Build.PRODUCT;
        if (!TextUtils.isEmpty(product)) {
            if (product.toLowerCase().contains("sdk")
                    || product.toLowerCase().contains("x86")
                    || product.toLowerCase().contains("vbox")
                    || product.equalsIgnoreCase("google_sdk")
                    || product.equalsIgnoreCase("sdk_x86")
                    || product.equalsIgnoreCase("vbox86p")
                    || product.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "pr", 1);
                flags++;
            }
        }

        // BOARD
        String board = Build.BOARD;
        if (!TextUtils.isEmpty(board)) {
            if (board.equalsIgnoreCase(Build.UNKNOWN)
                    || board.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "bo", 1);
                flags++;
            }
        }

        // BOOTLOADER
        String bootloader = Build.BOOTLOADER;
        if (!TextUtils.isEmpty(bootloader)) {
            if (/*bootloader.equalsIgnoreCase(Build.UNKNOWN)
                    || */bootloader.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "bl", 1);
                flags++;
            }
        }

        // SERIAL
        String serial = U.getBuildSerial(sContext);
        L.i(TAG, ">>> Build.SERIAL: " + serial + ", SDK_INT: " + Build.VERSION.SDK_INT);
        if (!TextUtils.isEmpty(serial)) {
            if (serial.toLowerCase().contains("android")
                    || serial.toLowerCase().contains("nox")
                    || serial.toLowerCase().contains("emulator")) {
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
        int flags = 0;
        boolean result = false;

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

        log("doCheckEmu(): " + flags + " (thresholds: " + MIN_EMU_FLAGS_THRESHOLD + ")");
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

        if (checkEmuFeature()) {
            if (isDebug) {
                result = true;
            } else {
                return true;
            }
        }

//        if (checkQemuFeature()) {
//            return true;
//        }
//        if (checkHaimaEmuFeature()) {
//            return true;
//        }
//        if (checkWenzhuoEmuFeature()) {
//            return true;
//        }
//        if (checkXiaoyaoEmuFeature()) {
//            return true;
//        }
//        if (checkBlueStackEmuFeature()) {
//            return true;
//        }
//        if (checkYeshenEmuFeature()) {
//            return true;
//        }
//        if (checkTiantianEmuFeature()) {
//            return true;
//        }
//        if (checkVboxFeature()) {
//            return true;
//        }
//        if (checkGenymotionFeature()) {
//            return true;
//        }

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
                /*|| checkFiles(GENY_FILES, "Geny")
                || checkFiles(ANDY_FILES, "Andy")
                || checkFiles(NOX_FILES, "Nox")*/
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

    private boolean checkPhoneNumber() {
        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && isSupportTelePhony()) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber = telephonyManager.getLine1Number();
            for (String number : PHONE_NUMBERS) {
                if (number.equalsIgnoreCase(phoneNumber)) {
                    log(" Check [" + number + "] is detected");
                    U.putJsonSafed(jEmu, "pn", 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDeviceId() {
        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && isSupportTelePhony()) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = telephonyManager.getDeviceId();
            for (String known_deviceId : DEVICE_IDS) {
                if (known_deviceId.equalsIgnoreCase(deviceId)) {
                    log("Check [" + known_deviceId + "] is detected");
                    U.putJsonSafed(jEmu, "de", 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkImsi() {
        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && isSupportTelePhony()) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = telephonyManager.getSubscriberId();
            for (String known_imsi : IMSI_IDS) {
                if (known_imsi.equalsIgnoreCase(imsi)) {
                    log("Check [" + known_imsi + "] is detected");
                    U.putJsonSafed(jEmu, "im", 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkOperatorNameAndroid() {
        String operatorName = ((TelephonyManager)
                sContext.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
        if (operatorName.equalsIgnoreCase("android")) {
            log("Check [" + operatorName + "] is detected");
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
                            log(">>> Check [" + known_qemu_driver + "] is detected");
                            U.putJsonSafed(jEmu, "qd", known_qemu_driver);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return false;
    }

    private boolean checkFiles(String[] targets) {
        for (String file : targets) {
            if (U.fileExist(file)) {
                log(">>> Check [" + file + "] is detected");
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
            if (TextUtils.isEmpty(property.seek_value) && !TextUtils.isEmpty(property_value)) {
                log(">>> Check [" + property + "] is detected");
                found_props++;
            }
            if (!TextUtils.isEmpty(property.seek_value) && property_value.contains(property.seek_value)) {
                log(">>> Check [" + property + "] is detected");
                found_props++;
            }
        }

        if (found_props >= MIN_PROPERTIES_THRESHOLD) {
            U.putJsonSafed(jEmu, "qp", found_props);
            return true;
        }
        return false;
    }

    private boolean checkIp() {
        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            String[] args = {"/system/bin/netcfg"};
            StringBuilder sb = new StringBuilder();
            try {
                ProcessBuilder pb = new ProcessBuilder(args);
                pb.directory(new File("/system/bin/"));
                pb.redirectErrorStream(true);
                Process process = pb.start();
                InputStream in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    sb.append(new String(re));
                }
                in.close();
            } catch (Exception ex) {
            }

            String netData = sb.toString();

            if (!TextUtils.isEmpty(netData)) {
                log(">>> netcfg data -> " + netData);
                String[] array = netData.split("\n");

                for (String ip : IPs) {
                    for (String lan : array) {
                        if ((lan.contains("wlan0") || lan.contains("tunl0") || lan.contains("eth0"))
                                && lan.contains(ip)) {
                            log(">>> Check [" + ip + "] is detected");
                            U.putJsonSafed(jEmu, "ip", 1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSupportTelePhony() {
        PackageManager packageManager = sContext.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    static class Property {
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
                    .append(" : ")
                    .append(seek_value)
                    .append("]")
                    .toString();
        }
    }

    static class EmuFeature {
        String name;
        String[] filePath;
        String[] systemProperties;

        EmuFeature(String name, String[] filePath, String[] systemProperties) {
            this.name = name;
            this.filePath = filePath;
            this.systemProperties = systemProperties;
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
                    .append("]")
                    .toString();
        }

    }
}

