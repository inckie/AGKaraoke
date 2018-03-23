package com.damn.karaoke.core.model;

import java.util.List;

/**
 * Created by ink on 2018-01-09.
 */

public class SongParser {

    // https://thebrickyblog.wordpress.com/2011/01/27/ultrastar-txt-files-in-more-depth/
    public static Song parse(List<String> data) throws Exception {
        Song song = new Song();
        Song.Line lastLine = null;
        for (String line : data) {
            // tags
            if(line.startsWith("#")) {
                if (line.startsWith("#TITLE")) {
                    song.title = getStringValue(line, "#TITLE");
                } else if (line.startsWith("#ARTIST")) {
                    song.artist = getStringValue(line, "#ARTIST");
                } else if (line.startsWith("#EDITION")) {
                    //
                } else if (line.startsWith("#LANGUAGE")) {
                    //
                } else if (line.startsWith("#GENRE")) {
                    //
                } else if (line.startsWith("#MP3")) {
                    song.file = getStringValue(line, "#MP3");
                } else if (line.startsWith("#COVER")) {
                   song.cover = getStringValue(line, "#COVER");
                } else if (line.startsWith("#BPM")) {
                    song.BPM = getFloatValue(line, "#BPM") * 4;
                } else if (line.startsWith("#GAP")) {
                    song.gap = getFloatValue(line, "#GAP") / 1000.0;
                }
            }
            else {
                // lyrics
                if (line.startsWith(":") || line.startsWith("*") || line.startsWith("F")) {
                    Song.Syllable syllable = parseSyllable(song, line);
                    if(null == lastLine) {
                        lastLine = new Song.Line();
                        song.lines.add(lastLine);
                    }
                    lastLine.syllables.add(syllable);
                } else if (line.startsWith("-")) {
                    int[] timestamps = parseInts(line);
                    if(timestamps.length < 1)
                        throw new Exception("Bad line delimiter: " + line);
                    if(null == lastLine)
                        continue;
                    lastLine.to = getTimestamp(song, timestamps[0]);
                    lastLine = new Song.Line();
                    song.lines.add(lastLine);
                    if(timestamps.length > 1)
                        lastLine.from = getTimestamp(song, timestamps[1]);
                }
            }
        }

        // fix starts
        for(Song.Line line : song.lines)
            if(line.from == 0 && line.syllables.size() > 0)
                line.from = line.syllables.get(0).from;

        return song;
    }

    private static double getTimestamp(Song song, int beat) {
        return song.gap + beat * 60 / song.BPM;
    }

    private static int[] parseInts(String line) {
        String[] parts = line.substring(2).split(" ");
        int[] res = new int[parts.length];
        for (int p = 0; parts.length != p; ++p)
            res[p] = Integer.parseInt(parts[p]);
        return res;
    }

    private static Song.Syllable parseSyllable(Song song, String line) {
        Song.Syllable s = new Song.Syllable();
        String[] split = line.split(" ", 5);
        String type = split[0];
        int pos = Integer.valueOf(split[1]);
        int len = Integer.valueOf(split[2]);
        s.tone = Integer.valueOf(split[3]);
        if(split.length > 4)
            s.text = split[4];
        else
            s.text = "~";
        s.from = getTimestamp(song, pos);
        s.to = s.from + len * 60 / song.BPM;
        return s;
    }

    private static float getFloatValue(String line, String tag) {
        String valueStr = getStringValue(line, tag).replace(',', '.');
        return Float.valueOf(valueStr);
    }

    private static String getStringValue(String line, String tag) {
        return line.substring(tag.length() + 1);
    }
}
