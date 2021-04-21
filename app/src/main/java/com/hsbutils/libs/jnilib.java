package com.hsbutils.libs;

public class jnilib {
    static {
        System.loadLibrary("hsb");
    }

    public static native void InitPaths(String arg0);

    public static native int getCpuCount();

    public static native String getCpuInfo();

    public static native String getData2(String arg0, String arg1);

    public static native String getString64(String arg0);

    public static native String getprop(String arg0);

    public static native void setCheck(int arg0);

    public static native void setColor(int arg0, int arg1, int arg2);
}

