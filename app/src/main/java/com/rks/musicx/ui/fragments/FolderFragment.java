package com.rks.musicx.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.FolderLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.FolderModel;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.FolderAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 03/24/2017.
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

public class FolderFragment extends BaseLoaderFragment implements SearchView.OnQueryTextListener {


    private final int folderloader = 2;
    private FastScrollRecyclerView folderrv;
    private FolderModel currentDir;
    private FolderAdapter fileadapter;
    private Helper helper;
    private SongListAdapter songListAdapter;
    private List<Song> songList;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener songOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            folderrv.smoothScrollToPosition(position);
            switch (view.getId()) {
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(false, trackloader, FolderFragment.this, FolderFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                    break;

            }
        }
    };

    private LoaderManager.LoaderCallbacks<List<FolderModel>> folderLoaderCallback = new LoaderManager.LoaderCallbacks<List<FolderModel>>() {
        @Override
        public Loader<List<FolderModel>> onCreateLoader(int id, Bundle args) {
            FolderLoader folderLoader = new FolderLoader(getContext(), getCurrentDir());
            if (id == folderloader) {
                return folderLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<FolderModel>> loader, List<FolderModel> data) {
            if (data == null) {
                return;
            }
            fileadapter.clear();
            fileadapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<FolderModel>> loader) {
            loader.reset();
            fileadapter.notifyDataSetChanged();
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener onClik = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            folderrv.smoothScrollToPosition(position);
            switch (view.getId()) {
                case R.id.folder_view:
                    if (fileadapter.getSnapshot().size() > 0) {
                        adapterLoader(fileadapter.getItem(position));
                    }
                    break;
            }
        }
    };

    public static FolderFragment newInstance() {
        return new FolderFragment();
    }

    private void adapterLoader(FolderModel folderModel) {
        currentDir = new FolderModel(folderModel.getPath());
        if (currentDir.isDirectory()){
            getLoaderManager().restartLoader(folderloader, null, folderLoaderCallback);
        }
        getLoaderManager().restartLoader(trackloader, null, FolderFragment.this);
        folderrv.setAdapter(songListAdapter);
    }

    @Override
    protected int setLayout() {
        return R.layout.common_rv;
    }

    @Override
    protected void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
    }

    @Override
    protected void funtion() {
        String storagePath = Extras.getInstance().getFolderPath();
        if (storagePath == null) {
            currentDir = new FolderModel(Helper.getStoragePath());
        } else {
            currentDir = new FolderModel(storagePath);
        }
        String atekey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), atekey);
        folderrv.setPopupBgColor(colorAccent);
        folderrv.setItemAnimator(new DefaultItemAnimator());
        helper = new Helper(getContext());
        setHasOptionsMenu(true);
        songList = new ArrayList<>();
        background();
    }


    @Override
    protected String filter() {
        return MediaStore.Audio.Media.DATA + " like ? ";
    }

    @Override
    protected String[] argument() {
        return new String[]{
                "%" + currentDir.getPath() + "%"
        };
    }

    @Override
    protected String sortOder() {
        return Extras.getInstance().getSongSortOrder();
    }

    @Override
    protected void background() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (getActivity() != null){
                    fileadapter = new FolderAdapter(getContext());
                    songListAdapter = new SongListAdapter(getContext());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (getActivity() == null){
                    return;
                }
                CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
                customLayoutManager.setSmoothScrollbarEnabled(true);
                folderrv.setLayoutManager(customLayoutManager);
                folderrv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
                fileadapter.setOnItemClickListener(onClik);
                folderrv.setHasFixedSize(true);
                initLoader();
                songListAdapter.setOnItemClickListener(songOnClick);
                folderrv.setAdapter(fileadapter);
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

    private void initLoader() {
        getLoaderManager().initLoader(folderloader, null, folderLoaderCallback);
        getLoaderManager().initLoader(trackloader, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }



    public FolderModel getCurrentDir() {
        return currentDir;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        menu.findItem(R.id.grid_view).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                load();
                break;
            case R.id.menu_sort_by_year:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                load();
                break;
            case R.id.menu_sort_by_artist:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                load();
                break;
            case R.id.menu_sort_by_album:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                load();
                break;
            case R.id.menu_sort_by_duration:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                load();
                break;
            case R.id.menu_sort_by_date:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                load();
                break;

        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void setAdapater(List<Song> data) {
        songListAdapter.addDataList(data);
    }

    @Override
    public void notifyChanges() {
        songListAdapter.notifyDataSetChanged();
    }
}
