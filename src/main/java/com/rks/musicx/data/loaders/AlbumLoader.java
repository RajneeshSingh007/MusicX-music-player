package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.data.model.Album;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumLoader extends BaseAsyncTaskLoader<List<Album>> {

    private String Where;
    private String sortorder;
    private String [] selectionargs;

    public AlbumLoader(Context context) {
        super(context);
    }

    private String[] datacol = {BaseColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
            MediaStore.Audio.AlbumColumns.FIRST_YEAR
    };

    @Override
    public List<Album> loadInBackground() {
        ArrayList<Album> albums = new ArrayList<>();
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED){
            Cursor musicCursor = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, datacol, Where, selectionargs, sortorder);
            if (musicCursor != null && musicCursor.moveToFirst()) {
                int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
                int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
                int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);
                int numOfSongsColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);
                int albumfirstColumn = musicCursor.getColumnIndex(MediaStore.Audio.AlbumColumns.FIRST_YEAR);
                do{
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
                }while (musicCursor.moveToNext());
                musicCursor.close();
            }
            if (musicCursor == null) {
                return Collections.emptyList();
            }
            return albums;
        }else {
            return null;
        }
    }

    public void setSortOrder(String orderBy) {
        sortorder = orderBy;
    }

    public void filterartistsong(String filter, String [] args){
        Where = filter;
        selectionargs = args;
    }

}
