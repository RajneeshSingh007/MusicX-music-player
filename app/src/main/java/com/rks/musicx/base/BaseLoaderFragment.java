package com.rks.musicx.base;

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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rks.musicx.data.loaders.FavoritesLoader;
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.ui.adapters.SongListAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.DOWNLOAD_ARTWORK;

/**
 * Created by Coolalien on 5/17/2017.
 */
public abstract class BaseLoaderFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>>{


    public final int trackloader = 1, recentlyplayed = 2, favloader = 3;
    public List<Song> songList;
    public SongListAdapter songListAdapter;
    private CommonDatabase commonDatabase;

    protected abstract int setLayout();

    protected abstract void ui(View view);

    protected abstract void funtion();

    protected abstract String filter();

    protected abstract String[] argument();

    protected abstract String sortOder();

    protected abstract void background();

    protected abstract boolean isTrack ();

    protected abstract boolean isRecentPlayed();

    protected abstract boolean isFav();

    protected abstract int getLimit();

    abstract public void load();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setLayout(), container, false);
        songListAdapter = new SongListAdapter(getContext());
        songList = new ArrayList<>();
        ui(view);
        funtion();
        commonDatabase = new CommonDatabase(getContext(), DOWNLOAD_ARTWORK, true);
        return view;
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        if (isTrack() && id == trackloader){
            TrackLoader trackLoader = new TrackLoader(getContext());
            trackLoader.filteralbumsong(filter(), argument());
            trackLoader.setSortOrder(sortOder());
            return trackLoader;
        }
        if (isRecentPlayed() && id == recentlyplayed){
            RecentlyPlayedLoader recentlyPlayedLoader = new RecentlyPlayedLoader(getContext(), getLimit());
            return recentlyPlayedLoader;
        }
        if (isFav() && id == favloader){
            FavoritesLoader favoritesLoader = new FavoritesLoader(getContext(), getLimit());
            return favoritesLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null){
            return;
        }
        if (isTrack() || isFav() || isRecentPlayed()){
            try {
                commonDatabase.add(data);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                commonDatabase.close();
            }
            songListAdapter.addDataList(data);
            songList = data;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        if (isTrack() || isFav() || isRecentPlayed()){
            songListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    public List<Song> getSongList() {
        return songList;
    }

}
