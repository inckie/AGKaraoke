package com.damn.karaoke.glass;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.damn.karaoke.core.controller.KaraokeController;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.File;

public class SingActivity extends Activity {

    public static final String EXTRA_SONG = "EXTRA_SONG";

    @SuppressWarnings("SpellCheckingInspection")
    private KaraokeController mKaraokeKonroller;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sing);
        mGestureDetector = createGestureDetector(this);
        mKaraokeKonroller = new KaraokeController();
        mKaraokeKonroller.init(findViewById(R.id.root), R.id.lyrics, R.id.tone_render);

        String songFile = getIntent().getStringExtra(EXTRA_SONG);
        if(null == songFile || !mKaraokeKonroller.load(new File(songFile)))
            finish();
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    // todo: pause/resume
                    return true;
                } else if (gesture == Gesture.SWIPE_DOWN) {
                    finish();
                    return true;
                }

                return false;
            }
        });
        return gestureDetector;
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKaraokeKonroller.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKaraokeKonroller.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mKaraokeKonroller.onStop();
    }

}
