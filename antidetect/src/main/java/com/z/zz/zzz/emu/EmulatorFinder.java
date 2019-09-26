package com.z.zz.zzz.emu;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;

import static com.z.zz.zzz.emu.EmulatorDetector.jsonDump;

final class EmulatorFinder {
    private static final String TAG = "EmulatorFinder";
    private static final int BUILD_THRESHOLDS = 5;
    private static Context sContext;

    static long doCheckEmu(Context context) {
        sContext = context;

        long flag = 0x0;
        int i = -1;

        if (checkBuildProperty()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkResolveDialAction(context)) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkBluetoothHardware()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkGPSHardware(context)) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkMultiTouch(context)) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkBatteryTemperature(context)) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkBatteryVoltage(context)) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkOriginSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkHaimaSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkWenzhuoSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkXiaoyaoSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkBlueStackSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkYeshenSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkTiantianSimulatorFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkVboxFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkGenymotionFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkQemuFeature()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkCpuInfo()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkDeviceInfo()) {
            i++;
            flag |= (0x1 << i);
        }
        if (checkNetworkOperatorName(context)) {
            i++;
            flag |= (0x1 << i);
        }

        return flag;
    }

    // 是否能跳转拨号盘
    private static boolean checkResolveDialAction(Context context) {
        String url = "tel:" + "12345678910";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            L.w(TAG, "checkResolveDialAction failed --- Failed to resolve dial action");
            U.putJsonSafed(jsonDump, "dial", 1);
            return true;
        }
        return false;
    }

    // 是否有蓝牙硬件
    private static boolean checkBluetoothHardware() {
        // 兼容64位ARM处理器
        if (!U.fileExist("/system/lib/libbluetooth_jni.so")
                && !U.fileExist("/system/lib64/libbluetooth_jni.so")
                && !U.fileExist("/system/lib/arm64/libbluetooth_jni.so")
                && !U.fileExist("/system/vendor/lib64/libbluetooth_jni.so")) {
            L.w(TAG, "checkBluetoothHardware failed --- Not found libbluetooth_jni.so");
            U.putJsonSafed(jsonDump, "bt", 1);
            return true;
        }
        return false;
    }

    // 是否有GPS硬件
    private static boolean checkGPSHardware(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager service");
            U.putJsonSafed(jsonDump, "gps", 1);
            return true;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager providers");
            U.putJsonSafed(jsonDump, "gps", 1);
            return true;
        }
        boolean containGPS = providers.contains(LocationManager.GPS_PROVIDER);
        if (!containGPS) {
            L.w(TAG, "checkGPSHardware failed --- No GPS provider");
            U.putJsonSafed(jsonDump, "gps", 1);
            return true;
        }
        return false;
    }

    // 是否支持多点触控
    private static boolean checkMultiTouch(Context context) {
        boolean hasFeature = context.getPackageManager().hasSystemFeature(
                "android.hardware.touchscreen.multitouch");
        if (!hasFeature) {
            L.w(TAG, "checkMultiTouch failed --- No multitouch feature");
            U.putJsonSafed(jsonDump, "mt", 1);
            return true;
        }
        return false;
    }

    // 电池温度
    private static boolean checkBatteryTemperature(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryTemperature failed --- No BATTERY_CHANGED receiver");
            U.putJsonSafed(jsonDump, "temp", 1);
            return true;
        }
        int temp = batteryStatus.getIntExtra("temperature", -999);
        if (temp == -999) {
            L.w(TAG, "checkBatteryTemperature failed --- temperature is -999");
            U.putJsonSafed(jsonDump, "temp", 1);
            return true;
        } else if (temp > 0) {
            L.d(TAG, "Temperature is: " + U.tempToStr(((float) temp) / 10.0f, 1));
        }
        return false;
    }

    // 电池电压
    private static boolean checkBatteryVoltage(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryVoltage failed --- No BATTERY_CHANGED receiver");
            U.putJsonSafed(jsonDump, "volt", 1);
            return true;
        }
        int volt = batteryStatus.getIntExtra("voltage", -999);
        if (volt == -999) {
            L.w(TAG, "checkBatteryVoltage failed --- voltage is -999");
            U.putJsonSafed(jsonDump, "volt", 1);
            return true;
        } else if (volt > 0) {
            L.d(TAG, "Voltage is: " + volt);
        }
        return false;
    }

    // 源生模拟器特征文件
    private static boolean checkOriginSimulatorFeature() {
        String[] known_files = {"/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
                "/system/bin/qemu-props", "/system/bin/qemu_props"};
        for (String pipe : known_files) {
            if (U.fileExist(pipe)) {
                L.v(TAG, "checkOriginSimulatorFeature: " + pipe);
                U.putJsonSafed(jsonDump, "pipe", 1);
                return true;
            }
        }
        return false;
    }

    // 海马模拟器特征文件
    private static boolean checkHaimaSimulatorFeature() {
        if (U.fileExist("/system/lib/libdroid4x.so")
                || U.fileExist("/system/bin/droid4x-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.droid4x"))) {
            U.putJsonSafed(jsonDump, "droid4x", 1);
            return true;
        }
        return false;
    }

    // 文卓爷模拟器特征文件
    private static boolean checkWenzhuoSimulatorFeature() {
        if (U.fileExist("/system/bin/windroyed")) {
            U.putJsonSafed(jsonDump, "windroye", 1);
            return true;
        }
        return false;
    }

    // 逍遥模拟器特征文件
    private static boolean checkXiaoyaoSimulatorFeature() {
        if (U.fileExist("/system/bin/microvirt-prop")
                || U.fileExist("/system/bin/microvirtd")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.microvirtd"))) {
            U.putJsonSafed(jsonDump, "microvirt", 1);
            return true;
        }
        return false;
    }

    // BlueStack模拟器特征文件
    private static boolean checkBlueStackSimulatorFeature() {
        if (U.fileExist("/data/.bluestacks.prop")) {
            U.putJsonSafed(jsonDump, "bluestack", 1);
            return true;
        }
        return false;
    }

    // 夜神模拟器特征文件
    private static boolean checkYeshenSimulatorFeature() {
        if (U.fileExist("/system/bin/nox-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.noxd"))) {
            U.putJsonSafed(jsonDump, "nox", 1);
            return true;
        }
        return false;
    }

    // 天天模拟器特征文件
    private static boolean checkTiantianSimulatorFeature() {
        if (U.fileExist("/system/bin/ttVM-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.ttVM_x86-setup"))) {
            U.putJsonSafed(jsonDump, "ttvm", 1);
            return true;
        }
        return false;
    }

    // Vbox特征
    private static boolean checkVboxFeature() {
        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.vbox86-setup"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_dpi"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_graph_mode"))) {
            U.putJsonSafed(jsonDump, "vbox", 1);
            return true;
        }
        return false;
    }

    // Genymotion特征
    private static boolean checkGenymotionFeature() {
        if (U.getSystemProperties("ro.product.manufacturer").contains("Genymotion")) {
            U.putJsonSafed(jsonDump, "genym", 1);
            return true;
        }
        return false;
    }

    // Qemu特征
    private static boolean checkQemuFeature() {
        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.qemud"))
                || !TextUtils.isEmpty(U.getSystemProperties("ro.kernel.android.qemud"))) {
            U.putJsonSafed(jsonDump, "qemu", 1);
            return true;
        }
        return false;
    }

    // CPU信息
    private static boolean checkCpuInfo() {
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
                U.putJsonSafed(jsonDump, "cpu", 1);
                return true;
            }
        }
        return false;
    }

    // 设备版本
    private static boolean checkDeviceInfo() {
        String device = getDeviceInfo();
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("qemu")
                    || device.toLowerCase().contains("tencent")
                    || device.toLowerCase().contains("ttvm")
                    || device.toLowerCase().contains("tiantian")) {
                L.v(TAG, "checkDeviceInfo(): " + device);
                U.putJsonSafed(jsonDump, "device", 1);
                return true;
            }
        }
        return false;
    }

    // Build属性
    private static boolean checkBuildProperty() {
        int flags = 0;

        // FINGERPRINT
        String fingerprint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerprint)) {
            if (fingerprint.toLowerCase().contains("generic")
                    || fingerprint.toLowerCase().contains("x86")
                    || fingerprint.toLowerCase().contains("vbox")
                    || fingerprint.toLowerCase().contains("test-keys")) {
                U.putJsonSafed(jsonDump, "fp", 1);
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
                U.putJsonSafed(jsonDump, "mo", 1);
                flags++;
            }
        }

        // MANUFACTURER
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.toLowerCase().contains("genymotion")) {
                U.putJsonSafed(jsonDump, "ma", 1);
                flags++;
            }
        }

        // BRAND
        String brand = Build.BRAND;
        if (!TextUtils.isEmpty(brand)) {
            if (brand.toLowerCase().contains("generic")
                    || brand.toLowerCase().contains("android")) {
                U.putJsonSafed(jsonDump, "br", 1);
                flags++;
            }
        }

        // DEVICE
        String device = Build.DEVICE;
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("generic")
                    || device.toLowerCase().contains("vbox")) {
                U.putJsonSafed(jsonDump, "de", 1);
                flags++;
            }
        }

        // HARDWARE
        String hardware = Build.HARDWARE;
        if (!TextUtils.isEmpty(hardware)) {
            if (hardware.equalsIgnoreCase("goldfish")
                    || hardware.equalsIgnoreCase("vbox86")
                    || hardware.toLowerCase().contains("nox")) {
                U.putJsonSafed(jsonDump, "hw", 1);
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
                U.putJsonSafed(jsonDump, "pr", 1);
                flags++;
            }
        }

        // BOARD
        String board = Build.BOARD;
        if (!TextUtils.isEmpty(board)) {
            if (board.equalsIgnoreCase(Build.UNKNOWN)
                    || board.toLowerCase().contains("nox")) {
                U.putJsonSafed(jsonDump, "bo", 1);
                flags++;
            }
        }

        // BOOTLOADER
        String bootloader = Build.BOOTLOADER;
        if (!TextUtils.isEmpty(bootloader)) {
            if (bootloader.equalsIgnoreCase(Build.UNKNOWN)
                    || bootloader.toLowerCase().contains("nox")) {
                U.putJsonSafed(jsonDump, "bl", 1);
                flags++;
            }
        }

        // SERIAL
        String serial = U.getBuildSerial(sContext);
        L.i(TAG, ">>> Build.SERIAL: " + serial + ", SDK_INT: " + Build.VERSION.SDK_INT);
        if (!TextUtils.isEmpty(serial)) {
            if (serial.toLowerCase().contains("android") || serial.toLowerCase().contains("nox")) {
                U.putJsonSafed(jsonDump, "se", 1);
                flags++;
            }
        }

        L.v(TAG, "checkBuildProperty(): " + flags + " (thresholds: " + BUILD_THRESHOLDS + ")");
        if (flags >= BUILD_THRESHOLDS) {
            return true;
        }

        return false;
    }

    // 检查网络运营商名称
    private static boolean checkNetworkOperatorName(Context context) {
        String networkOP = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName();
        if (networkOP.equalsIgnoreCase("android")) {
            L.v(TAG, "checkNetworkOperatorName(): " + networkOP);
            U.putJsonSafed(jsonDump, "netop", 1);
            return true;
        }
        return false;
    }


    // ========================Helper==============================

    private static String getCPUInfo() {
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

    private static String getDeviceInfo() {
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
}
