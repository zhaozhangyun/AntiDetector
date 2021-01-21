package com.z.zz.zzz.antidetector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.LinkedList;


public class StepService extends Service {
    private String notificationId = "channelId";
    private String notificationName = "channelName";
    private static final int f3013b = -16777216;
    private static final String f8333e = "NX549J";
    private static final String f8334f = "\u6d88\u606f\u63a8\u9001";
    private static final String f8335g = "\u901a\u77e5\u680f";
    private static final String f8336h = "cart_step_channel_notify_id";
    private static final String[] f8332d = {"vivo Y31A", "vivo Y51", "vivo Y31", "vivo Y51e", "vivo Y51A", "vivo Y51t L", "vivo Y51n"};
    private static String f8953a = "channel_notify_id";
    private static NotificationManager nm;
    private final C2263b f8328a = new C2263b();
    private boolean f8340l = false;

    private static NotificationManager m11235a(Context context, boolean z) {
        NotificationManager notificationManager;
        synchronized (StepService.class) {
            if (nm == null) {
                nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int step = 3;
                    if (!z) {
                        step = 2;
                    }
                    NotificationChannel notificationChannel = new NotificationChannel(f8336h, f8334f, step);
                    notificationChannel.setDescription(f8335g);
                    if (z) {
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(-65536);
                    } else {
                        notificationChannel.enableLights(false);
                        notificationChannel.enableVibration(false);
                        notificationChannel.setVibrationPattern(new long[]{0});
                        notificationChannel.setSound((Uri) null, (AudioAttributes) null);
                    }
                    nm.createNotificationChannel(notificationChannel);
                }
            }
            notificationManager = nm;
        }
        return notificationManager;
    }

    private static boolean m11242e() {
        String str = Build.MODEL;
        if (f8333e.contains(str)) {
            return false;
        }
        if ("5.1.1".equals(Build.VERSION.RELEASE)) {
            for (String equals : f8332d) {
                if (equals.equals(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean m12266b(Context context) {
        return m12264a(f3013b, m12267c(context));
    }

    private static int m12267c(Context context) {
        Notification notification;
        try {
            notification = new NotificationCompat.Builder(context, f8953a).build();
        } catch (Exception e) {
            e.printStackTrace();
            notification = null;
        }
        if (notification == null || notification.contentView == null) {
            Log.e("getui", "getNotificationColor");
            return -1;
        }
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(notification.contentView.getLayoutId(), (ViewGroup) null, false);
        return /*viewGroup.findViewById(16908310) != null ? ((TextView) viewGroup.findViewById(16908310)).getCurrentTextColor() :*/ m12263a(viewGroup);
    }

    private static int m12263a(ViewGroup viewGroup) {
        int currentTextColor;
        LinkedList linkedList = new LinkedList();
        linkedList.add(viewGroup);
        int step = 0;
        while (linkedList.size() > 0) {
            ViewGroup viewGroup2 = (ViewGroup) linkedList.getFirst();
            int i2 = 0;
            while (i2 < viewGroup2.getChildCount()) {
                if (viewGroup2.getChildAt(i2) instanceof ViewGroup) {
                    linkedList.add((ViewGroup) viewGroup2.getChildAt(i2));
                } else if ((viewGroup2.getChildAt(i2) instanceof TextView) && ((TextView) viewGroup2.getChildAt(i2)).getCurrentTextColor() != -1) {
                    currentTextColor = ((TextView) viewGroup2.getChildAt(i2)).getCurrentTextColor();
                    i2++;
                    step = currentTextColor;
                }
                currentTextColor = step;
                i2++;
                step = currentTextColor;
            }
            linkedList.remove(viewGroup2);
        }
        return step;
    }

    private static boolean m12264a(int step, int i2) {
        int i3 = step | f3013b;
        int i4 = -16777216 | i2;
        int red = Color.red(i3) - Color.red(i4);
        int green = Color.green(i3) - Color.green(i4);
        int blue = Color.blue(i3) - Color.blue(i4);
        return Math.sqrt((double) ((blue * blue) + ((red * red) + (green * green)))) < 180.0d;
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("测试服务")
                .setContentText("我正在运行");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    public void showNotification(String str, int step) {
        Log.v("StepService", "call showNotification()");
        try {
            ContextCompat.startForegroundService(this,
                    new Intent(this, StepService.class));
//            startService(new Intent(this, StepService.class));
            startForeground(100, getNotification(this, str, step));
//            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(notificationId, notificationName,
//                        NotificationManager.IMPORTANCE_HIGH);
//                nm.createNotificationChannel(channel);
//            }
//            startForeground(100, getNotification());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v("StepService", "call onBind(): " + intent);
        this.f8328a.mo8059a(this);
        return this.f8328a;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("StepService", "call onDestroy()");
        stopForeground(true);
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForeground(100, getNotification(this, "\u53bb\u9886\u94b1", 1));
//        }
//        return START_NOT_STICKY;
//    }

    public Notification getNotification(Context context, String str, int step) {
        RemoteViews remoteViews;
        RemoteViews remoteViews2;
        m11235a(context, false);
        Intent intent = new Intent(context, CustomPushReceiver.class);
        intent.setAction(CustomPushReceiver.f8321a);
        Bundle bundle = new Bundle();
        bundle.putLong(CustomPushReceiver.f8322b, System.currentTimeMillis());
        intent.putExtras(bundle);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, f8336h);
        if (m11242e()) {
            if (!m12266b(context)) {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.notitfy_stepcounter_layout);
                remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.notitfy_collaps_stepcounter_layout);
            } else if (this.f8340l) {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.notitfy_stepcounter_mi_layout);
                remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.notitfy_collaps_stepcounter_mi_layout);
            } else {
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.notitfy_dark_stepcounter_layout);
                remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.notitfy_dark_collaps_stepcounter_layout);
            }
            double d = 0.0d;
            double d2 = 0.0d;
            if (step > 0) {
                d = m11234a(((double) step) * 0.5d * 0.001d, 1);
                d2 = m11234a(((double) step) * 0.03d, 1);
            }
            remoteViews.setTextViewText(2131755254, str);
            remoteViews.setTextViewText(R.id.tv_step_count, String.valueOf(step));
            remoteViews.setTextViewText(R.id.tv_mileage_count, String.valueOf(d));
            remoteViews.setTextViewText(R.id.tv_kcal_count, String.valueOf(d2));
            builder.setCustomBigContentView(remoteViews);
            remoteViews2.setTextViewText(2131755254, str);
            remoteViews2.setTextViewText(R.id.tv_step_count, String.valueOf(step));
            remoteViews2.setTextViewText(R.id.tv_mileage_count, String.valueOf(d));
            builder.setCustomContentView(remoteViews2);
        }
        return builder.setContentTitle(str)
                .setContentText(step + "\u6b65")
                .setSmallIcon(R.drawable.ic_notify_step)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(broadcast)
                .setAutoCancel(true)
                .setLights(0, 0, 0)
                .setVibrate(new long[]{0})
                .setSound((Uri) null)
                .setDefaults(8)
                .build();
    }

    private static double m11234a(double d, int step) {
        try {
            return new BigDecimal(d).setScale(step, 4).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }
}
