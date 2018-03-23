package com.damn.karaoke.phone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.damn.karaoke.core.model.Song;
import com.damn.karaoke.core.model.SongsDB;

public class SongsActivity
        extends AppCompatActivity
        implements SongsListFragment.OnListFragmentInteractionListener {

    private SongsDB mSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
        setContentView(R.layout.activity_songs);
    }

    @Override
    public void onListFragmentInteraction(Song item) {
        Intent intent = new Intent(this, SingActivity.class);
        intent.putExtra(SingActivity.EXTRA_SONG, item.fullPath.toString());
        startActivity(intent);
    }

    @Override
    public SongsDB getSongs() {
        return mSongs;
    }
}
