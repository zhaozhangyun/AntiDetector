package com.z.zz.zzz.emu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.List;

import static com.z.zz.zzz.emu.EmulatorDetector.log;

final class EmulatorFinder {
    private static final String TAG = "EmulatorFinder";

    private static Context sContext;

    // 模拟器检测一个20项目, 依次为:
    // 1. 拨号盘
    // 2. 蓝牙
    // 3. GPS
    // 4. 多点触控
    // 5. 电池温度
    // 6. 电池电压
    // 7. 原始模拟器特征
    // 8. 海马模拟器特征
    // 9. 文卓爷模拟器特征
    // 10. 逍遥模拟器特征
    // 11. BlueStack模拟器特征
    // 12. 夜神模拟器特征
    // 13. 天天模拟器特征
    // 14. VBOX虚拟机特征
    // 15. Genymotion特征
    // 16. Qemu特征
    // 17. CPU信息
    // 18. 设备信息
    // 19. 出厂信息
    // 20. 网络运营商信息
    static int findEmulatorFeatureFlag(Context context) {
        sContext = context;

        int flag = 0x0;

        if (!checkResolveDailAction(context)) {
            flag |= (0x1);
        }
        if (!checkBluetoothHardware()) {
            flag |= (0x1 << 1);
        }
        if (!checkGPSHardware(context)) {
            flag |= (0x1 << 2);
        }
        if (!checkMultiTouch(context)) {
            flag |= (0x1 << 3);
        }
        if (!checkBatteryTemperature(context)) {
            flag |= (0x1 << 4);
        }
        if (!checkBatteryVoltage(context)) {
            flag |= (0x1 << 5);
        }
        if (!checkOriginSimulatorFeature()) {
            flag |= (0x1 << 6);
        }
        if (!checkHaimaSimulatorFeature()) {
            flag |= (0x1 << 7);
        }
        if (!checkWenzhuoSimulatorFeature()) {
            flag |= (0x1 << 8);
        }
        if (!checkXiaoyaoSimulatorFeature()) {
            flag |= (0x1 << 9);
        }
        if (!checkBlueStackSimulatorFeature()) {
            flag |= (0x1 << 10);
        }
        if (!checkYeshenSimulatorFeature()) {
            flag |= (0x1 << 11);
        }
        if (!checkTiantianSimulatorFeature()) {
            flag |= (0x1 << 12);
        }
        if (!checkVboxFeature()) {
            flag |= (0x1 << 13);
        }
        if (!checkGenymotionFeature()) {
            flag |= (0x1 << 14);
        }
        if (!checkQemuFeature()) {
            flag |= (0x1 << 15);
        }
        if (!checkCpuInfo()) {
            flag |= (0x1 << 16);
        }
        if (!checkDeviceInfo()) {
            flag |= (0x1 << 17);
        }
        if (!checkBuildProperty()) {
            flag |= (0x1 << 18);
        }
        if (!checkNetworkOperatorName(context)) {
            flag |= (0x1 << 19);
        }

        log("findEmulatorFeatureFlag: " + flag);

        return flag;
    }

    // 1 是否能跳转拨号盘
    private static boolean checkResolveDailAction(Context context) {
        String url = "tel:" + "12345678910";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            L.w(TAG, "checkResolveDailAction failed --- Failed to resolve dial action");
            return false;
        }
        return true;
    }

    // 2 是否有蓝牙硬件
    private static boolean checkBluetoothHardware() {
        // 兼容64位ARM处理器
        if (!U.fileExist("/system/lib/libbluetooth_jni.so")
                && !U.fileExist("/system/lib64/libbluetooth_jni.so")
                && !U.fileExist("/system/lib/arm64/libbluetooth_jni.so")
                && !U.fileExist("/system/vendor/lib64/libbluetooth_jni.so")) {
            L.w(TAG, "checkBluetoothHardware failed --- Not found libbluetooth_jni.so");
            return false;
        }
        return true;
    }

    // 3 是否有GPS硬件
    private static boolean checkGPSHardware(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager service");
            return false;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            L.w(TAG, "checkGPSHardware failed --- No LocationManager providers");
            return false;
        }
        boolean containGPS = providers.contains(LocationManager.GPS_PROVIDER);
        if (!containGPS) {
            L.w(TAG, "checkGPSHardware failed --- No GPS provider");
            return false;
        }
        return true;
    }

    // 4 是否支持多点触控
    private static boolean checkMultiTouch(Context context) {
        boolean hasFeature = context.getPackageManager().hasSystemFeature(
                "android.hardware.touchscreen.multitouch");
        if (!hasFeature) {
            L.w(TAG, "checkMultiTouch failed --- No multitouch feature");
            return false;
        }
        return true;
    }

    // 5 电池温度
    private static boolean checkBatteryTemperature(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryTemperature failed --- No BATTERY_CHANGED receiver");
            return false;
        }
        int temp = batteryStatus.getIntExtra("temperature", -999);
        if (temp == -999) {
            L.w(TAG, "checkBatteryTemperature failed --- temperature is -999");
            return false;
        } else if (temp > 0) {
            L.d(TAG, "Temperature is: " + U.tempToStr(((float) temp) / 10.0f, 1));
        }
        return true;
    }

    // 6 电池电压
    private static boolean checkBatteryVoltage(Context context) {
        Intent batteryStatus = context.registerReceiver(null,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus == null) {
            L.w(TAG, "checkBatteryVoltage failed --- No BATTERY_CHANGED receiver");
            return false;
        }
        int volt = batteryStatus.getIntExtra("voltage", -999);
        if (volt == -999) {
            L.w(TAG, "checkBatteryVoltage voltage --- temperature is -999");
            return false;
        } else if (volt > 0) {
            L.d(TAG, "Voltage is: " + volt);
        }
        return true;
    }

    // 7 源生模拟器特征文件
    private static boolean checkOriginSimulatorFeature() {
        if (U.fileExist("/system/bin/qemu_props")) {
            return false;
        }
        return true;
    }

    // 8 海马模拟器特征文件
    private static boolean checkHaimaSimulatorFeature() {
        if (U.fileExist("/system/lib/libdroid4x.so")
                || U.fileExist("/system/bin/droid4x-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.droid4x"))) {
            return false;
        }
        return true;
    }

    // 9 文卓爷模拟器特征文件
    private static boolean checkWenzhuoSimulatorFeature() {
        if (U.fileExist("/system/bin/windroyed")) {
            return false;
        }
        return true;
    }

    // 10 逍遥模拟器特征文件
    private static boolean checkXiaoyaoSimulatorFeature() {
        if (U.fileExist("/system/bin/microvirt-prop")
                || U.fileExist("/system/bin/microvirtd")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.microvirtd"))) {
            return false;
        }
        return true;
    }

    // 11 BlueStack模拟器特征文件
    private static boolean checkBlueStackSimulatorFeature() {
        if (U.fileExist("/data/.bluestacks.prop")) {
            return false;
        }
        return true;
    }

    // 12 夜神模拟器特征文件
    private static boolean checkYeshenSimulatorFeature() {
        if (U.fileExist("/system/bin/nox-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.noxd"))) {
            return false;
        }
        return true;
    }

    // 13 天天模拟器特征文件
    private static boolean checkTiantianSimulatorFeature() {
        if (U.fileExist("/system/bin/ttVM-prop")
                || !TextUtils.isEmpty(U.getSystemProperties("init.svc.ttVM_x86-setup"))) {
            return false;
        }
        return true;
    }

    // 14 Vbox特征
    private static boolean checkVboxFeature() {
        if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.vbox86-setup"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_dpi"))
                || !TextUtils.isEmpty(U.getSystemProperties("androVM.vbox_graph_mode"))) {
            return false;
        }
        return true;
    }

    // 15 Genymotion特征
    private static boolean checkGenymotionFeature() {
        if (U.getSystemProperties("ro.product.manufacturer").contains("Genymotion")) {
            return false;
        }
        return true;
    }

    // 16 Qemu特征
    private static boolean checkQemuFeature() {
        try {
            if (!TextUtils.isEmpty(U.getSystemProperties("init.svc.qemud"))
                    || !TextUtils.isEmpty(U.getSystemProperties("ro.kernel.android.qemud"))) {
                return false;
            }
        } catch (Exception e) {
            L.e(TAG, "checkQemuFeature error: ", e);
        }
        return true;
    }

    // 17 CPU信息
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
                return false;
            }
        }
        return true;
    }

    // 18 设备版本
    private static boolean checkDeviceInfo() {
        String device = getDeviceInfo();
        if (!TextUtils.isEmpty(device)) {
            if (device.toLowerCase().contains("qemu")
                    || device.toLowerCase().contains("tencent")
                    || device.toLowerCase().contains("ttvm")
                    || device.toLowerCase().contains("tiantian")) {
                return false;
            }
        }
        return true;
    }

    // 19 Build属性
    private static boolean checkBuildProperty() {
        // FINGERPRINT
        String fingerprint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerprint)) {
            if (fingerprint.startsWith("generic")
                    || fingerprint.toLowerCase().contains("vbox")
                    || fingerprint.toLowerCase().contains("test-keys")) {
                L.v(TAG, "Build.FINGERPRINT: " + fingerprint);
                return false;
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
                L.v(TAG, "Build.MODEL: " + model);
                return false;
            }
        }

        // MANUFACTURER
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            if (manufacturer.contains("Genymotion")) {
                L.v(TAG, "Build.MANUFACTURER: " + manufacturer);
                return false;
            }
        }

        // BRAND
        String brand = Build.BRAND;
        if (!TextUtils.isEmpty(brand)) {
            if (brand.startsWith("generic")
                    || brand.equalsIgnoreCase("generic")) {
                L.v(TAG, "Build.BRAND: " + brand);
                return false;
            }
        }

        // DEVICE
        String device = Build.DEVICE;
        if (!TextUtils.isEmpty(device)) {
            if (device.startsWith("generic")
                    || device.equalsIgnoreCase("generic")) {
                L.v(TAG, "Build.DEVICE: " + device);
                return false;
            }
        }

        // HARDWARE
        String hardware = Build.HARDWARE;
        if (!TextUtils.isEmpty(hardware)) {
            if (hardware.equalsIgnoreCase("goldfish")
                    || hardware.equalsIgnoreCase("vbox86")
                    || hardware.toLowerCase().contains("nox")) {
                L.v(TAG, "Build.HARDWARE: " + hardware);
                return false;
            }
        }

        // PRODUCT
        String product = Build.PRODUCT;
        if (!TextUtils.isEmpty(product)) {
            if (product.equalsIgnoreCase("sdk")
                    || product.equalsIgnoreCase("google_sdk")
                    || product.equalsIgnoreCase("sdk_x86")
                    || product.equalsIgnoreCase("vbox86p")
                    || product.toLowerCase().contains("nox")) {
                L.v(TAG, "Build.PRODUCT: " + product);
                return false;
            }
        }

        // BOARD
        String board = Build.BOARD;
        if (!TextUtils.isEmpty(board)) {
            if (board.equalsIgnoreCase(Build.UNKNOWN)
                    || board.toLowerCase().contains("nox")) {
                L.v(TAG, "Build.BOARD: " + board);
                return false;
            }
        }

        // BOOTLOADER
        String bootloader = Build.BOOTLOADER;
        if (!TextUtils.isEmpty(bootloader)) {
            if (bootloader.equalsIgnoreCase(Build.UNKNOWN)
                    || bootloader.toLowerCase().contains("nox")) {
                L.v(TAG, "Build.BOOTLOADER: " + bootloader);
                return false;
            }
        }

        // SERIAL
        String serial = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//9.0+
            if (ContextCompat.checkSelfPermission(sContext, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                serial = Build.getSerial();
            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
            serial = Build.SERIAL;
        } else {//8.0-
            serial = U.getSystemProperties("ro.serialno");
        }
        L.i(TAG, ">>> Build.SERIAL: " + serial + ", SDK_INT: " + Build.VERSION.SDK_INT);
        if (!TextUtils.isEmpty(serial)) {
            if (serial.equalsIgnoreCase("android")
                    || serial.toLowerCase().contains("nox")) {
                L.v(TAG, "Build.SERIAL: " + serial);
                return false;
            }
        }
        return true;
    }

    // 20 检查网络运营商名称
    private static boolean checkNetworkOperatorName(Context context) {
        try {
            String networkOP = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getNetworkOperatorName();
            if (networkOP.equalsIgnoreCase("android")) {
                return false;
            }
        } catch (Exception e) {
            L.e(TAG, "checkNetworkOperatorName error: ", e);
        }
        return true;
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
