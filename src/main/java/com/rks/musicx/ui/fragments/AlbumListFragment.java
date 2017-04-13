package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.AlbumLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.ItemOffsetDecoration;
import com.rks.musicx.ui.adapters.AlbumListAdapter;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class AlbumListFragment extends miniFragment implements LoaderCallbacks<List<Album>>, SearchView.OnQueryTextListener {

    private AlbumListAdapter albumListAdapter;
    private FastScrollRecyclerView rv;
    private int albumLoader = -1;
    private List<Album> albumList;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener gridClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.item_view:
                    Fragment fragment = AlbumFragment.newInstance(albumListAdapter.getItem(position));
                    ImageView imageView = (ImageView) view.findViewById(R.id.album_artwork);
                    fragTransition(fragment, imageView);
                    rv.smoothScrollToPosition(position);
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener listOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_listartwork:
                case R.id.item_view:
                    Fragment fragments = AlbumFragment.newInstance(albumListAdapter.getItem(position));
                    ImageView Listartwork = (ImageView) view.findViewById(R.id.album_listartwork);
                    fragTransition(fragments, Listartwork);
                    rv.smoothScrollToPosition(position);
                    break;
            }
        }
    };

    public static AlbumListFragment newInstance() {
        return new AlbumListFragment();
    }

    private void fragTransition(Fragment fragment, ImageView imageView) {
        ViewCompat.setTransitionName(imageView, "TransitionArtwork");
        Helper.setFragmentTransition(getActivity(), AlbumListFragment.this, fragment, new Pair<View, String>(imageView, "TransitionArtwork"));
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
        rv.setPopupBgColor(colorAccent);
        rv.setHasFixedSize(true);
        rv.setAdapter(albumListAdapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
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
                break;
            case R.id.bythree:
                Extras.getInstance().setAlbumGrid(3);
                loadGridView();
                break;
            case R.id.byfour:
                Extras.getInstance().setAlbumGrid(4);
                loadGridView();
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
            load();
        } else if (Extras.getInstance().getAlbumGrid() == 3) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            rv.setLayoutManager(layoutManager);
            load();
        } else if (Extras.getInstance().getAlbumGrid() == 4) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
            rv.setLayoutManager(layoutManager);
            load();
        }
    }

    private void albumView() {
        if (Extras.getInstance().albumView()) {
            albumListAdapter.setOnItemClickListener(listOnClick);
            albumListAdapter.setLayoutID(R.layout.item_list_view);
            CustomLayoutManager custom = new CustomLayoutManager(getContext());
            custom.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(custom);
            rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        } else {
            albumListAdapter.setLayoutID(R.layout.item_grid_view);
            albumListAdapter.setOnItemClickListener(gridClick);
            rv.addItemDecoration(new ItemOffsetDecoration(2));
            loadGridView();
        }
    }

}
