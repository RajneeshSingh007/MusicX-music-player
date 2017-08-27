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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.LyricsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


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
        File file = new File(Helper.getAlbumArtworkLocation(), albumName + ".jpeg");
        AndroidNetworking.get(Constants.lastFmUrl + "?method=album.getinfo&format=json&api_key=" + Services.lastFmApi)
                .addQueryParameter("album", albumName)
                .addQueryParameter("artist", artistName)
                .setTag("AlbumArtwork")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            try {
                                JSONObject json = response.getJSONObject("album");
                                JSONArray jsonObject = json.getJSONArray("image");
                                JSONObject exLarge = jsonObject.getJSONObject(3);
                                String exLargeUrl = exLarge.getString("#text");
                                JSONObject large = jsonObject.getJSONObject(2);
                                String largeUrl = large.getString("#text");
                                if (!file.exists()) {
                                    AndroidNetworking.download(Extras.getInstance().hqArtistArtwork() ? exLargeUrl : largeUrl, Helper.getAlbumArtworkLocation(), albumName + ".jpeg")
                                            .setPriority(Priority.HIGH)
                                            .build()
                                            .startDownload(new DownloadListener() {
                                                @Override
                                                public void onDownloadComplete() {
                                                    Log.d("NetworkLyricsHelper", "Success");
                                                }

                                                @Override
                                                public void onError(ANError anError) {

                                                }
                                            });
                                }
                            } catch (JSONException e) {
                                // ignored
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // ignored
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
        File file = new File(Helper.getArtistArtworkLocation(), artistName + ".jpeg");
        AndroidNetworking.get(Constants.lastFmUrl + "?method=artist.getinfo&format=json&api_key=" + Services.lastFmApi)
                .addQueryParameter("artist", artistName)
                .setTag("ArtistArtwork")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.length() > 0) {
                            try {
                                JSONObject json = response.getJSONObject("artist");
                                JSONArray jsonObject = json.getJSONArray("image");
                                JSONObject exLarge = jsonObject.getJSONObject(3);
                                String exLargeUrl = exLarge.getString("#text");
                                JSONObject large = jsonObject.getJSONObject(2);
                                String largeUrl = large.getString("#text");
                                if (!file.exists()) {
                                    AndroidNetworking.download(Extras.getInstance().hqArtistArtwork() ? exLargeUrl : largeUrl, Helper.getArtistArtworkLocation(), artistName + ".jpeg")
                                            .setPriority(Priority.HIGH)
                                            .build()
                                            .startDownload(new DownloadListener() {
                                                @Override
                                                public void onDownloadComplete() {
                                                    Log.d("NetworkLyricsHelper", "Success");
                                                }

                                                @Override
                                                public void onError(ANError anError) {

                                                }
                                            });
                                }
                            } catch (JSONException e) {
                                // ignored
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        // ignored
                    }
                });
    }


    /**
     * Fetch Lyrics from vag
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @8Search
     */
    public static void vagLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.vagUrl + queryLyrics(artistName, "-") + "/" + queryLyrics(songName, "-") + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div itemprop=description>";
                            String scrapEnd = "<div id=lyrFoot>";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                int start = response.indexOf(scrapStart);
                                int end = response.indexOf(scrapEnd);
                                if (start >= 0 && end >= 0) {
                                    String fin = TextUtils.substring(response, start, end);
                                    if (fin.length() > 0) {
                                        // other unwanted stuff clearance
                                        fin = fin
                                                .trim()
                                                .replace("<div id=lyrFoot>", "")
                                                .replace("<div itemprop=description>", "")
                                                .replace("<hr>", "")
                                                .replace("<i>", "")
                                                .replace("</i>", "")
                                                .replaceAll("<a.*?</a>", "")
                                                .replace("<br/>", "\n")
                                                .replace("</div>", "");
                                        // set lyrics
                                        setLyrics(context, songName, path, fin, lyrics);
                                        Log.e("NetworkHelper", "lyrics from Vag");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        metroLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from metro
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @9Search
     */
    public static void metroLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.metroUrl + queryLyrics(songName, "-") + "-lyrics-" + queryLyrics(artistName, "-") + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div id=\"lyrics-body-text\" class=\"js-lyric-text\">";
                            String scrapEnd = "<p class=\"writers\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                int start = response.indexOf(scrapStart);
                                int end = response.indexOf(scrapEnd);
                                if (start >= 0 && end >= 0) {
                                    String fin = TextUtils.substring(response, start, end);
                                    if (fin.length() > 0) {
                                        // other unwanted stuff clearance
                                        fin = fin
                                                .trim()
                                                .replace("<div id=\"lyrics-body-text\" class=\"js-lyric-text\">", "")
                                                .replace("<p class='verse'>", "\n")
                                                .replace("<br>", "")
                                                .replace("<i>", "")
                                                .replace("</i>", "")
                                                .replaceAll("<a.*?</a>", "")
                                                .replace("</p>", "\n")
                                                .replace("</div>", "");
                                        // set lyrics
                                        setLyrics(context, songName, path, fin, lyrics);
                                        Log.e("NetworkHelper", "lyrics from Metro");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        directLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from atoz
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @5Search
     */
    public static void atozLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.atozUrl + queryLyrics(artistName, "") + "/" + queryLyrics(songName, "") + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div>";
                            String scrapEnd = "<div class=\"noprint\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replaceAll("<!--.*?-->", "")
                                            .replace("<div>", "")
                                            .replace("<b>", "")
                                            .replace("</b>", "")
                                            .replace("<br>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replaceAll("<a.*?</a>", "")
                                            .replace("</div>", "");
                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from AtoZ");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        lyricsondemandLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from songLyrics
     *
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @3Search
     */
    public static void songsLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.songlyricsUrl + queryLyrics(artistName, "-") + "/" + queryLyrics(songName, "-") + "-lyrics" + "/";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div id=\"songLyricsDiv-outer\">";
                            String scrapEnd = "<div itemscope itemtype=\"http://schema.org/MusicRecording\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<div id=\"songLyricsDiv-outer\">", "")
                                            .replaceAll("<p id=\"songLyricsDiv\"  class=\"songLyricsV14 iComment-text\">", "")
                                            .replace("<br />", "")
                                            .replaceAll("<a.*?</a>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replace("<br>", "")
                                            .replace("</div>", "")
                                            .replace("</p>", "");
                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from SongLyrics");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        lyricsbogieLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from lyricsondemand
     *
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @6Search
     */
    public static void lyricsondemandLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        char firstLetter = artistName.charAt(0);
        String gotFirstChar = String.valueOf(firstLetter)
                .toLowerCase();
        String extraStuff = "lyrics";
        String url = Constants.lyricsondemandUrl + gotFirstChar + "/" + queryLyrics(artistName, "") + extraStuff + "/" + queryLyrics(songName, "") + extraStuff + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div class=\"lcontent\" >";
                            String scrapEnd = "<div id=\"lfcredits\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<div class=\"lcontent\" >", "")
                                            .replace("</div>", "")
                                            .replace("<br />", "\n")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replaceAll("<!--.*?-->", "");
                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from LyricsOnDemand");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        absolutesLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from absolutelyrics
     *
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @7Search
     */
    public static void absolutesLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.absolutelyricsUrl + queryLyrics(artistName, "_") + "/" + queryLyrics(songName, "_");
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<p id=\"view_lyrics\">";
                            String scrapEnd = "<div id=\"view_lyricsinfo\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<p id=\"view_lyrics\">", "")
                                            .replace("</p>", "")
                                            .replace("<br />", "\n")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replace("<script language=JavaScript>", "")
                                            .replace("</script>", "")
                                            .replaceAll("document.write(.*)", "")
                                            .replaceAll("<!--.*?-->", "");

                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from Absolute");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        vagLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from indicine
     *
     * @param context
     * @param artistName
     * @param songName
     * @param path
     * @param lyrics
     * @1Search
     */
    public static void indicineLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.indicineUrl + "movies/lyrics/" + queryLyrics(songName, "-") + "-lyrics-" + queryLyrics(album, "-") + "/";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div class=\"lyrics\">";
                            String scrapEnd = "<a title=\"Hindi Lyrics\" href=\"http://www.indicine.com/hindi-lyrics/\"";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<div class=\"lyrics\">", "")
                                            .replace("<h2>", "")
                                            .replace("</h2>", "")
                                            .replace("<h1>", "")
                                            .replace("</h1>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replaceAll("<a.*?</a>", "")
                                            .replace(queryLyrics(songName, " "), "")
                                            .replace("<br />", "")
                                            .replace("<p>", "")
                                            .replace("</p>", "\n")
                                            .replace("&#8217;", "'")
                                            .replace("&#8230", "")
                                            .replace("</div>", "")
                                            .replace("</div>", "\n");
                                    StringBuffer buffer = new StringBuffer(fin);
                                    String start = "<div style=\"float: right;\">";
                                    String end = "</iframe>";
                                    if (fin.contains(start) && fin.contains(end)) {
                                        int removeStart = fin.indexOf(start);
                                        int removeEnd = fin.indexOf(end);
                                        buffer = buffer.replace(removeStart, removeEnd, "");
                                        String filter = buffer.toString();
                                        filter = filter.replace(end, "");
                                        // set lyrics
                                        setLyrics(context, songName, path, filter, lyrics);
                                        Log.e("NetworkHelper", "lyrics from Indicine");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        lyricsedLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from lyricsed
     *
     * @param context
     * @param artistName
     * @param songName
     * @param album
     * @param path
     * @param lyrics
     * @2Search
     */
    public static void lyricsedLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.lyricsedUrl + queryLyrics(songName, "-") + "-song-lyrics-" + queryLyrics(album, "-") + "/";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div class=\"entry\">";
                            String scrapEnd = "<span style=\"display:none\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance

                                    fin = fin
                                            .trim()
                                            .replace("<div class=\"entry\">", "")
                                            .replace("<h2>", "")
                                            .replace("</h2>", "")
                                            .replace("<br />", "")
                                            .replace("<p>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replace("</p>", "\n")
                                            .replace("&#8217;", "'")
                                            .replace("&#8230;", "...")
                                            .replace("<em>", " ")
                                            .replace("</em>", " ")
                                            .replaceAll("<a.*?</a>", "")
                                            .replaceAll("<!--.*?-->", "");
                                    StringBuffer buffer = new StringBuffer(fin);
                                    String start = "<strong>";
                                    String end = "</span>";
                                    if (fin.contains(start) && fin.contains(end)) {
                                        int removeStart = fin.indexOf(start);
                                        int removeEnd = fin.indexOf(end);
                                        buffer = buffer.replace(removeStart, removeEnd, "");
                                        String filter = buffer.toString();
                                        filter = filter
                                                .replace("</div>", "\n")
                                                .replace(end, "");
                                        // set lyrics
                                        setLyrics(context, songName, path, filter, lyrics);
                                        Log.e("NetworkHelper", "lyrics from Lyricssed");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        songsLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from lyricsbogie
     *
     * @param context
     * @param artistName
     * @param songName
     * @param album
     * @param path
     * @param lyrics
     * @4Search
     */
    public static void lyricsbogieLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.lyricsbogieUrl + queryLyrics(album, "-") + "/" + queryLyrics(songName, "-") + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div id=\"lyricsDiv\" class=\"left\">";
                            String scrapEnd = "<div class=\"right\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<div id=\"lyricsDiv\" class=\"left\">", "")
                                            .replace("<p id=\"view_lyrics\">", "")
                                            .replace("</blockquote>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replace("<br/>", "\n")
                                            .replace("<blockquote>", "")
                                            .replace("<p>", "")
                                            .replace("</div>", "")
                                            .replace("</p>", "\n\n");

                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from LyricsBoogie");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        atozLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    /**
     * Fetch Lyrics from HindiGeet
     *
     * @param context
     * @param artistName
     * @param songName
     * @param album
     * @param path
     * @param lyrics
     * @11Search
     */
    public static void HindiGeetLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.hindigeetUrl + "song/" + queryLyrics(songName, "_") + ".html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<pre>";
                            String scrapEnd = "</pre>";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin
                                            .trim()
                                            .replace("<pre>", "")
                                            .replace("</pre>", "");
                                    // set lyrics
                                    setLyrics(context, songName, path, fin, lyrics);
                                    Log.e("NetworkHelper", "lyrics from HindiGeet");
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        lyrics.setText(LyricsHelper.getInbuiltLyrics(path));
                    }
                });
    }

    /**
     * Fetch Lyrics from directLyrics
     *
     * @param context
     * @param artistName
     * @param songName
     * @param album
     * @param path
     * @param lyrics
     * @10Search
     */
    public static void directLyrics(Context context, String artistName, String songName, String album, String path, TextView lyrics) {
        String url = Constants.directUrl + queryLyrics(artistName, "-") + queryLyrics(songName, "-") + "-lyrics.html";
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.isEmpty()) {
                            String scrapStart = "<div class=\"lyrics lyricsselect\">";
                            String scrapEnd = "<ul class=\"menu\">";
                            if (response.contains(scrapStart) && response.contains(scrapEnd)) {
                                String fin = TextUtils.substring(response, response.indexOf(scrapStart), response.indexOf(scrapEnd));
                                if (fin.length() > 0) {
                                    // other unwanted stuff clearance
                                    fin = fin.trim()
                                            .replace(scrapStart, "")
                                            .replace("<br>", "")
                                            .replace("</p>", "\n")
                                            .replace("<p>", "")
                                            .replace("<hr>", "")
                                            .replace("<i>", "")
                                            .replace("</i>", "")
                                            .replaceAll("<a.*?</a>", "")
                                            .replace("<div class=\"ad\">", "")
                                            .replace("</div>", "")
                                            .replace("<ins id=\"lyrics-inline-300x250\">", "")
                                            .replace("</ins>", "");
                                    StringBuffer buffer = new StringBuffer(fin);
                                    String start = "<script>";
                                    String end = "</script>";
                                    if (fin.contains(start) && fin.contains(end)) {
                                        int removeStart = fin.indexOf(start);
                                        int removeEnd = fin.indexOf(end);
                                        buffer = buffer.replace(removeStart, removeEnd, "");
                                        String filter = buffer.toString();
                                        filter = filter.replace(end, "");
                                        // set lyrics
                                        setLyrics(context, songName, path, filter, lyrics);
                                        Log.e("NetworkHelper", "lyrics from Direct");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        HindiGeetLyrics(context, artistName, songName, album, path, lyrics);
                    }
                });
    }

    @NonNull
    private static String queryLyrics(String all, String replaceWord) {
        return all
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "")
                //  .replaceAll("[^A-Za-z0-9\\[\\]]", replaceWord)
                .replaceAll("\\s+", replaceWord)
                .toLowerCase();
    }

    private static void setLyrics(Context context, String songName, String path, String lyrics, TextView lrcView) {
        if (lrcView == null) {
            return;
        }
        String savePath = LyricsHelper.saveLyrics(songName);
        File file = new File(savePath);
        Log.e("Path", savePath);
        if (Extras.getInstance().saveLyrics()) {
            if (!file.exists()) {
                LyricsHelper.writeLyrics(savePath, lyrics);
            }
        }
        try {
            LyricsHelper.insertLyrics(path, lyrics);
        }catch (Exception e) {
            e.printStackTrace();
        }
        lrcView.setText(lyrics);
    }

}