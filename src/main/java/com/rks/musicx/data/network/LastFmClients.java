package com.rks.musicx.data.network;


import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Coolalien on 8/29/2016.
 */

public class LastFmClients {

    Context context;

    public LastFmClients(Context context){
        this.context = context;
    }
    private String LastFmUrl = "http://ws.audioscrobbler.com/2.0/";

    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(LastFmUrl)
            .addConverterFactory(GsonConverterFactory.create());

    public <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
