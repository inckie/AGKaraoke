package com.damn.karaoke.phone;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.karaoke.core.model.Song;
import com.damn.karaoke.phone.SongsListFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

    // there can be other sort strategies and filters
    private static final Comparator<Song> mComparator = new Comparator<Song>() {
        @Override
        public int compare(Song a, Song b) {
            if (!a.artist.equalsIgnoreCase(b.artist))
                return a.artist.compareToIgnoreCase(b.artist);
            return a.title.compareToIgnoreCase(b.title);
        }
    };

    private List<Song> mValues;
    private final OnListFragmentInteractionListener mListener;

    public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener) {
        setData(items);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_song_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setItem(mValues.get(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<Song> songs) {
        mValues = new ArrayList<Song>(songs); // make a copy
        Collections.sort(mValues, mComparator);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mLblTitle;
        private final TextView mLblArtist;
        private final ImageView mCover;

        public Song mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLblTitle = view.findViewById(R.id.lbl_title);
            mLblArtist = view.findViewById(R.id.lbl_artist);
            mCover = view.findViewById(R.id.img_cover);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLblArtist.getText() + "'";
        }

        public void setItem(Song song) {
            mItem = song;
            mLblTitle.setText(song.title);
            mLblArtist.setText(song.artist);
            if (null != song.getCoverImage())
                mCover.setImageBitmap(song.getCoverImage());
            else
                mCover.setImageResource(R.drawable.ic_cover_empty);
        }
    }
}
