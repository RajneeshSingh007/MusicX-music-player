package com.rks.musicx.data.network;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.rks.musicx.data.network.model.Album;
import com.rks.musicx.data.network.model.Album_;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Coolalien on 5/10/2017.
 */
public class AlbumArtwork extends AsyncTask<Void, Void, Void> {

    private Call<Album> albumCall;
    private Clients clients;
    private Services services;
    private Context context;
    private long albumId;
    private String artistName, albumName;
    private ImageView imageView;
    private palette palettework;
    private Helper helper;

    public AlbumArtwork(Context context, String artistName, String albumName, long albumId, ImageView imageView, palette palettework) {
        this.context = context;
        this.artistName = artistName;
        this.albumName = albumName;
        this.albumId = albumId;
        this.imageView = imageView;
        this.palettework = palettework;
        helper = new Helper(context);
        clients = new Clients(context, Constants.lastFmUrl);
        services = clients.createService(Services.class);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        albumCall = services.getalbum(albumName, artistName);
        albumCall.enqueue(new Callback<Album>() {
            @Override
            public void onResponse(Call<Album> call, Response<Album> response) {
                Album getalbum = response.body();
                if (response.isSuccessful() && getalbum != null) {
                    final Album_ album_ = getalbum.getAlbum();
                    if (album_ != null && album_.getImageList() != null && album_.getImageList().size() > 0) {
                        String artistImagePath = helper.loadAlbumImage(albumName);
                        File file = new File(artistImagePath);
                        File file1 = new File(helper.getAlbumArtworkLocation(), albumName + ".jpeg");
                        for (Image_ artistArtwork : album_.getImageList()){
                           if (!file.exists()){
                               if (!Extras.getInstance().saveData()) {
                                   if (Extras.getInstance().hqArtistArtwork()){
                                       if(artistArtwork.getSize().equals("mega")){
                                           AndroidNetworking.download(artistArtwork.getText(), helper.getAlbumArtworkLocation(), albumName + ".jpeg")
                                                   .setTag("DownloadingAlbumImage")
                                                   .setPriority(Priority.MEDIUM)
                                                   .build()
                                                   .startDownload(new DownloadListener() {
                                                       @Override
                                                       public void onDownloadComplete() {
                                                           Log.d("Album", "successfully downloaded");
                                                       }

                                                       @Override
                                                       public void onError(ANError anError) {
                                                           Log.d("Album", "failed");
                                                       }
                                                   });
                                       }
                                   }else {
                                       if(artistArtwork.getSize().equals("extralarge")){
                                           AndroidNetworking.download(artistArtwork.getText(), helper.getAlbumArtworkLocation(), albumName + ".jpeg")
                                                   .setTag("DownloadingAlbumImage")
                                                   .setPriority(Priority.MEDIUM)
                                                   .build()
                                                   .startDownload(new DownloadListener() {
                                                       @Override
                                                       public void onDownloadComplete() {
                                                           Log.d("Album", "successfully downloaded");
                                                       }

                                                       @Override
                                                       public void onError(ANError anError) {
                                                           Log.d("Album", "failed");
                                                       }
                                                   });
                                       }
                                   }
                               }
                           }else {
                               boolean delete = file1.delete();
                               if (delete){
                                   Log.d("Artist", "deleted");
                               }else {
                                   Log.d("Artist", "delete failed");
                               }
                           }
                        }
                    } else {
                        Log.d("haha", "downloading failed");
                    }
                } else {
                    Log.d("haha", "downloading failed");
                }
            }

            @Override
            public void onFailure(Call<Album> call, Throwable t) {
                Log.d("AlbumArtwork", "error", t);
            }
        });
        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        ArtworkUtils.ArtworkLoader(context, albumName , null, albumId,palettework, new bitmap() {
            @Override
            public void bitmapwork(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void bitmapfailed(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
        Log.d("AlbumArtwork", "Success");
    }


}
