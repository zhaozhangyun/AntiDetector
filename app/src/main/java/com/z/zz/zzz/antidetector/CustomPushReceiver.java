package com.z.zz.zzz.antidetector;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class CustomPushReceiver extends BroadcastReceiver {

    /* renamed from: a */
    public static final String f8321a = "action_stepcounter_notify_click";

    /* renamed from: b */
    public static final String f8322b = "key_push_send_time";

    /* renamed from: a */
    private boolean m11230a(Context context, String str) {
        if (context == null || TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            String packageName = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity.getPackageName();
            return !TextUtils.isEmpty(packageName) && packageName.equals(str);
        } catch (Exception e) {
            return false;
        }
    }

    /* renamed from: a */
    public void mo8047a(Context context) {
        try {
            Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            launchIntentForPackage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntentForPackage.putExtra("open_self", true);
            context.startActivity(launchIntentForPackage);
        } catch (Exception e) {
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && f8321a.equals(intent.getAction())) {
            intent.getLongExtra(f8322b, 0);
            if (!m11230a(context, context.getPackageName())) {
                mo8047a(context);
            }
        }
    }
}
