package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.StartSnapHelper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.SongListAdapter;

import java.util.List;

/*
 * Created by Coolalien on 26/03/2017.
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

public class RecentFragment extends BaseLoaderFragment {

    private final int recentloader = 5;
    private TextView recentName, recentPlayed, More, RecentlyAddedMore;
    private int accentcolor;
    private RecyclerView recentlyPlaying;
    private RecyclerView recentlyAdding;
    private RecentPlayedFragment recentPlayedFragment;
    private RecentlyAddedFragment recentlyAddedFragment;
    private Helper helper;
    private SongListAdapter recentlyPlayed;
    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {

            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(recentlyPlayed.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    Song song = recentlyPlayed.getItem(position);
                    helper.showMenu(false, new RefreshData() {
                        @Override
                        public void refresh() {
                            getLoaderManager().restartLoader(recentloader, null, RecentFragment.this);
                        }

                        @Override
                        public Fragment currentFrag() {
                            return RecentFragment.this;
                        }
                    }, ((MainActivity) getActivity()), view, getContext(), song);
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener onClicks = new BaseRecyclerViewAdapter.OnItemClickListener() {

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
                            getLoaderManager().restartLoader(trackloader, null, RecentFragment.this);
                        }

                        @Override
                        public Fragment currentFrag() {
                            return RecentFragment.this;
                        }
                    }, ((MainActivity) getActivity()), view, getContext(), song);
                    break;
            }
        }
    };
    private LoaderManager.LoaderCallbacks<List<Song>> recentPlaying = new LoaderManager.LoaderCallbacks<List<Song>>() {
        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            RecentlyPlayedLoader playedLoader = new RecentlyPlayedLoader(getContext(), 9);
            if (id == recentloader) {
                return playedLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            if (data == null) {
                return;
            }
            recentlyPlayed.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            loader.reset();
            recentlyPlayed.notifyDataSetChanged();
        }
    };

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_recent;
    }

    @Override
    protected void ui(View view) {
        recentName = (TextView) view.findViewById(R.id.recentAdded);
        recentPlayed = (TextView) view.findViewById(R.id.recentPlayed);
        More = (TextView) view.findViewById(R.id.recentPlayedMore);
        RecentlyAddedMore = (TextView) view.findViewById(R.id.recentAddedMore);
        recentlyPlaying = (RecyclerView) view.findViewById(R.id.recentplaying);
        recentlyAdding = (RecyclerView) view.findViewById(R.id.recentadded);
    }

    @Override
    protected void funtion() {
        recentPlayedFragment = new RecentPlayedFragment().newInstance(-1, true);
        recentlyAddedFragment = new RecentlyAddedFragment().newInstance("%s limit -1", true);
        RecentlyAddedMore.setOnClickListener(v -> {
            ((MainActivity) getActivity()).setFragment(recentlyAddedFragment);
        });
        More.setOnClickListener(v -> {
            ((MainActivity) getActivity()).setFragment(recentPlayedFragment);
        });
        accentcolor = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            recentName.setTextColor(Color.WHITE);
            recentPlayed.setTextColor(Color.WHITE);
            More.setTextColor(accentcolor);
            RecentlyAddedMore.setTextColor(accentcolor);
        } else {
            recentName.setTextColor(Color.BLACK);
            recentPlayed.setTextColor(Color.BLACK);
            More.setTextColor(accentcolor);
            RecentlyAddedMore.setTextColor(accentcolor);
        }
        helper = new Helper(getContext());
        background();
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
        return String.format("%s limit 9", MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
    }

    @Override
    protected void background() {
        recentlyPlayed = new SongListAdapter(getContext());

        CustomLayoutManager customLayoutManager1 = new CustomLayoutManager(getContext());
        customLayoutManager1.setSmoothScrollbarEnabled(true);
        customLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        SnapHelper startSnapHelper = new StartSnapHelper();

        recentlyAdding.setLayoutManager(customLayoutManager1);
        recentlyAdding.setNestedScrollingEnabled(false);
        recentlyAdding.setVerticalScrollBarEnabled(false);
        recentlyAdding.setHorizontalScrollBarEnabled(false);
        songListAdapter.setLayoutId(R.layout.recent_list);
        recentlyAdding.setScrollBarSize(0);
        recentlyAdding.setAdapter(songListAdapter);
        songListAdapter.setOnItemClickListener(onClicks);
        recentlyAdding.setItemAnimator(new DefaultItemAnimator());
        recentlyAdding.setHasFixedSize(true);
        startSnapHelper.attachToRecyclerView(recentlyAdding);
        getLoaderManager().initLoader(trackloader, null, RecentFragment.this);

        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        customLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recentlyPlaying.setLayoutManager(customLayoutManager);
        recentlyPlaying.setNestedScrollingEnabled(false);
        recentlyPlaying.setVerticalScrollBarEnabled(false);
        recentlyPlaying.setHorizontalScrollBarEnabled(false);
        recentlyPlayed.setLayoutId(R.layout.recent_list);
        recentlyPlaying.setScrollBarSize(0);
        recentlyPlaying.setAdapter(recentlyPlayed);
        recentlyPlayed.setOnItemClickListener(onClick);
        recentlyPlaying.setItemAnimator(new DefaultItemAnimator());
        recentlyPlaying.setHasFixedSize(true);
        startSnapHelper.attachToRecyclerView(recentlyPlaying);
        getLoaderManager().initLoader(recentloader, null, recentPlaying);
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
        getLoaderManager().restartLoader(recentloader, null, recentPlaying);
        getLoaderManager().restartLoader(trackloader, null, this);
    }


}
