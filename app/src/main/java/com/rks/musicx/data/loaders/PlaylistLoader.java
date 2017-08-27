package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Song;

import java.util.ArrayList;
import java.util.Collections;
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

public class PlaylistLoader extends BaseAsyncTaskLoader<List<Song>> {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Playlists.Members.TITLE, MediaStore.Audio.Playlists.Members.ARTIST,
            MediaStore.Audio.Playlists.Members.ALBUM, MediaStore.Audio.Playlists.Members.ALBUM_ID,
            MediaStore.Audio.Playlists.Members.ARTIST_ID, MediaStore.Audio.Playlists.Members.TRACK,
            MediaStore.Audio.Playlists.Members.DATA
    };

    private long mPlaylistId;

    public PlaylistLoader(Context context, long playlistId) {
        super(context);
        mPlaylistId = playlistId;
    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> playlist = new ArrayList<>();
        String sortorder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
        if (mPlaylistId == 0) {
            return null;
        }
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId), sProjection, "", null, sortorder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                if (idCol == -1) {
                    idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID);
                }
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID);
                int trackCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TRACK);
                int datacol = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA);
                do {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    int track = cursor.getInt(trackCol);
                    String mSongPath = cursor.getString(datacol);
                    Song song = new Song();
                    /*
                     Setup metadata of playlist
                    */
                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);
                    playlist.add(song);
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (cursor == null) {
                Collections.emptyList();
            }
            return playlist;
        } else {
            return null;
        }
    }

}
