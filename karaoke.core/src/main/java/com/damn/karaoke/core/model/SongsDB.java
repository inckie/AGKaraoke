package com.damn.karaoke.core.model;

import android.os.AsyncTask;
import android.util.Log;

import com.damn.karaoke.core.utility.FileReader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class SongsDB {

    private final List<Song> mSongs = new LinkedList<>();

    private File mRoot;

    private AsyncTask mScanTask;

    public SongsDB(File root) {
        mRoot = root;
    }

    public interface IListener {
        void onScanStarted();
        void onListUpdated();
    }

    public List<Song> getSongs() {
        return mSongs;
    }

    private final HashSet<IListener> mListeners = new HashSet<>();

    public void subscribe(IListener listener) {
        if(mListeners.add(listener))
            if(null != mScanTask)
                listener.onScanStarted();
    }

    public void unsubscribe(IListener listener) {
        mListeners.remove(listener);
    }

    private void notifyScanStarted() {
        // make a copy before iterating
        for (IListener l : new HashSet<>(mListeners))
            l.onScanStarted();
    }

    private void notifyUpdated() {
        // make a copy before iterating
        for (IListener l : new HashSet<>(mListeners))
            l.onListUpdated();
    }

    public void scan() {
        notifyScanStarted();
        if(null == mRoot || !mRoot.exists()) {
            notifyUpdated();
            return;
        }
        if(null == mScanTask) {
            mScanTask = new ScanTask(this).execute(mRoot);
        }
    }

    public void setRoot(File rootDir){
        AsyncTask scanTask = mScanTask;
        if(null != scanTask)
            scanTask.cancel(true);
        mScanTask = null;
        mSongs.clear();
        mRoot = rootDir;
        if(null != scanTask)
            scan();
    }

    private void songsUpdated(List<Song> songs) {
        mScanTask = null;
        mSongs.clear();
        mSongs.addAll(songs);
        notifyUpdated();
    }

    private static class ScanTask extends AsyncTask<File, Song, List<Song>> {

        private static final FileFilter mFileFilter = (pathname)
                -> pathname.isFile() && pathname.getName().endsWith(".txt");

        private static final FileFilter mDirFilter = File::isDirectory;

        private final WeakReference<SongsDB> mListener;

        public ScanTask(@NotNull SongsDB songs) {
            mListener = new WeakReference<>(songs);
        }

        @Override
        protected List<Song> doInBackground(File... roots) {
            List<Song> res = new LinkedList<>();
            for(File rootDir : roots) {
                if (isCancelled())
                    return Collections.emptyList();
                File[] listFiles = rootDir.listFiles(mDirFilter);
                if (null == listFiles)
                    continue;
                for (File songDir : listFiles) {
                    if (isCancelled())
                        return Collections.emptyList();
                    // I think there should be only one song per folder
                    File[] songFiles = songDir.listFiles(mFileFilter);
                    if (null == songFiles)
                        continue;
                    for (File songFile : songFiles) {
                        try {
                            List<String> lines = FileReader.readLines(songFile);
                            Song song = SongParser.parse(lines);
                            if (song == null)
                                continue;
                            song.fullPath = songFile;
                            if (!song.isValid())
                                continue;
                            res.add(song);
                            publishProgress(song);
                        } catch (Exception e) {
                            Log.e("SongsDB", "Failed to parse song file " + songFile.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Collections.sort(res, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.title, b.title));
            return res;
        }

        @Override
        protected void onProgressUpdate(Song... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<Song> songs) {
            super.onPostExecute(songs);
            if(isCancelled())
                return;
            SongsDB listener = mListener.get();
            if(null != listener)
                listener.songsUpdated(songs);
        }

        @Override
        protected void onCancelled(List<Song> songs) {
            super.onCancelled(songs);
            SongsDB listener = mListener.get();
            if(null != listener)
                listener.songsUpdated(songs);
        }
    }

}
