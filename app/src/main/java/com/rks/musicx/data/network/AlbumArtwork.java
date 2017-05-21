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
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.rks.musicx.data.network.model.Album;
import com.rks.musicx.data.network.model.Album_;
import com.rks.musicx.data.network.model.Image_;
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
    private String artistName, albumName;

    public AlbumArtwork(Context context, String artistName, String albumName) {
        this.context = context;
        this.artistName = artistName;
        this.albumName = albumName;
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
                        String artistImagePath = new Helper(context).loadAlbumImage(albumName);
                        File file = new File(artistImagePath);
                        for (Image_ albumArtwork : album_.getImageList()) {
                            if (Extras.getInstance().hqArtistArtwork()) {
                                if (!file.exists()) {
                                    AndroidNetworking.download(artworkQuality(albumArtwork), new Helper(context).getAlbumArtworkLocation(), albumName + ".jpeg")
                                            .setTag("DownloadingAlbumImage")
                                            .setPriority(Priority.MEDIUM)
                                            .build()
                                            .startDownload(new DownloadListener() {
                                                @Override
                                                public void onDownloadComplete() {
                                                    Log.d("Artist", "successfully downloaded");
                                                }

                                                @Override
                                                public void onError(ANError anError) {
                                                    Log.d("Artist", "failed");
                                                }
                                            });
                                }
                            } else {
                                if (!file.exists()) {
                                    AndroidNetworking.download(artworkQuality(albumArtwork), new Helper(context).getAlbumArtworkLocation(), albumName + ".jpeg")
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
        Log.d("AlbumArtwork", "Success");
    }

    private String artworkQuality(Image_ artistArtwork) {
        if (artistArtwork.getSize().equals("large")) {
            return artistArtwork.getText();
        } else if (artistArtwork.getSize().equals("mega")) {
            return artistArtwork.getText();
        }
        return null;
    }

}
