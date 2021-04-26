package com.z.zz.zzz.antidetector;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.z.zz.zzz.AntiDetector;

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
    }
}
