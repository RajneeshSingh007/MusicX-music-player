package com.rks.musicx.data.network;

/*
 * Created by Coolalien on 12/23/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.rks.musicx.data.network.model.Artist;
import com.rks.musicx.data.network.model.Artist__;
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
    private Clients clients;
    private Services services;
    private Context context;
    private String artistName;
    private ImageView imageView;
    private palette palettework;
    private Helper helper;
    private boolean hqdownloaded = false, lowdownloaded = false;

    public ArtistArtwork(Context context, String artistName, ImageView imageView, palette palettework) {
        this.context = context;
        this.artistName = artistName;
        this.imageView = imageView;
        this.palettework = palettework;
        helper = new Helper(context);
        clients = new Clients(context, Constants.lastFmUrl);
        services = clients.createService(Services.class);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        artistCall = services.getartist(artistName);
        artistCall.enqueue(new Callback<Artist>() {
            @Override
            public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<Artist> response) {
                com.rks.musicx.data.network.model.Artist getartist = response.body();
                if (response.isSuccessful() && getartist != null) {
                    final Artist__ artist1 = getartist.getArtist();
                    if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                        String artistImagePath = new Helper(context).loadArtistImage(artistName);
                        File file = new File(artistImagePath);
                        File file1 = new File(helper.getAlbumArtworkLocation(), artistName + ".jpeg");
                        for (Image_ artistArtwork : artist1.getImage()) {
                            if (!file.exists()) {
                                if (!Extras.getInstance().saveData()) {
                                    if (Extras.getInstance().hqArtistArtwork()){
                                        if (artistArtwork.getSize().equals("mega")){
                                            hqdownloaded = true;
                                            AndroidNetworking.download(artistArtwork.getText(), helper.getArtistArtworkLocation(), artistName + ".jpeg")
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
                                    }else {
                                        if (artistArtwork.getSize().equals("extralarge")){
                                            lowdownloaded = true;
                                            AndroidNetworking.download(artistArtwork.getText(), helper.getArtistArtworkLocation(), artistName + ".jpeg")
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
        String artistImagePath = new Helper(context).loadArtistImage(artistName);
        File file = new File(artistImagePath);
        ArtworkUtils.ArtworkLoader(context, null, file.getAbsolutePath(),0, new palette() {
            @Override
            public void palettework(Palette palette) {
                palettework.palettework(palette);
            }
        }, new bitmap() {
            @Override
            public void bitmapwork(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void bitmapfailed(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }


}
