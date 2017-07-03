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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Coolalien on 6/27/2017.
 */
public class NetworkHelper {

    /**
     * DownloadArtwork
     *
     * @param context
     * @param albumName
     * @param artistName
     */
    public static void downloadAlbumArtwork(Context context, String albumName, String artistName) {
        AndroidNetworking.get(Constants.lastFmUrl + "?method=album.getinfo&format=json&api_key=" + Services.lastFmApi)
                .addQueryParameter("album", albumName)
                .addQueryParameter("artist", artistName)
                .setTag("AlbumArtwork")
                .setPriority(Priority.MEDIUM)
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .setImageScaleType(ImageView.ScaleType.FIT_CENTER)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            Helper helper = new Helper(context);
                            File file = new File(helper.getAlbumArtworkLocation(), albumName + ".jpeg");
                            try {
                                JSONObject json = response.getJSONObject("album");
                                JSONArray jsonObject = json.getJSONArray("image");
                                if (!file.exists()) {
                                    if (Extras.getInstance().hqArtistArtwork()) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject(4);
                                        if (jsonObject1 != null) {
                                            String url = jsonObject1.getString("#text");
                                            if (url != null) {
                                                saveFileBg(url, file);
                                                Log.e("kool_mega", url);
                                            }
                                        }
                                    } else {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject(3);
                                        if (jsonObject1 != null) {
                                            String url = jsonObject1.getString("#text");
                                            if (url != null) {
                                                saveFileBg(url, file);
                                                Log.e("kool_extralarge", url);
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e("kool", response.optString("url"));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Failed", "no data");
                    }
                });
    }

    /**
     * DownloadArtistArtwork
     *
     * @param context
     * @param artistName
     */
    public static void downloadArtistArtwork(Context context, String artistName) {
        AndroidNetworking.get(Constants.lastFmUrl + "?method=artist.getinfo&format=json&api_key=" + Services.lastFmApi)
                .addQueryParameter("artist", artistName)
                .setTag("ArtistArtwork")
                .setPriority(Priority.MEDIUM)
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .setImageScaleType(ImageView.ScaleType.FIT_CENTER)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            Helper helper = new Helper(context);
                            File file = new File(helper.getArtistArtworkLocation(), artistName + ".jpeg");
                            try {
                                JSONObject json = response.getJSONObject("artist");
                                JSONArray jsonObject = json.getJSONArray("image");
                                if (!file.exists()) {
                                    if (Extras.getInstance().hqArtistArtwork()) {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject(4);
                                        if (jsonObject1 != null) {
                                            String url = jsonObject1.getString("#text");
                                            if (url != null) {
                                                saveFileBg(url, file);
                                                Log.e("kool_mega", url);
                                            }
                                        }
                                    } else {
                                        JSONObject jsonObject1 = jsonObject.getJSONObject(3);
                                        if (jsonObject1 != null) {
                                            String url = jsonObject1.getString("#text");
                                            if (url != null) {
                                                saveFileBg(url, file);
                                                Log.e("kool_extralarge", url);
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e("kool", response.optString("url"));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Failed", "no data");
                    }
                });
    }


    /**
     * DownloadArtistArtwork
     *
     * @param context
     * @param artistName
     */
    public static void downloadLyrics(Context context, String artistName, String songName, String path, TextView lyrics) {
        Helper helper = new Helper(context);
        AndroidNetworking.get(Constants.vagUrl + "search.php?" + "apikey=" + Services.lyricsApi)
                .addQueryParameter("art", artistName)
                .addQueryParameter("mus", songName)
                .setTag("DownloadLyrics")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            String savePath = helper.loadLyrics(songName);
                            try {
                                JSONArray jsonObject = response.getJSONArray("mus");
                                JSONObject object = jsonObject.getJSONObject(0);
                                if (object != null) {
                                    String finallyLoaded = object.getString("text");
                                    Log.e("lyrics", finallyLoaded);
                                    if (finallyLoaded != null) {
                                        if (Extras.getInstance().saveLyrics()) {
                                            Helper.saveLyrics(savePath, finallyLoaded);
                                        }
                                        lyrics.setText(finallyLoaded);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e("kool", response.optString("url"));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("Failed", "no data");
                    }
                });
    }

    /**
     * Save file async
     *
     * @param sUrl
     * @param file
     */
    public static void saveFileBg(String sUrl, File file) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                saveFile(sUrl, file);
                return null;
            }
        }.execute();
    }

    /**
     * Save files
     *
     * @param sUrl
     * @param file
     */
    public static void saveFile(String sUrl, File file) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            Log.i("DownloadTask", "Response " + connection.getResponseCode());
            input = connection.getInputStream();
            output = new FileOutputStream(file, false);

            byte data[] = new byte[8096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }

            if (connection != null)
                connection.disconnect();
        }
    }


}
