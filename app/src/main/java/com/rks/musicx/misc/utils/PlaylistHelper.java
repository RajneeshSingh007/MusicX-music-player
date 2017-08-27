package com.rks.musicx.misc.utils;

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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.RefreshPlaylist;
import com.rks.musicx.interfaces.playlistPicked;
import com.rks.musicx.ui.fragments.PlayListPicker;

import java.util.List;

/**
 * Created by Coolalien on 6/19/2017.
 */
public class PlaylistHelper {

    /**
     * Delete Playlist Track
     *
     * @param context
     * @param playlistId
     * @param audioId
     */
    public static void deletePlaylistTrack(Context context, long playlistId, long audioId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        String filter = MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + audioId;
        resolver.delete(uri, filter, null);
    }

    /**
     * Delete playlist
     *
     * @param context
     * @param selectedplaylist
     */
    public static void deletePlaylist(Context context, String selectedplaylist) {
        String playlistid = getPlayListId(context, selectedplaylist);
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {playlistid};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
    }

    /**
     * Return Playlist Id
     *
     * @param context
     * @param playlist
     * @return
     */
    private static String getPlayListId(Context context, String playlist) {
        int recordcount;
        Uri newuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        final String playlistid = MediaStore.Audio.Playlists._ID;
        final String playlistname = MediaStore.Audio.Playlists.NAME;
        String where = MediaStore.Audio.Playlists.NAME + "=?";
        String[] whereVal = {playlist};
        String[] projection = {playlistid, playlistname};
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(newuri, projection, where, whereVal, null);
        if (cursor != null) {
            recordcount = cursor.getCount();
            String foundplaylistid = "";
            if (recordcount > 0) {
                cursor.moveToFirst();
                int idColumn = cursor.getColumnIndex(playlistid);
                foundplaylistid = cursor.getString(idColumn);
                cursor.close();
            }
            cursor.close();
            return foundplaylistid;
        } else {
            return "";
        }

    }

    /**
     * Playlist Chooser
     *
     * @param fragment
     * @param context
     * @param song
     */
    public static void PlaylistChooser(Fragment fragment, Context context, long song) {
        PlayListPicker playListPicker = new PlayListPicker();
        playListPicker.setPicked(new playlistPicked() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                addSongToPlaylist(context, playlist.getId(), song);
                Toast.makeText(context, "Song is added ", Toast.LENGTH_SHORT).show();
            }
        });
        playListPicker.show(fragment.getFragmentManager(), null);
    }

    /**
     * Multi playlist chooser
     *
     * @param fragment
     * @param context
     * @param songList
     */
    public static void PlaylistMultiChooser(Fragment fragment, Context context, List<Song> songList) {
        if (songList == null){
            return;
        }
        PlayListPicker playListPicker = new PlayListPicker();
        playListPicker.setPicked(new playlistPicked() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                try {
                    for (Song song : songList) {
                        addSongToPlaylist(context, playlist.getId(), song.getId());
                    }
                } finally {
                    Toast.makeText(context, "Song is added ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playListPicker.show(fragment.getFragmentManager(), null);
    }

    /**
     * Add songs to playlist
     *
     * @param playlistId
     * @param songId
     */
    public static void addSongToPlaylist(@NonNull Context context, long playlistId, long songId) {
        if (permissionManager.writeExternalStorageGranted(context)) {
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
            final int base = getSongCount(context.getContentResolver(), uri);
            insertPlaylist(context, uri, songId, base + 1);
        } else {
            Log.d("PlaylistHelper", "permissiongs failed");
        }
    }

    /**
     * Return song Count
     *
     * @param resolver
     * @param uri
     * @return
     */
    public static int getSongCount(ContentResolver resolver, Uri uri) {
        String[] cols = new String[]{"count(*)"};
        Cursor cur = resolver.query(uri, cols, null, null, null);
        if (cur != null) {
            cur.moveToFirst();
            final int count = cur.getInt(0);
            cur.close();
            return count;
        } else {
            return 0;
        }
    }

    /**
     * Create Playlist
     *
     * @param playlistName
     * @return
     */
    private static void createPlaylist(@NonNull Context context, String playlistName) {
        if (permissionManager.writeExternalStorageGranted(context)) {
            Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            String[] dataCol = new String[]{
                    MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists._ID
            };
            boolean isExist = true;
            Cursor cursor = context.getContentResolver().query(uri, dataCol, "", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
                do {
                    String name = cursor.getString(nameCol);
                    if (name.equals(playlistName)) {
                        isExist = false;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (isExist) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Playlists.NAME, playlistName);
                values.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
                context.getContentResolver().insert(uri, values);
            } else {
                Toast.makeText(context, "Playlist Already Exists", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("PlaylistHelper", "Permission failed");
        }
    }

    /**
     * Insert Playlist
     *
     * @param uri
     * @param songId
     * @param index
     */
    private static void insertPlaylist(@NonNull Context context, Uri uri, long songId, int index) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        //  values.put(MediaStore.Audio.Playlists.Members.DATE_ADDED, System.currentTimeMillis());
        //  values.put(MediaStore.Audio.Playlists.Members.DATE_MODIFIED, System.currentTimeMillis());
        context.getContentResolver().insert(uri, values);
    }

    /**
     * Rename Playlist
     *
     * @param context
     * @param newplaylist
     * @param playlist_id
     */
    public static void renamePlaylist(Context context, String newplaylist, long playlist_id) {
        if (permissionManager.writeExternalStorageGranted(context)) {
            Uri newuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            String where = MediaStore.Audio.Playlists._ID + " =? ";
            String[] whereVal = {Long.toString(playlist_id)};
            values.put(MediaStore.Audio.Playlists.NAME, newplaylist);
            resolver.update(newuri, values, where, whereVal);
        } else {
            Log.e("PlaylistHelper", "Permission failed");
        }
    }

    /**
     * Create Playlist dailog
     *
     * @param context
     * @param refreshPlaylist
     */
    public static void showCreatePlaylistDialog(@NonNull Context context, RefreshPlaylist refreshPlaylist) {
        View layout = LayoutInflater.from(context).inflate(R.layout.create_playlist, new LinearLayout(context), false);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(R.string.create_playlist);
        builder.positiveText(android.R.string.ok);
        TextInputEditText editText = (TextInputEditText) layout.findViewById(R.id.playlist_name);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                createPlaylist(context, editText.getText().toString());
                refreshPlaylist.refresh();
            }
        });
        builder.negativeText(android.R.string.cancel);
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                builder.autoDismiss(true);
            }
        });
        builder.typeface(Helper.getFont(context), Helper.getFont(context));
        builder.customView(layout, false);
        builder.show();
    }

    /**
     * Rename Playlist dailog
     *
     * @param context
     * @param refreshPlaylist
     */
    public static void showRenameDialog(@NonNull Context context, RefreshPlaylist refreshPlaylist, long id) {
        View view = LayoutInflater.from(context).inflate(R.layout.create_playlist, new LinearLayout(context), false);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Rename Playlist");
        builder.positiveText(android.R.string.ok);
        TextInputEditText editText = (TextInputEditText) view.findViewById(R.id.playlist_name);
        TextInputLayout inputLayout = (TextInputLayout) view.findViewById(R.id.inputlayout);
        inputLayout.setHint("Rename playlist");
        builder.autoDismiss(true);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                renamePlaylist(context, editText.getText().toString(), id);
                refreshPlaylist.refresh();
            }
        });
        builder.negativeText(android.R.string.cancel);
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        builder.typeface(Helper.getFont(context), Helper.getFont(context));
        builder.customView(view, false);
        builder.show();
    }

    /**
     * Delete playlist Dialog
     *
     * @param context
     * @param Playlistname
     */
    public static void deletePlaylistDailog(@NonNull Context context, String Playlistname, RefreshPlaylist refreshPlaylist) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title(Playlistname);
        builder.content(R.string.deleteplaylist);
        builder.positiveText(R.string.delete);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                deletePlaylist(context, Playlistname);
                Toast.makeText(context, Playlistname + " Deleted", Toast.LENGTH_SHORT).show();
                refreshPlaylist.refresh();
            }
        });
        builder.typeface(Helper.getFont(context), Helper.getFont(context));
        builder.negativeText(R.string.cancel);
        builder.show();
    }


}
