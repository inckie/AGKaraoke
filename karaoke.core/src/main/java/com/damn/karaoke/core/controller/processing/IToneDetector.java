package com.damn.karaoke.core.controller.processing;

public interface IToneDetector {
    int analyze(byte[] data, int read);
}
