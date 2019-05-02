package com.damn.karaoke.core.model;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class SongsDB {

    private final List<Song> mSongs = new LinkedList<Song>();

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

    private final HashSet<IListener> mListeners = new HashSet<IListener>();

    public void subscribe(IListener listener) {
        if(!mListeners.add(listener))
            if(null != mScanTask)
                listener.onScanStarted();
    }

    public void unsubscribe(IListener listener) {
        mListeners.remove(listener);
    }

    private void notifyScanStarted() {
        // make a copy before iterating
        for (IListener l : new HashSet<IListener>(mListeners))
            l.onScanStarted();
    }

    private void notifyUpdated() {
        // make a copy before iterating
        for (IListener l : new HashSet<IListener>(mListeners))
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

        private static final FileFilter mFileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".txt");
            }
        };

        private static final FileFilter mDirFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };

        private final WeakReference<SongsDB> mListener;

        public ScanTask(@NotNull SongsDB songs) {
            mListener = new WeakReference<SongsDB>(songs);
        }

        @Override
        protected List<Song> doInBackground(File... roots) {
            List<Song> res = new LinkedList<Song>();
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
                            List<String> lines = FileUtils.readLines(songFile, Charset.forName("UTF-8"));
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
            Collections.sort(res, new Comparator<Song>() {
                @Override
                public int compare(Song a, Song b) {
                    return String.CASE_INSENSITIVE_ORDER.compare(a.title, b.title);
                }
            });
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
    };

}
