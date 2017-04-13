package com.rks.musicx.misc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Song;

import static com.rks.musicx.misc.utils.Constants.ALBUMGRID;
import static com.rks.musicx.misc.utils.Constants.ALBUM_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.ARTISTGRID;
import static com.rks.musicx.misc.utils.Constants.ARTIST_ALBUM_SORT;
import static com.rks.musicx.misc.utils.Constants.ARTIST_SORT_ORDER;
import static com.rks.musicx.misc.utils.Constants.ARTWORKCOLOR;
import static com.rks.musicx.misc.utils.Constants.CURRENTPOS;
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
import static com.rks.musicx.misc.utils.Constants.PLAYINGSTATE;
import static com.rks.musicx.misc.utils.Constants.REORDER_TAB;
import static com.rks.musicx.misc.utils.Constants.REPEATMODE;
import static com.rks.musicx.misc.utils.Constants.RESTORE_LASTTAB;
import static com.rks.musicx.misc.utils.Constants.SAVE_DATA;
import static com.rks.musicx.misc.utils.Constants.SAVE_EQ;
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
import static com.rks.musicx.misc.utils.Constants.TRACKFOLDER;
import static com.rks.musicx.misc.utils.Constants.TextFonts;
import static com.rks.musicx.misc.utils.Constants.VIZCOLOR;
import static com.rks.musicx.misc.utils.Constants.sInstance;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Extras {

    public SharedPreferences mPreferences;
    public SharedPreferences metaData;
    public SharedPreferences tagEditor;
    private Context mcontext;

    public Extras(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        metaData = context.getSharedPreferences("MetaData", Context.MODE_PRIVATE);
        tagEditor = context.getSharedPreferences("tagEditor", Context.MODE_PRIVATE);
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

    public void setArtistAlbumSortOrder(String value) {
        putString(ARTIST_ALBUM_SORT, value);
    }

    public String getArtistAlbumSort() {
        return mPreferences.getString(ARTIST_ALBUM_SORT, SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z);
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

    public boolean artworkColor() {
        return mPreferences.getBoolean(ARTWORKCOLOR, false);
    }

    ////////////////// folder pref //////////////////

    public void saveFolderPath(String path) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(FOLDERPATH, path);
        editor.apply();
    }


    public String getFolderPath() {
        return mPreferences.getString(FOLDERPATH, null);
    }

    public void trackFolderPath(boolean torf) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(TRACKFOLDER, torf);
        editor.apply();
    }

    public boolean gettrackFolderpath() {
        return mPreferences.getBoolean(TRACKFOLDER, false);
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

    public void saveServices(boolean savestate, int pos, int repeat, boolean shuffle, String songTitle, String songArtist, long songID, long albumID) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(CURRENTPOS, pos);
        editor.putInt(REPEATMODE, repeat);
        editor.putBoolean(SHUFFLEMODE, shuffle);
        editor.putBoolean(PLAYINGSTATE, savestate);
        editor.putString(SONG_TITLE, songTitle);
        editor.putString(SONG_ARTIST, songArtist);
        editor.putLong(SONG_ID, songID);
        editor.putLong(SONG_ALBUM_ID, albumID);
        editor.apply();
    }

    public int getCurrentpos() {
        return mPreferences.getInt(CURRENTPOS, 0);
    }

    public int getRepeatMode(int repeatmode) {
        return mPreferences.getInt(REPEATMODE, repeatmode);
    }

    public boolean getShuffle(boolean shuffle) {
        return mPreferences.getBoolean(SHUFFLEMODE, shuffle);
    }

    public boolean getState(boolean state) {
        return mPreferences.getBoolean(PLAYINGSTATE, state);
    }

    public String getSongTitle(String songtitle) {
        return mPreferences.getString(SONG_TITLE, songtitle);
    }

    public String getSongArtist(String artist) {
        return mPreferences.getString(SONG_ARTIST, artist);
    }

    public long getSongId(long id) {
        return mPreferences.getLong(SONG_ID, id);
    }

    public long getAlbumId(long albumid) {
        return mPreferences.getLong(SONG_ALBUM_ID, albumid);
    }

    public int getCurrentPos(){
        return mPreferences.getInt(CURRENTPOS, 0);
    }


    //////////////////// Save Metadata pref ////////////////////////

    public void saveMetaData(Song song) {
        SharedPreferences.Editor editor = metaData.edit();
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
        return metaData.getString(SONG_TITLE, null);
    }

    public String getArtist() {
        return metaData.getString(SONG_ARTIST, null);
    }

    public String getAlbum() {
        return metaData.getString(SONG_ALBUM, null);
    }

    public String getPath() {
        return metaData.getString(SONG_PATH, null);
    }

    public int getNo() {
        return metaData.getInt(SONG_TRACK_NUMBER, 0);
    }

    public long getAlbumID() {
        return metaData.getLong(SONG_ALBUM_ID, 0);
    }

    public long getId() {
        return metaData.getLong(SONG_ID, 0);
    }

    public String getYear() {
        return metaData.getString(SONG_YEAR, null);
    }


    //////////////////////// Album GridView///////////////////

    public int getAlbumGrid() {
        return mPreferences.getInt(ALBUMGRID, 2);
    }

    public void setAlbumGrid(int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUMGRID, value);
        editor.commit();
    }

    //////////////////////// Artist GridView///////////////////

    public int getArtistGrid() {
        return mPreferences.getInt(ARTISTGRID, 2);
    }

    public void setArtistGrid(int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ARTISTGRID, value);
        editor.commit();
    }


    //////////////////////// Song GridView///////////////////

    public int getSongGrid() {
        return mPreferences.getInt(SONGGRID, 2);
    }

    public void setSongGrid(int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONGGRID, value);
        editor.commit();
    }

}
