package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.AlbumLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.network.ArtistArtwork;
import com.rks.musicx.data.network.Clients;
import com.rks.musicx.data.network.Services;
import com.rks.musicx.data.network.model.Artist__;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.GestureListerner;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.AlbumListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    private ArtistArtwork artistArtwork;

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                rv.smoothScrollToPosition(position);
                Extras.getInstance().saveSeekServices(0);
                break;
            case R.id.menu_button:
                helper.showMenu(false, trackloader, this, ArtistFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                break;
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClickAlbum = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ImageView Listartwork = (ImageView) view.findViewById(R.id.album_artwork);
                    fragTransition(albumListAdapter.getItem(position), Listartwork, "TransitionArtwork");
                    rv.smoothScrollToPosition(position);
                    break;
            }
        }
    };


    private View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.shuffle_fab:
                ((MainActivity) getActivity()).onShuffleRequested(songListAdapter.getSnapshot(), true);
                Extras.getInstance().saveSeekServices(0);
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
                albumLoader.setSortOrder(MediaStore.Audio.Albums.FIRST_YEAR);
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
        ViewCompat.setTransitionName(imageView, transition);
        Helper.setFragmentTransition(getActivity(), ArtistFragment.this, AlbumFragment.newInstance(album), new Pair<View, String>(imageView, "TransitionArtwork"));
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
        if (getActivity() == null){
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

        CustomLayoutManager c = new CustomLayoutManager(getContext());
        c.setSmoothScrollbarEnabled(true);
        rv.setAdapter(songListAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        rv.setLayoutManager(c);
        rv.setHasFixedSize(true);

        songListAdapter.setOnItemClickListener(mOnClick);
        albumListAdapter.setOnItemClickListener(mOnClickAlbum);

        loadTrak();
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
       if (!Extras.getInstance().saveData()){
           artistArtwork = new ArtistArtwork(getContext(), artist.getName());
           artistArtwork.execute();
       }
        if (ArtworkUtils.getArtistFileName(getContext(), artist.getName()).equalsIgnoreCase(artist.getName())){
            ArtworkUtils.ArtworkLoader(getContext(), 300, 600,ArtworkUtils.getArtistCoverPath(getContext(),artist.getName()).getAbsolutePath(), new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    if (getActivity() == null) {
                        return;
                    }
                    Helper.setColor(getActivity(), colors[0], toolbar);
                    Helper.animateViews(getContext(), toolbar, colors[0]);
                }
            }, artworkView);
        }

    }

    private void artistBio() {
        Clients last = new Clients(getContext(), Constants.lastFmUrl);
        Services lastFmServices = last.createService(Services.class);
        Call<com.rks.musicx.data.network.model.Artist> artistCall = lastFmServices.getartist(artist.getName());
        artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
            @Override
            public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
                com.rks.musicx.data.network.model.Artist getartist = response.body();
                if (response.isSuccessful() && getartist != null) {
                    final Artist__ artist1 = getartist.getArtist();
                    if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                        artistBio.setText(artist1.getBio().getSummary());
                    } else {
                        Log.d("haha", "bio load failed");
                        artistBio.setText("No bio found");
                    }
                } else {
                    Log.d("haha", "bio load failed");
                }
            }

            @Override
            public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
                Log.d("ArtistFrag", "error", t);
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
                extras.setAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_Z_A);
                reload();
                break;
            case R.id.album_no_songs:
                extras.setAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                reload();
                break;
            case R.id.album_year:
                extras.setAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR);
                reload();
                break;
            case R.id.last_year:
                extras.setAlbumSortOrder(SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR_LAST);
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        if (!Extras.getInstance().saveData()){
           if (artistArtwork != null){
               artistArtwork.cancel(true);
           }
        }
        super.onDestroy();
    }
}
