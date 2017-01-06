package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.FavoritesLoader;
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

import static com.rks.musicx.misc.utils.Constants.PARAM_PLAYLIST_FAVORITES;

/**
 * Created by Coolalien on 8/15/2016.
 */

public class FavFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>>{

    private FastScrollRecyclerView rv;
    private SongListAdapter playlistViewAdapter;
    private boolean mFavorites = false;
    private Helper helper;
    private FavoritesLoader favoritesLoader;
    private int trackloader = -1;


    public static FavFragment newFavoritesFragment() {
        FavFragment fragment = new FavFragment();
        Bundle args = new Bundle();
        args.putBoolean(PARAM_PLAYLIST_FAVORITES, true);
        fragment.setArguments(args);
        return fragment;
    }

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(playlistViewAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                helper.showMenu(trackloader,FavFragment.this,FavFragment.this,((MainActivity) getActivity()),position,view,getContext(),playlistViewAdapter);
                break;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {
            if (args.getBoolean(PARAM_PLAYLIST_FAVORITES)) {
                mFavorites = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fav, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.favrv);
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
        playlistViewAdapter = new SongListAdapter(getContext());
        playlistViewAdapter.setLayoutId(R.layout.song_list);
        playlistViewAdapter.setOnItemClickListener(onClick);
        rv.setAdapter(playlistViewAdapter);
        rv.hasFixedSize();
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(),ateKey);
        rv.setPopupBgColor(colorAccent);
        loadTracks();
        helper = new Helper(getContext());
        favoritesLoader = new FavoritesLoader(getContext());
        return rootView;
    }

    /*
    load tracks
     */
    private void loadTracks(){
        getLoaderManager().initLoader(trackloader,null,this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        if (id == trackloader){
            if (mFavorites) {
                return favoritesLoader;
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null){
            return;
        }
        playlistViewAdapter.addDataList(data);
        playlistViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        playlistViewAdapter.notifyDataSetChanged();
    }
}
