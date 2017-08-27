package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

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

public class RecentlyAddedFragment extends BaseLoaderFragment implements SearchView.OnQueryTextListener {

    private FastScrollRecyclerView rv;
    private Helper helper;
    private String limit;
    private boolean isgridView;
    private Toolbar toolbar;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {

            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    Song song = songListAdapter.getItem(position);
                    helper.showMenu(false, new RefreshData() {
                        @Override
                        public void refresh() {
                            getLoaderManager().restartLoader(trackloader, null, RecentlyAddedFragment.this);
                        }

                        @Override
                        public Fragment currentFrag() {
                            return RecentlyAddedFragment.this;
                        }
                    }, ((MainActivity) getActivity()), view, getContext(), song);
                    break;
            }
        }
    };

    public RecentlyAddedFragment newInstance(String limit, boolean isgridView) {
        Bundle bundle = new Bundle();
        bundle.putString("Limit", limit);
        bundle.putBoolean("IsgridView", isgridView);
        RecentlyAddedFragment fragment = new RecentlyAddedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String limit = bundle.getString("Limit");
            boolean isgridview = bundle.getBoolean("IsgridView");
            setLimit(limit);
            setIsgridView(isgridview);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }

    /*
     * Load track.
     */
    private void loadTrak() {
        getLoaderManager().initLoader(trackloader, null, this);
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_recentadded;
    }

    @Override
    protected void ui(View view) {
        rv = (FastScrollRecyclerView) view.findViewById(R.id.recentrv);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    }

    @Override
    protected void funtion() {
        helper = new Helper(getContext());
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        isgridView = true;
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setPopupBgColor(colorAccent);
        rv.setHasFixedSize(true);
        background();
        toolbar.showOverflowMenu();
        songList = new ArrayList<>();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null){
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected String filter() {
        return MediaStore.Audio.Media.DATE_ADDED;
    }

    @Override
    protected String[] argument() {
        return null;
    }

    @Override
    protected String sortOder() {
        return String.format(limit, MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
    }

    @Override
    protected void background() {
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        if (isgridView()) {
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
            songListAdapter.setLayoutId(R.layout.song_list);
            loadTrak();
            toolbar.setVisibility(View.VISIBLE);
        } else {
            customLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv.setLayoutManager(customLayoutManager);
            rv.setNestedScrollingEnabled(false);
            songListAdapter.setLayoutId(R.layout.recent_list);
            rv.setVerticalScrollBarEnabled(false);
            rv.setHorizontalScrollBarEnabled(false);
            rv.setScrollBarSize(0);
            rv.setAutoHideEnabled(true);
            loadTrak();
            toolbar.setVisibility(View.GONE);
        }
        rv.setAdapter(songListAdapter);
        songListAdapter.setOnItemClickListener(onClick);
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

    public void setLimit(String limit) {
        this.limit = limit;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        menu.findItem(R.id.grid_view).setVisible(false);
        menu.findItem(R.id.default_folder).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        if (getActivity() == null){
            return false;
        }
        switch (item.getItemId()) {
            case R.id.shuffle_all:
                if (songListAdapter.getSnapshot().size() > 0) {
                    ((MainActivity) getActivity()).onShuffleRequested(songListAdapter.getSnapshot(), true);
                }
                break;
            case R.id.menu_sort_by_az:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                load();
                break;
            case R.id.menu_sort_by_year:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                load();
                break;
            case R.id.menu_sort_by_artist:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                load();
                break;
            case R.id.menu_sort_by_album:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                load();
                break;
            case R.id.menu_sort_by_duration:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                load();
                break;
            case R.id.menu_sort_by_date:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                load();
                break;
            case R.id.menu_refresh:
                load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        reload track
        */
    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, this);
    }

    public String getsLimit() {
        return limit;
    }

    public boolean isgridView() {
        return isgridView;
    }

    public void setIsgridView(boolean isgridView) {
        this.isgridView = isgridView;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        final List<Song> filterlist = helper.filter(songList, newText);
        if (filterlist.size() > 0) {
            songListAdapter.setFilter(filterlist);
            return true;
        } else {
            Toast.makeText(getContext(), "No data found...", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
