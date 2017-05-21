package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.base.BaseRefreshFragment;
import com.rks.musicx.data.loaders.AlbumLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.ItemOffsetDecoration;
import com.rks.musicx.ui.adapters.AlbumListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
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

public class AlbumListFragment extends BaseRefreshFragment implements LoaderCallbacks<List<Album>>, SearchView.OnQueryTextListener {

    private AlbumListAdapter albumListAdapter;
    private FastScrollRecyclerView rv;
    private int albumLoader = -1;
    private List<Album> albumList;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener OnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    ImageView Listartwork = (ImageView) view.findViewById(R.id.album_artwork);
                    fragTransition(albumListAdapter.getItem(position), Listartwork, "TransitionArtwork");
                    rv.smoothScrollToPosition(position);
                    break;
            }
        }
    };

    public static AlbumListFragment newInstance() {
        return new AlbumListFragment();
    }

    private void fragTransition(Album album, ImageView imageView, String transition) {
        ViewCompat.setTransitionName(imageView, transition);
        Helper.setFragmentTransition(getActivity(), AlbumListFragment.this, AlbumFragment.newInstance(album), new Pair<View, String>(imageView, transition));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_rv, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
        albumListAdapter = new AlbumListAdapter(getContext());
        int colorAccent = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        setHasOptionsMenu(true);
        initload();
        albumView();
        albumList = new ArrayList<>();
        albumListAdapter.setOnItemClickListener(OnClick);
        rv.setPopupBgColor(colorAccent);
        rv.setHasFixedSize(true);
        rv.setAdapter(albumListAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_view_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.album_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search album");
        if (Extras.getInstance().albumView()) {
            menu.findItem(R.id.grid_view).setVisible(false);
        } else {
            menu.findItem(R.id.grid_view).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                extras.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                extras.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_Z_A);
                load();
                break;
            case R.id.menu_sort_by_year:
                extras.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_YEAR);
                load();
                break;
            case R.id.menu_sort_by_artist:
                extras.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_ARTIST);
                load();
                break;
            case R.id.menu_sort_by_number_of_songs:
                extras.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                load();
                break;
            case R.id.bytwo:
                Extras.getInstance().setAlbumGrid(2);
                loadGridView();
                load();
                break;
            case R.id.bythree:
                Extras.getInstance().setAlbumGrid(3);
                loadGridView();
                load();
                break;
            case R.id.byfour:
                Extras.getInstance().setAlbumGrid(4);
                loadGridView();
                load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Album>> onCreateLoader(int id, Bundle args) {

        AlbumLoader albumsLoader = new AlbumLoader(getContext());
        if (id == albumLoader) {
            albumsLoader.setSortOrder(Extras.getInstance().getAlbumSortOrder());
            albumsLoader.filterartistsong(null, null);
            return albumsLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
        if (data == null) {
            return;
        }
        albumList = data;
        albumListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Album>> loader) {
        albumListAdapter.notifyDataSetChanged();
    }

    /*
    load album
     */
    private void initload() {
        getLoaderManager().initLoader(albumLoader, null, this);
    }

    /*
    reload album
     */
    @Override
    public void load() {
        getLoaderManager().restartLoader(albumLoader, null, this);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Album> filterlist = Helper.filterAlbum(albumList, newText);
        albumListAdapter.setFilter(filterlist);
        return true;
    }

    private void loadGridView() {
        if (Extras.getInstance().getAlbumGrid() == 2) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            rv.setLayoutManager(layoutManager);
        } else if (Extras.getInstance().getAlbumGrid() == 3) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            rv.setLayoutManager(layoutManager);
        } else if (Extras.getInstance().getAlbumGrid() == 4) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
            rv.setLayoutManager(layoutManager);
        }
    }

    private void albumView() {
        if (Extras.getInstance().albumView()) {
            albumListAdapter.setLayoutID(R.layout.item_list_view);
            CustomLayoutManager custom = new CustomLayoutManager(getContext());
            custom.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(custom);
            rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        } else {
            albumListAdapter.setLayoutID(R.layout.item_grid_view);
            rv.addItemDecoration(new ItemOffsetDecoration(2));
            loadGridView();
        }
    }

}
