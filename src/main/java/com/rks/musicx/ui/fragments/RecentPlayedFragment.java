package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
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
 * Created by Coolalien on 10/10/2016.
 */

public class RecentPlayedFragment extends miniFragment implements LoaderManager.LoaderCallbacks<List<Song>>{

    private FastScrollRecyclerView rv;
    private SongListAdapter songListAdapter;
    private int trackloader = -1;
    private Helper helper;
    private int limit;
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
                    helper.showMenu(trackloader,RecentPlayedFragment.this,RecentPlayedFragment.this,((MainActivity) getActivity()),position,view,getContext(),songListAdapter);
                    break;
            }
        }
    };

    public RecentPlayedFragment newInstance(int limit, boolean isgridView){
        Bundle bundle = new Bundle();
        bundle.putInt("Limit",limit);
        bundle.putBoolean("Isgridview",isgridView);
        RecentPlayedFragment recentPlayedFragment = new RecentPlayedFragment();
        recentPlayedFragment.setArguments(bundle);
        return recentPlayedFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null){
            int limit = bundle.getInt("Limit");
            boolean isgrid = bundle.getBoolean("Isgridview");
            setLimit(limit);
            setIsgridView(isgrid);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recentlyplayed,container,false);
        setupInstance(rootView);
        return rootView;
    }

    private void setupInstance(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.recentplayedrv);
        if (isgridView()){
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
            isgridView = false;
            songListAdapter = new SongListAdapter(getContext());
            rv.setAdapter(songListAdapter);
            songListAdapter.setOnItemClickListener(onClick);
            songListAdapter.setLayoutId(R.layout.song_list);
            rv.setItemAnimator(new DefaultItemAnimator());
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String ateKey = Helper.getATEKey(getContext());
            int colorAccent = Config.accentColor(getContext(),ateKey);
            rv.setPopupBgColor(colorAccent);
            helper = new Helper(getContext());
            loadTracks();
        }else {
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            customLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv.setLayoutManager(customLayoutManager);
            rv.setItemAnimator(new DefaultItemAnimator());
            rv.setNestedScrollingEnabled(false);
            rv.setVerticalScrollBarEnabled(false);
            rv.setHorizontalScrollBarEnabled(false);
            songListAdapter = new SongListAdapter(getContext());
            songListAdapter.setLayoutId(R.layout.recent_list);
            rv.setAdapter(songListAdapter);
            songListAdapter.setOnItemClickListener(onClick);
            rv.setItemAnimator(new DefaultItemAnimator());
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.GONE);
            String ateKey = Helper.getATEKey(getContext());
            int colorAccent = Config.accentColor(getContext(),ateKey);
            rv.setPopupBgColor(colorAccent);
            helper = new Helper(getContext());
            loadTracks();
            isgridView = true;
        }
        isgridView = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getActivity());
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

    /*
    Load Tracks
     */
    private void loadTracks(){
        getLoaderManager().initLoader(trackloader,null,this);
    }

    /*
    reload track
    */
    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, this);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        if (id == trackloader){
            return new RecentlyPlayedLoader(getActivity(), limit);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null){
            return;
        }
        songListAdapter.addDataList(data);
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        songListAdapter.notifyDataSetChanged();
    }

    /**
     * getter
     */
    public int getLimit() {
        return limit;
    }

    public boolean isgridView() {
        return isgridView;
    }

    /**
     * setter
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setIsgridView(boolean isgridView) {
        this.isgridView = isgridView;
    }
}
