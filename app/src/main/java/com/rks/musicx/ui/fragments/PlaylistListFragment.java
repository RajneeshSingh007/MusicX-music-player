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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.base.BaseRefreshFragment;
import com.rks.musicx.data.loaders.PlaylistLoaders;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlaylistHelper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.PlaylistListAdapter;
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

public class PlaylistListFragment extends BaseRefreshFragment implements LoaderCallbacks<List<Playlist>> {

    private FastScrollRecyclerView rv;
    private PlaylistListAdapter playlistAdapter;
    private int playloader = -1;

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (getActivity() == null) {
                return;
            }
            Playlist playlist = playlistAdapter.getItem(position);
            switch (view.getId()) {
                case R.id.item_view:
                    if (playlistAdapter.getSnapshot().size() > 0 && position < playlistAdapter.getSnapshot().size()) {
                        Extras.getInstance().savePlaylistId(playlist.getId());
                        PlaylistFragment fragment = PlaylistFragment.newInstance(playlist);
                        ((MainActivity) getActivity()).setFragment(fragment);
                    }
                    break;
                case R.id.delete_playlist:
                    showMenu(view, playlistAdapter.getItem(position));
                    break;
            }
        }
    };


    private void showMenu(View view, Playlist playlist) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist_delete:
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
                        builder.title(playlist.getName());
                        builder.content(getContext().getString(R.string.deleteplaylist));
                        builder.positiveText(R.string.delete);
                        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                PlaylistHelper.deletePlaylist(getContext(), playlist.getName());
                                Toast.makeText(getContext(), playlist.getName() + " Deleted", Toast.LENGTH_SHORT).show();
                                load();
                            }
                        });
                        builder.typeface(Helper.getFont(getContext()), Helper.getFont(getContext()));
                        builder.negativeText(R.string.cancel);
                        builder.show();
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_list, menu);
        menu.findItem(R.id.shuffle_all).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        switch (item.getItemId()) {
            case R.id.action_create_playlist:
                showCreatePlaylistDialog();
                break;
            case R.id.menu_sort_by_az:
                extras.setPlaylistSortOrder(SortOrder.PlaylistSortOrder.PLAYLIST_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                extras.setPlaylistSortOrder(SortOrder.PlaylistSortOrder.PLAYLIST_Z_A);
                load();
                break;
            case R.id.menu_sort_by_date:
                extras.setPlaylistSortOrder(SortOrder.PlaylistSortOrder.PLAYLIST_DATE_MODIFIED);
                load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreatePlaylistDialog() {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.create_playlist, null);
        MaterialDialog.Builder createplaylist = new MaterialDialog.Builder(getContext());
        createplaylist.title(R.string.create_playlist);
        createplaylist.positiveText(android.R.string.ok);
        TextInputEditText editText = (TextInputEditText) layout.findViewById(R.id.playlist_name);
        createplaylist.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                PlaylistHelper.createPlaylist(getContext().getContentResolver(), editText.getText().toString());
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
        createplaylist.typeface(Helper.getFont(getContext()), Helper.getFont(getContext()));
        createplaylist.customView(layout, false);
        createplaylist.show();
    }


    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        PlaylistLoaders playlistloader = new PlaylistLoaders(getContext());
        if (id == playloader) {
            playlistloader.setSortOrder(Extras.getInstance().getPlaylistSort());
            return playlistloader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        if (data == null) {
            return;
        }
        playlistAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        loader.reset();
        playlistAdapter.notifyDataSetChanged();

    }


    @Override
    protected int setLayout() {
        return R.layout.common_rv;
    }

    @Override
    protected void ui(View view) {
        rv = (FastScrollRecyclerView) view.findViewById(R.id.commonrv);
    }

    @Override
    protected void funtion() {
        CustomLayoutManager customlayout = new CustomLayoutManager(getContext());
        customlayout.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customlayout);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        playlistAdapter = new PlaylistListAdapter(getContext());
        playlistAdapter.setOnItemClickListener(mOnClick);
        rv.setAdapter(playlistAdapter);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(playloader, null, this);
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(playloader, null, this);
    }
}
