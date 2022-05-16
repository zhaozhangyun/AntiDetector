package com.z.zz.zzz.antidetector.fakecamera;

import java.util.Arrays;

public class ReprocessFormatsMap {
    public ReprocessFormatsMap(final int[] entry) {

        int numInputs = 0;
        int left = entry.length;
        for (int i = 0; i < entry.length; ) {
            int inputFormat = entry[i];

            left--;
            i++;

            if (left < 1) {
                throw new IllegalArgumentException(
                        String.format("Input %x had no output format length listed", inputFormat));
            }

            final int length = entry[i];
            left--;
            i++;

            for (int j = 0; j < length; ++j) {
                int outputFormat = entry[i + j];
            }

            if (length > 0) {
                if (left < length) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Input %x had too few output formats listed (actual: %d, " +
                                            "expected: %d)", inputFormat, left, length));
                }

                i += length;
                left -= length;
            }

            numInputs++;
        }

        mEntry = entry;
        mInputCount = numInputs;
    }

    @Override
    public String toString() {
        return "ReprocessFormatsMap{" +
                "mEntry=" + Arrays.toString(mEntry) +
                ", mInputCount=" + mInputCount +
                '}';
    }

    private final int[] mEntry;
    /*
     * Dependent fields: values are derived from mEntry
     */
    private final int mInputCount;
}
