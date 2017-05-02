package com.rks.musicx.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.CreatedPlaylistLoader;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.playlistPicked;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
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
    private playlistPicked playlistPicked;
    private String ateKey;
    private int colorAccent;
    private Button CreatePlaylist;

    private View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.create_playlist:
                showCreatePlaylistDialog();
                break;
        }
    };
    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    if (playlistPicked != null) {
                        playlistPicked.onPlaylistPicked(playlistListAdapter.getItem(position));
                    }
                    dismiss();
                    break;
                case R.id.delete_playlist:
                    showMenu(view, playlistListAdapter.getItem(position));
                    break;
            }
        }
    };

    private void showCreatePlaylistDialog() {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.create_playlist, null);
        MaterialDialog.Builder createplaylist = new MaterialDialog.Builder(getContext());
        TextInputEditText editText = (TextInputEditText) layout.findViewById(R.id.playlist_name);
        createplaylist.title(R.string.create_playlist);
        createplaylist.positiveText(android.R.string.ok);
        createplaylist.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                new Helper(getContext()).createPlaylist(getContext().getContentResolver(), editText.getText().toString());
                Toast.makeText(getContext(), "Playlist Created", Toast.LENGTH_LONG).show();
                refresh();
            }
        });
        createplaylist.negativeText(android.R.string.cancel);
        createplaylist.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                createplaylist.autoDismiss(true);
            }
        });
        createplaylist.customView(layout, false);
        createplaylist.show();

    }

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
                                Helper.deletePlaylist(getContext(), playlist.getName());
                                Toast.makeText(getContext(),playlist.getName() + " Deleted", Toast.LENGTH_SHORT).show();
                                refresh();
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


    public PlayListPicker newInstance() {
        return new PlayListPicker();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.playlist_picker, null);

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


    public void refresh() {
        getLoaderManager().restartLoader(playlistLoader, null, this);
    }

    public void setPicked(playlistPicked listener) {
        playlistPicked = listener;
    }

    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        CreatedPlaylistLoader playlistloader = new CreatedPlaylistLoader(getContext());
        if (id == playlistLoader) {
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
