package com.rks.musicx.data.network;

import com.rks.musicx.data.network.model.Album;
import com.rks.musicx.data.network.model.Artist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
 * Created by Coolalien on 6/28/2016.
 */

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

public interface LastFmServices {

    @GET("?method=artist.getinfo&api_key=658dd0ee3563543f1087e014b74be8a6&format=json")
    Call<Artist> getartist(@Query("artist") String artist);

    @GET("?method=album.getinfo&api_key=658dd0ee3563543f1087e014b74be8a6&format=json")
    Call<Album> getalbum(@Query("album") String album, @Query("artist") String artist);
}
