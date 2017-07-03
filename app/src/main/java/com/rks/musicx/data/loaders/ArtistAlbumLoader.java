package com.rks.musicx.data.loaders;

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

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Coolalien on 6/24/2017.
 */
public class ArtistAlbumLoader extends BaseAsyncTaskLoader<List<Artist>> {

    private long artistID;
    private String sortorder;

    public ArtistAlbumLoader(Context context) {
        super(context);
    }

    @Override
    public List<Artist> loadInBackground() {
        List<Artist> artistList = new ArrayList<>();

        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistID), null, null, null, sortorder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(BaseColumns._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ARTIST);
                int albumsNbCol = cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
                int tracksNbCol = cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS);
                do {
                    long id = cursor.getLong(idCol);
                    String artistName = cursor.getString(nameCol);
                    int albumCount = cursor.getInt(albumsNbCol);
                    int trackCount = cursor.getInt(tracksNbCol);
                    artistList.add(new Artist(id, artistName, albumCount, trackCount));
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (cursor == null) {
                return Collections.emptyList();
            }
            return artistList;
        } else {
            return null;
        }
    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }

    public void setArtistID(long artistID) {
        this.artistID = artistID;
    }
}
