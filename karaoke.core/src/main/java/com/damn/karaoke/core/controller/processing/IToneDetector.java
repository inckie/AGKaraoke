package com.damn.karaoke.core.controller.processing;

public interface IToneDetector {
    int analyze(short[] data, int read);
}
