package com.damn.karaoke.core.controller.processing;

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
    public int analyze(short[] data, int read) {
        if (read < 2)
            return -1;
        if(!hasSignal(data, read))
            return -1;
        return autocorrelation(data, read, mSampleRate);
    }

    private static int autocorrelation(short[] buff, int size, int sampleRate) {
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
    private static long autocorrelationFreq(short[] buff, int size, float freq, int sampleRate) {
        long accumDist = 0;  // accumulated distances
        int sampleIndex = 0; // index of sample to analyze

        int samplesPerPeriod = (int) (sampleRate / freq);
        int correlatingSampleIndex = sampleIndex + samplesPerPeriod; // index of sample one period ahead

        while (correlatingSampleIndex < size) {
            // distance (0=equal .. 65536=totally different) between correlated samples
            accumDist += Math.abs((int)buff[sampleIndex++] - (int)buff[correlatingSampleIndex++]);
        }
        return accumDist;
    }

}
