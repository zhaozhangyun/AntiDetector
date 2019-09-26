package com.z.zz.zzz.emu;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.z.zz.zzz.AntiDetector;
import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.z.zz.zzz.AntiDetector.TAG;

/**
 * Copyright 2016 Framgia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Created by Pham Quy Hai on 5/16/16.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class EmulatorDetector {

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
    static JSONObject jsonDump;
    private static EmulatorDetector mEmulatorDetector;
    private static Context sContext;
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

    private static String dumpBuildInfo() {
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

    public static String dump() {
        if (jsonDump == null) {
            return null;
        }
        return jsonDump.toString();
    }

    static void log(String str) {
        L.v(TAG, "Emu ---> " + str);
    }

    public boolean isCheckPackage() {
        return isCheckPackage;
    }

//    public boolean isCheckTelephony() {
//        return isTelephony;
//    }

//    public EmulatorDetector setCheckTelephony(boolean telephony) {
//        this.isTelephony = telephony;
//        return this;
//    }

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
        jsonDump = new JSONObject();

        boolean result;

        // Check Emu flag
        long flag = EmulatorFinder.doCheckEmu(sContext);
        result = flag != 0x0;
        log(">>> Find emulator feature flag: " + result);
        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("emu_flag", Long.toBinaryString(flag));
        }

        // Check Advanced
        if (!result) {
            result = checkAdvanced();
            log(">>> Check Advanced: " + result);
        }

        // Check Package Name
        if (!result) {
            result = checkPackageName();
            log(">>> Check Package Name: " + result);
        }

        log(dumpBuildInfo());

        if (AntiDetector.getDefault().mData != null) {
            AntiDetector.getDefault().mData.put("snapshot", jsonDump.toString());
        }

        return result;
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
        return result;
    }

    private boolean checkPackageName() {
        if (!isCheckPackage || mListPackageName.isEmpty()) {
            return false;
        }
        final PackageManager packageManager = sContext.getPackageManager();
        for (String pkgName : mListPackageName) {
            Intent tryIntent = packageManager.getLaunchIntentForPackage(pkgName);
            if (tryIntent != null) {
                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(
                        tryIntent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!resolveInfos.isEmpty()) {
                    U.putJsonSafed(jsonDump, "pkg", 1);
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
                            U.putJsonSafed(jsonDump, "qemu", known_qemu_driver);
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
                U.putJsonSafed(jsonDump, "pipe", 1);
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
            U.putJsonSafed(jsonDump, "qemu_p", found_props);
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
                        U.putJsonSafed(jsonDump, "ip", 1);
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

