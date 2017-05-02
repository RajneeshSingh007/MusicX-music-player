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
import static com.rks.musicx.misc.utils.Constants.Queue_TableName;

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

public class Queue extends SQLiteOpenHelper implements DefaultColumn {

    public Queue(Context context) {
        super(context, Queue_TableName, null, DbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DefaultColumn(Queue_TableName));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.Queue_TableName);
        onCreate(sqLiteDatabase);
    }

    public void add(Song song) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
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
        sqLiteDatabase.insertWithOnConflict(Queue_TableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(Queue_TableName, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void add(List<Song> songList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Song song : songList) {
                addSongMetaData(db, song);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<Song> read() {
        SQLiteDatabase db = getReadableDatabase();
        List<Song> list = new ArrayList<>();
        Cursor cursor = db.query(Queue_TableName, null, null, null, null, null, null);
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
        db.close();
        return list;
    }

    public boolean exists(long songId) {
        SQLiteDatabase db = getReadableDatabase();
        boolean result = false;
        Cursor cursor = db.query(Queue_TableName, null, SongId + "= ?", new String[]{String.valueOf(songId)}, null, null, null, "1");
        if (cursor != null && cursor.moveToFirst()) {
            result = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public void delete(long songId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Queue_TableName, SongId + "= ?", new String[]{String.valueOf(songId)});
        db.close();
    }
}
