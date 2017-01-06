package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.data.model.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtistLoader extends BaseAsyncTaskLoader<List<Artist>> {


    private String sortorder;

    public ArtistLoader(Context context) {
        super(context);
    }

    @Override
    public List<Artist> loadInBackground() {

        List<Artist> artistList = new ArrayList<>();

        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED){
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,null,null,null,sortorder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(BaseColumns._ID);
                int nameCol = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST);
                int albumsNbCol = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS);
                int tracksNbCol = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);
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
        }else {
            return null;
        }

    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }
}
