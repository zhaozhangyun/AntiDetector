package com.z.zz.zzz.antidetector.fakecamera;

import android.util.Size;

public class StreamConfigurationDuration {

    public StreamConfigurationDuration(
            final int format, final int width, final int height, final long durationNs) {
        mFormat = format;
        mWidth = width;
        mHeight = height;
        mDurationNs = durationNs;
    }

    public final int getFormat() {
        return mFormat;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Size getSize() {
        return new Size(mWidth, mHeight);
    }

    public long getDuration() {
        return mDurationNs;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof StreamConfigurationDuration) {
            final StreamConfigurationDuration other = (StreamConfigurationDuration) obj;
            return mFormat == other.mFormat &&
                    mWidth == other.mWidth &&
                    mHeight == other.mHeight &&
                    mDurationNs == other.mDurationNs;
        }
        return false;
    }

    @Override
    public String toString() {
        return "StreamConfigurationDuration{" +
                "mFormat=" + StreamConfMap.formatToString(mFormat) + "(" + mFormat + ")" +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mDurationNs=" + mDurationNs +
                '}';
    }

    private final int mFormat;
    private final int mWidth;
    private final int mHeight;
    private final long mDurationNs;
}
