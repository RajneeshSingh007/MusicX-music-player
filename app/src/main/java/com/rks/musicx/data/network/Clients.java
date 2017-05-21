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

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Coolalien on 5/7/2017.
 */
public class Clients {

    private Context context;
    private String baseUrl;
    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public Clients(Context context, String url) {
        this.context = context;
        this.baseUrl = url;
    }

    public <S> S createService(Class<S> serviceClass) {
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build()).build();
        return builder.create(serviceClass);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
