package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;

import java.util.List;

/*
 * Created by Coolalien on 26/03/2017.
 */

public class RecentFragment extends miniFragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    private TextView recentName, recentPlayed, More, RecentlyAddedMore;
    private int accentcolor;
    private RecyclerView recentlyPlaying;
    private RecyclerView recentlyAdding;
    private RecentPlayedFragment recentPlayedFragment;
    private RecentlyAddedFragment recentlyAddedFragment;
    private Helper helper;
    private SongListAdapter recentlyPlayed, recentlyAdded;
    private int trackloader = 0, trackloaders = 1;


    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(recentlyPlayed.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloader, RecentFragment.this, RecentFragment.this, ((MainActivity) getActivity()), position, view, getContext(), recentlyPlayed);
                    break;
            }
        }
    };
    private LoaderManager.LoaderCallbacks<List<Song>> songLoaders = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            TrackLoader tracksloader = new TrackLoader(getContext());
            if (id == trackloaders) {
                String sortingrecent = MediaStore.Audio.Media.DATE_ADDED;// "%s limit 3" + ">" + (System.currentTimeMillis() / 1000 - 2 * 60 * 60 * 24);
                tracksloader.filteralbumsong(sortingrecent, null);
                String sortOrder = String.format("%s limit 9", MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
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
            recentlyAdded.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            loader.reset();
            recentlyAdded.notifyDataSetChanged();
        }

    };
    private BaseRecyclerViewAdapter.OnItemClickListener onClicks = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(recentlyAdded.getSnapshot(), position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloaders, songLoaders, RecentFragment.this, ((MainActivity) getActivity()), position, view, getContext(), recentlyAdded);
                    break;
            }
        }
    };

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recent, container, false);
        setupInstance(rootView);
        return rootView;
    }

    /*private void recentlyPlayed() {
        RecentPlayedFragment recentPlayedFragment = new RecentPlayedFragment().newInstance(9, false);
        if (recentPlayedFragment != null) {
          getChildFragmentManager().beginTransaction().add(recentlyPlaying.getId(), recentPlayedFragment).commitAllowingStateLoss();
        }
    }

    private void recentlyAdded() {
        RecentlyAddedFragment recentlyAddedFragment = new RecentlyAddedFragment().newInstance("%s limit 9", false);
        if (recentlyAddedFragment != null) {
          getChildFragmentManager().beginTransaction().add(recentlyAdded.getId(), recentlyAddedFragment).commitAllowingStateLoss();
        }
    }*/

    private void setupInstance(View rootView) {
        recentName = (TextView) rootView.findViewById(R.id.recentAdded);
        recentPlayed = (TextView) rootView.findViewById(R.id.recentPlayed);
        More = (TextView) rootView.findViewById(R.id.recentPlayedMore);
        RecentlyAddedMore = (TextView) rootView.findViewById(R.id.recentAddedMore);
        recentlyPlaying = (RecyclerView) rootView.findViewById(R.id.recentplaying);
        recentlyAdding = (RecyclerView) rootView.findViewById(R.id.recentadded);

        recentPlayedFragment = new RecentPlayedFragment().newInstance(-1, true);
        recentlyAddedFragment = new RecentlyAddedFragment().newInstance("%s limit -1", true);
        RecentlyAddedMore.setOnClickListener(v -> {
            ((MainActivity) getActivity()).setFragment(recentlyAddedFragment);
        });
        More.setOnClickListener(v -> {
            ((MainActivity) getActivity()).setFragment(recentPlayedFragment);
        });
        accentcolor = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
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

        CustomLayoutManager customLayoutManager1 = new CustomLayoutManager(getContext());
        customLayoutManager1.setSmoothScrollbarEnabled(true);
        customLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recentlyAdded = new SongListAdapter(getContext());
        recentlyAdding.setLayoutManager(customLayoutManager1);
        recentlyAdding.setNestedScrollingEnabled(false);
        recentlyAdding.setVerticalScrollBarEnabled(false);
        recentlyAdding.setHorizontalScrollBarEnabled(false);
        recentlyAdded.setLayoutId(R.layout.recent_list);
        recentlyAdding.setScrollBarSize(0);
        recentlyAdding.setAdapter(recentlyAdded);
        recentlyAdded.setOnItemClickListener(onClicks);
        recentlyAdding.setItemAnimator(new DefaultItemAnimator());
        recentlyAdding.setHasFixedSize(true);
        getLoaderManager().initLoader(trackloaders, null, songLoaders);
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
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        customLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recentlyPlayed = new SongListAdapter(getContext());
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
        getLoaderManager().initLoader(trackloader, null, this);
    }

    /*
     reload track
     */
    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, this);
        getLoaderManager().restartLoader(trackloaders, null, songLoaders);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        if (id == trackloader) {
            return new RecentlyPlayedLoader(getContext(), 9);
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
}
