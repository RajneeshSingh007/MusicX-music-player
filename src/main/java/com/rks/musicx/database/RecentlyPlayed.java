package com.rks.musicx.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.DbVersion;
import static com.rks.musicx.misc.utils.Constants.DefaultColumn;
import static com.rks.musicx.misc.utils.Constants.RecentlyPlayed_TableName;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class RecentlyPlayed extends SQLiteOpenHelper implements DefaultColumn {


    private SQLiteDatabase sqLiteDatabase;

    public RecentlyPlayed(Context context) {
        super(context, RecentlyPlayed_TableName, null, DbVersion);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DefaultColumn(RecentlyPlayed_TableName));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.RecentlyPlayed_TableName);
        onCreate(sqLiteDatabase);
    }

    public void add(Song song) {
        sqLiteDatabase = getWritableDatabase();
        addSongMetaData(sqLiteDatabase, song);
        sqLiteDatabase.close();
    }

    private void addSongMetaData(SQLiteDatabase sqLiteDatabase, Song song) {
        ContentValues values = new ContentValues();
        values.put(SongId, song.getId());
        values.put(SongTitle, song.getTitle());
        values.put(SongAlbum, song.getAlbum());
        values.put(SongArtist, song.getArtist());
        values.put(SongNumber, song.getTrackNumber());
        values.put(SongPath, song.getmSongPath());
        values.put(SongAlbumId, song.getAlbumId());
        sqLiteDatabase.insertWithOnConflict(RecentlyPlayed_TableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeAll() {
        sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            sqLiteDatabase.delete(RecentlyPlayed_TableName, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        sqLiteDatabase.close();
    }

    public void add(List<Song> songList) {
        sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.beginTransaction();
        try {
            for (Song song : songList) {
                addSongMetaData(sqLiteDatabase, song);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
        sqLiteDatabase.close();
    }

    public List<Song> read() {
        return readLimit(-1);
    }

    public List<Song> readLimit(int limit) {
        sqLiteDatabase = getReadableDatabase();
        List<Song> list = new ArrayList<>();
        String order = _ID + " DESC";
        Cursor cursor;
        if (limit > 0) {
            cursor = sqLiteDatabase.query(RecentlyPlayed_TableName, null, null, null, null, null, order, String.valueOf(limit));
            if (cursor != null && cursor.moveToFirst()) {

                int idCol = cursor.getColumnIndex(SongId);
                int titleCol = cursor.getColumnIndex(SongTitle);
                int artistCol = cursor.getColumnIndex(SongArtist);
                int albumCol = cursor.getColumnIndex(SongAlbum);
                int albumIdCol = cursor.getColumnIndex(SongAlbumId);
                int trackCol = cursor.getColumnIndex(SongNumber);
                int datacol = cursor.getColumnIndex(SongPath);
                do {
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

                    Song song = new Song();

                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);

                    list.add(song);
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }
        } else {
            cursor = sqLiteDatabase.query(RecentlyPlayed_TableName, null, null, null, null, null, order);
            if (cursor != null && cursor.moveToFirst()) {

                int idCol = cursor.getColumnIndex(SongId);
                int titleCol = cursor.getColumnIndex(SongTitle);
                int artistCol = cursor.getColumnIndex(SongArtist);
                int albumCol = cursor.getColumnIndex(SongAlbum);
                int albumIdCol = cursor.getColumnIndex(SongAlbumId);
                int trackCol = cursor.getColumnIndex(SongNumber);
                int datacol = cursor.getColumnIndex(SongPath);

                do {
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

                    Song song = new Song();

                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);

                    list.add(song);
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }

        }

        sqLiteDatabase.close();
        return list;
    }

    public boolean exists(long songId) {
        sqLiteDatabase = getReadableDatabase();
        boolean result = false;
        Cursor cursor = sqLiteDatabase.query(RecentlyPlayed_TableName, null, SongId + "= ?", new String[]{String.valueOf(songId)}, null, null, null, "1");
        if (cursor != null && cursor.moveToFirst()) {
            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public void delete(long songId) {
        sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.delete(RecentlyPlayed_TableName, SongId + "= ?", new String[]{String.valueOf(songId)});
        sqLiteDatabase.close();
    }


}
