package com.z.zz.zzz.antidetector;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

        Button btnTest0 = findViewById(R.id.btn_test_lz);
        Button btnTest1 = findViewById(R.id.btn_test_jd_1);
        Button btnTest2 = findViewById(R.id.btn_test_jd_2);
        Button btnTest3 = findViewById(R.id.btn_test_jd_3);

        btnTest0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(
                            "com.yibasan.lizhifm");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "openapp.jdmobile://virtual?params=%7B%22category%22%3A%22jump%22%2C%22des%22%3A%22m%22%2C%22url%22%3A%22http%3A%2F%2Fccc-x.jd.com%2Fdsp%2Fcl%3Fposid%3D1999%26v%3D707%26union_id%3D1000027286%26pid%3D3404%26tagid%3D157383%26didmd5%3D__IMEI__%26idfamd5%3D__IDFAIDFA__%26did%3D__IMEIIMEI__%26idfa%3D__IDFA__%26oaid%3D__OAID__%26caid%3D__CAID__%26to%3Dhttps%253A%252F%252Fprodev.m.jd.com%252Fmall%252Factive%252F3CtzrUxE8UeT4jXkLV6rX3bYKwYZ%252Findex.html%253Fad_od%253D1%22%2C%22m_param%22%3A%7B%22jdv%22%3A%22122270672%7Ckong%7Ct_1000027286_157383%7Czssc%7Cd36d13b9-61c4-4fdf-b7f2-11dbc28d14dd-p_1999-pr_3404-at_157383%22%7D%2C%22keplerID%22%3A%22kpl_jdjdtg00001322%22%2C%22keplerFrom%22%3A%221%22%2C%22kepler_param%22%3A%7B%22source%22%3A%22kepler-open%22%2C%22otherData%22%3A%7B%22mopenbp7%22%3A%22kpl_jdjdtg00001322%22%2C%22channel%22%3A%22b4dc3278288f4a25982ccdec07ebdc41%22%7D%7D%7D";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "openapp.jdmobile://virtual?params=%7B%22category%22%3A%22jump%22%2C%22des%22%3A%22m%22%2C%22url%22%3A%22https%3A%2F%2Fccc-x.jd.com%2Fdsp%2Fcl%3Fposid%3D1999%26v%3D707%26union_id%3D1000027281%26pid%3D3421%26tagid%3D114445%26didmd5%3D__IMEI__%26idfamd5%3D__IDFA__%26did%3D__IMEIIMEI__%26idfa%3D__IDFAIDFA__%26oaid%3D__OAID__%26caid%3D__CAID__%26to%3Dhttps%253A%252F%252Fh5.m.jd.com%252FbabelDiy%252FZeus%252FedcFQrBkH45AkBDyrud4fQXjrF4%252Findex.html%253Fad_od%253D1%22%2C%22m_param%22%3A%7B%22jdv%22%3A%22122270672%7Ckong%7Ct_1000027281_114445%7Czssc%7Cd36d13b9-61c4-4fdf-b7f2-11dbc28d14dd-p_1999-pr_3421-at_114445%22%7D%2C%22keplerFrom%22%3A%221%22%2C%22kepler_param%22%3A%7B%22source%22%3A%22kepler-open%22%2C%22otherData%22%3A%7B%22channel%22%3A%22b4dc3278288f4a25982ccdec07ebdc41%22%7D%7D%7D";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnTest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String uri = "openapp.jdmobile://virtual?params={\"category\":\"jump\",\"sourceType\":\"sourceType_test\",\"des\":\"m\",\"url\":\"https://u.jd.com/QCMe7OZ\",\"unionSource\":\"Awake\",\"channel\":\"5768f16df47b40cb8906fb3fa141cd4e\",\"union_open\":\"union_cps\"}";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

//        try {
//            jnilib.InitPaths(this.getFilesDir().getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }
//        L.i("main", "============================== cpuinfo: " + jnilib.getCpuInfo());
//        L.i("main", "============================== cpucore: " + jnilib.getCpuCount());
//
//        AntiDetector.create(this)
//                .setDebug(BuildConfig.DEBUG)
//                .setSticky(true)
//                .setMinEmuFlagsThresholds(3)
//                .detect((result, data) ->
//                        Log.i("Main", "AntiDetector result: " + result + ", data: " + data)
//                );
//
//        try {
//            L.i("main", getBuildProperties());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        L.i("main", "filelength: " + getFileLength());
//        L.i("main", "availableProcessors: " + Runtime.getRuntime().availableProcessors());
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
