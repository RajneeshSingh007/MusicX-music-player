package com.rks.musicx.data.network;

/*
 * Created by Coolalien on 12/23/2016.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.rks.musicx.data.network.model.Artist;
import com.rks.musicx.data.network.model.Artist__;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

public class ArtistArtwork extends AsyncTask<Void, Void, Void> {

    private Call<Artist> artistCall;
    private LastFmClients lastFmClients;
    private LastFmServices lastFmServices;
    private Context context;
    private String artistName;

    public ArtistArtwork(Context context, String artistName) {
        this.context = context;
        this.artistName = artistName;
        lastFmClients = new LastFmClients(context);
        lastFmServices = lastFmClients.createService(LastFmServices.class);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        artistCall = lastFmServices.getartist(artistName);
        artistCall.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<Artist> response) {
                com.rks.musicx.data.network.model.Artist getartist = response.body();
                if (response.isSuccessful() && getartist != null) {
                    final Artist__ artist1 = getartist.getArtist();
                    if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                        String artistImagePath = new Helper(context).loadArtistImage(artistName);
                        File file = new File(artistImagePath);
                        for (Image_ artistArtwork : artist1.getImage()) {
                            if (Extras.getInstance().hqArtistArtwork()) {
                                if (file.exists()) {
                                    AndroidNetworking.delete(file.getAbsolutePath());
                                }
                                AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(context).getArtistArtworkLocation(), artistName + ".jpeg")

                                        .setTag("DownloadingArtistImage")
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

                            } else {
                                if (file.exists()) {
                                    AndroidNetworking.delete(file.getAbsolutePath());
                                }
                                AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(context).getArtistArtworkLocation(), artistName + ".jpeg")
                                        .setTag("DownloadingArtistImage")
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
                        }
                    } else {
                        Log.d("haha", "downloading failed");
                    }
                } else {
                    Log.d("haha", "downloading failed");
                }
            }

            @Override
            public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
                Log.d("ArtistArtwork", "error", t);
            }
        });
        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("ArtistArtwork", "Success");
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
