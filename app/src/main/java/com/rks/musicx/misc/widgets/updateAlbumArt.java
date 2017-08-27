package com.rks.musicx.misc.widgets;

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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.rks.musicx.interfaces.changeAlbumArt;

import java.io.File;

/**
 * Created by Coolalien on 5/6/2017.
 */
public class updateAlbumArt extends AsyncTask<Void, Void, Void> {

    private Uri albumCover;
    private ContentValues values;
    private String artrowkpath, filePath;
    private Context context;
    private long albumID;
    private changeAlbumArt changeAlbumArt;


    public updateAlbumArt(String artrowkpath, String filePath, Context context, long albumID, com.rks.musicx.interfaces.changeAlbumArt changeAlbumArt) {
        this.artrowkpath = artrowkpath;
        this.filePath = filePath;
        this.context = context;
        this.albumID = albumID;
        this.changeAlbumArt = changeAlbumArt;
    }

    @Override
    protected Void doInBackground(Void... params) {
        albumCover = Uri.parse("content://media/external/audio/albumart");
        try {
            context.getContentResolver().delete(ContentUris.withAppendedId(albumCover, albumID), null, null);
            values = new ContentValues();
            values.put("album_id", albumID);
            values.put("_data", artrowkpath);
        } catch (Exception e) {
            Log.d("playing", "error", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Uri newUri = context.getContentResolver().insert(albumCover, values);
        if (newUri != null || filePath != null) {
            File file = new File(filePath);
            Toast.makeText(context, "AlbumArt Changed", Toast.LENGTH_SHORT).show();
            Log.d("updateAlbumCover", "success hurray !!!");
            context.getContentResolver().notifyChange(albumCover, null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            changeAlbumArt.onPostWork();
        } else {
            Toast.makeText(context, "AlbumArt Failed", Toast.LENGTH_SHORT).show();
            Log.d("updateAlbumCover", "failed lol !!!");
        }
    }
}
