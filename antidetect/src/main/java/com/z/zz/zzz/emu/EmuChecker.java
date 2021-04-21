package com.z.zz.zzz.emu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;

import com.z.zz.zzz.utils.L;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmuChecker {

    /*--------------------------------- C CODE SETUP ---------------------------------*/
//    static {
//        System.loadLibrary("native-lib");
//    }
//    private native String getNativeString();
    /*------------------------------------------------------------------*/

    private static final int ALL_PERMISSIONS = 1;

    public static Map<String, Boolean> flags = new HashMap<>();
    public static final int DETECTION_THRESHOLD = 3;

    public static final int NUM_CONTACTS_THRESHOLD = 1;
    public static final int NUM_CALLS_THRESHOLD = 3;
    public static final int NUM_PHOTOS_THRESHOLD = 3;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CAMERA
    };

    private static final String[] BUILD_MODELS = {
            "android sdk built for x86",
            "android sdk built for x86_64",
            "emulator",
            "google_sdk",
            "droid4x",
            "sdk",
            "tiantianvm",
    };

    private static final String[] BUILD_PRODUCTS = {
            "emulator",
            "simulator",
            "sdk_google",
            "google_sdk",
            "sdk",
            "sdk_x86",
            "vbox86p",
            "nox"
    };

    private static final String[] BUILD_FINGERPRINTS = {
            "vsemu",
            "generic/sdk/generic",
            "generic_x86/sdk_x86/generic_x86",
            "generic/google_sdk/generic",
            "generic/vbox86p/vbox86p",
            "generic_x86_64",
            "ttvm_hdragon",
            "vbox86p"
    };

    private static final String[] BUILD_HARDWARE = {
            "goldfish",
            "ranchu",
            "vbox86",
            "nox",
            "ttvm_x86"
    };

    private static final String[] BUILD_MANUFACTURERS = {
            "Genymotion",
            "MIT",
            "nox",
            "TiantianVM"
    };

    private static final String[] BUILD_HOSTS = {
            "apa27.mtv.corp.google.com",
            "android-test-15.mtv.corp.google.com",
            "android-test-13.mtv.corp.google.com",
            "android-test-25.mtv.corp.google.com",
            "android-test-26.mtv.corp.google.com",
            "vpbs30.mtv.corp.google.com",
            "vpak21.mtv.corp.google.com"
    };

    private static final String[] DEVICE_IDS = {
            "000000000000000",
            "e21833235b6eef10",
            "012345678912345"
    };

    private static final String[] LINE_NUMBERS = { //numbers starting with 155552155 and ending with any even number from 54-84 are emulators
            "15555215554", "15555215556", "15555215558", "15555215560", "15555215562", "15555215564",
            "15555215566", "15555215568", "15555215570", "15555215572", "15555215574", "15555215576",
            "15555215578", "15555215580", "15555215582", "15555215584"
    };

    private static final int[] SENSOR_TYPES = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PROXIMITY
    };


    private static final String[] NETWORK_NAMES = {
            "AndroidWifi"
    };

    public static void appendNewLine(String txt) {
        L.d("EmuChecker", txt);
    }

    /**
     * Executes all the main detection checks, called once permissions are determined
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void executeChecks(Context context) {
        boolean build = checkBuild();
        boolean telephony = checkTelephony(context);
        boolean sensors = checkSensors(context);
        boolean cpu = checkCpu();
        boolean bluetooth = checkBluetooth();
        boolean wifi = checkWifi(context);
        boolean camera = checkCamera(context);
        boolean calls = checkCalls(context);
        boolean contacts = checkContacts(context);
        boolean photos = checkPhotos(context);

        flags.put("build", build);
        flags.put("telephony", telephony);
        flags.put("sensors", sensors);
        flags.put("cpu", cpu);
        flags.put("bluetooth", bluetooth);
        flags.put("wifi", wifi);
        flags.put("camera", camera);

        //flags involving thresholds
        flags.put("calls", calls);
        flags.put("contacts", contacts);
        flags.put("photos", photos);


        appendNewLine("\n-----Test Results-----");
        int count = 0;
        int total = 0;
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();

            if (value) {
                count++;
            }
            total++;

            String line = value ? key.toUpperCase() + ":  detected" : key.toUpperCase() + ": ---";
            appendNewLine(line);
        }

        if (count >= DETECTION_THRESHOLD) {
            appendNewLine("Emulator detected: " + count + "/" + total + "\n");
        } else {
            appendNewLine("Emulator not detected: " + count + "/" + total + "\n");
        }
    }

    interface Predicate {
        boolean call(String param);
    }

    /**
     * Generic function that checks an array of strings using the predicate function and outputs message to the main view upon check.
     *
     * @return True if predicate was found true at least once, else returns false.
     */
    public static boolean checkArray(String[] array, Predicate checkFunction, String message) {
        boolean ret = false;
        for (String s : array) {
            if (checkFunction.call(s)) {
                appendNewLine(message + " '" + s + "'");
                ret = true;
            }
        }
        return ret;
    }


    private static boolean checkBuildModel() {
        return checkArray(BUILD_MODELS, (String txt) -> Build.MODEL.toLowerCase().contains(txt), "Build model contains");
    }

    private static boolean checkBuildProduct() {
        return checkArray(BUILD_PRODUCTS, (String txt) -> Build.PRODUCT.toLowerCase().contains(txt), "Build product contains");
    }

    private static boolean checkBuildFingerprint() {
        return checkArray(BUILD_FINGERPRINTS, (String txt) -> Build.FINGERPRINT.toLowerCase().contains(txt), "Build fingerprint starts with");
    }

    private static boolean checkBuildHardware() {
        return checkArray(BUILD_HARDWARE, (String txt) -> Build.HARDWARE.toLowerCase().contains(txt), "Build hardware contains");
    }

    private static boolean checkBuildHosts() {
        return checkArray(BUILD_HOSTS, Build.HOST::equals, "Build host equals");
    }

    private static boolean checkBuildManufacturer() {
        boolean ret = checkArray(BUILD_MANUFACTURERS, Build.MANUFACTURER::contains, "Build manufacturer contains");
        if (Build.MANUFACTURER.equalsIgnoreCase("unknown")) {
            appendNewLine("Build manufacturer equals 'unknown'");
            ret = true;
        }
        return ret;
    }

    /**
     * Checks for common emulator indicators in Build.
     * Outputs to screen what indicators it finds.
     *
     * @return True if emulator is detected
     */
    private static boolean checkBuild() {
        boolean ret = (checkBuildModel() | checkBuildProduct() | checkBuildHardware() | checkBuildFingerprint() | checkBuildHosts() | checkBuildManufacturer());

        if (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) {
            appendNewLine("Build brand and build device start with 'generic'");
            ret = true;
        }

        return ret;
    }


    /**
     * Checks IMEI numbers and device IDs using the telephony manager
     * Tells you if permissions not granted for telephony checks
     *
     * @return True if emulator is detected
     */
    @SuppressLint("MissingPermission")
    private static boolean checkTelephony(Context context) {
        boolean ret = false;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            ret = checkArray(DEVICE_IDS, (String txt) -> tm.getDeviceId().equalsIgnoreCase(txt),
                    "Device ID equals") |
                    checkArray(LINE_NUMBERS, (String txt) -> tm.getLine1Number().equals(txt),
                            "Line1 number equals");
            if (tm.getSubscriberId().equals("310260000000000")) {
                appendNewLine("Subscriber ID equals '310260000000000'");
                ret = true;
            }
            if (tm.getVoiceMailNumber().equals("15552175049")) {
                appendNewLine("Voicemail number equals '15552175049'");
                ret = true;
            }
        } catch (Exception e) {
            appendNewLine("checkTelephony Exception caught: " + e);
            if (Build.VERSION.SDK_INT >= 29) {
                appendNewLine("\nSDK 29+ Cannot grant read privileged phone permission to access non-resettable ids.");
            }
        }

        return ret;
    }

    /**
     * Checks for 'goldfish' keyword in sensor names
     *
     * @return True if emulator is detected (aka virtual 'goldfish' sensor found)
     */
    private static boolean checkGoldfishSensor(SensorManager sm) {
        List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            if (sensor.getName().toLowerCase().contains("goldfish")) {
                appendNewLine("Listed sensor names contain 'goldfish'");
                return true;
            }
        }
        return false;
    }

    /**
     * Goes through list of sensor types to check if sensor exists
     *
     * @return True if emulator is detected (aka some sensor is not found)
     */
    private static boolean checkCommonSensors(SensorManager sm, int[] sensorTypes) {
        boolean ret = false;
        for (int type : sensorTypes) {
            if (sm.getDefaultSensor(type) == null) {
                appendNewLine("No sensor detected for sensor type " + type);
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Uses sensor manager to check for common emulator indicators in sensors
     *
     * @return True if emulator is detected
     */
    private static boolean checkSensors(Context context) {
        boolean ret = false;
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        try {
            ret = checkGoldfishSensor(sm) | checkCommonSensors(sm, SENSOR_TYPES);
        } catch (Exception e) {
            appendNewLine("checkSensors Exception caught: " + e);
            appendNewLine("\nCannot access sensors");
        }
        return ret;
    }

    /**
     * Checks whether cpuinfo min and max freq files exist and if there are integer values in them
     *
     * @return True if emulator is detected (files not found)
     */
    private static boolean checkCpuFrequencies() {
        String minFreq = execCommand("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
        Scanner minScanner = new Scanner(minFreq);
        String maxFreq = execCommand("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
        Scanner maxScanner = new Scanner(maxFreq);
        boolean ret = false;

        if (!minScanner.hasNextInt() || !maxScanner.hasNextInt()) {
            appendNewLine("CPU frequencies not found");
            ret = true;
        }
        minScanner.close();
        maxScanner.close();
        return ret;
    }

    private static boolean checkCpuInfo() {
        String cpuinfoOutput = execCommand("cat /proc/cpuinfo").toLowerCase();
        L.d("EmuChecker", "cpuinfoOutput: " + cpuinfoOutput);
        if (cpuinfoOutput.contains("goldfish")) {
            appendNewLine("cpuinfo contains 'goldfish'");
            return true;
        } else if (cpuinfoOutput.contains("virtual cpu")) {
            appendNewLine("cpuinfo contains 'virtual cpu'");
            return true;
        }
        return false;
    }

    private static boolean checkCpu() {
        return checkCpuFrequencies() | checkCpuInfo();
    }

    //this needs permissions actually
    private boolean checkDrivers() {
        String driversOutput = execCommand("cat /proc/tty/drivers").toLowerCase();
        appendNewLine(driversOutput);

        if (driversOutput.contains("goldfish")) {
            appendNewLine("/proc/tty/drivers contains 'goldfish'");
            return true;
        }
        return false;
    }

    /**
     * Checks if there is bluetooth capabilities
     *
     * @return true if no adapter found / likely an emulator
     */
    private static boolean checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            appendNewLine("No bluetooth adapter found");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the number of contacts is below a threshold
     *
     * @return true if emulator detected
     */
    private static boolean checkContacts(Context context) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            appendNewLine("Cursor for contacts check is null");
            return true;
        } else {
            int numContacts = cursor.getCount();
            cursor.close();
            if (numContacts < NUM_CONTACTS_THRESHOLD) {
                appendNewLine("Low number of contacts: " + numContacts);
                return true;
            } else {
                return false;
            }
        }
    }

    private static boolean checkCalls(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, null);
            int numCalls = cursor.getCount();
            cursor.close();

            if (numCalls <= NUM_CALLS_THRESHOLD) {
                appendNewLine("Low number of calls in call log: " + numCalls);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            appendNewLine("checkCalls Exception caught: " + e);
            return false;
        }
    }

    /**
     * Checks if wifi is supported and wifi network name
     */
    private static boolean checkWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            appendNewLine("Device does not support wifi");
            return true;
        } else {
            if (wifiManager.isWifiEnabled()) {
                String ssid = wifiManager.getConnectionInfo().getSSID();
                return checkArray(NETWORK_NAMES, txt -> ssid.contains(txt), "Wifi network name contains");
            } else {
                appendNewLine("Enabling wifi, please test again to get wifi results");
                wifiManager.setWifiEnabled(true); //enables wifi
                return false;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean checkCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            try {
                CameraManager cManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                return false;
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                appendNewLine("No camera available: " + e);
                return true;
            }
        } else {
            appendNewLine("No camera in system features");
            return true;
        }
    }

    private static boolean checkPhotos(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor imageCursor = cr.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            int numPhotos = imageCursor.getCount();
            imageCursor.close();

            if (numPhotos <= NUM_PHOTOS_THRESHOLD) {
                appendNewLine("Low number of photos found: " + numPhotos);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            appendNewLine("checkPhotos Exception caught: " + e);
            return false;
        }
    }

    /**
     * Execute a linux command in specified directory
     *
     * @return Output of the command as a string
     */
    private static String execCommand(String command, String[] envp, File dir) {
        String ret = null;
        try {
            Process p = Runtime.getRuntime().exec(command, envp, dir);
            InputStream inputStream = p.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            p.waitFor();
            reader.close();
            inputStream.close();

            ret = stringBuilder.toString();

        } catch (Exception e) {
            appendNewLine("execCommand Exception caught: " + e);
            appendNewLine("\nCannot run command");
        }
        return ret;
    }

    private static String execCommand(String command) {
        return execCommand(command, null, null);
    }
}
