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
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.data.model.Song;
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
                addSongToPlaylist(context.getContentResolver(), playlist.getId(), song);
                Toast.makeText(context, "Song is added ", Toast.LENGTH_SHORT).show();
            }
        });
        playListPicker.show(fragment.getFragmentManager(), null);
    }

    public static void PlaylistMultiChooser(Fragment fragment, Context context, List<Song> songList) {
        if (songList == null) {
            return;
        }
        PlayListPicker playListPicker = new PlayListPicker();
        playListPicker.setPicked(new playlistPicked() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                try {
                    for (Song song : songList) {
                        addSongToPlaylist(context.getContentResolver(), playlist.getId(), song.getId());
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
     * @param resolver
     * @param playlistId
     * @param songId
     */
    public static void addSongToPlaylist(ContentResolver resolver, long playlistId, long songId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        final int base = getSongCount(resolver, uri);
        insertPlaylist(resolver, uri, songId, base + 1);
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
     * @param resolver
     * @param playlistName
     * @return
     */
    public static Uri createPlaylist(ContentResolver resolver, String playlistName) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);
        return resolver.insert(uri, values);
    }

    /**
     * Return Playlist
     *
     * @param resolver
     * @param uri
     * @param songId
     * @param index
     */
    private static void insertPlaylist(ContentResolver resolver, Uri uri, long songId, int index) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
        resolver.insert(uri, values);

    }
}
