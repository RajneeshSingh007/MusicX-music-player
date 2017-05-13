package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.FavoritesLoader;
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

public class FavFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    private FastScrollRecyclerView rv;
    private SongListAdapter playlistViewAdapter;
    private Helper helper;
    private FavoritesLoader favoritesLoader;
    private int trackloader = -1;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(playlistViewAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                helper.showMenu(false, trackloader, FavFragment.this, FavFragment.this, ((MainActivity) getActivity()), position, view, getContext(), playlistViewAdapter);
                break;
        }
    };

    public static FavFragment newFavoritesFragment() {
        return new FavFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fav, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.favrv);
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        playlistViewAdapter = new SongListAdapter(getContext());
        playlistViewAdapter.setLayoutId(R.layout.song_list);
        playlistViewAdapter.setOnItemClickListener(onClick);
        rv.setAdapter(playlistViewAdapter);
        rv.hasFixedSize();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        loadTracks();
        helper = new Helper(getContext());
        favoritesLoader = new FavoritesLoader(getContext());
        return rootView;
    }

    /*
    load tracks
     */
    private void loadTracks() {
        getLoaderManager().initLoader(trackloader, null, this);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       if (getActivity() == null){
           return;
       }
       Extras.getInstance().getThemevalue(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        if (id == trackloader) {
            return favoritesLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null) {
            return;
        }
        playlistViewAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        playlistViewAdapter.notifyDataSetChanged();
    }

}
