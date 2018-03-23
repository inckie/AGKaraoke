package com.damn.karaoke.core.model;

/**
 * Created by ink on 2018-03-23.
 */
public class Tone {
    public int tone;
    public long duration = 0;
    public long from; // from the line start

    public Tone(int tone, long duration, long from) {
        this.tone = tone;
        this.duration = duration;
        this.from = from;
    }
}
