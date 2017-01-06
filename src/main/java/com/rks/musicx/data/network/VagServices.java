package com.rks.musicx.data.network;

import com.rks.musicx.data.network.VagModel.Vag;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Coolalien on 11/22/2016.
 */

public interface VagServices {


    @GET("search.php?apikey=b8308eb4f31bb04e4644e8c2a467bb37")
    Call<Vag> getLyrics(@Query("art") String artist, @Query("mus") String song);

    @GET("search.php?extra=alb&apikey=b8308eb4f31bb04e4644e8c2a467bb37")
    Call<Vag> getAlbumart(@Query("art") String artist, @Query("mus") String song);
}
