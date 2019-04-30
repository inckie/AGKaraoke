package com.damn.karaoke.core.controller.processing;

public class GoertzelToneDetector extends BaseToneDetector{

    private final float[] sCos = new float[NumHalftones];
    private final float[] sWnk = new float[NumHalftones];

    private static final float sNorm = 1.0f / 16356.0f;

    public GoertzelToneDetector(int sampleRate, int threshold, int peak_count) {
        super(threshold, peak_count);
        for (int tone = 0; NumHalftones != tone; ++tone) {
            double omega = 2 * Math.PI * sTones[tone] / sampleRate;
            sCos[tone] = 2 * (float) (Math.cos(omega));
            sWnk[tone] = (float) Math.exp(-omega);
        }
    }

    @Override
    public int analyze(short[] data, int read) {
        if (read < 2)
            return -1;

        if(!hasSignal(data, read))
            return -1;

        int bestTone = 0;
        float maxPower = goertzel(data, read, bestTone);
        for (int tone = 1; NumHalftones != tone; ++tone) {
            float power = goertzel(data, read, tone);
            if (power < maxPower)
                continue;
            maxPower = power;
            bestTone = tone;
        }
        return bestTone;
    }

    private float goertzel(short[] x, int size, int tone) {
        float skn0 = 0;
        float skn1 = 0;
        float skn2 = 0;

        for (int i = 0; size != i; ++i) {
            skn2 = skn1;
            skn1 = skn0;
            skn0 = sCos[tone] * skn1 - skn2 + x[i] * sNorm;
        }
        return Math.abs(skn0 - sWnk[tone] * skn1);
    }
}
