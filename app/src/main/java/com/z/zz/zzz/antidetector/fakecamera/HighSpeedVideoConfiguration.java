package com.z.zz.zzz.antidetector.fakecamera;

import android.util.Range;
import android.util.Size;

public class HighSpeedVideoConfiguration {
    static final private int HIGH_SPEED_MAX_MINIMAL_FPS = 120;

    public HighSpeedVideoConfiguration(
            final int width, final int height, final int fpsMin, final int fpsMax,
            final int batchSizeMax) {
        if (fpsMax < HIGH_SPEED_MAX_MINIMAL_FPS) {
            throw new IllegalArgumentException("fpsMax must be at least " +
                    HIGH_SPEED_MAX_MINIMAL_FPS);
        }
        mFpsMax = fpsMax;
        mWidth = width;
        mHeight = height;
        mFpsMin = fpsMin;
        mSize = new Size(mWidth, mHeight);
        mBatchSizeMax = batchSizeMax;
        mFpsRange = new Range<>(mFpsMin, mFpsMax);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getFpsMin() {
        return mFpsMin;
    }

    public int getFpsMax() {
        return mFpsMax;
    }

    public Size getSize() {
        return mSize;
    }

    public int getBatchSizeMax() {
        return mBatchSizeMax;
    }

    public Range<Integer> getFpsRange() {
        return mFpsRange;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof HighSpeedVideoConfiguration) {
            final HighSpeedVideoConfiguration other = (HighSpeedVideoConfiguration) obj;
            return mWidth == other.mWidth &&
                    mHeight == other.mHeight &&
                    mFpsMin == other.mFpsMin &&
                    mFpsMax == other.mFpsMax &&
                    mBatchSizeMax == other.mBatchSizeMax;
        }
        return false;
    }

    @Override
    public String toString() {
        return "HighSpeedVideoConfiguration{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mFpsMin=" + mFpsMin +
                ", mFpsMax=" + mFpsMax +
                ", mBatchSizeMax=" + mBatchSizeMax +
                ", mSize=" + mSize +
                ", mFpsRange=" + mFpsRange +
                '}';
    }

    private final int mWidth;
    private final int mHeight;
    private final int mFpsMin;
    private final int mFpsMax;
    private final int mBatchSizeMax;
    private final Size mSize;
    private final Range<Integer> mFpsRange;
}
