package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import static com.rks.musicx.misc.utils.Constants.ALBUM_ARTIST;
import static com.rks.musicx.misc.utils.Constants.ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.ALBUM_NAME;
import static com.rks.musicx.misc.utils.Constants.ALBUM_TRACK_COUNT;
import static com.rks.musicx.misc.utils.Constants.ALBUM_YEAR;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class AlbumFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>> {


    private final int trackLoader = 1;
    private ImageView artworkView;
    private Album mAlbum;
    private SongListAdapter songListAdapter;
    private FastScrollRecyclerView rv;
    private Toolbar toolbar;
    private Helper helper;
    private FloatingActionButton shuffle;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shuffle_fab:
                    ((MainActivity) getActivity()).onShuffleRequested(songListAdapter.getSnapshot(), true);
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                helper.showMenu(trackLoader, this, AlbumFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                break;
        }
    };

    public static AlbumFragment newInstance(Album album) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putLong(ALBUM_ID, album.getId());
        args.putString(ALBUM_NAME, album.getAlbumName());
        args.putString(ALBUM_ARTIST, album.getArtistName());
        args.putInt(ALBUM_YEAR, album.getYear());
        args.putInt(ALBUM_TRACK_COUNT, album.getTrackCount());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            long id = args.getLong(ALBUM_ID);
            String title = args.getString(ALBUM_NAME);
            String artist = args.getString(ALBUM_ARTIST);
            int year = args.getInt(ALBUM_YEAR);
            int trackCount = args.getInt(ALBUM_TRACK_COUNT);
            mAlbum = new Album();
            mAlbum.setAlbumName(title);
            mAlbum.setId(id);
            mAlbum.setArtistName(artist);
            mAlbum.setYear(year);
            mAlbum.setTrackCount(trackCount);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);
        ui(rootView);
        funtion();
        return rootView;
    }

    private void ui(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.songrv);
        artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        shuffle = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    }


    private void funtion() {

        CustomLayoutManager c = new CustomLayoutManager(getContext());
        c.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(c);
        songListAdapter = new SongListAdapter(getContext());
        songListAdapter.setLayoutId(R.layout.detail_list);
        songListAdapter.setOnItemClickListener(mOnClick);
        rv.setAdapter(songListAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
        rv.setHasFixedSize(true);
        shuffle.setOnClickListener(mOnClickListener);
        toolbar.setTitle(mAlbum.getAlbumName());
        toolbar.setTitleTextColor(Color.WHITE);
        AlbumCover();
        loadTrak();
        helper = new Helper(getContext());
        int colorAccent = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        rv.setPopupBgColor(colorAccent);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        } else {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        }
    }


    private void AlbumCover() {
        if (getActivity() == null) {
            return;
        }
        ArtworkUtils.ArtworkLoaderPalette(getContext(), mAlbum.getAlbumName(), mAlbum.getId(), artworkView, new palette() {
            @Override
            public void palettework(Palette palette) {
                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                toolbar.setBackgroundColor(colors[0]);
                if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                } else {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                }
            }
        });
    }

    /*
    reload track
     */
    public void reload() {
        getLoaderManager().restartLoader(trackLoader, null, this);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        TrackLoader tracksloader = new TrackLoader(getActivity());
        if (id == trackLoader) {
            tracksloader.filteralbumsong(MediaStore.Audio.Media.ALBUM_ID + "=?", new String[]{String.valueOf(mAlbum.getId())});
            tracksloader.setSortOrder(MediaStore.Audio.Media.TRACK);
            return tracksloader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        songListAdapter.notifyDataSetChanged();
    }

    private void loadTrak() {
        getLoaderManager().initLoader(trackLoader, null, this);
    }


}
