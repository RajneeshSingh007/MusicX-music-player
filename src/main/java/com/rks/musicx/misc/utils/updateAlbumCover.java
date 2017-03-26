package com.rks.musicx.misc.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChosenImage;

import java.io.File;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class updateAlbumCover extends AsyncTask<Void, Void, Void> {

    private Uri albumCover;
    private ContentValues values;
    private long albumId;
    private Context context;
    private ChosenImage path;


    public updateAlbumCover(Context context, long albumId, ChosenImage path) {
        this.albumId = albumId;
        this.context = context;
        this.path = path;
    }

    @Override
    protected Void doInBackground(Void... params) {
        albumCover = Uri.parse("content://media/external/audio/albumart");
        context.getContentResolver().delete(ContentUris.withAppendedId(albumCover, albumId), null, null);
        values = new ContentValues();
        values.put("album_id", albumId);
        values.put("_data", path.getFilePathOriginal());
        return null;
    }


    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Uri newUri = context.getContentResolver().insert(albumCover, values);
        if (newUri != null) {
            context.getContentResolver().insert(Uri.parse("content://media/external/audio/albumart"), values);
            File file = new File("content://media/external/audio/albumart");
            Toast.makeText(context, "AlbumArt Changed", Toast.LENGTH_LONG).show();
            Log.d("updateAlbumCover", "success hurray !!!");
            context.getContentResolver().notifyChange(albumCover, null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

        } else {
            Toast.makeText(context, "AlbumArt Failed", Toast.LENGTH_LONG).show();
            Log.d("updateAlbumCover", "failed lol !!!");
        }
    }
}