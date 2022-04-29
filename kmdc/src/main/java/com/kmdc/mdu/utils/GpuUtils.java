package com.kmdc.mdu.utils;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

import org.json.JSONException;
import org.json.JSONObject;

public class GpuUtils {

    public static JSONObject getGLParams() {
        JSONObject jo = new JSONObject();

        EGLDisplay mEGLDisplay = EGL14.eglGetDisplay(0);
        EGL14.eglInitialize(mEGLDisplay, new int[2], 0, new int[2], 1);
        int[] params = new int[]{0x303F, 0x308E, 0x3029, 0, 0x3040, 4, 0x3033, 1, 0x3038};
        EGLConfig[] mEGLConfigs = new EGLConfig[]{null};
        EGL14.eglChooseConfig(mEGLDisplay, params, 0, mEGLConfigs, 0,
                1, new int[]{1}, 0);

        EGLConfig mEGLConfig = mEGLConfigs[0];
        EGLSurface mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
                new int[]{0x3057, 0x40, 0x3056, 0x40, 0x3038}, 0);
        EGLContext mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT,
                new int[]{0x3098, 2, 0x3038}, 0);
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        GLES20.glGetIntegerv(0xD33, new int[1], 0);

        try {
            jo.put("renderer", GLES20.glGetString(0x1F01));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jo.put("vendor", GLES20.glGetString(0x1F00));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jo.put("version", GLES20.glGetString(0x1F02));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jo.put("extensions", GLES20.glGetString(0x1F03));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        EGL14.eglTerminate(mEGLDisplay);

        return jo;
    }
}
