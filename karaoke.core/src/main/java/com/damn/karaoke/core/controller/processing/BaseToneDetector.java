package com.damn.karaoke.core.controller.processing;

import java.nio.ShortBuffer;

public abstract class BaseToneDetector implements IToneDetector {

    protected static final double HalftoneBase = 1.05946309436; // 2^(1/12) -> HalftoneBase^12 = 2 (one octave)
    protected static final double BaseToneFreq = 65.4064;       // lowest (half-)tone to analyze (C2 = 65.4064 Hz)
    public static final int NumHalftones = 69;               // C2-

    protected static final float[] sTones = new float[NumHalftones];

    private final int mThreshold; // can be short
    private final int mPeakCountThreshold;

    static {
        for (int tone = 0; NumHalftones != tone; ++tone) {
            sTones[tone] = (float) (BaseToneFreq * Math.pow(HalftoneBase, tone));
        }
    }

    public BaseToneDetector(int threshold, int peak_count) {
        mThreshold = threshold;
        mPeakCountThreshold = peak_count;
    }

    protected boolean hasSignal(ShortBuffer buff, int size) {
        int peaks = 0;
        for (int s = 0; size != s && peaks < mPeakCountThreshold; ++s)
            if (Math.abs(buff.get(s)) > mThreshold)
                ++peaks;

        return peaks == mPeakCountThreshold;
    }
}
