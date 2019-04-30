package com.damn.karaoke.core.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.damn.karaoke.core.controller.processing.BaseToneDetector;
import com.damn.karaoke.core.model.Song;
import com.damn.karaoke.core.model.Tone;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by ink on 2018-01-09.
 */

public class ToneRender extends View {

    private Bitmap mCanvasBitmap;
    private Canvas mCanvas;

    private Matrix mMatrix = new Matrix();
    private Song.Line mLine;
    private TextPaint mTextPaint;
    private Paint mTonePaint = new Paint();
    private Paint mPassedPaint = new Paint();
    private Paint mVoicePaint = new Paint();
    private double mPosition;
    private List<Tone> mTones;
    private float mTextWidth;

    private boolean mHasBG;

    public ToneRender(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTonePaint.setARGB(255, 255, 0,0);
        mPassedPaint.setARGB(255, 0, 255,0);
        mVoicePaint.setARGB(255, 0, 0,255);
        mMatrix.reset();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(null == mLine || mLine.syllables.isEmpty())
            return;

        int width = getWidth();
        int height = getHeight();

        // Handle resize
        if (null != mCanvasBitmap)
            if (mCanvasBitmap.getWidth() != width ||
                mCanvasBitmap.getHeight() != height) {
                mCanvasBitmap.recycle();
                mCanvasBitmap = null;
                mCanvas = null;
            }

        if (mCanvasBitmap == null)
            mCanvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if (mCanvas == null) {
            mCanvas = new Canvas(mCanvasBitmap);
            mHasBG = false;
        }

        float startX = (width - mTextWidth) / 2;

        float minTime = (float) mLine.syllables.get(0).from;
        float length = (float) (mLine.syllables.get(mLine.syllables.size() - 1).to - minTime);

        float baseY = height * 0.9f;

        float norm = mTextWidth / length;

        float step = height * 0.8f / BaseToneDetector.NumHalftones;

        if (!mHasBG) {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (Song.Syllable s : mLine.syllables) {
                mCanvas.drawRect(
                        startX + norm * ((float) s.from - minTime),
                        baseY - s.tone * step,
                        startX + norm * ((float) s.to - minTime),
                        baseY + 10 - s.tone * step,
                        mTonePaint);
            }
            mHasBG = true;
        }

        for (Song.Syllable s : mLine.syllables) {
            if(s.from <= mPosition)
                mCanvas.drawRect(
                        startX + norm * ((float) s.from - minTime),
                        baseY - s.tone * step,
                        (float) (startX + norm * (Math.min(s.to, mPosition) - minTime)),
                        baseY + 10 - s.tone * step,
                        mPassedPaint);
        }

        for(Tone tone : mTones){
            mCanvas.drawRect(
                    startX + norm * tone.from / 1000.0f,
                    baseY - tone.tone * step,
                    startX + norm * (tone.from + tone.duration) / 1000.0f,
                    baseY + 10 - tone.tone * step,
                    mVoicePaint);
        }

        canvas.drawBitmap(mCanvasBitmap, mMatrix, null);
    }

    public void setTextField(TextView tv){
        mTextPaint = tv.getPaint();
    }

    public void setLine(Song.Line line){
        mLine = line;
        mHasBG = false;
        if(null != mTextPaint && null != mLine) {
            mTextWidth = mTextPaint.measureText(mLine.toString());
            invalidate();
        }
    }

    public void setPosition(double position) {
        if(null == mLine)
            return;
        mPosition = position;
        invalidate();
    }

    public void setTones(List<Tone> tones) {
        mTones = tones;
    }
}
