package com.rks.musicx.database;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.rks.musicx.data.loaders.DefaultSongLoader;
import com.rks.musicx.misc.utils.Constants;

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

public class FavHelper {

    private Context context;
    private DefaultSongLoader defaultSongLoader;
    private CommonDatabase commonDatabase;

    public FavHelper(Context context) {
        this.context = context;
        defaultSongLoader = new DefaultSongLoader(context);
        commonDatabase = new CommonDatabase(context, Constants.Fav_TableName, true);
        defaultSongLoader.setProvider(true);
    }

    public void addFavorite(long songId) {
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA,
        };
        if (defaultSongLoader.isProvider()) {
            defaultSongLoader.setUri(Uri.parse(String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)));
            defaultSongLoader.setSortOrder(MediaStore.Audio.Media.TITLE);
            defaultSongLoader.setQueryTable2(new String[]{String.valueOf(songId)});
            defaultSongLoader.setSelection(MediaStore.Audio.Media._ID + "= ?");
            defaultSongLoader.setQueryTable(projection);
            if (defaultSongLoader.getSongData() != null) {
                commonDatabase.add(defaultSongLoader.getSongData());
                commonDatabase.close();
            }
        }
    }

    public boolean isFavorite(long songId) {
        boolean result = commonDatabase.exists(songId);
        commonDatabase.close();
        return result;
    }

    public void removeFromFavorites(long songId) {
        commonDatabase.delete(songId);
        commonDatabase.close();
    }
}
