package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.base.BaseRefreshFragment;
import com.rks.musicx.data.loaders.PlaylistLoader;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.PARAM_PLAYLIST_ID;
import static com.rks.musicx.misc.utils.Constants.PARAM_PLAYLIST_NAME;


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

public class PlaylistFragment extends BaseRefreshFragment {

    private FastScrollRecyclerView rv;
    private Playlist mPlaylist;
    private SongListAdapter playlistViewAdapter;
    private int trackloader = -1;
    private Helper helper;
    private List<Song> playList;
    private Toolbar toolbar;

    private LoaderManager.LoaderCallbacks<List<Song>> playlistLoader = new LoaderCallbacks<List<Song>>() {
        @Override
        public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
            if (mPlaylist.getId() == 0) {
                return null;
            }
            PlaylistLoader playlistLoader = new PlaylistLoader(getContext(), mPlaylist.getId());
            if (id == trackloader) {
                return playlistLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
            if (data == null) {
                return;
            }
            playList = data;
            playlistViewAdapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Song>> loader) {
            loader.reset();
            playlistViewAdapter.notifyDataSetChanged();
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnclick = (position, view) -> {

        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(playlistViewAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                Song song = playlistViewAdapter.getItem(position);
                helper.showMenu(false, new RefreshData() {
                    @Override
                    public void refresh() {
                        getLoaderManager().restartLoader(trackloader, null, playlistLoader);
                    }

                    @Override
                    public Fragment currentFrag() {
                        return PlaylistFragment.this;
                    }
                }, ((MainActivity) getActivity()), view, getContext(), song);
                break;
        }
    };

    public static PlaylistFragment newInstance(Playlist playlist) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        args.putLong(PARAM_PLAYLIST_ID, playlist.getId());
        args.putString(PARAM_PLAYLIST_NAME, playlist.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {
            long id = args.getLong(PARAM_PLAYLIST_ID);
            String name = args.getString(PARAM_PLAYLIST_NAME);
            mPlaylist = new Playlist();
            mPlaylist.setId(id);
            mPlaylist.setName(name);
        }
    }

    private void init() {
        getLoaderManager().initLoader(trackloader, null, playlistLoader);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_list, menu);
        menu.findItem(R.id.menu_sort_by).setVisible(false);
        menu.findItem(R.id.action_create_playlist).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null){
            return false;
        }
        switch (item.getItemId()) {
            case R.id.shuffle_all:
                ((MainActivity) getActivity()).onShuffleRequested(playList, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_playlist;
    }

    @Override
    protected void ui(View view) {
        rv = (FastScrollRecyclerView) view.findViewById(R.id.playlistrv);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    }

    @Override
    protected void funtion() {
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
        playlistViewAdapter = new SongListAdapter(getContext());
        playlistViewAdapter.setLayoutId(R.layout.song_list);
        playlistViewAdapter.setOnItemClickListener(mOnclick);
        rv.setAdapter(playlistViewAdapter);
        rv.hasFixedSize();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        toolbar.setVisibility(View.VISIBLE);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null){
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        helper = new Helper(getContext());
        init();
        playList = new ArrayList<>();
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, playlistLoader);
    }


}
