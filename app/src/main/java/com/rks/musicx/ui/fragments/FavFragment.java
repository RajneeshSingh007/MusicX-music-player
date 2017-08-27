package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
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

public class FavFragment extends BaseLoaderFragment implements SearchView.OnQueryTextListener{

    private FastScrollRecyclerView rv;
    private Helper helper;
    private SearchView searchView;
    private Toolbar toolbar;
    private CommonDatabase commonDatabase;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                Song song = songListAdapter.getItem(position);
                helper.showMenu(false, new RefreshData() {
                    @Override
                    public void refresh() {
                        getLoaderManager().restartLoader(trackloader, null, FavFragment.this);
                    }

                    @Override
                    public Fragment currentFrag() {
                        return FavFragment.this;
                    }
                }, ((MainActivity) getActivity()), view, getContext(), song);
                break;
        }
    };

    public static FavFragment newFavoritesFragment() {
        return new FavFragment();
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_fav;
    }

    @Override
    protected void ui(View view) {
        rv = (FastScrollRecyclerView) view.findViewById(R.id.favrv);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);

    }

    @Override
    protected void funtion() {
        background();
        setHasOptionsMenu(true);
        rv.hasFixedSize();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        helper = new Helper(getContext());
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null){
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        commonDatabase = new CommonDatabase(getContext(), Constants.Fav_TableName, true);
        try {
            songList = commonDatabase.readLimit(-1, null);
        }finally {
            commonDatabase.close();
        }
    }

    @Override
    protected String filter() {
        return null;
    }

    @Override
    protected String[] argument() {
        return null;
    }

    @Override
    protected String sortOder() {
        return null;
    }

    @Override
    protected void background() {
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        songListAdapter.setLayoutId(R.layout.song_list);
        songListAdapter.setOnItemClickListener(onClick);
        rv.setAdapter(songListAdapter);
        loadTracks();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        menu.findItem(R.id.grid_view).setVisible(false);
        menu.findItem(R.id.menu_sort_by).setVisible(false);
        menu.findItem(R.id.default_folder).setVisible(false);
        menu.findItem(R.id.menu_refresh).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null){
            return false;
        }
        switch (item.getItemId()) {
            case R.id.shuffle_all:
                ((MainActivity) getActivity()).onShuffleRequested(songList, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isTrack() {
        return false;
    }

    @Override
    protected boolean isRecentPlayed() {
        return false;
    }

    @Override
    protected boolean isFav() {
        return true;
    }

    @Override
    protected int getLimit() {
        return -1;
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(favloader, null, this);
    }

    /*
    load tracks
     */
    private void loadTracks() {
        getLoaderManager().initLoader(favloader, null, this);
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        final List<Song> filterlist = helper.filter(songList, newText);
        if (filterlist.size() > 0) {
            songListAdapter.setFilter(filterlist);
            return true;
        } else {
            Toast.makeText(getContext(), "No data found...", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
