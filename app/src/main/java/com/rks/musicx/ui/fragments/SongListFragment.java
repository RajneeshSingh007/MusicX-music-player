package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
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
import com.rks.musicx.data.network.NetworkHelper;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.interfaces.Action;
import com.rks.musicx.interfaces.ExtraCallback;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.ItemOffsetDecoration;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.FolderAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import static com.rks.musicx.misc.utils.Constants.DOWNLOAD_ARTWORK;

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

public class SongListFragment extends BaseLoaderFragment implements SearchView.OnQueryTextListener {

    private FastScrollRecyclerView rv;
    private Helper helper;
    private SearchView searchView;
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
                    return SongListFragment.this;
                }

                @Override
                public void refresh() {
                    getLoaderManager().restartLoader(trackloader, null, SongListFragment.this);
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

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {

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
                if (songListAdapter.getLayout() == R.layout.song_list) {
                    switch (view.getId()) {
                        case R.id.item_view:
                            ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                            Extras.getInstance().saveSeekServices(0);
                            break;
                        case R.id.menu_button:
                            Song song = songListAdapter.getItem(position);
                            helper.showMenu(false, new RefreshData() {
                                @Override
                                public void refresh() {
                                    getLoaderManager().restartLoader(trackloader, null, SongListFragment.this);
                                }

                                @Override
                                public Fragment currentFrag() {
                                    return SongListFragment.this;
                                }
                            }, ((MainActivity) getActivity()), view, getContext(), song);
                            break;

                    }
                } else if (songListAdapter.getLayout() == R.layout.item_grid_view) {
                    switch (view.getId()) {
                        case R.id.album_artwork:
                        case R.id.album_info:
                            ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                            Extras.getInstance().saveSeekServices(0);
                            break;
                        case R.id.menu_button:
                            Song song = songListAdapter.getItem(position);
                            helper.showMenu(false, new RefreshData() {
                                @Override
                                public void refresh() {
                                    getLoaderManager().restartLoader(trackloader, null, SongListFragment.this);
                                }

                                @Override
                                public Fragment currentFrag() {
                                    return SongListFragment.this;
                                }
                            }, ((MainActivity) getActivity()), view, getContext(), song);
                            break;

                    }
                }
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        if (Extras.getInstance().songView()) {
            menu.findItem(R.id.grid_view).setVisible(true);
        } else {
            menu.findItem(R.id.grid_view).setVisible(false);
        }
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
            case R.id.bytwo:
                Extras.getInstance().setSongGrid(2);
                loadGridView();
                load();
                break;
            case R.id.bythree:
                Extras.getInstance().setSongGrid(3);
                loadGridView();
                load();
                break;
            case R.id.byfour:
                Extras.getInstance().setSongGrid(4);
                loadGridView();
                load();
                break;
            case R.id.menu_refresh:
                load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
        downloadArtwork();
    }

    private void loadTrak() {
        getLoaderManager().initLoader(trackloader, null, this);
    }

    @Override
    protected int setLayout() {
        return R.layout.common_rv;
    }

    @Override
    protected void ui(View view) {
        rv = (FastScrollRecyclerView) view.findViewById(R.id.commonrv);
    }

    @Override
    protected void funtion() {
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        setHasOptionsMenu(true);
        rv.setPopupBgColor(colorAccent);
        rv.setItemAnimator(new DefaultItemAnimator());
        helper = new Helper(getContext());
        background();
    }

    @Override
    protected String filter() {
        return MediaStore.Audio.Media.IS_MUSIC + " !=0";//+ " OR " + MediaStore.Audio.Media.TRACK + " !=0";
    }

    @Override
    protected String[] argument() {
        return null;
    }

    @Override
    protected String sortOder() {
        return Extras.getInstance().getSongSortOrder();
    }

    @Override
    protected void background() {
        loadTrak();
        songView();
        rv.setAdapter(songListAdapter);
        songListAdapter.setOnItemClickListener(onClick);
        songListAdapter.setOnLongClickListener(onLongClick);
    }

    public void downloadArtwork() {
        //download artwork
        CommonDatabase commonDatabase = new CommonDatabase(getContext(), DOWNLOAD_ARTWORK, true);
        List<Song> downloadList = commonDatabase.readLimit(-1, null);
        try {
            if (downloadList == null) {
                return;
            }
            if (!Extras.getInstance().saveData()) {
                for (Song song : downloadList) {
                    NetworkHelper.downloadAlbumArtwork(getContext(), song.getAlbum(), song.getArtist());
                }
            }
        } finally {
            commonDatabase.close();
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
        getLoaderManager().restartLoader(trackloader, null, this);
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


    private void loadGridView() {
        if (Extras.getInstance().getSongGrid() == 2) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            rv.setLayoutManager(layoutManager);
        } else if (Extras.getInstance().getSongGrid() == 3) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            rv.setLayoutManager(layoutManager);
        } else if (Extras.getInstance().getSongGrid() == 4) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
            rv.setLayoutManager(layoutManager);
        }
    }

    private void songView() {
        if (Extras.getInstance().songView()) {
            songListAdapter.setLayoutId(R.layout.item_grid_view);
            rv.addItemDecoration(new ItemOffsetDecoration(2));
            loadGridView();
        } else {
            songListAdapter.setLayoutId(R.layout.song_list);
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        }
    }

}
