package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.FolderLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.FolderModel;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.FileAdapter;
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

public class FolderFragment extends miniFragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Song>> {


    private final int trackloader = 1, folderloader = 2;
    private FastScrollRecyclerView folderrv;
    private FolderModel currentDir;
    private FileAdapter fileadapter;
    private Helper helper;
    private SongListAdapter songListAdapter;
    private List<Song> songList;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener songOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    folderrv.smoothScrollToPosition(position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(false, trackloader, FolderFragment.this, FolderFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                    break;

            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener onClik = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            FolderModel folderModel = fileadapter.getItem(position);
            currentDir = new FolderModel(folderModel.getPath());
            switch (view.getId()) {
                case R.id.folder_view:
                    adapterLoader(folderModel);
                    if (fileadapter.getSnapshot().size() > 0){
                        folderrv.setAdapter(songListAdapter);
                        getLoaderManager().restartLoader(trackloader, null, FolderFragment.this);
                    }
                    break;
            }
        }
    };

    private void adapterLoader(FolderModel folderModel){
        if (folderModel.getmFile().isDirectory()){
            getLoaderManager().restartLoader(folderloader, null, folderLoaderCallback);
        }
    }

    public static FolderFragment newInstance() {
        return new FolderFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_rv, container, false);
        ui(rootView);
        function();
        return rootView;
    }

    private void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
    }

    private void function() {
        String storagePath = Extras.getInstance().getFolderPath();
        if ( storagePath == null){
            currentDir = new FolderModel(Helper.getStoragePath());
        }else {
            currentDir = new FolderModel(storagePath);
        }
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        folderrv.setLayoutManager(customLayoutManager);
        folderrv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
        String atekey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), atekey);
        folderrv.setPopupBgColor(colorAccent);
        folderrv.setItemAnimator(new DefaultItemAnimator());
        fileadapter = new FileAdapter(getContext());
        fileadapter.setOnItemClickListener(onClik);
        helper = new Helper(getContext());
        songListAdapter = new SongListAdapter(getContext());
        songListAdapter.setOnItemClickListener(songOnClick);
        setHasOptionsMenu(true);
        songList = new ArrayList<>();
        initLoader();
        folderrv.setAdapter(fileadapter);
    }

    private void initLoader(){
        getLoaderManager().initLoader(trackloader, null, FolderFragment.this);
        getLoaderManager().initLoader(folderloader, null, folderLoaderCallback);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String atekey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), atekey, Config.primaryColor(getContext(), atekey));
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        TrackLoader trackLoaders = new TrackLoader(getContext());
        if (id == trackloader) {
            String[] selectargs = new String[]{
                    "%" + getCurrentDir().getPath() +"%"
            };
            String selection = MediaStore.Audio.Media.DATA + " like ? ";
            trackLoaders.setSortOrder(Extras.getInstance().getSongSortOrder());
            trackLoaders.filteralbumsong(selection, selectargs);
            return trackLoaders;
        }
        return null;
    }

    public FolderModel getCurrentDir() {
        return currentDir;
    }


    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null) {
            return;
        }
        songList = data;
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        songListAdapter.notifyDataSetChanged();
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

    private LoaderManager.LoaderCallbacks<List<FolderModel>> folderLoaderCallback = new LoaderManager.LoaderCallbacks<List<FolderModel>>() {
        @Override
        public Loader<List<FolderModel>> onCreateLoader(int id, Bundle args) {
            FolderLoader folderLoader = new FolderLoader(getContext(), getCurrentDir());
            if (id == folderloader){
                return folderLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<FolderModel>> loader, List<FolderModel> data) {
            if (data == null){
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

}
