package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import com.rks.musicx.data.network.AlbumArtwork;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.ItemOffsetDecoration;
import com.rks.musicx.misc.utils.PlaylistHelper;
import com.rks.musicx.ui.activities.MainActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
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
    private AlbumArtwork albumArtwork;
    private android.support.v7.view.ActionMode mActionMode;
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
                            helper.showMenu(false, trackloader, SongListFragment.this, SongListFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
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
                            helper.showMenu(false, trackloader, SongListFragment.this, SongListFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                            break;

                    }
                }
            }
        }
    };
    private android.support.v7.view.ActionMode.Callback mActionModeCallback = new android.support.v7.view.ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.multi_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_add_to_playlist:
                    if (getSelectedSong().size() > 0) {
                        PlaylistHelper.PlaylistMultiChooser(SongListFragment.this, getContext(), getSelectedSong());
                    }
                    break;
                case R.id.action_add_to_queue:
                    if (getSelectedSong().size() > 0) {
                        try {
                            for (Song song : getSelectedSong()) {
                                ((MainActivity) getActivity()).addToQueue(song);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Toast.makeText(getContext(), "Added to queue", Toast.LENGTH_SHORT).show();
                        }

                    }
                    break;
                case R.id.action_play:
                    if (getSelectedSong().size() > 0) {
                        ((MainActivity) getActivity()).onSongSelected(getSelectedSong(), 0);
                    }
                    break;
                case R.id.action_delete:
                    if (getSelectedSong().size() > 0) {
                        helper.multiDeleteTrack(trackloader, SongListFragment.this, SongListFragment.this, getSelectedSong(), getContext());
                    }
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
            mActionMode.setTitle("");
            mActionMode.finish();
            mActionMode = null;
            songListAdapter.exitMultiselectMode();
        }

    };
    private BaseRecyclerViewAdapter.OnLongClickListener onLongClick = new BaseRecyclerViewAdapter.OnLongClickListener() {
        @Override
        public void onLongItemClick(int position) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        if (getActivity() == null){
            return false;
        }
        switch (item.getItemId()) {
            case R.id.shuffle_all:
                ((MainActivity) getActivity()).onShuffleRequested(songList, true);
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
        //download artwork
        CommonDatabase commonDatabase = new CommonDatabase(getContext(), DOWNLOAD_ARTWORK, true);
        List<Song> downloadList = commonDatabase.readLimit(-1, null);
        try {
            if (downloadList == null || getActivity() == null) {
                return;
            }
            if (!Extras.getInstance().saveData()){
                for (Song song : downloadList) {
                    albumArtwork = new AlbumArtwork(getActivity(), song.getArtist(), song.getAlbum());
                    albumArtwork.execute();
                    Log.e("MetaData", song.getTitle());
                }
            }
        }finally {
            commonDatabase.close();
        }
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
        songListAdapter.setFilter(filterlist);
        return true;
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



    @Override
    public void onDestroy() {
        if (!Extras.getInstance().saveData()){
            if (albumArtwork != null) {
                albumArtwork.cancel(true);
            }
        }
        super.onDestroy();
    }

    /**
     * MultiSelection SongList
     * @return
     */
    private List<Song> getSelectedSong() {
        List<Integer> selectedPos = songListAdapter.getSelectedItems();
        List<Song> songList = new ArrayList<>();
        int pos;
        for (int i = selectedPos.size() - 1; i >= 0; i--) {
            pos = selectedPos.get(i);
            Song song = songListAdapter.getItem(pos);
            songList.add(song);
        }
        return songList;
    }


}
