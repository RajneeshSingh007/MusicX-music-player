package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.CreatedPlaylistLoader;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.PlaylistListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class PlaylistListFragment extends miniFragment implements LoaderCallbacks<List<Playlist>> {

    private FastScrollRecyclerView rv;
    private PlaylistListAdapter playlistListAdapter;
    private int playloader = -1;

    private  BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    PlaylistFragment fragment = PlaylistFragment.newInstance(playlistListAdapter.getItem(position));
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.delete_playlist:
                    showMenu(view,position);
                    break;
            }
        }
    };

    private void showMenu(View view , int pos){
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist_delete:
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
                        builder.title(playlistListAdapter.getItem(pos).getName());
                        builder.content(getContext().getString(R.string.deleteplaylist));
                        builder.positiveText(R.string.delete);
                        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new Helper(getContext()).deletePlaylist(getContext().getContentResolver(),playlistListAdapter.getItem(pos).getId());
                                Toast.makeText(getContext(),"Playlist Deleted",Toast.LENGTH_LONG).show();
                                load();
                            }
                        });
                        builder.negativeText(R.string.cancel);
                        builder.show();
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    public static PlaylistListFragment newInstance(int pos) {
        Extras.getInstance().setTabIndex(pos);
        return new PlaylistListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_rv, container, false);
        findbyid(rootView);
        function();
        return rootView;
    }

    private void findbyid(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
    }


    private void function() {
        CustomLayoutManager customlayout = new CustomLayoutManager(getContext());
        customlayout.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customlayout);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
        playlistListAdapter = new PlaylistListAdapter(getContext());
        playlistListAdapter.setOnItemClickListener(mOnClick);
        rv.setAdapter(playlistListAdapter);
        getLoaderManager().initLoader(playloader,null,this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_playlist:
                showCreatePlaylistDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreatePlaylistDialog() {
        View layout = LayoutInflater.from(getActivity()).inflate(R.layout.create_playlist, new LinearLayout(getActivity()), false);
        MaterialDialog.Builder createplaylist = new MaterialDialog.Builder(getContext());
        createplaylist.title(R.string.create_playlist);
        createplaylist.positiveText(android.R.string.ok);
        TextInputEditText editText = (TextInputEditText) layout.findViewById(R.id.playlist_name);
        createplaylist.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                new Helper(getContext()).createPlaylist(getActivity().getContentResolver(), editText.getText().toString());
                load();
            }
        });
        createplaylist.negativeText(android.R.string.cancel);
        createplaylist.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                createplaylist.autoDismiss(true);
            }
        });
        createplaylist.customView(layout,false);
        createplaylist.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        CreatedPlaylistLoader playlistloader = new CreatedPlaylistLoader(getContext());
        if (id == playloader){
            return playlistloader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        if (data == null){
            return;
        }
        playlistListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        loader.reset();
        playlistListAdapter.notifyDataSetChanged();

    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(playloader, null, this);
    }
}
