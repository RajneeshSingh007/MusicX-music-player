package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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

public class RecentlyAddedFragment extends miniFragment {

    private SongListAdapter songListAdapter;
    private FastScrollRecyclerView rv;
    private int trackloader = -1;
    private Helper helper;
    private String limit;
    private boolean isgridView;
    private Toolbar toolbar;

    private LoaderManager.LoaderCallbacks<List<Song>> songLoaders = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            TrackLoader tracksloader = new TrackLoader(getActivity());
            if (id == trackloader) {
                String sortingrecent = MediaStore.Audio.Media.DATE_ADDED;// "%s limit 3" + ">" + (System.currentTimeMillis() / 1000 - 2 * 60 * 60 * 24);
                tracksloader.filteralbumsong(sortingrecent, null);
                String sortOrder = String.format(limit, MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
                tracksloader.setSortOrder(sortOrder);
                return tracksloader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            if (data == null) {
                return;
            }
            songListAdapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            songListAdapter.notifyDataSetChanged();
        }

    };
    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(false, trackloader, songLoaders, RecentlyAddedFragment.this,
                            ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recentadded, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.recentrv);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        helper = new Helper(getContext());
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        songListAdapter = new SongListAdapter(getContext());
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        if (isgridView()) {
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
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
        isgridView = true;
        songListAdapter.setOnItemClickListener(onClick);
        rv.setAdapter(songListAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setPopupBgColor(colorAccent);
        rv.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() == null){
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }

    /*
     * Load track.
     */
    private void loadTrak() {
        getLoaderManager().initLoader(trackloader, null, songLoaders);
    }

    /*
    reload track
    */
    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, songLoaders);
    }


    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public boolean isgridView() {
        return isgridView;
    }

    public void setIsgridView(boolean isgridView) {
        this.isgridView = isgridView;
    }
}
