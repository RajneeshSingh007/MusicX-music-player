package com.rks.musicx.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.loaders.PlaylistLoaders;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.interfaces.RefreshPlaylist;
import com.rks.musicx.interfaces.playlistPicked;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlaylistHelper;
import com.rks.musicx.ui.adapters.PlaylistListAdapter;

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

public class PlayListPicker extends DialogFragment implements LoaderManager.LoaderCallbacks<List<Playlist>> {

    private final int playlistLoader = -1;
    private RecyclerView rv;
    private PlaylistListAdapter playlistListAdapter;
    private playlistPicked onPlaylistPicked;
    private String ateKey;
    private int colorAccent;
    private Button CreatePlaylist;

    private View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.create_playlist:
                PlaylistHelper.showCreatePlaylistDialog(getContext(), new RefreshPlaylist() {
                    @Override
                    public void refresh() {
                        load();
                    }
                });
                break;
        }
    };
    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    if (onPlaylistPicked != null) {
                        onPlaylistPicked.onPlaylistPicked(playlistListAdapter.getItem(position));
                    }
                    dismiss();
                    break;
                case R.id.delete_playlist:
                    showMenu(view, playlistListAdapter.getItem(position));
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
                        PlaylistHelper.deletePlaylistDailog(getContext(), playlist.getName(), new RefreshPlaylist() {
                            @Override
                            public void refresh() {
                                load();
                            }
                        });
                        break;

                    case R.id.action_playlist_rename:
                        PlaylistHelper.showRenameDialog(getContext(), new RefreshPlaylist() {
                            @Override
                            public void refresh() {
                                load();
                            }
                        }, playlist.getId());
                        break;

                }
                return false;
            }
        });
        popup.show();
    }


    public PlayListPicker newInstance() {
        return new PlayListPicker();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_picker, new LinearLayout(getContext()), false);

        rv = (RecyclerView) rootView.findViewById(R.id.rv);

        MaterialDialog.Builder pickDialog = new MaterialDialog.Builder(getContext());
        pickDialog.title(R.string.choose_playlist);
        playlistListAdapter = new PlaylistListAdapter(getContext());
        playlistListAdapter.setOnItemClickListener(onClick);
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        rv.setLayoutManager(customLayoutManager);
        rv.setAdapter(playlistListAdapter);
        ateKey = Helper.getATEKey(getContext());
        colorAccent = Config.accentColor(getContext(), ateKey);
        CreatePlaylist = (Button) rootView.findViewById(R.id.create_playlist);
        CreatePlaylist.setOnClickListener(mOnClickListener);
        CreatePlaylist.setBackgroundColor(colorAccent);
        pickDialog.customView(rootView, false);
        loadPlaylist();
        return pickDialog.show();
    }


    public void load() {
        getLoaderManager().restartLoader(playlistLoader, null, this);
    }

    public void setPicked(playlistPicked listener) {
        onPlaylistPicked = listener;
    }


    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        PlaylistLoaders playlistloader = new PlaylistLoaders(getContext());
        if (id == playlistLoader) {
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
        playlistListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        loader.reset();
        playlistListAdapter.notifyDataSetChanged();

    }

    /*
    *Load Playlist
    */
    private void loadPlaylist() {
        getLoaderManager().initLoader(playlistLoader, null, this);
    }

}
