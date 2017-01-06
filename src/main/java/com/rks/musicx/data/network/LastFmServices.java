package com.rks.musicx.data.network;

import com.rks.musicx.data.network.model.Album;
import com.rks.musicx.data.network.model.Artist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Coolalien on 8/29/2016.
 */

public interface LastFmServices {

    @GET("?method=artist.getinfo&api_key=658dd0ee3563543f1087e014b74be8a6&format=json")
    Call<Artist> getartist(@Query("artist") String artist);

    @GET("?method=album.getinfo&api_key=658dd0ee3563543f1087e014b74be8a6&format=json")
    Call<Album> getalbum(@Query("album") String album, @Query("artist") String artist);
}
