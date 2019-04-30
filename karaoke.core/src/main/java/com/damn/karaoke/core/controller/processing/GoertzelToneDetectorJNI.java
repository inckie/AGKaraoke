package com.damn.karaoke.core.controller.processing;

public class GoertzelToneDetectorJNI extends BaseToneDetector {

    private final int sampleRate;

    public GoertzelToneDetectorJNI(int sampleRate, int threshold, int peak_count) {
        super(threshold, peak_count);
        this.sampleRate = sampleRate;
    }

    @Override
    public int analyze(short[] data, int read) {
        if (read < 2)
            return -1;

        if(!hasSignal(data, read))
            return -1;

        return bestMatchTone(data, read, sampleRate);
    }

    private static native int bestMatchTone(short[] data, int size, int sampleRate);

    static {
        System.loadLibrary("toneanalyzer");
    }
}
