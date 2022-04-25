package com.kmdc.mdu.gui;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GpuInfoUtil implements GLSurfaceView.Renderer {

    private OnSurfaceCreatedListener listener;

    public GpuInfoUtil(OnSurfaceCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (listener != null) {
            listener.onSurfaceCreated(gl);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
    }

    public interface OnSurfaceCreatedListener {
        void onSurfaceCreated(GL10 gl);
    }
}
