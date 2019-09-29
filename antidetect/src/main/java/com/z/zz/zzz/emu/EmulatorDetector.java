package com.z.zz.zzz.emu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.z.zz.zzz.AntiDetector.TAG;

public final class EmulatorDetector {

    private static final int MIN_EMU_FLAGS_THRESHOLD = 5;
    private static final int MIN_BUILD_THRESHOLD = 5;

    private static final String[] GENY_FILES = {
            "/dev/socket/genyd",
            "/dev/socket/baseband_genyd"
    };

    //    private static final String[] PHONE_NUMBERS = {
//            "15555215554", "15555215556", "15555215558", "15555215560", "15555215562", "15555215564",
//            "15555215566", "15555215568", "15555215570", "15555215572", "15555215574", "15555215576",
//            "15555215578", "15555215580", "15555215582", "15555215584"
//    };
//
//    private static final String[] DEVICE_IDS = {
//            "000000000000000",
//            "e21833235b6eef10",
//            "012345678912345"
//    };
//
//    private static final String[] IMSI_IDS = {
//            "310260000000000"
//    };
    private static final String[] QEMU_DRIVERS = {"goldfish"};
    private static final String[] PIPES = {
            "/dev/socket/qemud",
            "/dev/qemu_pipe"
    };
    private static final String[] X86_FILES = {
            "ueventd.android_x86.rc",
            "x86.prop",
            "ueventd.ttVM_x86.rc",
            "init.ttVM_x86.rc",
            "fstab.ttVM_x86",
            "fstab.vbox86",
            "init.vbox86.rc",
            "ueventd.vbox86.rc"
    };
    private static final String[] ANDY_FILES = {
            "fstab.andy",
            "ueventd.andy.rc"
    };
    private static final String[] NOX_FILES = {
            "fstab.nox",
            "init.nox.rc",
            "ueventd.nox.rc"
    };
    private static final Property[] PROPERTIES = {
            new Property("init.svc.qemud", null),
            new Property("init.svc.qemu-props", null),
            new Property("qemu.hw.mainkeys", null),
            new Property("qemu.sf.fake_camera", null),
            new Property("qemu.sf.lcd_density", null),
            new Property("ro.bootloader", Build.UNKNOWN),
            new Property("ro.bootmode", Build.UNKNOWN),
            new Property("ro.hardware", "goldfish"),
            new Property("ro.kernel.android.qemud", null),
            new Property("ro.kernel.qemu.gles", null),
            new Property("ro.kernel.qemu", "1"),
            new Property("ro.product.device", "generic"),
            new Property("ro.product.model", "sdk"),
            new Property("ro.product.name", "sdk"),
            new Property("ro.serialno", null)
    };
    private static final String IP = "10.0.2.15";
    private static final int MIN_PROPERTIES_THRESHOLD = 0x5;
    private static JSONObject jEmu;
    private static EmulatorDetector mEmulatorDetector;
    private static Context sContext;
    private JSONObject jBuild;
    //    private boolean isTelephony = false;
    private boolean isCheckPackage = true;
    private List<String> mListPackageName = new ArrayList<>();

    private EmulatorDetector(Context pContext) {
        sContext = pContext;
        mListPackageName.add("com.google.android.launcher.layouts.genymotion");
        mListPackageName.add("com.bluestacks");
        mListPackageName.add("com.bignox.app");
    }

    public static EmulatorDetector with(Context pContext) {
        if (pContext == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        if (mEmulatorDetector == null)
            mEmulatorDetector = new EmulatorDetector(pContext.getApplicationContext());
        return mEmulatorDetector;
    }

    public static String dump() {
        if (jEmu == null) {
            return null;
        }
        return jEmu.toString();
    }

    private String dumpBuildInfo() {
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

        return jo.toString();
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
            L.w(TAG, "checkResolveDialAction failed --- Failed to resolve dial action");
            U.putJsonSafed(jEmu, "da", 1);
            return true;
        }
        return false;
    }

//    public boolean isCheckTelephony() {
//        return isTelephony;
//    }

//    public EmulatorDetector setCheckTelephony(boolean telephony) {
//        this.isTelephony = telephony;
//        return this;
//    }

    // 是否有蓝牙硬件
    private boolean checkBluetoothHardware() {
        // 兼容64位ARM处理器
        if (!U.fileExist("/system/lib/libbluetooth_jni.so")
                && !U.fileExist("/system/lib64/libbluetooth_jni.so")
                && !U.fileExist("/system/lib/arm64/libbluetooth_jni.so")
                && !U.fileExist("/system/vendor/lib64/libbluetooth_jni.so")) {
            L.w(TAG, "checkBluetoothHardware failed --- Not found libbluetooth_jni.so");
            U.putJsonSafed(jEmu, "bt", 1);
            return true;
        }
        return false;
    }

    // 是否有GPS硬件
    private boolean checkGPSHardware(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager service");
            U.putJsonSafed(jEmu, "gp", 1);
            return true;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager providers");
            U.putJsonSafed(jEmu, "gp", 1);
            return true;
        }
        boolean containGPS = providers.contains(LocationManager.GPS_PROVIDER);
        if (!containGPS) {
            L.w(TAG, "checkGPSHardware failed --- No GPS provider");
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
            L.w(TAG, "checkMultiTouch failed --- No multitouch feature");
            U.putJsonSafed(jEmu, "mt", 1);
            return true;
        }
        return false;
    }

    // 电池温度
    private boolean checkBatteryTemperature(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryTemperature failed --- No BATTERY_CHANGED receiver");
            U.putJsonSafed(jEmu, "te", 1);
            return true;
        }
        int temp = batteryStatus.getIntExtra("temperature", -999);
        if (temp == -999) {
            L.w(TAG, "checkBatteryTemperature failed --- temperature is -999");
            U.putJsonSafed(jEmu, "te", 1);
            return true;
        } else if (temp > 0) {
            L.d(TAG, "Temperature is: " + U.tempToStr(((float) temp) / 10.0f, 1));
        }
        return false;
    }

    // 电池电压
    private boolean checkBatteryVoltage(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryVoltage failed --- No BATTERY_CHANGED receiver");
            U.putJsonSafed(jEmu, "vo", 1);
            return true;
        }
        int volt = batteryStatus.getIntExtra("voltage", -999);
        if (volt == -999) {
            L.w(TAG, "checkBatteryVoltage failed --- voltage is -999");
            U.putJsonSafed(jEmu, "vo", 1);
            return true;
        } else if (volt > 0) {
            L.d(TAG, "Voltage is: " + volt);
        }
        return false;
    }

    // 源生模拟器特征文件
    private boolean checkOriginEmuFeature() {
        String[] known_files = {"/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
                "/system/bin/qemu-props", "/system/bin/qemu_props"};
        for (String pipe : known_files) {
            if (U.fileExist(pipe)) {
                L.v(TAG, "checkOriginEmuFeature: " + pipe);
                U.putJsonSafed(jEmu, "or", 1);
                return true;
            }
        }
        return false;
    }

    // 海马模拟器特征文件
    private boolean checkHaimaEmuFeature() {
        if (U.fileExist("/system/lib/libdroid4x.so")
                || U.fileExist("/system/bin/droid4x-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.droid4x"))) {
            U.putJsonSafed(jEmu, "hm", 1);
            return true;
        }
        return false;
    }

    // 文卓爷模拟器特征文件
    private boolean checkWenzhuoEmuFeature() {
        if (U.fileExist("/system/bin/windroyed")) {
            U.putJsonSafed(jEmu, "wz", 1);
            return true;
        }
        return false;
    }

    // 逍遥模拟器特征文件
    private boolean checkXiaoyaoEmuFeature() {
        if (U.fileExist("/system/bin/microvirt-prop")
                || U.fileExist("/system/bin/microvirtd")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.microvirtd"))) {
            U.putJsonSafed(jEmu, "xy", 1);
            return true;
        }
        return false;
    }

    // BlueStack模拟器特征文件
    private boolean checkBlueStackEmuFeature() {
        if (U.fileExist("/data/.bluestacks.prop")) {
            U.putJsonSafed(jEmu, "bs", 1);
            return true;
        }
        return false;
    }

    // 夜神模拟器特征文件
    private boolean checkYeshenEmuFeature() {
        if (U.fileExist("/system/bin/nox-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.noxd"))) {
            U.putJsonSafed(jEmu, "ys", 1);
            return true;
        }
        return false;
    }

    // 天天模拟器特征文件
    private boolean checkTiantianEmuFeature() {
        if (U.fileExist("/system/bin/ttVM-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.ttVM_x86-setup"))) {
            U.putJsonSafed(jEmu, "tt", 1);
            return true;
        }
        return false;
    }

    // Vbox特征
    private boolean checkVboxFeature() {
        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.vbox86-setup"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_dpi"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_graph_mode"))) {
            U.putJsonSafed(jEmu, "vb", 1);
            return true;
        }
        return false;
    }

    // Genymotion特征
    private boolean checkGenymotionFeature() {
        if (U.getSystemProperties("ro.product.manufacturer").contains("Genymotion")) {
            U.putJsonSafed(jEmu, "gy", 1);
            return true;
        }
        return false;
    }

    // Qemu特征
    private boolean checkQemuFeature() {
        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.qemud"))
                || !TextUtils.isEmpty(U.getSystemProperties("ro.kernel.android.qemud"))) {
            U.putJsonSafed(jEmu, "qe", 1);
            return true;
        }
        return false;
    }

    // CPU信息
    private boolean checkCpuInfo() {
        String cpu = getCPUInfo();
        if (!TextUtils.isEmpty(cpu)) {
            if (cpu.contains("Genuine Intel(R)")
                    || cpu.contains("Intel(R) Core(TM)")
                    || cpu.contains("Intel(R) Pentium(R)")
                    || cpu.contains("Intel(R) Xeon(R)")
                    || cpu.contains("AMD")
                    || cpu.toLowerCase().contains("intel")
                    || cpu.toLowerCase().contains("amd")) {
                L.v(TAG, "checkCpuInfo(): " + cpu);
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
                L.v(TAG, "checkDeviceInfo(): " + device);
                U.putJsonSafed(jEmu, "di", 1);
                return true;
            }
        }
        return false;
    }

    // 检查网络运营商名称
    private boolean checkNetworkOperatorName(Context context) {
        String networkOP = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName();
        if (networkOP.equalsIgnoreCase("android")) {
            L.v(TAG, "checkNetworkOperatorName(): " + networkOP);
            U.putJsonSafed(jEmu, "no", 1);
            return true;
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
                    if ("Hardware".equals(k)) {
                        name = v;
                    } else if ("model name".equals(k)) {
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
            if (bootloader.equalsIgnoreCase(Build.UNKNOWN)
                    || bootloader.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "bl", 1);
                flags++;
            }
        }

        // SERIAL
        String serial = U.getBuildSerial(sContext);
        L.i(TAG, ">>> Build.SERIAL: " + serial + ", SDK_INT: " + Build.VERSION.SDK_INT);
        if (!TextUtils.isEmpty(serial)) {
            if (serial.toLowerCase().contains("android") || serial.toLowerCase().contains("nox")) {
                U.putJsonSafed(jBuild, "se", 1);
                flags++;
            }
        }

        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("emu_build", jBuild.toString());
        }
        L.v(TAG, "checkBuildProperty(): " + flags + " (thresholds: " + MIN_BUILD_THRESHOLD + ")");
        if (flags >= MIN_BUILD_THRESHOLD) {
            U.putJsonSafed(jEmu, "bd", 1);
            return true;
        }
        return false;
    }

    public boolean isCheckPackage() {
        return isCheckPackage;
    }

    public EmulatorDetector setCheckPackage(boolean chkPackage) {
        this.isCheckPackage = chkPackage;
        return this;
    }

    public EmulatorDetector addPackageName(String pPackageName) {
        this.mListPackageName.add(pPackageName);
        return this;
    }

    public EmulatorDetector addPackageName(List<String> pListPackageName) {
        this.mListPackageName.addAll(pListPackageName);
        return this;
    }

    public List<String> getPackageNameList() {
        return mListPackageName;
    }

    public boolean detect() {
        jEmu = new JSONObject();

        boolean result = doCheckEmu(sContext);

        log(dumpBuildInfo());

        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("emu_snapshot", jEmu.toString());
        }

        return result;
    }

    private boolean doCheckEmu(Context context) {
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
        if (checkBatteryTemperature(context)) {
            flags++;
        }
        if (checkBatteryVoltage(context)) {
            flags++;
        }
        if (checkOriginEmuFeature()) {
            flags++;
        }
        if (checkHaimaEmuFeature()) {
            flags++;
        }
        if (checkWenzhuoEmuFeature()) {
            flags++;
        }
        if (checkXiaoyaoEmuFeature()) {
            flags++;
        }
        if (checkBlueStackEmuFeature()) {
            flags++;
        }
        if (checkYeshenEmuFeature()) {
            flags++;
        }
        if (checkTiantianEmuFeature()) {
            flags++;
        }
        if (checkVboxFeature()) {
            flags++;
        }
        if (checkGenymotionFeature()) {
            flags++;
        }
        if (checkQemuFeature()) {
            flags++;
        }
        if (checkCpuInfo()) {
            flags++;
        }
        if (checkDeviceInfo()) {
            flags++;
        }
        if (checkNetworkOperatorName(context)) {
            flags++;
        }
        if (checkAdvanced()) {
            flags++;
        }
        if (checkPackageName()) {
            flags++;
        }

        L.v(TAG, "doCheckEmu(): " + flags + " (thresholds: " + MIN_BUILD_THRESHOLD + ")");
        return flags >= MIN_EMU_FLAGS_THRESHOLD;
    }

    private boolean checkAdvanced() {
        boolean result = /*checkTelephony()
                ||*/ checkFiles(GENY_FILES, "Geny")
                || checkFiles(ANDY_FILES, "Andy")
                || checkFiles(NOX_FILES, "Nox")
                || checkQEmuDrivers()
                || checkFiles(PIPES, "Pipes")
                || checkIp()
                || (checkQEmuProps() && checkFiles(X86_FILES, "x86"));
        if (result) {
            U.putJsonSafed(jEmu, "ad", 1);
        }
        return result;
    }

    private boolean checkPackageName() {
        if (!isCheckPackage || mListPackageName.isEmpty()) {
            return false;
        }
        PackageManager packageManager = sContext.getPackageManager();
        for (String pkgName : mListPackageName) {
            Intent tryIntent = packageManager.getLaunchIntentForPackage(pkgName);
            if (tryIntent != null) {
                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(
                        tryIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!resolveInfos.isEmpty()) {
                    U.putJsonSafed(jEmu, "pg", 1);
                    return true;
                }
            }
        }
        return false;
    }

//    private boolean checkTelephony() {
//        if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.READ_PHONE_STATE)
//                == PackageManager.PERMISSION_GRANTED && this.isTelephony && isSupportTelePhony()) {
//            return checkPhoneNumber()
//                    || checkDeviceId()
//                    || checkImsi()
//                    || checkOperatorNameAndroid();
//        }
//        return false;
//    }
//
//    private boolean checkPhoneNumber() {
//        TelephonyManager telephonyManager =
//                (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
//
//        @SuppressLint("HardwareIds") String phoneNumber = telephonyManager.getLine1Number();
//
//        for (String number : PHONE_NUMBERS) {
//            if (number.equalsIgnoreCase(phoneNumber)) {
//                log(" check phone number is detected");
//                return true;
//            }
//
//        }
//        return false;
//    }
//
//    private boolean checkDeviceId() {
//        TelephonyManager telephonyManager =
//                (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
//
//        @SuppressLint("HardwareIds") String deviceId = telephonyManager.getDeviceId();
//
//        for (String known_deviceId : DEVICE_IDS) {
//            if (known_deviceId.equalsIgnoreCase(deviceId)) {
//                log("Check device id is detected");
//                return true;
//            }
//
//        }
//        return false;
//    }
//
//    private boolean checkImsi() {
//        TelephonyManager telephonyManager =
//                (TelephonyManager) sContext.getSystemService(Context.TELEPHONY_SERVICE);
//        @SuppressLint("HardwareIds") String imsi = telephonyManager.getSubscriberId();
//
//        for (String known_imsi : IMSI_IDS) {
//            if (known_imsi.equalsIgnoreCase(imsi)) {
//                log("Check imsi is detected");
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private boolean checkOperatorNameAndroid() {
//        String operatorName = ((TelephonyManager)
//                sContext.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
//        if (operatorName.equalsIgnoreCase("android")) {
//            log("Check operator name android is detected");
//            return true;
//        }
//        return false;
//    }

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
                            log(">>> Check QEmuDrivers is detected");
                            U.putJsonSafed(jEmu, "qe", known_qemu_driver);
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

    private boolean checkFiles(String[] targets, String type) {
        for (String file : targets) {
            if (U.fileExist(file)) {
                log(">>> Check " + type + " is detected");
                L.v(TAG, "checkFiles: " + file);
                U.putJsonSafed(jEmu, "pi", 1);
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
                found_props++;
            }
            if (!TextUtils.isEmpty(property.seek_value) && property_value.contains(property.seek_value)) {
                found_props++;
            }
        }

        if (found_props >= MIN_PROPERTIES_THRESHOLD) {
            log(">>> Check QEmuProps is detected");
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

                for (String lan : array) {
                    if ((lan.contains("wlan0") || lan.contains("tunl0") || lan.contains("eth0"))
                            && lan.contains(IP)) {
                        log(">>> Check IP is detected");
                        U.putJsonSafed(jEmu, "ip", 1);
                        return true;
                    }
                }

            }
        }
        return false;
    }

//    private boolean isSupportTelePhony() {
//        PackageManager packageManager = sContext.getPackageManager();
//        boolean isSupport = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
//        log("Supported TelePhony: " + isSupport);
//        return isSupport;
//    }

    static class Property {
        String name;
        String seek_value;

        Property(String name, String seek_value) {
            this.name = name;
            this.seek_value = seek_value;
        }
    }
}

