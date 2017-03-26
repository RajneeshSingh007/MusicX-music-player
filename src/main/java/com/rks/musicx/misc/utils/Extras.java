package com.rks.musicx.misc.utils;

import static com.rks.musicx.misc.utils.Constants.ALBUM_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.ARTIST_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.EQSWITCH;
import static com.rks.musicx.misc.utils.Constants.FOLDERPATH;
import static com.rks.musicx.misc.utils.Constants.FloatingView;
import static com.rks.musicx.misc.utils.Constants.GridViewAlbum;
import static com.rks.musicx.misc.utils.Constants.GridViewArtist;
import static com.rks.musicx.misc.utils.Constants.GridViewSong;
import static com.rks.musicx.misc.utils.Constants.HIDE_LOCKSCREEEN;
import static com.rks.musicx.misc.utils.Constants.HIDE_NOTIFY;
import static com.rks.musicx.misc.utils.Constants.HQ_ARTISTARTWORK;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_X;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_Y;
import static com.rks.musicx.misc.utils.Constants.REORDER_TAB;
import static com.rks.musicx.misc.utils.Constants.RESTORE_LASTTAB;
import static com.rks.musicx.misc.utils.Constants.SAVE_DATA;
import static com.rks.musicx.misc.utils.Constants.SAVE_EQ;
import static com.rks.musicx.misc.utils.Constants.SONG_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.SaveHeadset;
import static com.rks.musicx.misc.utils.Constants.SaveLyrics;
import static com.rks.musicx.misc.utils.Constants.SaveTelephony;
import static com.rks.musicx.misc.utils.Constants.TRACKFOLDER;
import static com.rks.musicx.misc.utils.Constants.TextFonts;
import static com.rks.musicx.misc.utils.Constants.VIZCOLOR;
import static com.rks.musicx.misc.utils.Constants.sInstance;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import com.rks.musicx.data.loaders.SortOrder;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Extras {

    public SharedPreferences mPreferences;
    private Context mcontext;

    public Extras(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mcontext = context;
    }


    public static int px2Dp(int dp, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void init(Context context) {
        sInstance = new Extras(context);
    }

    public static Extras getInstance() {
        return sInstance;
    }

    public SharedPreferences getmPreferences() {
        return mPreferences;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    //////////////////// Sorting ////////////////////////
    public String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void setSongSortOrder(String value) {
        putString(SONG_SORT_ORDER, value);
    }

    public String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void setArtistSortOrder(String value) {
        putString(ARTIST_SORT_ORDER, value);
    }

    public String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void setAlbumSortOrder(String value) {
        putString(ALBUM_SORT_ORDER, value);
    }

    ////////////////////////// Preferences /////////////////////////

    public void setwidgetPosition(int pos) {
        SharedPreferences.Editor sharededitor = mPreferences.edit();
        sharededitor.putInt(KEY_POSITION_X, pos);
        sharededitor.putInt(KEY_POSITION_Y, pos);
        sharededitor.apply();
    }

    public int getwidgetPositionX() {
        return mPreferences.getInt(KEY_POSITION_X, 100);
    }

    public int getwidgetPositionY() {
        return mPreferences.getInt(KEY_POSITION_Y, 100);
    }

    public String getTabIndex() {
        return mPreferences.getString(REORDER_TAB, "0");
    }

    public void setTabIndex(final int index) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(REORDER_TAB, String.valueOf(index));
        editor.apply();
    }

    public boolean saveLyrics() {
        return mPreferences.getBoolean(SaveLyrics, true);
    }

    public boolean songView() {
        return mPreferences.getBoolean(GridViewSong, false);
    }

    public boolean albumView() {
        return mPreferences.getBoolean(GridViewAlbum, false);
    }

    public boolean artistView() {
        return mPreferences.getBoolean(GridViewArtist, false);
    }

    public boolean floatingWidget() {
        return mPreferences.getBoolean(FloatingView, false);
    }

    public String fontConfig() {
        return mPreferences.getString(TextFonts, "11");
    }

    public boolean headsetConfig() {
        return mPreferences.getBoolean(SaveHeadset, true);
    }

    public boolean phonecallConfig() {
        return mPreferences.getBoolean(SaveTelephony, true);
    }

    public SharedPreferences saveEq() {
        return mcontext.getSharedPreferences(SAVE_EQ, Context.MODE_PRIVATE);
    }

    public boolean saveData() {
        return mPreferences.getBoolean(SAVE_DATA, true);
    }

    public boolean hideNotify() {
        return mPreferences.getBoolean(HIDE_NOTIFY, false);
    }

    public boolean hideLockscreen() {
        return mPreferences.getBoolean(HIDE_LOCKSCREEEN, false);
    }

    public boolean restoreLastTab() {
        return mPreferences.getBoolean(RESTORE_LASTTAB, false);
    }

    public boolean hqArtistArtwork() {
        return mPreferences.getBoolean(HQ_ARTISTARTWORK, false);
    }


    public boolean vizColor() {
        return mPreferences.getBoolean(VIZCOLOR, false);
    }

    ////////////////// folder pref //////////////////

    public void saveFolderPath(String path){
      SharedPreferences.Editor editor = mPreferences.edit();
      editor.putString(FOLDERPATH, path);
      editor.apply();
    }


    public String getFolderPath(){
      return mPreferences.getString(FOLDERPATH, null);
    }

    public void trackFolderPath(boolean torf){
      SharedPreferences.Editor editor = mPreferences.edit();
      editor.putBoolean(TRACKFOLDER, torf);
      editor.apply();
    }

    public boolean gettrackFolderpath(){
      return mPreferences.getBoolean(TRACKFOLDER, false);
    }


    //////////////////// eq switch track //////////////////

    public void eqSwitch(Boolean torf){
      SharedPreferences.Editor editor = mPreferences.edit();
      editor.putBoolean(EQSWITCH, torf);
      editor.apply();
    }


    public boolean geteqSwitch(){
      return mPreferences.getBoolean(EQSWITCH, false);
    }

}
