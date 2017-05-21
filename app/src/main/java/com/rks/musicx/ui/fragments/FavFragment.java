package com.rks.musicx.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseLoaderFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
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

public class FavFragment extends BaseLoaderFragment {

    private FastScrollRecyclerView rv;
    private SongListAdapter playlistViewAdapter;
    private Helper helper;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(playlistViewAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                helper.showMenu(false, favloader, FavFragment.this, FavFragment.this, ((MainActivity) getActivity()), position, view, getContext(), playlistViewAdapter);
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
    }

    @Override
    protected void funtion() {
        setHasOptionsMenu(true);
        rv.hasFixedSize();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        helper = new Helper(getContext());
        background();
    }

    @Override
    protected String filter() {
        return null;
    }

    @Override
    protected String[] argument() {
        return new String[0];
    }

    @Override
    protected String sortOder() {
        return null;
    }

    @Override
    protected void background() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (getActivity() != null){
                    playlistViewAdapter = new SongListAdapter(getContext());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
                customLayoutManager.setSmoothScrollbarEnabled(true);
                rv.setLayoutManager(customLayoutManager);
                rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
                playlistViewAdapter.setLayoutId(R.layout.song_list);
                playlistViewAdapter.setOnItemClickListener(onClick);
                rv.setAdapter(playlistViewAdapter);
                loadTracks();
            }
        }.execute();
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
        return 0;
    }

    @Override
    public void load() {

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
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(favloader, null, this);
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void setAdapater(List<Song> data) {
        playlistViewAdapter.addDataList(data);
    }

    @Override
    public void notifyChanges() {
        playlistViewAdapter.notifyDataSetChanged();
    }
}
