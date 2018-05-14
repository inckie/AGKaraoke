package com.damn.karaoke.core.controller;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;

import com.damn.karaoke.core.model.Song;
import com.damn.karaoke.core.model.SongParser;
import com.damn.karaoke.core.model.Tone;
import com.damn.karaoke.core.views.LyricsView;
import com.damn.karaoke.core.views.ToneRender;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class KaraokeController {

    private final MediaPlayer mPlayer;
    private final Recorder mRecorder;
    private final Handler mHandler;

    // realtime data
    private Song mSong;
    private Song.Line mCurrentLine;
    private long mLastUpdate;
    private long mLineStart;
    private final List<Tone> mTones = new ArrayList<Tone>();

    // views
    private LyricsView mLyrics;
    private ToneRender mToneRender;

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mUpdater, 20);
            updateUI();
        }
    };

    public KaraokeController() {
        mHandler = new Handler();
        mPlayer = new MediaPlayer();
        mRecorder = new Recorder(this);
    }

    public void init(View view, int lyrics, int tone_render) {
        mLyrics = (LyricsView) view.findViewById(lyrics);
        mToneRender = (ToneRender) view.findViewById(tone_render);
        mToneRender.setTextField(mLyrics);
        mToneRender.setTones(mTones); // risky a bit, but we all are in the UI thread
    }

    public boolean load(File file) {
        try {
            List<String> lines = FileUtils.readLines(file);
            mSong = SongParser.parse(lines);
            mSong.fullPath = file;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        File audioFile = mSong.getAudioFile();
        if(null == audioFile)
            return false;

        if (!loadAudio(audioFile))
            return false;

        mPlayer.start();
        mRecorder.start();
        return true;
    }

    private boolean loadAudio(@NotNull File file) {
        try {
            mPlayer.setDataSource(file.toString());
            mPlayer.prepare();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateUI() {
        double position = mPlayer.getCurrentPosition() / 1000.0;

        if (null != mCurrentLine && mCurrentLine.isIn(position)) {
            mLyrics.setPosition(position);
            mToneRender.setPosition(position);
        } else {
            // todo: not very optimal here: can keep an index,
            // todo: and also break when line is beyond position
            for (Song.Line line : mSong.lines) {
                if (line.isIn(position)) {
                    mCurrentLine = line;
                    mLyrics.setLine(mCurrentLine);
                    mLyrics.setPosition(position);
                    mToneRender.setLine(mCurrentLine);
                    mToneRender.setPosition(position);
                    mTones.clear();
                    mLastUpdate = System.currentTimeMillis();
                    mLineStart = mLastUpdate;
                    return;
                }
            }
            mCurrentLine = null;
            mLyrics.setLine(null);
            mToneRender.setLine(null);
            mLineStart = -1;
            mTones.clear();
        }
    }

    public void onPause() {
        if (mPlayer.isPlaying())
            mPlayer.pause();
        mHandler.removeCallbacks(mUpdater);
        mRecorder.stop();
    }

    public void onStop() {
        mPlayer.release();
    }

    public void onResume() {
        if (0 != mPlayer.getDuration()) {
            mPlayer.start();
            mRecorder.start();
        }
        mHandler.post(mUpdater);
    }

    public void toneChanged(int tone, long duration) {
        if (!mPlayer.isPlaying() || -1 == mLineStart)
            return;

        long timeMillis = System.currentTimeMillis();

        Tone last = mTones.isEmpty() ? null : mTones.get(mTones.size() - 1);
        if (null != last && last.tone == tone)
            last.duration += timeMillis - mLastUpdate;
        else if (-1 != tone)
            mTones.add(new Tone(tone, duration, timeMillis - mLineStart));

        mLastUpdate = timeMillis;
    }

}
