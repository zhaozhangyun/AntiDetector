package com.z.zz.zzz.antidetector.fakecamera;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;

public final class StreamConfMap {

    public static int depthFormatToPublic(int format) {
        switch (format) {
            case HAL_PIXEL_FORMAT_BLOB:
                return ImageFormat.DEPTH_POINT_CLOUD;
            case HAL_PIXEL_FORMAT_Y16:
                return ImageFormat.DEPTH16;
            case HAL_PIXEL_FORMAT_RAW16:
                return 0x1002/*ImageFormat.RAW_DEPTH*/;
            case ImageFormat.JPEG:
                throw new IllegalArgumentException(
                        "ImageFormat.JPEG is an unknown internal format");
            case HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED:
                throw new IllegalArgumentException(
                        "IMPLEMENTATION_DEFINED must not leak to public API");
            default:
                throw new IllegalArgumentException(
                        "Unknown DATASPACE_DEPTH format " + format);
        }
    }

    public static int publicToDepthFormat(int format) {
        switch (format) {
            case ImageFormat.DEPTH_POINT_CLOUD:
                return HAL_PIXEL_FORMAT_BLOB;
            case ImageFormat.DEPTH16:
                return HAL_PIXEL_FORMAT_Y16;
            case 0x1002/*ImageFormat.RAW_DEPTH*/:
                return HAL_PIXEL_FORMAT_RAW16;
            default:
                throw new IllegalArgumentException(
                        "Unknown public DATASPACE_DEPTH format " + format);
        }
    }

    public static int imageFormatToPublic(int format) {
        switch (format) {
            case HAL_PIXEL_FORMAT_BLOB:
                return ImageFormat.JPEG;
            case ImageFormat.JPEG:
                throw new IllegalArgumentException(
                        "ImageFormat.JPEG is an unknown internal format");
            default:
                return format;
        }
    }

    public static int publicToImageFormat(int format) {
        switch (format) {
            case ImageFormat.JPEG:
                return HAL_PIXEL_FORMAT_BLOB;
            case HAL_PIXEL_FORMAT_BLOB:
                throw new IllegalArgumentException(
                        "HAL_PIXEL_FORMAT_BLOB is an unknown public format");
            default:
                return format;
        }
    }

    public static String formatToString(int format) {
        switch (format) {
            case ImageFormat.YV12:
                return "YV12";
            case ImageFormat.YUV_420_888:
                return "YUV_420_888";
            case ImageFormat.NV21:
                return "NV21";
            case ImageFormat.NV16:
                return "NV16";
            case PixelFormat.RGB_565:
                return "RGB_565";
            case PixelFormat.RGBA_8888:
                return "RGBA_8888";
            case PixelFormat.RGBX_8888:
                return "RGBX_8888";
            case PixelFormat.RGB_888:
                return "RGB_888";
            case ImageFormat.JPEG:
                return "JPEG";
            case ImageFormat.YUY2:
                return "YUY2";
            case ImageFormat.Y8:
                return "Y8";
            case 0x20363159/*ImageFormat.Y16*/:
                return "Y16";
            case ImageFormat.RAW_SENSOR:
                return "RAW_SENSOR";
            case ImageFormat.RAW_PRIVATE:
                return "RAW_PRIVATE";
            case ImageFormat.RAW10:
                return "RAW10";
            case ImageFormat.DEPTH16:
                return "DEPTH16";
            case ImageFormat.DEPTH_POINT_CLOUD:
                return "DEPTH_POINT_CLOUD";
            case ImageFormat.DEPTH_JPEG:
                return "DEPTH_JPEG";
            case 0x1002/*ImageFormat.RAW_DEPTH*/:
                return "RAW_DEPTH";
            case ImageFormat.PRIVATE:
                return "PRIVATE";
            case ImageFormat.HEIC:
                return "HEIC";
            default:
                return "UNKNOWN";
        }
    }

    // from system/core/include/system/graphics.h
    private static final int HAL_PIXEL_FORMAT_RAW16 = 0x20;
    private static final int HAL_PIXEL_FORMAT_BLOB = 0x21;
    private static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 0x22;
    private static final int HAL_PIXEL_FORMAT_YCbCr_420_888 = 0x23;
    private static final int HAL_PIXEL_FORMAT_RAW_OPAQUE = 0x24;
    private static final int HAL_PIXEL_FORMAT_RAW10 = 0x25;
    private static final int HAL_PIXEL_FORMAT_RAW12 = 0x26;
    private static final int HAL_PIXEL_FORMAT_Y16 = 0x20363159;


    private static final int HAL_DATASPACE_STANDARD_SHIFT = 16;
    private static final int HAL_DATASPACE_TRANSFER_SHIFT = 22;
    private static final int HAL_DATASPACE_RANGE_SHIFT = 27;

    private static final int HAL_DATASPACE_UNKNOWN = 0x0;
    private static final int HAL_DATASPACE_V0_JFIF =
            (2 << HAL_DATASPACE_STANDARD_SHIFT) |
                    (3 << HAL_DATASPACE_TRANSFER_SHIFT) |
                    (1 << HAL_DATASPACE_RANGE_SHIFT);

    private static final int HAL_DATASPACE_DEPTH = 0x1000;
    private static final int HAL_DATASPACE_DYNAMIC_DEPTH = 0x1002;
    private static final int HAL_DATASPACE_HEIF = 0x1003;
    private static final long DURATION_20FPS_NS = 50000000L;

    private static final int DURATION_MIN_FRAME = 0;
    private static final int DURATION_STALL = 1;

    StreamConfiguration[] mConfigurations;
    StreamConfigurationDuration[] mMinFrameDurations;
    StreamConfigurationDuration[] mStallDurations;

    StreamConfiguration[] mDepthConfigurations;
    StreamConfigurationDuration[] mDepthMinFrameDurations;
    StreamConfigurationDuration[] mDepthStallDurations;

    StreamConfiguration[] mDynamicDepthConfigurations;
    StreamConfigurationDuration[] mDynamicDepthMinFrameDurations;
    StreamConfigurationDuration[] mDynamicDepthStallDurations;

    StreamConfiguration[] mHeicConfigurations;
    StreamConfigurationDuration[] mHeicMinFrameDurations;
    StreamConfigurationDuration[] mHeicStallDurations;

    HighSpeedVideoConfiguration[] mHighSpeedVideoConfigurations;
    ReprocessFormatsMap mInputOutputFormatsMap;

    boolean mListHighResolution;
}
