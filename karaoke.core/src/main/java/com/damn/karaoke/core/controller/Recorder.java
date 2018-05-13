package com.damn.karaoke.core.controller;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.damn.karaoke.core.controller.processing.GoertzelToneDetector;
import com.damn.karaoke.core.controller.processing.IToneDetector;

/**
 * Created by ink on 2017-06-12.
 */

public class Recorder {

    public static final int SAMPLE_RATE_IN_HZ = 16000;

    public static final int SENSITIVITY_NORMAL = 750;
    public static final int SENSITIVITY_AMBIENT = 80;

    private final Handler mHandler;
    private volatile AudioRecordingThread mThread;

    public Recorder(final KaraokeController controller) {
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                controller.toneChanged(msg.what, msg.arg1);
            }
        };
    }

    public boolean start() {
        if (null != mThread)
            return true; // running
        try {

            mThread = new AudioRecordingThread();
            mThread.start();
        } catch (Exception e) {
            mThread = null;
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        AudioRecordingThread thread = this.mThread;
        if (null == thread)
            return;
        try {
            thread.stopRecording();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return null != mThread;
    }

    private class AudioRecordingThread extends Thread {

        private byte[] mAudioBuffer;

        private volatile boolean mIsRecording = true;

        AudioRecordingThread() throws Exception {

            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize <= 0)
                throw new Exception("Unsupported record profile");
            mAudioBuffer = new byte[2 * bufferSize];
        }

        @Override
        public void run() {
            AudioRecord record = null;
            NoiseSuppressor noise = null;
            AcousticEchoCanceler echo = null;
            try {
                do {
                    // should be in constructor
                    record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            SAMPLE_RATE_IN_HZ,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mAudioBuffer.length);

                    if (record.getRecordingState() != AudioRecord.STATE_INITIALIZED)
                        break;

                    if (NoiseSuppressor.isAvailable()) {
                        noise = NoiseSuppressor.create(record.getAudioSessionId());
                        if (null != noise && !noise.getEnabled())
                            noise.setEnabled(true);
                    }

                    if (AcousticEchoCanceler.isAvailable()) {
                        echo = AcousticEchoCanceler.create(record.getAudioSessionId());
                        if (null != echo && !echo.getEnabled())
                            echo.setEnabled(true);
                    }

                    IToneDetector detector = new GoertzelToneDetector(SAMPLE_RATE_IN_HZ, SENSITIVITY_NORMAL, 10);
                    //IToneDetector detector = new AutoCorrelationToneDetector(SAMPLE_RATE_IN_HZ, SENSITIVITY_NORMAL, 10);
                    record.startRecording();

                    int currentTone = -1;
                    while (mIsRecording) {
                        int read = record.read(mAudioBuffer, 0, mAudioBuffer.length);
                        if ((read == AudioRecord.ERROR_INVALID_OPERATION) ||
                                (read == AudioRecord.ERROR_BAD_VALUE) ||
                                (read <= 0)) {
                            continue;
                        }
                        int tone = detector.analyze(mAudioBuffer, read);
                        if (currentTone != tone) {
                            currentTone = tone;
                            mHandler.obtainMessage(currentTone, 1000 * read / 2 / SAMPLE_RATE_IN_HZ, 0).sendToTarget();
                        }
                    }

                    record.stop();

                } while (false);

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (null != record)
                    record.release();
                if (null != echo)
                    echo.release();
                if (null != noise)
                    noise.release();
            }
            mThread = null;
        }

        public void stopRecording() throws InterruptedException {
            mIsRecording = false;
            join();
        }
    }

}
