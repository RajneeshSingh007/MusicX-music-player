package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
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
import com.rks.musicx.data.loaders.ArtistLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.adapters.ArtistListAdapter;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class ArtistListFragment extends miniFragment implements LoaderManager.LoaderCallbacks<List<Artist>>, SearchView.OnQueryTextListener {

    private FastScrollRecyclerView rv;
    private ArtistListAdapter artistListAdapter;
    private int artistLoader = -1;
    private List<Artist> artistlist;
    private SearchView searchView;

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_listartwork:
                case R.id.item_view:
                    Fragment fragments = ArtistFragment.newInstance(artistListAdapter.getItem(position));
                    ImageView listartwork = (ImageView) view.findViewById(R.id.album_listartwork);
                    fragTransition(fragments, listartwork);
                    rv.smoothScrollToPosition(position);
                    break;
            }
        }
    };

    public static ArtistListFragment newInstance(int pos) {
        Extras.getInstance().setTabIndex(pos);
        return new ArtistListFragment();
    }

    private void fragTransition(Fragment fragment, ImageView imageView) {
        ViewCompat.setTransitionName(imageView, "TransitionArtwork");
        Helper.setFragmentTransition(getActivity(), ArtistListFragment.this, fragment, new Pair<View, String>(imageView, "TransitionArtwork"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_rv, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
        artistListAdapter = new ArtistListAdapter(getActivity());
        artistListAdapter.setLayoutID(R.layout.item_list_view);
        artistListAdapter.setOnItemClickListener(mOnClick);
        CustomLayoutManager custom = new CustomLayoutManager(getActivity());
        rv.setLayoutManager(custom);
        rv.setAdapter(new ArtistListAdapter(getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        rv.setHasFixedSize(true);
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        rv.setAdapter(artistListAdapter);
        setHasOptionsMenu(true);
        loadArtist();
        artistlist = new ArrayList<>();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.artist_view_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.artist_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search artist");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                extras.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                extras.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_Z_A);
                load();
                break;
            case R.id.menu_sort_by_number_of_songs:
                extras.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                load();
                break;
            case R.id.menu_sort_by_number_of_albums:
                extras.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        ArtistLoader artistsLoader = new ArtistLoader(getActivity());
        if (id == artistLoader) {
            artistsLoader.setSortOrder(Extras.getInstance().getArtistSortOrder());
            return artistsLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
        if (data == null) {
            return;
        }
        artistlist = data;
        artistListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {
        artistListAdapter.notifyDataSetChanged();
    }

    private void loadArtist() {
        getLoaderManager().initLoader(artistLoader, null, this);
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(artistLoader, null, this);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Artist> filterlist = Helper.filterArtist(artistlist, newText);
        artistListAdapter.setFilter(filterlist);
        return true;
    }

}
