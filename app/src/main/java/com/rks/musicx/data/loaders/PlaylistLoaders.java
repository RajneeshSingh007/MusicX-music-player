package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Playlist;

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

public class PlaylistLoaders extends BaseAsyncTaskLoader<List<Playlist>> {

    private String sortorder;
    private String[] dataCol = new String[]{
            MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists._ID
    };

    public PlaylistLoaders(Context context) {
        super(context);
    }

    @Override
    public List<Playlist> loadInBackground() {
        List<Playlist> playlistList = new ArrayList<>();
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, dataCol, "", null, sortorder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
                do {
                    long id = cursor.getLong(idCol);
                    String name = cursor.getString(nameCol);

                    Playlist playlist = new Playlist();
                    playlist.setId(id);
                    playlist.setName(name);
                    playlistList.add(playlist);
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (cursor == null) {
                return Collections.emptyList();
            }
            return playlistList;
        } else {
            return null;
        }
    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }

}
