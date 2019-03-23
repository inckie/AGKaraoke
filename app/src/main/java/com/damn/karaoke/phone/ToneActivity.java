package com.damn.karaoke.phone;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.damn.karaoke.core.controller.Recorder;
import com.damn.karaoke.core.controller.processing.BaseToneDetector;

public class ToneActivity extends AppCompatActivity implements Recorder.IToneListener {

    private TextView mToneLabel;
    private Recorder mRecorder;

    private static final String[] Notes = new String[] {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tone);
        mToneLabel = findViewById(R.id.lbl_tone);
        mRecorder = new Recorder(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecorder.stop();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void toneChanged(int tone, long duration) {
        if(tone < 0)
            mToneLabel.setText("No signal");
        else if(BaseToneDetector.getTones().length > tone) {
            String t = Notes[tone % Notes.length] + (2 + tone / Notes.length);
            mToneLabel.setText(t + ": " + BaseToneDetector.getTones()[tone] + " Hz");
        }
        else
            mToneLabel.setText("Above range:" + tone);
    }
}
