package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Album;

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

public class AlbumLoader extends BaseAsyncTaskLoader<List<Album>> {

    private String Where;
    private String sortorder;
    private String[] selectionargs = null;

    private String[] datacol = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR
    };

    public AlbumLoader(Context context) {
        super(context);
    }

    @Override
    public List<Album> loadInBackground() {
        ArrayList<Album> albums = new ArrayList<>();
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor musicCursor = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, datacol, Where, selectionargs, sortorder);
            if (musicCursor != null && musicCursor.moveToFirst()) {
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
                int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
                int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
                int numOfSongsColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);
                int albumfirstColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR);
                do {
                    String albumName = musicCursor.getString(titleColumn);
                    long albumId = musicCursor.getLong(idColumn);
                    String artistName = musicCursor.getString(artistColumn);
                    int year = musicCursor.getInt(albumfirstColumn);
                    int no = musicCursor.getInt(numOfSongsColumn);
                    Album album = new Album();
                    /**
                     * Setting Album Metadata
                     */
                    album.setArtistName(artistName);
                    album.setAlbumName(albumName);
                    album.setId(albumId);
                    album.setTrackCount(no);
                    album.setYear(year);
                    albums.add(album);
                } while (musicCursor.moveToNext());
                musicCursor.close();
            }
            if (musicCursor == null) {
                return Collections.emptyList();
            }
            return albums;
        } else {
            return null;
        }
    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }

    public void filterartistsong(String filter, String[] args) {
        Where = filter;
        selectionargs = args;
    }

}
