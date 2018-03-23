package com.damn.karaoke.core.controller.processing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class GoertzelToneDetector extends BaseToneDetector{

    private final float[] sCos = new float[NumHalftones];
    private final float[] sWnk = new float[NumHalftones];

    public GoertzelToneDetector(int sampleRate, float time_sec, int threshold, int peak_count) {
        super(sampleRate, time_sec, threshold, peak_count);
        for (int tone = 0; NumHalftones != tone; ++tone) {
            double omega = 2 * Math.PI * sTones[tone] / sampleRate;
            sCos[tone] = 2 * (float) (Math.cos(omega));
            sWnk[tone] = (float) Math.exp(-omega);
        }
    }

    @Override
    public int analyze(byte[] data, int read) {
        if (read < 2)
            return -1;

        int size = read / 2;
        ShortBuffer buff = ByteBuffer.wrap(data, 0, read).order(ByteOrder.nativeOrder()).asShortBuffer();

        if(!hasSignal(buff, size))
            return -1;

        int bestTone = -1;
        float maxPower = Float.MIN_VALUE;
        for (int tone = 0; NumHalftones != tone; ++tone) {
            float power = goertzel(buff, size, tone);
            if (power < maxPower)
                continue;
            maxPower = power;
            bestTone = tone;
        }
        return bestTone;
    }

    private float goertzel(ShortBuffer x, int size, int tone) {
        float skn0 = 0;
        float skn1 = 0;
        float skn2 = 0;

        for (int i = 0; size != i; ++i) {
            skn2 = skn1;
            skn1 = skn0;
            skn0 = sCos[tone] * skn1 - skn2 + x.get(i) / 16356.0f;
        }
        return Math.abs(skn0 - sWnk[tone] * skn1);
    }
}
