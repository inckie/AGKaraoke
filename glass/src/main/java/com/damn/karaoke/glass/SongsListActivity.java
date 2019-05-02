package com.damn.karaoke.glass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.karaoke.core.model.Song;
import com.damn.karaoke.core.model.SongsDB;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Activity} with list of songs
 */
public class SongsListActivity extends Activity implements SongsDB.IListener {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;

    private SongsDB mSongs;

    private List<Song> mSongList = new ArrayList<Song>(); // keep a copy

    private SongsAdapter mAdapter = new SongsAdapter();

    private class SongsAdapter
            extends CardScrollAdapter
            implements AdapterView.OnItemClickListener {

        private View mEmptyView;

        // to avoid conflict with isEmpty
        private boolean empty() {
            return mSongList.isEmpty();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return empty() ? 1 : 2;
        }

        @Override
        public int getCount() {
            return empty() ? 1 : mSongList.size();
        }

        @Override
        public Object getItem(int position) {
            return empty() ? getEmptyView() : mSongList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // Never do this in production quality code.
            // I have checked for many strings, and change of conflict is close to zero
            return empty() ? -1 : mSongList.get(position).fullPath.hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (empty())
                return getEmptyView();

            if (null == convertView || convertView == mEmptyView)
                convertView = getLayoutInflater().inflate(R.layout.view_song_card, null);

            // We do not use CardBuilder due to the need of keeping CardBuilder instances for every song
            Song song = mSongList.get(position);
            ImageView cover = (ImageView) convertView.findViewById(R.id.img_cover);
            cover.setImageBitmap(song.getCoverImage());
            TextView title = (TextView) convertView.findViewById(R.id.lbl_title);
            title.setText(song.title);
            TextView artist = (TextView) convertView.findViewById(R.id.lbl_artist);
            artist.setText(song.artist);
            return convertView;
        }

        private View getEmptyView() {
            if (mEmptyView == null) {
                mEmptyView = new CardBuilder(SongsListActivity.this, CardBuilder.Layout.TEXT)
                        .setText(R.string.msg_no_songs)
                        .getView();
            }
            return mEmptyView;
        }

        @Override
        public int getPosition(Object item) {
            if (item instanceof Song)
                return mSongList.indexOf(item);
            return AdapterView.INVALID_POSITION;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (empty())
                return;
            Song song = mSongList.get(position);
            Intent intent = new Intent(SongsListActivity.this, SingActivity.class);
            intent.putExtra(SingActivity.EXTRA_SONG, song.fullPath.toString());
            startActivity(intent);
        }
    }

    private Slider.Indeterminate mSlider;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        mCardScroller.setOnItemClickListener(mAdapter);
        setContentView(mCardScroller);
        mSlider = Slider.from(mCardScroller).startIndeterminate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSongs.subscribe(this);
        mSongs.scan();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mSongs.unsubscribe(this);
        mCardScroller.deactivate();
        mSlider.hide();
        super.onPause();
    }

    @Override
    public void onScanStarted() {
        mSlider.show();
    }

    @Override
    public void onListUpdated() {
        mSlider.hide();
        mSongList = new ArrayList<Song>(mSongs.getSongs());
        mAdapter.notifyDataSetChanged();
    }
}
