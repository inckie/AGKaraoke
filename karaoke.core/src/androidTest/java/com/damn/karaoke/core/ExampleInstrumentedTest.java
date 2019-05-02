package com.damn.karaoke.core;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.damn.karaoke.core.controller.processing.AutoCorrelationToneDetector;
import com.damn.karaoke.core.controller.processing.GoertzelToneDetector;
import com.damn.karaoke.core.controller.processing.GoertzelToneDetectorJNI;
import com.damn.karaoke.core.controller.processing.IToneDetector;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ShortBuffer;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void benchMark() throws Exception {
        int data_len = 16365;
        ShortBuffer buffer = ShortBuffer.allocate(data_len);
        for(int i = 0; data_len != i; ++i)
            buffer.put((short) (12000 * Math.sin(440 * i * 2 * Math.PI / data_len)));

        short[] data = buffer.array();

        bench(new GoertzelToneDetector(44100, 1, 1), data);
        bench(new GoertzelToneDetectorJNI(44100, 1, 1), data);
        bench(new AutoCorrelationToneDetector(44100, 1, 1), data);
    }

    private void bench(IToneDetector detector, short[] data) {
        Log.d("TEST", detector.getClass().getName() + " started");
        long l = System.currentTimeMillis();
        for(int i = 0; 16 != i; ++i)
            detector.analyze(data, data.length);
        long delta = System.currentTimeMillis() - l;
        Log.d("TEST", detector.getClass().getName() + ": " + delta);
    }
}
