package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.AlbumLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.data.network.NetworkHelper;
import com.rks.musicx.data.network.Services;
import com.rks.musicx.interfaces.Action;
import com.rks.musicx.interfaces.ExtraCallback;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.GestureListerner;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.StartSnapHelper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.AlbumListAdapter;
import com.rks.musicx.ui.adapters.FolderAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

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

public class ArtistFragment extends BaseLoaderFragment {

    private final int albumLoaders = 4;
    public String selection;
    private FastScrollRecyclerView rv;
    private ImageView artworkView;
    private Artist artist;
    private Toolbar toolbar;
    private Helper helper;
    private FloatingActionButton fab;
    private FrameLayout bioView;
    private TextView artistBio;
    private RecyclerView albumrv;
    private AlbumListAdapter albumListAdapter;
    private String[] selectionArgs;
    private android.support.v7.view.ActionMode mActionMode;

    private BaseRecyclerViewAdapter.OnLongClickListener onLongClick = new BaseRecyclerViewAdapter.OnLongClickListener() {
        @Override
        public void onLongItemClick(int position) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(Helper.getActionCallback(((MainActivity) getActivity()), getContext(), new Action() {
                @Override
                public void clear() {
                    if (mActionMode != null) {
                        mActionMode.setTitle("");
                        mActionMode.finish();
                        mActionMode = null;
                    }
                    songListAdapter.exitMultiselectMode();
                }

                @Override
                public Fragment currentFrag() {
                    return ArtistFragment.this;
                }

                @Override
                public void refresh() {
                    getLoaderManager().restartLoader(trackloader, null, ArtistFragment.this);
                }
            }, true, new ExtraCallback() {
                @Override
                public SongListAdapter songlistAdapter() {
                    return songListAdapter;
                }

                @Override
                public FolderAdapter folderAdapter() {
                    return null;
                }
            }));
            Helper.setActionModeBackgroundColor(mActionMode, Config.primaryColor(getContext(), Helper.getATEKey(getContext())));
            if (position > 0) {
                if (mActionMode != null) {
                    mActionMode.setTitle(position + " selected");
                }
            } else {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = (position, view) -> {
        if (songListAdapter.isMultiselect()) {
            if (position > 0) {
                if (mActionMode != null) {
                    mActionMode.setTitle(position + " selected");
                }
            } else {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        } else {
            switch (view.getId()) {
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    Song song = songListAdapter.getItem(position);
                    helper.showMenu(false, new RefreshData() {
                        @Override
                        public void refresh() {
                            getLoaderManager().restartLoader(trackloader, null, ArtistFragment.this);
                        }

                        @Override
                        public Fragment currentFrag() {
                            return ArtistFragment.this;
                        }
                    }, ((MainActivity) getActivity()), view, getContext(), song);
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClickAlbum = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ImageView Listartwork = (ImageView) view.findViewById(R.id.album_artwork);
                    fragTransition(albumListAdapter.getItem(position), Listartwork, "TransitionArtwork" + position);
                    break;
            }
        }
    };


    private View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.shuffle_fab:
                ((MainActivity) getActivity()).onShuffleRequested(songListAdapter.getSnapshot(), true);
                break;
        }
    };

    private LoaderManager.LoaderCallbacks<List<com.rks.musicx.data.model.Album>> albumLoadersCallbacks = new LoaderManager.LoaderCallbacks<List<com.rks.musicx.data.model.Album>>() {

        @Override
        public Loader<List<com.rks.musicx.data.model.Album>> onCreateLoader(int id, Bundle args) {
            if (id == albumLoaders) {
                AlbumLoader albumLoader = new AlbumLoader(getContext());
                String[] selectargs = getSelectionArgs();
                String selection = getSelection();
                if (artist.getName() != null) {
                    selection = DatabaseUtilsCompat.concatenateWhere(selection, MediaStore.Audio.Albums.ARTIST + " = ?");
                    selectargs = DatabaseUtilsCompat.appendSelectionArgs(selectargs, new String[]{artist.getName()});
                }
                albumLoader.setSortOrder(Extras.getInstance().getArtistAlbumSort());
                albumLoader.filterartistsong(selection, selectargs);
                return albumLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<com.rks.musicx.data.model.Album>> loader, List<com.rks.musicx.data.model.Album> data) {
            if (data == null){
                return;
            }
            albumListAdapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<com.rks.musicx.data.model.Album>> loader) {
            loader.reset();
            albumListAdapter.notifyDataSetChanged();
        }

    };

    public static ArtistFragment newInstance(Artist artist) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ARTIST_ID, artist.getId());
        args.putString(Constants.ARTIST_NAME, artist.getName());
        args.putInt(Constants.ARTIST_ALBUM_COUNT, artist.getAlbumCount());
        args.putInt(Constants.ARTIST_TRACK_COUNT, artist.getTrackCount());
        fragment.setArguments(args);
        return fragment;
    }

    private void fragTransition(Album album, ImageView imageView, String transition) {
        Helper.setFragmentTransition(((MainActivity) getActivity()), ArtistFragment.this, AlbumFragment.newInstance(album), imageView, transition, "albumdetail");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {
            long id = args.getLong(Constants.ARTIST_ARTIST_ID);
            String name = args.getString(Constants.ARTIST_NAME);
            int albumCount = args.getInt(Constants.ARTIST_ALBUM_COUNT);
            int trackCount = args.getInt(Constants.ARTIST_TRACK_COUNT);
            artist = new Artist(id, name, albumCount, trackCount);
        }
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_artist;
    }

    @Override
    protected void ui(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.songrv);
        artworkView = (ImageView) rootView.findViewById(R.id.artist_artwork);
        fab = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        artistBio = (TextView) rootView.findViewById(R.id.artist_bio);
        bioView = (FrameLayout) rootView.findViewById(R.id.artistview_bio);
        albumrv = (RecyclerView) rootView.findViewById(R.id.artist_albumrv);
    }

    @Override
    protected void funtion() {
        background();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        fab.setOnClickListener(mOnClickListener);
        toolbar.setTitle(artist.getName());
        toolbar.setTitleTextColor(Color.WHITE);
        helper = new Helper(getContext());
        if (getActivity() == null || getActivity().getWindow() == null) {
            return;
        }
        Helper.setColor(getActivity(), colorAccent, toolbar);
        artistCover();
        bioView.setVisibility(View.GONE);
        artworkView.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {

            }

            @Override
            public void onTopToBottom() {

            }

            @Override
            public void doubleClick() {

            }

            @Override
            public void singleClick() {
                bioView.setVisibility(View.VISIBLE);
            }

            @Override
            public void otherFunction() {
            }
        });
        bioView.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {

            }

            @Override
            public void onTopToBottom() {

            }

            @Override
            public void doubleClick() {

            }

            @Override
            public void singleClick() {
                bioView.setVisibility(View.GONE);
            }

            @Override
            public void otherFunction() {

            }
        });
        artistBio.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {

            }

            @Override
            public void onTopToBottom() {

            }

            @Override
            public void doubleClick() {

            }

            @Override
            public void singleClick() {

            }

            @Override
            public void otherFunction() {
                bioView.setVisibility(View.GONE);
            }
        });
        artistBio();
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "1400");
        sequence.setConfig(config);
        sequence.addSequenceItem(artworkView, "Tap to view avaiable artist Bio", "GOT IT");
        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                materialShowcaseView.hide();
            }
        });
        toolbar.showOverflowMenu();
        Helper.rotateFab(fab);
    }

    @Override
    protected String filter() {
        return MediaStore.Audio.Media.ARTIST_ID + "=?";
    }

    @Override
    protected String[] argument() {
        return new String[]{String.valueOf(artist.getId())};
    }

    @Override
    protected String sortOder() {
        return MediaStore.Audio.Media.DATE_MODIFIED;
    }

    @Override
    protected void background() {
        songListAdapter.setLayoutId(R.layout.song_list);

        albumListAdapter = new AlbumListAdapter(getContext());
        albumListAdapter.setLayoutID(R.layout.recent_list);

        SnapHelper startSnapHelper = new StartSnapHelper();

        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        customLayoutManager.setOrientation(CustomLayoutManager.HORIZONTAL);

        albumrv.setAdapter(albumListAdapter);
        albumrv.setLayoutManager(customLayoutManager);
        albumrv.setHasFixedSize(true);
        albumrv.setNestedScrollingEnabled(false);
        albumrv.setVerticalScrollBarEnabled(false);
        albumrv.setHorizontalScrollBarEnabled(false);
        albumrv.setScrollBarSize(0);
        startSnapHelper.attachToRecyclerView(albumrv);

        CustomLayoutManager c = new CustomLayoutManager(getContext());
        c.setSmoothScrollbarEnabled(true);
        rv.setAdapter(songListAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        rv.setLayoutManager(c);
        rv.setHasFixedSize(true);

        songListAdapter.setOnItemClickListener(mOnClick);
        songListAdapter.setOnLongClickListener(onLongClick);
        albumListAdapter.setOnItemClickListener(mOnClickAlbum);

        loadTrak();
        if (((MainActivity) getActivity()) != null) {
            AppCompatActivity appCompatActivity = ((MainActivity) getActivity());
            if (appCompatActivity != null && appCompatActivity.getSupportActionBar() != null) {
                appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                appCompatActivity.setSupportActionBar(toolbar);
            }
        }
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
        reload();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }

    /*
    *
    * ArtistCover
    */
    private void artistCover() {
        if (!Extras.getInstance().saveData()) {
            NetworkHelper.downloadArtistArtwork(getContext(), artist.getName());
        }
        ArtworkUtils.ArtworkLoader(getContext(), 300, 600, helper.loadArtistImage(artist.getName()), new palette() {
            @Override
            public void palettework(Palette palette) {
                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                if (getActivity() == null) {
                    return;
                }
                Helper.setColor(getActivity(), colors[0], toolbar);
                // Helper.animateViews(getContext(), toolbar, colors[0]);
            }
        }, artworkView);
        artworkView.setTransitionName("TransitionArtworks");
    }

    private void artistBio() {
        AndroidNetworking.get(Constants.lastFmUrl + "?method=artist.getinfo&format=json&api_key=" + Services.lastFmApi)
                .addQueryParameter("artist", artist.getName())
                .setTag("ArtistArtwork")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            try {
                                JSONObject json = response.getJSONObject("artist");
                                JSONObject jsonObject = json.getJSONObject("bio");
                                if (jsonObject != null) {
                                    String bio = jsonObject.getString("summary");
                                    if (bio != null) {
                                        Log.e("kool", bio);
                                        artistBio.setText(bio);
                                    } else {
                                        artistBio.setText("No bio found");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.printStackTrace();
                    }
                });
    }

    /*
    * reload track,album
   */
    public void reload() {
        getLoaderManager().restartLoader(trackloader, null, this);
        getLoaderManager().restartLoader(albumLoaders, null, albumLoadersCallbacks);
    }

    private void loadTrak() {
        getLoaderManager().initLoader(trackloader, null, this);
        getLoaderManager().initLoader(albumLoaders, null, albumLoadersCallbacks);
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public String getSelection() {
        return selection;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artistdetail_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        switch (item.getItemId()) {
            case R.id.a_to_z:
                extras.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
                reload();
                break;
            case R.id.z_to_a:
                extras.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_Z_A);
                reload();
                break;
            case R.id.album_no_songs:
                extras.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                reload();
                break;
            case R.id.album_year:
                extras.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR);
                reload();
                break;
            case R.id.last_year:
                extras.setArtistAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR_LAST);
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
