package com.damn.karaoke.core.controller.processing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class GoertzelToneDetectorJNI extends BaseToneDetector {

    private final int sampleRate;

    public GoertzelToneDetectorJNI(int sampleRate, int threshold, int peak_count) {
        super(threshold, peak_count);
        this.sampleRate = sampleRate;
    }

    @Override
    public int analyze(byte[] data, int read) {
        if (read < 2)
            return -1;

        int size = read / 2;
        ShortBuffer buff = ByteBuffer.wrap(data, 0, read).order(ByteOrder.nativeOrder()).asShortBuffer();

        if(!hasSignal(buff, size))
            return -1;

        return bestMatchTone(data, read, sampleRate);
    }

    private static native int bestMatchTone(byte[] data, int size, int sampleRate);

    static {
        System.loadLibrary("toneanalyzer");
    }
}
