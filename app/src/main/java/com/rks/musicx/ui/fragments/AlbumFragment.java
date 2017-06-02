package com.rks.musicx.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.data.network.AlbumArtwork;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
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

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AlbumFragment extends BaseLoaderFragment {

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
                    Extras.getInstance().saveSeekServices(0);
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                Extras.getInstance().saveSeekServices(0);
                break;
            case R.id.menu_button:
                helper.showMenu(false, trackloader, this, AlbumFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
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
    protected int setLayout() {
        return R.layout.fragment_album;
    }

    @Override
    protected void ui(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.songrv);
        artworkView = (ImageView) rootView.findViewById(R.id.album_artwork);
        shuffle = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    }

    @Override
    protected void funtion() {
        rv.setHasFixedSize(true);
        shuffle.setOnClickListener(mOnClickListener);
        toolbar.setTitle(mAlbum.getAlbumName());
        toolbar.setTitleTextColor(Color.WHITE);
        AlbumCover();
        helper = new Helper(getContext());
        int colorAccent = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        rv.setPopupBgColor(colorAccent);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        } else {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        }
        background();
    }

    @Override
    protected String filter() {
        return MediaStore.Audio.Media.ALBUM_ID + "=?";
    }

    @Override
    protected String[] argument() {
        return new String[]{String.valueOf(mAlbum.getId())};
    }

    @Override
    protected String sortOder() {
        return MediaStore.Audio.Media.DATE_ADDED;
    }

    @Override
    protected void background() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (getActivity() != null){
                    songListAdapter = new SongListAdapter(getContext());
                    songListAdapter.setLayoutId(R.layout.detail_list);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (getActivity() == null){
                    return;
                }
                CustomLayoutManager c = new CustomLayoutManager(getContext());
                c.setSmoothScrollbarEnabled(true);
                getLoaderManager().initLoader(trackloader, null, AlbumFragment.this);
                rv.setLayoutManager(c);
                rv.setAdapter(songListAdapter);
                rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
                songListAdapter.setOnItemClickListener(mOnClick);
            }
        }.execute();
    }

    @Override
    protected boolean isTrack() {
        return true;
    }

    @Override
    protected boolean isRecentPlayed() {
        return false;
    }

    @Override
    protected boolean isFav() {
        return false;
    }

    @Override
    protected int getLimit() {
        return 0;
    }

    @Override
    public void load() {
        if (getActivity() == null){
            return;
        }
        getLoaderManager().restartLoader(trackloader, null, this);
    }


    private void AlbumCover() {
        if (!Extras.getInstance().saveData()){
            AlbumArtwork albumArtwork = new AlbumArtwork(getContext(), mAlbum.getArtistName(), mAlbum.getArtistName());
            albumArtwork.execute();
        }
        ArtworkUtils.ArtworkLoader(getContext(), mAlbum.getAlbumName(), null, mAlbum.getId(), new palette() {
            @Override
            public void palettework(Palette palette) {
                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                toolbar.setBackgroundColor(colors[0]);
                if (getActivity() == null || getActivity().getWindow() == null) {
                    return;
                }
                if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                } else {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                }
            }
        }, new bitmap() {
            @Override
            public void bitmapwork(Bitmap bitmap) {
                artworkView.setImageBitmap(bitmap);
            }

            @Override
            public void bitmapfailed(Bitmap bitmap) {
                artworkView.setImageBitmap(bitmap);
            }
        });
    }


    @Override
    public void setAdapater(List<Song> data) {
        if (data == null){
            return;
        }
        songListAdapter.addDataList(data);
    }

    @Override
    public void notifyChanges() {
        songListAdapter.notifyDataSetChanged();
    }
}
