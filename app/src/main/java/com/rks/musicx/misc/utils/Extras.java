package com.rks.musicx.misc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Environment;

import com.afollestad.appthemeengine.ATE;
import com.rks.musicx.MusicXApplication;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;
import static com.rks.musicx.misc.utils.Constants.ALBUMGRID;
import static com.rks.musicx.misc.utils.Constants.ALBUM_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.ARTISTGRID;
import static com.rks.musicx.misc.utils.Constants.ARTIST_ALBUM_SORT;
import static com.rks.musicx.misc.utils.Constants.ARTIST_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.ARTWORKCOLOR;
import static com.rks.musicx.misc.utils.Constants.AUDIO_FILTER;
import static com.rks.musicx.misc.utils.Constants.BlackTheme;
import static com.rks.musicx.misc.utils.Constants.BlurView;
import static com.rks.musicx.misc.utils.Constants.CURRENTPOS;
import static com.rks.musicx.misc.utils.Constants.DOWNLOADED_ARTWORK;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.EQSWITCH;
import static com.rks.musicx.misc.utils.Constants.FADEINOUT_DURATION;
import static com.rks.musicx.misc.utils.Constants.FADETRACK;
import static com.rks.musicx.misc.utils.Constants.FOLDERPATH;
import static com.rks.musicx.misc.utils.Constants.FloatingView;
import static com.rks.musicx.misc.utils.Constants.GridViewAlbum;
import static com.rks.musicx.misc.utils.Constants.GridViewArtist;
import static com.rks.musicx.misc.utils.Constants.GridViewSong;
import static com.rks.musicx.misc.utils.Constants.HD_ARTWORK;
import static com.rks.musicx.misc.utils.Constants.HIDE_LOCKSCREEEN;
import static com.rks.musicx.misc.utils.Constants.HIDE_NOTIFY;
import static com.rks.musicx.misc.utils.Constants.HQ_ARTISTARTWORK;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_X;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_Y;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.NAV_SETTINGS;
import static com.rks.musicx.misc.utils.Constants.PLAYER_POS;
import static com.rks.musicx.misc.utils.Constants.PLAYINGSTATE;
import static com.rks.musicx.misc.utils.Constants.PLAYINGVIEW_TRACK;
import static com.rks.musicx.misc.utils.Constants.PLAYLIST_ID;
import static com.rks.musicx.misc.utils.Constants.PLAYLIST_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.PREF_AUTO_PAUSE;
import static com.rks.musicx.misc.utils.Constants.PRESET_POS;
import static com.rks.musicx.misc.utils.Constants.REMOVE_TABLIST;
import static com.rks.musicx.misc.utils.Constants.REORDER_TAB;
import static com.rks.musicx.misc.utils.Constants.REPEATMODE;
import static com.rks.musicx.misc.utils.Constants.RESTORE_LASTTAB;
import static com.rks.musicx.misc.utils.Constants.SAVE_DATA;
import static com.rks.musicx.misc.utils.Constants.SETTINGS_TRACK;
import static com.rks.musicx.misc.utils.Constants.SHUFFLEMODE;
import static com.rks.musicx.misc.utils.Constants.SONGGRID;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_PATH;
import static com.rks.musicx.misc.utils.Constants.SONG_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;
import static com.rks.musicx.misc.utils.Constants.SONG_YEAR;
import static com.rks.musicx.misc.utils.Constants.SaveHeadset;
import static com.rks.musicx.misc.utils.Constants.SaveLyrics;
import static com.rks.musicx.misc.utils.Constants.SaveTelephony;
import static com.rks.musicx.misc.utils.Constants.TRYPEFACE_PATH;
import static com.rks.musicx.misc.utils.Constants.TextFonts;
import static com.rks.musicx.misc.utils.Constants.WIDGETTRACK;
import static com.rks.musicx.misc.utils.Constants.WIDGET_COLOR;

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

public class Extras {

    /*private SharedPreferences MusicXApplication.getmPreferences();
    private SharedPreferences MusicXApplication.getMetaData();
    private static Context context;*/
    private static Extras instance;

    public Extras() {
        //context = contexts;
        //MusicXApplication.getmPreferences() = PreferenceManager.getDefaultSharedPreferences(context);
        //MusicXApplication.getMetaData() = context.getSharedPreferences(TAG_METADATA, MODE_PRIVATE);
    }

    public static Extras init() {
        if (instance == null){
            instance = new Extras();
        }
        return instance;
    }

    public static Extras getInstance(){
        return instance;
    }

    public SharedPreferences getmPreferences() {
        return MusicXApplication.getmPreferences();
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    /////////////////// Theme Pref //////////////////////

    public boolean getDarkTheme() {
        return getmPreferences().getBoolean(DarkTheme, false);
    }

    public boolean getLighTheme() {
        return getmPreferences().getBoolean(LightTheme, false);
    }

    public boolean getBlackTheme() {
        return getmPreferences().getBoolean(BlackTheme, false);
    }

    public void getThemevalue(Activity activity) {
        if (getDarkTheme()) {
            ATE.postApply(activity, DarkTheme);
        } else if (getBlackTheme()) {
            ATE.postApply(activity, BlackTheme);
        } else {
            ATE.postApply(activity, LightTheme);
        }
    }

    //////////////////// Sorting ////////////////////////
    public String getSongSortOrder() {
        return MusicXApplication.getmPreferences().getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setSongSortOrder(String value) {
        putString(SONG_SORT_ORDER, value);
    }

    public String getArtistSortOrder() {
        return MusicXApplication.getmPreferences().getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void setArtistSortOrder(String value) {
        putString(ARTIST_SORT_ORDER, value);
    }

    public String getAlbumSortOrder() {
        return MusicXApplication.getmPreferences().getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(String value) {
        putString(ALBUM_SORT_ORDER, value);
    }

    public void setArtistAlbumSortOrder(String value) {
        putString(ARTIST_ALBUM_SORT, value);
    }

    public String getArtistAlbumSort() {
        return MusicXApplication.getmPreferences().getString(ARTIST_ALBUM_SORT, SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
    }

    public void setPlaylistSortOrder(String value) {
        putString(PLAYLIST_SORT_ORDER, value);
    }

    public String getPlaylistSort() {
        return MusicXApplication.getmPreferences().getString(PLAYLIST_SORT_ORDER, SortOrder.PlaylistSortOrder.PLAYLIST_A_Z);
    }

    ////////////////////////// Preferences /////////////////////////

    public void setwidgetPosition(int pos) {
        SharedPreferences.Editor sharededitor = MusicXApplication.getmPreferences().edit();
        sharededitor.putInt(KEY_POSITION_X, pos);
        sharededitor.putInt(KEY_POSITION_Y, pos);
        sharededitor.commit();
    }

    public int getwidgetPositionX() {
        return MusicXApplication.getmPreferences().getInt(KEY_POSITION_X, 100);
    }

    public int getwidgetPositionY() {
        return MusicXApplication.getmPreferences().getInt(KEY_POSITION_Y, 100);
    }

    public String getTabIndex() {
        return MusicXApplication.getmPreferences().getString(REORDER_TAB, "0");
    }

    public void setTabIndex(final int index) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putString(REORDER_TAB, String.valueOf(index));
        editor.apply();
    }

    public boolean saveLyrics() {
        return MusicXApplication.getmPreferences().getBoolean(SaveLyrics, true);
    }

    public boolean songView() {
        return MusicXApplication.getmPreferences().getBoolean(GridViewSong, false);
    }

    public boolean albumView() {
        return MusicXApplication.getmPreferences().getBoolean(GridViewAlbum, false);
    }

    public boolean artistView() {
        return MusicXApplication.getmPreferences().getBoolean(GridViewArtist, false);
    }

    public boolean floatingWidget() {
        return MusicXApplication.getmPreferences().getBoolean(FloatingView, false);
    }

    public String fontConfig() {
        return MusicXApplication.getmPreferences().getString(TextFonts, "11");
    }

    public boolean headsetConfig() {
        return MusicXApplication.getmPreferences().getBoolean(SaveHeadset, true);
    }

    public boolean phonecallConfig() {
        return MusicXApplication.getmPreferences().getBoolean(SaveTelephony, true);
    }

    public SharedPreferences saveEq() {
        return MusicXApplication.getEqPref();
    }

    public boolean saveData() {
        return MusicXApplication.getmPreferences().getBoolean(SAVE_DATA, true);
    }

    public boolean hideNotify() {
        return MusicXApplication.getmPreferences().getBoolean(HIDE_NOTIFY, false);
    }

    public boolean hideLockscreen() {
        return MusicXApplication.getmPreferences().getBoolean(HIDE_LOCKSCREEEN, false);
    }

    public boolean restoreLastTab() {
        return MusicXApplication.getmPreferences().getBoolean(RESTORE_LASTTAB, false);
    }

    public boolean hqArtistArtwork() {
        return MusicXApplication.getmPreferences().getBoolean(HQ_ARTISTARTWORK, false);
    }

    public boolean artworkColor() {
        return MusicXApplication.getmPreferences().getBoolean(ARTWORKCOLOR, false);
    }

    public String getFadeDuration() {
        return MusicXApplication.getmPreferences().getString(FADEINOUT_DURATION, "0");
    }

    public boolean getFadeTrack() {
        return MusicXApplication.getmPreferences().getBoolean(FADETRACK, false);
    }

    public boolean getWigetColor(){
        return MusicXApplication.getmPreferences().getBoolean(WIDGET_COLOR, false);
    }

    public boolean getHdArtwork() {
        return MusicXApplication.getmPreferences().getBoolean(HD_ARTWORK, false);
    }

    public boolean getDownloadedArtwork(){
        return MusicXApplication.getmPreferences().getBoolean(DOWNLOADED_ARTWORK, false);
    }

    public String getAudioFilter(){
        return MusicXApplication.getmPreferences().getString(AUDIO_FILTER, "0");
    }

    public String getBlurView() {
        return MusicXApplication.getmPreferences().getString(BlurView, "2");
    }

    /////////// Fade Track ///////////

    public void saveFadeTrack(boolean value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putBoolean(FADETRACK, value);
        editor.commit();
    }

    ////////////////// folder pref //////////////////

    public void saveFolderPath(String path) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putString(FOLDERPATH, path);
        editor.apply();
    }


    public String getFolderPath() {
        return MusicXApplication.getmPreferences().getString(FOLDERPATH, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
    }

    //////////////////// eq switch track //////////////////

    public void eqSwitch(Boolean torf) {
        SharedPreferences.Editor editor = saveEq().edit();
        editor.putBoolean(EQSWITCH, torf);
        editor.commit();
    }


    public boolean geteqSwitch() {
        return saveEq().getBoolean(EQSWITCH, false);
    }


    ///////////////////// MusicX Service pref /////////////////////////

    public void saveServices(boolean savestate, int pos, int repeat, boolean shuffle, String songTitle, String songArtist, String path, long songID, long albumID) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(CURRENTPOS, pos);
        editor.putInt(REPEATMODE, repeat);
        editor.putBoolean(SHUFFLEMODE, shuffle);
        editor.putBoolean(PLAYINGSTATE, savestate);
        editor.putString(SONG_TITLE, songTitle);
        editor.putString(SONG_ARTIST, songArtist);
        editor.putLong(SONG_ID, songID);
        editor.putLong(SONG_ALBUM_ID, albumID);
        editor.putString(SONG_PATH, path);
        editor.apply();
    }

    public void saveSeekServices(int playerPos){
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(PLAYER_POS, playerPos);
        editor.commit();
    }

    public int getCurrentpos() {
        return MusicXApplication.getmPreferences().getInt(CURRENTPOS, 0);
    }

    public int getRepeatMode(int repeatmode) {
        return MusicXApplication.getmPreferences().getInt(REPEATMODE, repeatmode);
    }

    public boolean getShuffle(boolean shuffle) {
        return MusicXApplication.getmPreferences().getBoolean(SHUFFLEMODE, shuffle);
    }

    public boolean getState(boolean state) {
        return MusicXApplication.getmPreferences().getBoolean(PLAYINGSTATE, state);
    }

    public String getSongTitle(String songtitle) {
        return MusicXApplication.getmPreferences().getString(SONG_TITLE, songtitle);
    }

    public String getSongArtist(String artist) {
        return MusicXApplication.getmPreferences().getString(SONG_ARTIST, artist);
    }

    public long getSongId(long id) {
        return MusicXApplication.getmPreferences().getLong(SONG_ID, id);
    }

    public long getAlbumId(long albumid) {
        return MusicXApplication.getmPreferences().getLong(SONG_ALBUM_ID, albumid);
    }

    public String getSongPath(String path) {
        return MusicXApplication.getmPreferences().getString(SONG_PATH, path);
    }

    public boolean getHeadset() {
        return Extras.getInstance().getmPreferences().getBoolean(PREF_AUTO_PAUSE, false);
    }

    //////////////////// Save Metadata pref ////////////////////////

    public void saveMetaData(Song song) {
        SharedPreferences.Editor editor = MusicXApplication.getMetaData().edit();
        editor.putString(Constants.SONG_TITLE, song.getTitle());
        editor.putString(Constants.SONG_ARTIST, song.getArtist());
        editor.putString(Constants.SONG_ALBUM, song.getAlbum());
        editor.putString(Constants.SONG_YEAR, song.getYear());
        editor.putInt(Constants.SONG_TRACK_NUMBER, song.getTrackNumber());
        editor.putLong(Constants.SONG_ALBUM_ID, song.getAlbumId());
        editor.putString(Constants.SONG_PATH, song.getmSongPath());
        editor.putLong(Constants.SONG_ID, song.getId());
        editor.apply();
    }

    public String getTitle() {
        return MusicXApplication.getMetaData().getString(SONG_TITLE, null);
    }

    public String getArtist() {
        return MusicXApplication.getMetaData().getString(SONG_ARTIST, null);
    }

    public String getAlbum() {
        return MusicXApplication.getMetaData().getString(SONG_ALBUM, null);
    }

    public String getPath() {
        return MusicXApplication.getMetaData().getString(SONG_PATH, null);
    }

    public int getNo() {
        return MusicXApplication.getMetaData().getInt(SONG_TRACK_NUMBER, 0);
    }

    public long getAlbumID() {
        return MusicXApplication.getMetaData().getLong(SONG_ALBUM_ID, 0);
    }

    public long getId() {
        return MusicXApplication.getMetaData().getLong(SONG_ID, 0);
    }

    public String getYear() {
        return MusicXApplication.getMetaData().getString(SONG_YEAR, null);
    }


    //////////////////////// Album GridView///////////////////

    public int getAlbumGrid() {
        return MusicXApplication.getmPreferences().getInt(ALBUMGRID, 2);
    }

    public void setAlbumGrid(int value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(ALBUMGRID, value);
        editor.commit();
    }

    //////////////////////// Artist GridView///////////////////

    public int getArtistGrid() {
        return MusicXApplication.getmPreferences().getInt(ARTISTGRID, 2);
    }

    public void setArtistGrid(int value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(ARTISTGRID, value);
        editor.commit();
    }


    //////////////////////// Song GridView///////////////////

    public int getSongGrid() {
        return MusicXApplication.getmPreferences().getInt(SONGGRID, 2);
    }

    public void setSongGrid(int value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(SONGGRID, value);
        editor.commit();
    }

    ///////////////// widgetTracking ///////////////

    public boolean getWidgetTrack() {
        return MusicXApplication.getmPreferences().getBoolean(WIDGETTRACK, false);
    }

    public void setWidgetTrack(boolean torf) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putBoolean(WIDGETTRACK, torf);
        editor.commit();
    }

    /////////////// Settings Permission /////////////

    public boolean getSettings(){
        return MusicXApplication.getmPreferences().getBoolean(SETTINGS_TRACK, false);
    }

    public void setSettings(boolean torf){
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putBoolean(SETTINGS_TRACK, torf);
        editor.commit();
    }

    ///////////////// Init Setup //////////////////

    public int getInitValue(Context context, String spName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(spName, MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public void setInitValue(Context context, int ammount, String spName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(spName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, ammount);
        editor.commit();
    }

    ///////////////// save Playlist Id /////////////

    public void savePlaylistId(long id) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putLong(PLAYLIST_ID, id);
        editor.apply();
    }

    public long getPlaylistId() {
        return MusicXApplication.getmPreferences().getLong(PLAYLIST_ID, 0);
    }


    ////////////////////////// Remove tabs ////////////////

    public void saveRemoveTab(List<Integer> list) {
        String s = "";
        for (Integer i : list) {
            s += i + ",";
        }
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putString(REMOVE_TABLIST, s);
        editor.commit();
    }

    public ArrayList<Integer> getRemoveTab() {
        String s = MusicXApplication.getmPreferences().getString(REMOVE_TABLIST, "");
        StringTokenizer st = new StringTokenizer(s, ",");
        ArrayList<Integer> result = new ArrayList<Integer>();
        while (st.hasMoreTokens()) {
            result.add(Integer.parseInt(st.nextToken()));
        }
        return result;
    }

    ///////////////// Typeface path /////////////

    public void saveTypeface(String path){
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putString(TRYPEFACE_PATH,path);
        editor.commit();
    }

    public String getTypeface(){
        return MusicXApplication.getmPreferences().getString(TRYPEFACE_PATH, Typeface.DEFAULT.toString());
    }


    //////////// Preset Pos ///////////////

    public void savePresetPos(int position){
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putInt(PRESET_POS, position);
        editor.commit();
    }

    public int getPresetPos(){
        return MusicXApplication.getmPreferences().getInt(PRESET_POS, 0);
    }

    ///////////// PlayingView Track ///////////

    public void savePlayingViewTrack(boolean torf){
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putBoolean(PLAYINGVIEW_TRACK, torf);
        editor.commit();
    }

    public boolean getPlayingViewTrack(){
        return MusicXApplication.getmPreferences().getBoolean(PLAYINGVIEW_TRACK, false);
    }

    public void setNaviSettings(boolean value) {
        SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
        editor.putBoolean(NAV_SETTINGS, value);
        editor.commit();
    }

    public Boolean getNavSettings() {
        return MusicXApplication.getmPreferences().getBoolean(NAV_SETTINGS, false);
    }


}
