package com.rks.musicx.data.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.rks.musicx.data.network.VagModel.Mu;
import com.rks.musicx.data.network.VagModel.Vag;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

public class LyricsData extends AsyncTask<String, String, String> {

    private String songName, songArtist, finallyLoaded, songPath;
    private TextView setLyrics;
    private Call<Vag> vagCall;
    private Context context;

    public LyricsData(Context context, String songName, String songArtist, String songpath, TextView setLyrics) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.context = context;
        this.setLyrics = setLyrics;
        this.songPath = songpath;
    }

    @Override
    protected String doInBackground(String... strings) {

        VagClient vagClient = new VagClient(context);
        VagServices vagServices = vagClient.createService(VagServices.class);
        vagCall = vagServices.getLyrics(songArtist, songName);
        return "Executed";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null) {
            vagCall.enqueue(new Callback<Vag>() {
                @Override
                public void onResponse(Call<Vag> call, Response<Vag> response) {
                    if (response.isSuccessful()) {
                        Vag vag = response.body();
                        List<Mu> lyricsdata = vag.getMus();
                        if (lyricsdata.size() > 0) {
                            finallyLoaded = lyricsdata.get(0).getText();
                            if (Extras.getInstance().saveLyrics()) {
                                String path = new Helper(context).loadLyrics(songName);
                                Helper.saveLyrics(path, finallyLoaded);
                            }
                            setLyrics.setText(finallyLoaded);
                        }else {
                            setLyrics.setText(Helper.getInbuiltLyrics(songPath));
                        }
                    } else {
                        Log.d("r u kidding me ?", "connect your phone to internet");
                    }
                }

                @Override
                public void onFailure(Call<Vag> call, Throwable t) {
                    Log.d("LyricsData", "lol error", t);
                }
            });
        }
    }


}
