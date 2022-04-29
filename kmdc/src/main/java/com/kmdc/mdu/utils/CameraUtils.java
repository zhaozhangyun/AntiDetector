package com.kmdc.mdu.utils;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import zizzy.zhao.bridgex.l.L;

public class CameraUtils {

    public static synchronized JSONArray getCameraCharacteristics(Context context) {
        JSONArray jCamera = new JSONArray();
        if (!context.getPackageManager().hasSystemFeature("android.hardware.camera.any")) {
            L.w("Oops!!! You don't has system feature - android.hardware.camera.any");
            return jCamera;
        }

        CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cm.getCameraIdList();
            for (int i = 0; i < cameraIdList.length; ++i) {
                JSONObject jo = new JSONObject();
                String cameraId = cameraIdList[i];
//                L.d("------------ cameraId: " + cameraId);
                jo.put("cameraId", cameraId);
                CameraCharacteristics cc = cm.getCameraCharacteristics(cameraId);
                List<Key<?>> keys = cc.getKeys();
                Iterator<Key<?>> it = keys.listIterator();
                while (it.hasNext()) {
                    Key key = it.next();
                    String keyName = key.getName();
                    Object obj = cc.get(key);
                    String value = obj.toString();
                    String className = obj.getClass().getName();
//                    L.d("key: " + keyName + ", className: " + className + ", obj: " + value);
                    if (className.startsWith("[")) {
                        if (className.startsWith("[L")) {
                            value = Arrays.toString((Object[]) obj);
                        } else {
                            switch (className) {
                                case "[Z":
                                    value = Arrays.toString((boolean[]) obj);
                                    break;
                                case "[B":
                                    value = Arrays.toString((byte[]) obj);
                                    break;
                                case "[C":
                                    value = Arrays.toString((char[]) obj);
                                    break;
                                case "[D":
                                    value = Arrays.toString((double[]) obj);
                                    break;
                                case "[F":
                                    value = Arrays.toString((float[]) obj);
                                    break;
                                case "[I":
                                    value = Arrays.toString((int[]) obj);
                                    break;
                                case "[J":
                                    value = Arrays.toString((long[]) obj);
                                    break;
                                case "[S":
                                    value = Arrays.toString((short[]) obj);
                                    break;
                            }
                        }
                    }
//                    L.d("key: " + keyName + ", className: " + className + ", obj: " + obj);
                    jo.put(keyName, value);
                }
                jCamera.put(jo);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return jCamera;
    }
}
