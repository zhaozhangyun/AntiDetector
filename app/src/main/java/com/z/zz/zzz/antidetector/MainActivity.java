package com.z.zz.zzz.antidetector;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.z.zz.zzz.AntiDetector;
import com.z.zz.zzz.utils.L;
import com.z.zz.zzz.utils.U;

import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AntiDetector.create(this)
                .setDebug(BuildConfig.DEBUG)
                .setSticky(true)
                .setMinEmuFlagsThresholds(3)
                .detect(new AntiDetector.OnDetectorListener() {
                    @Override
                    public void onResult(boolean result, Map<String, String> data) {
                        Log.i("Main", "AntiDetector result: " + result + ", data: " + data);
                    }
                });

        L.i("main", getBuildInfo());
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
