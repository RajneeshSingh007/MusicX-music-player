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

public class LyricsData extends AsyncTask<String, String, String> {

    private String songName, songArtist, finallyLoaded, songPath;
    private TextView setLyrics;
    private Call<Vag> vagCall;
    private Context context;

    public LyricsData(Context context, String songName, String songArtist, TextView setLyrics) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.context = context;
        this.setLyrics = setLyrics;
    }

    public LyricsData(Context context, String songName, String songArtist, String songPath, TextView setLyrics) {
        this.songName = songName;
        this.songArtist = songArtist;
        this.context = context;
        this.setLyrics = setLyrics;
        this.songPath = songPath;
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
                        } else {
                            setLyrics.setText(Helper.getInbuiltLyrics(songPath));
                            Log.d("ops ", "No lyrics found");
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
