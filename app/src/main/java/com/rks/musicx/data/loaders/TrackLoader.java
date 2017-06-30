package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.Helper;

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

public class TrackLoader extends BaseAsyncTaskLoader<List<Song>> {

    private String Where = null;
    private String sortorder = null;
    private String[] selectionargs = null;
    private String mFilter = null;

    private String[] datacol = {MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.COMPOSER
    };

    public TrackLoader(Context context) {
        super(context);
    }

    @Override
    public List<Song> loadInBackground() {

        List<Song> songList = new ArrayList<>();

        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, datacol, Where, selectionargs, sortorder);

            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                int artistIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                do {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    int track = cursor.getInt(trackCol);
                    String mSongPath = cursor.getString(datacol);
                    String year = cursor.getString(yearCol);
                    long artistId = cursor.getLong(artistIdCol);

                    Song song = new Song();
                    /*
                    Setup metadata of songs
                     */
                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);
                    song.setYear(year);
                    song.setArtistId(artistId);
                    songList.add(song);
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (cursor == null) {
                return Collections.emptyList();
            }
            return songList;
        } else {
            return null;
        }
    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }

    public void filteralbumsong(String filter, String[] args) {
        Where = filter + " AND " + MediaStore.Audio.Media.DURATION + ">=" + Helper.filterAudio(); // list song greater than 30sec duration
        selectionargs = args;
    }

    public String getFilter() {
        return mFilter;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }

}
