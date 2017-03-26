package com.rks.musicx.data.loaders;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import com.rks.musicx.data.model.Song;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class DefaultSongLoader {

    Context context;
    private String[] queryTable, queryTable2;
    private Song song = new Song();
    private Uri uri;
    private boolean provider = true;
    private SQLiteDatabase sqLiteDatabase;
    private String dbtable, groupby, having, SortOrder, selection;
    private Cursor cursor;
    private String songId, songTitle, songArtist, songTrack, songAlbum, songAlbumId, songData;

    public DefaultSongLoader(Context context) {
        this.context = context;
    }

    public Song getSongData() {
        if (provider) {
            cursor = context.getContentResolver().query(uri, queryTable, selection, queryTable2, SortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                /**
                 * @return songs metadata
                 */
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);
                String artist = cursor.getString(artistCol);
                String album = cursor.getString(albumCol);
                long albumId = cursor.getLong(albumIdCol);
                int track = cursor.getInt(trackCol);
                String mSongPath = cursor.getString(datacol);

                song.setAlbum(album);
                song.setmSongPath(mSongPath);
                song.setArtist(artist);
                song.setId(id);
                song.setAlbumId(albumId);
                song.setTrackNumber(track);
                song.setTitle(title);
            }
            if (cursor != null) {
                cursor.close();
            }

        } else {
            cursor = sqLiteDatabase.query(dbtable, queryTable, selection, queryTable2, groupby, having, SortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(songId);
                int titleCol = cursor.getColumnIndex(songTitle);
                int artistCol = cursor.getColumnIndex(songArtist);
                int albumCol = cursor.getColumnIndex(songAlbum);
                int albumIdCol = cursor.getColumnIndex(songAlbumId);
                int trackCol = cursor.getColumnIndex(songTrack);
                int datacol = cursor.getColumnIndex(songData);

                /**
                 * @return songs metadata
                 */
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);
                String artist = cursor.getString(artistCol);
                String album = cursor.getString(albumCol);
                long albumId = cursor.getLong(albumIdCol);
                int track = cursor.getInt(trackCol);
                String mSongPath = cursor.getString(datacol);

                song.setAlbum(album);
                song.setmSongPath(mSongPath);
                song.setArtist(artist);
                song.setId(id);
                song.setAlbumId(albumId);
                song.setTrackNumber(track);
                song.setTitle(title);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return song;
    }

    public void setSongData(String songData) {
        this.songData = songData;
    }

    public String[] getQueryTable() {
        return queryTable;
    }

    public void setQueryTable(String[] queryTable) {
        this.queryTable = queryTable;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String[] getQueryTable2() {
        return queryTable2;
    }

    public void setQueryTable2(String[] queryTable2) {
        this.queryTable2 = queryTable2;
    }

    public String getSortOrder() {
        return SortOrder;
    }

    public void setSortOrder(String sortOrder) {
        SortOrder = sortOrder;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isProvider() {
        return provider;
    }

    public SQLiteDatabase getSqLiteDatabase() {
        return sqLiteDatabase;
    }

    public void setSqLiteDatabase(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public String getDbtable() {
        return dbtable;
    }

    public void setDbtable(String dbtable) {
        this.dbtable = dbtable;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getGroupby() {
        return groupby;
    }

    public void setGroupby(String groupby) {
        this.groupby = groupby;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    public void setSongAlbumId(String songAlbumId) {
        this.songAlbumId = songAlbumId;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setSongTrack(String songTrack) {
        this.songTrack = songTrack;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
