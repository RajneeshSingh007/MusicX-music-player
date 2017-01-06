package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

/**
 * Created by Coolalien on 12/8/2016.
 */

public class RecentlyAddedFragment extends miniFragment {

    private SongListAdapter songListAdapter;
    private FastScrollRecyclerView rv;
    private int trackloader = -1;
    private Helper helper;
    private String limit;
    private boolean isgridView;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.artwork:
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(),position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloader,songLoaders,RecentlyAddedFragment.this,((MainActivity) getActivity()),position,view,getContext(),songListAdapter);
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
        if (bundle != null){
            String limit = bundle.getString("Limit");
            boolean isgridview = bundle.getBoolean("IsgridView");
            setLimit(limit);
            setIsgridView(isgridview);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recentadded, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.recentrv);

        if (isgridView()){
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
            songListAdapter = new SongListAdapter(getContext());
            songListAdapter.setLayoutId(R.layout.song_list);
            songListAdapter.setOnItemClickListener(onClick);
            rv.setAdapter(songListAdapter);
            rv.setItemAnimator(new DefaultItemAnimator());
            String ateKey = Helper.getATEKey(getContext());
            int colorAccent = Config.accentColor(getContext(),ateKey);
            rv.setPopupBgColor(colorAccent);
            loadTrak();
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            helper = new Helper(getContext());
            isgridView = false;
        }else {
            isgridView = true;
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            customLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv.setLayoutManager(customLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setNestedScrollingEnabled(false);
            songListAdapter = new SongListAdapter(getContext());
            songListAdapter.setOnItemClickListener(onClick);
            songListAdapter.setLayoutId(R.layout.recent_list);
            rv.setAdapter(songListAdapter);
            rv.setItemAnimator(new DefaultItemAnimator());
            String ateKey = Helper.getATEKey(getContext());
            int colorAccent = Config.accentColor(getContext(),ateKey);
            rv.setPopupBgColor(colorAccent);
            rv.setVerticalScrollBarEnabled(false);
            rv.setHorizontalScrollBarEnabled(false);
            loadTrak();
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.GONE);
            helper = new Helper(getContext());

        }
          /*try {
            ApplicationInfo applicationInfo = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            UnitID = bundle.getString("adsdata");
            adView.setAdUnitId(UnitID);
            adView.setAdSize(AdSize.BANNER);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }*/
        isgridView = true;
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
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

    private LoaderManager.LoaderCallbacks<List<Song>> songLoaders = new LoaderManager.LoaderCallbacks<List<Song>>() {

        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            TrackLoader tracksloader = new TrackLoader(getActivity());
            if (id == trackloader){
                String sortingrecent = MediaStore.Audio.Media.DATE_ADDED;// "%s limit 3" + ">" + (System.currentTimeMillis() / 1000 - 2 * 60 * 60 * 24);
                tracksloader.filteralbumsong(sortingrecent,null);
                String sortOrder = String.format(limit, MediaStore.Audio.Media.DATE_MODIFIED + " DESC");
                tracksloader.setSortOrder(sortOrder);
                return tracksloader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            if(data ==null){
                return;
            }
            songListAdapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            songListAdapter.notifyDataSetChanged();
        }

    };

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


    /**
     * getter
     */

    public String getLimit() {
        return limit;
    }

    public boolean isgridView() {
        return isgridView;
    }

    /**
     * setter
     */
    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setIsgridView(boolean isgridView) {
        this.isgridView = isgridView;
    }
}
