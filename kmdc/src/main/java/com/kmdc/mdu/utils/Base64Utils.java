package com.kmdc.mdu.utils;

import android.util.Base64;

public class Base64Utils {

    /**
     * 将字节数组转换成 Base64 编码
     * 用 Base64.DEFAULT 模式会导致加密的 text 下面多一行（在应用中显示是这样）
     */
    public static byte[] base64Encode(byte[] rawData) {
        return base64Encode(rawData, Base64.NO_WRAP);
    }

    public static byte[] base64Encode(byte[] rawData, int flags) {
        return Base64.encode(rawData, flags);
    }

    public static String base64EncodeToStr(byte[] rawData) {
        return base64EncodeToStr(rawData, Base64.NO_WRAP);
    }

    public static String base64EncodeToStr(byte[] rawData, int flags) {
        return Base64.encodeToString(rawData, flags);
    }

    /**
     * 将 Base64 字符串解码成字节数组
     */
    public static byte[] base64Decode(String rawData) {
        return base64Decode(rawData, Base64.NO_WRAP);
    }

    public static byte[] base64Decode(String rawData, int flags) {
        return Base64.decode(rawData, flags);
    }
}
