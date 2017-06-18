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
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rks.musicx.data.network.model.Album;
import com.rks.musicx.data.network.model.Album_;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.FileTarget;
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
    private Helper helper;
    private File file;
    private RequestManager mRequestManager;
    private FileTarget lowTarget, highTarget;

    public AlbumArtwork(@NonNull Context context, String artistName, String albumName) {
        this.context = context;
        this.artistName = artistName;
        this.albumName = albumName;
        helper = new Helper(context);
        clients = new Clients(context, Constants.lastFmUrl);
        services = clients.createService(Services.class);
        file = new File(helper.getAlbumArtworkLocation(), albumName + ".jpeg");
        mRequestManager = Glide.with(context);
        lowTarget = new FileTarget(file.getAbsolutePath(), 300, 300);
        highTarget = new FileTarget(file.getAbsolutePath(), 600, 600);
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
                        for (Image_ artistArtwork : album_.getImageList()){
                            if (!file.exists()){
                                    if (Extras.getInstance().hqArtistArtwork()){
                                        if(artistArtwork.getSize().equals("mega")){
                                            mRequestManager
                                                    .load(artistArtwork.getText())
                                                    .asBitmap()
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .format(DecodeFormat.PREFER_ARGB_8888)
                                                    .override(600, 600)
                                                    .into(highTarget);
                                        }
                                    }else {
                                        if(artistArtwork.getSize().equals("extralarge")) {
                                            mRequestManager
                                                    .load(artistArtwork.getText())
                                                    .asBitmap()
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .override(300, 300)
                                                    .into(lowTarget);
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
            public void onFailure(Call<Album> call, Throwable t) {
                Log.d("AlbumArtwork", "error", t);
            }
        });
        return null;

    }


}
