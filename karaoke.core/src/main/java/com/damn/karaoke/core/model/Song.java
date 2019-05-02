package com.damn.karaoke.core.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ink on 2018-01-09.
 */

public class Song {

    public String file;
    public String title = "";
    public String artist = "";
    public float BPM;
    public String cover;

    public File fullPath;
    private File audioFile;

    public double gap;
    public List<Line> lines = new ArrayList<>();

    //todo: use cache there
    private Bitmap coverImage;
    private boolean coverImageChecked;

    private static final String[] sCovers = {
            "cover.jpg",
            "cover.png"
    };

    @Nullable
    public Bitmap getCoverImage() {
        if(null != coverImage || coverImageChecked)
            return coverImage;
        coverImageChecked = true;
        if(null == fullPath)
            return null;
        File songDir = fullPath.getParentFile();
        if (null != cover)
            coverImage = BitmapFactory.decodeFile(new File(songDir, cover).toString());
        else {
            for(String c : sCovers) {
                File cover = new File(songDir, c);
                if (cover.exists()) {
                    coverImage = BitmapFactory.decodeFile(cover.toString());
                    if (null != coverImage)
                        break;
                }
            }
        }
        return coverImage;
    }

    @Nullable
    public File getAudioFile(){
        if(null == fullPath)
            return null;
        if(null == audioFile) {
            File songDir = fullPath.getParentFile();
            audioFile = new File(songDir, file);
        }
        return audioFile.exists() ? audioFile : null;
    }

    public boolean isValid() {
        return null != getAudioFile();
    }

    public static class Syllable {
        public double from;
        public double to;
        public String text;
        public int tone;
    }

    public static class Line {
        public double from;
        public double to;
        public List<Syllable> syllables = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for(Syllable s : syllables)
                b.append(s.text);
            return b.toString();
        }

        public boolean isIn(double position) {
            return from <= position && position <= to;
        }

    }
}
