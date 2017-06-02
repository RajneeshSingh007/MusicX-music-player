package com.rks.musicx.data.network;

/*
 * Created by Coolalien on 12/23/2016.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rks.musicx.R;
import com.rks.musicx.data.network.model.Artist;
import com.rks.musicx.data.network.model.Artist__;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.FileTarget;
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
    private Helper helper;
    private File file;

    public ArtistArtwork(Context context, String artistName) {
        this.context = context;
        this.artistName = artistName;
        helper = new Helper(context);
        clients = new Clients(context, Constants.lastFmUrl);
        services = clients.createService(Services.class);
        file = new File(helper.getArtistArtworkLocation() + artistName + ".jpeg");
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
                        for (Image_ artistArtwork : artist1.getImage()) {
                            if (!file.exists()) {
                                    if (Extras.getInstance().hqArtistArtwork()){
                                        if (artistArtwork.getSize().equals("mega")){
                                            Glide
                                                    .with(context)
                                                    .load(artistArtwork.getText())
                                                    .asBitmap()
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .placeholder(R.mipmap.ic_launcher)
                                                    .error(R.mipmap.ic_launcher)
                                                    .format(DecodeFormat.PREFER_ARGB_8888)
                                                    .centerCrop()
                                                    .override(600, 600)
                                                    .into(new FileTarget(file.getAbsolutePath(), 600, 600));
                                        }
                                    }else {
                                        if (artistArtwork.getSize().equals("extralarge")){
                                            Glide
                                                    .with(context)
                                                    .load(artistArtwork.getText())
                                                    .asBitmap()
                                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                    .skipMemoryCache(true)
                                                    .placeholder(R.mipmap.ic_launcher)
                                                    .error(R.mipmap.ic_launcher)
                                                    .format(DecodeFormat.PREFER_ARGB_8888)
                                                    .centerCrop()
                                                    .override(600, 600)
                                                    .into(new FileTarget(file.getAbsolutePath(), 600, 600));
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
    }


}
