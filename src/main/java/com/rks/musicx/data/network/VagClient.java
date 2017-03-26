package com.rks.musicx.data.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class VagClient {

    Context context;
    private String VagUrl = "http://api.vagalume.com.br/";
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    private Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(VagUrl)
            .addConverterFactory(GsonConverterFactory.create());

    public VagClient(Context context) {
        this.context = context;
    }

    public <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
