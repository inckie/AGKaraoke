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

    private List<Song> mSongList; // keep a copy

    // CardScroller.setEmptyView is not working, seems to be a bug in OS
    private View mEmptyView;

    private Slider.Indeterminate mSlider;

    private static class Holder {

        private final ImageView cover;
        private final TextView title;
        private final TextView artist;

        Holder(View convertView){
            cover = (ImageView) convertView.findViewById(R.id.img_cover);
            title = (TextView) convertView.findViewById(R.id.lbl_title);
            artist = (TextView) convertView.findViewById(R.id.lbl_artist);
        }

        void display(Song song) {
            cover.setImageBitmap(song.getCoverImage());
            title.setText(song.title);
            artist.setText(song.artist);
        }
    }

    private class SongsAdapter
            extends CardScrollAdapter
            implements AdapterView.OnItemClickListener {

        // to avoid conflict with isEmpty
        private boolean empty() {
            return null == mSongList || mSongList.isEmpty();
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
            return empty() ? mEmptyView : mSongList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (empty())
                return mEmptyView;

            if (null == convertView || convertView == mEmptyView){
                convertView = getLayoutInflater().inflate(R.layout.view_song_card, parent, false);
                convertView.setTag(new Holder(convertView));
            }

            Holder holder = (Holder) convertView.getTag();
            // We do not use CardBuilder due to the need of keeping CardBuilder instances for every song
            Song song = mSongList.get(position);
            holder.display(song);
            return convertView;
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

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSongs = new SongsDB(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        mEmptyView = new CardBuilder(SongsListActivity.this, CardBuilder.Layout.TEXT)
                .setText(R.string.msg_scanning)
                .getView();

        mCardScroller = new CardScrollView(this);

        SongsAdapter adapter = new SongsAdapter();
        mCardScroller.setAdapter(adapter);
        mCardScroller.setOnItemClickListener(adapter);

        setContentView(mCardScroller);

        mSlider = Slider.from(mCardScroller).startIndeterminate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSongs.subscribe(this);
        mCardScroller.activate();
        mSongs.scan();
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
        mSongList = new ArrayList<>(mSongs.getSongs());
        if(mSongList.isEmpty()) {
            mEmptyView = new CardBuilder(SongsListActivity.this, CardBuilder.Layout.TEXT)
                    .setText(R.string.msg_no_songs)
                    .getView();
        }
        mCardScroller.getAdapter().notifyDataSetChanged();
    }
}
