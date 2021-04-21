package com.z.zz.zzz.antidetector;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.hsbutils.libs.jnilib;
import com.z.zz.zzz.AntiDetector;
import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            jnilib.InitPaths(this.getFilesDir().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        L.i("main", "============================== cpuinfo: " + jnilib.getCpuInfo());
        L.i("main", "============================== cpucore: " + jnilib.getCpuCount());

        AntiDetector.create(this)
                .setDebug(BuildConfig.DEBUG)
                .setSticky(true)
                .setMinEmuFlagsThresholds(3)
                .detect((result, data) ->
                        Log.i("Main", "AntiDetector result: " + result + ", data: " + data)
                );

        try {
            L.i("main", getBuildProperties());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        L.i("main", "filelength: " + getFileLength());
        L.i("main", "availableProcessors: " + Runtime.getRuntime().availableProcessors());
    }

    private int getFileLength() {
        return new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
            @Override
            public boolean accept(File arg2) {
                return Pattern.matches("cpu[0-9]", arg2.getName());
            }
        }).length;
    }

    // 获取build.prop中的指定属性
    public String getBuildProperties() throws JSONException {
        JSONObject jo = new JSONObject();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(
                    new File("/system/build.prop")));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                L.v("main", "line: " + line);
                if (!TextUtils.isEmpty(line)) {
                    if (line.indexOf("=") != -1) {
                        String[] lines = line.split("=");
                        if (lines.length == 2) {
                            jo.put(lines[0], lines[1]);
                        } else {
                            jo.put(lines[0], "");
                        }
                    }
                }
            }
            br.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jo.toString(2);
    }

    private String getBuildInfo() {
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
            U.putJsonSafed(jo, "SE", U.getBuildSerial(this));
        } catch (Exception e) {
        }

        return U.formatJson(jo);
    }
}
