package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.data.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class TrackLoader extends BaseAsyncTaskLoader<List<Song>> {

    private String Where;
    private String sortorder;
    private String[] selectionargs;
    private String mFilter;

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
                    Setup metadata of songs
                     */
                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);
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
        Where = filter;
        selectionargs = args;
    }

    public String getFilter() {
        return mFilter;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }
}
