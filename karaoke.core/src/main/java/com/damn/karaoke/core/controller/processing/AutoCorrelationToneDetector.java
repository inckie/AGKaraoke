package com.damn.karaoke.core.controller.processing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by Ink on 2017-06-07.
 */
public class AutoCorrelationToneDetector extends BaseToneDetector {

    private final int mSampleRate;

    public AutoCorrelationToneDetector(int sampleRate, int threshold, int peak_count) {
        super(threshold, peak_count);
        mSampleRate = sampleRate;
    }

    @Override
    public int analyze(byte[] data, int read) {
        if (read < 2)
            return -1;
        int size = read / 2;
        ShortBuffer buff = ByteBuffer.wrap(data, 0, read).order(ByteOrder.nativeOrder()).asShortBuffer();
        if(!hasSignal(buff, size))
            return -1;
        return autocorrelation(buff, size, mSampleRate);
    }

    private static int autocorrelation(ShortBuffer buff, int size, int sampleRate) {
        int bestTone = -1;
        long bestInvCorrelation = Long.MAX_VALUE;
        for(int tone = 0; NumHalftones != tone; ++tone) {
            long invCorrelation = autocorrelationFreq(buff, size, sTones[tone], sampleRate);
            if(invCorrelation > bestInvCorrelation)
                continue;
            bestInvCorrelation = invCorrelation;
            bestTone = tone;
        }
        return bestTone;
    }

    // result medium difference
    private static long autocorrelationFreq(ShortBuffer buff, int size, float freq, int sampleRate) {
        long accumDist = 0;  // accumulated distances
        int sampleIndex = 0; // index of sample to analyze

        int samplesPerPeriod = (int) (sampleRate / freq);
        int correlatingSampleIndex = sampleIndex + samplesPerPeriod; // index of sample one period ahead

        while (correlatingSampleIndex < size) {
            // distance (0=equal .. 65536=totally different) between correlated samples
            accumDist += Math.abs((int)buff.get(sampleIndex++) - (int)buff.get(correlatingSampleIndex++));
        }
        return accumDist;
    }

}
