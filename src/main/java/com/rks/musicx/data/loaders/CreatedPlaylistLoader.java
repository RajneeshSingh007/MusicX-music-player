package com.rks.musicx.data.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.rks.musicx.data.model.Playlist;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Coolalien on 12/31/2016.
 */

public class CreatedPlaylistLoader extends BaseAsyncTaskLoader<List<Playlist>> {

    public CreatedPlaylistLoader(Context context) {
        super(context);
    }

    final String[] tableQuery = {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
    };

    @Override
    public List<Playlist> loadInBackground() {
        List<Playlist> playlistList = new ArrayList<>();
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,tableQuery, null,null,MediaStore.Audio.Playlists.DATE_MODIFIED);
        if (cursor != null && cursor.moveToFirst()){
            int idCol = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
            int nameCol = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            do {
                long id = cursor.getLong(idCol);
                String name = cursor.getString(nameCol);
                playlistList.add(new Playlist(id, name));
            } while (cursor.moveToNext());

            Collections.sort(playlistList, new Comparator<Playlist>() {

                @Override
                public int compare(Playlist lhs, Playlist rhs) {
                    Collator c = Collator.getInstance(Locale.getDefault());
                    c.setStrength(Collator.PRIMARY);
                    return c.compare(lhs.getName(), rhs.getName());
                }
            });
            cursor.close();
        }
        return playlistList;
    }
}
