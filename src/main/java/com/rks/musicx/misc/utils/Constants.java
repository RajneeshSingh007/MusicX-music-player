package com.rks.musicx.misc.utils;

import com.rks.musicx.database.DefaultColumn;

/**
 * Created by Coolalien on 8/13/2016.
 */

public class Constants {

    /*
    permission properties
     */
    public static final int PERMISSIONS_REQ = 10;
    public static final int OVERLAY_REQ = 1;

    /*
    album properties
     */
    public static final String ALBUM_ID = "id";
    public static final String ALBUM_NAME = "name";
    public static final String ALBUM_ARTIST = "artist";
    public static final String ALBUM_YEAR = "year";
    public static final String ALBUM_TRACK_COUNT = "track_count";

    /*
    artist properties
     */
    public static final String ARTIST_ARTIST_ID = "artist_id";
    public static final String ARTIST_NAME = "artist_name";
    public static final String ARTIST_ALBUM_COUNT = "album_count";
    public static final String ARTIST_TRACK_COUNT = "track_count";

    /*
    song properties
     */
    public static final String SONG_ID = "song_id";
    public static final String SONG_TITLE = "song_title";
    public static final String SONG_ARTIST = "song_artist";
    public static final String SONG_ALBUM = "song_album";
    public static final String SONG_ALBUM_ID = "song_album_id";
    public static final String SONG_TRACK_NUMBER = "song_track_number";
    public static final String SONG_PATH = "song_path";

    /*
    playing propertiess
     */
    public static final String PREF_AUTO_PAUSE = "AUTO_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_CHANGE_STATE = "ACTION_CHANGE_STATE";
    public static final String ACTION_TOGGLE = "ACTION_TOGGLE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_CHOOSE_SONG = "ACTION_CHOOSE_SONG";
    public static final String META_CHANGED ="META_CHANGED";
    public static final String PLAYSTATE_CHANGED = "PLAYSTATE_CHANGED";
    public static final String QUEUE_CHANGED = "QUEUE_CHANGED";
    public static final String POSITION_CHANGED = "POSITION_CHANGED";
    public static final String ITEM_ADDED = "ITEM_ADDED";
    public static final String ORDER_CHANGED = "ORDER_CHANGED";
    public static final String EXTRA_POSITION = "POSITION";
    public static final String REPEAT_MODE_CHANGED = "REPEAT_MODE_CHANGED";
    public static final String REPEATMODE = "repeatMode";
    public static final String SHUFFLEMODE = "shuffle";
    public static final String PLAYINGSTATE = "playingState";
    public static final String CURRENTPOS = "position";
    public static final String ACTION_PLAYINGVIEW = "PLAYING_VIEW";
    public static final String FAV = "FAV";

    /*
    Sorting properties
     */
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";

    /*
    Playlist  & fav. properties
     */

    public static final String PARAM_PLAYLIST_ID = "playlist_id";
    public static final String PARAM_PLAYLIST_NAME = "playlist_name";
    public static final String PARAM_PLAYLIST_FAVORITES = "favorites";

    /*
    Floating Widget properties
     */
    public static final String EXTRA_CHANGE_STATE = "EXTRA_CHANGE_STATE";
    public static final String KEY_POSITION_X = "position_x";
    public static final String KEY_POSITION_Y = "position_y";

    /*
    Instance
     */
    public static Extras sInstance = null;

    /*
    Preferences
     */
    public static final String PlayingView = "playing_selection";
    public static final String EQMODE = "equalizer_selection";
    public static final String FloatingView = "floating_view";
    public static final String TextFonts = "change_fonts";
    public static final String BlurView = "blur_view";
    public static final String GridViewAlbum = "gridlist_albumview";
    public static final String GridViewArtist = "gridlist_artistview";
    public static final String GridViewSong = "gridlist_songview";
    public static final String SaveLyrics = "save_lyrics";
    public static final String SaveTelephony = "save_telephony";
    public static final String SaveHeadset = "save_headset";
    public static final String ClearFav = "clear_favdb";
    public static final String ClearRecently = "clear_recentdb";
    public static final String SAVE_DATA = "save_internet";
    public static final String HIDE_NOTIFY = "hide_notification";
    public static final String HIDE_LOCKSCREEEN = "hide_lockscreenMedia";
    public static final String REORDER_TAB = "tab_selection";
    public static final String RESTORE_LASTTAB = "restore_lasttab";
    public static final String STORAGE_SELECTION = "storage_selection";

    /*
    Choices
     */
    public static final String Zero = "0";
    public static final String One = "1";
    public static final String Two = "2";
    public static final String Three = "3";
    public static final String Four = "4";

    /*
    Theming Properties
     */
    public static final String LightTheme = "light_theme";
    public static final String DarkTheme = "dark_theme";

    /*
    Database Properties
     */
    public static final String RecentlyPlayed_TableName = "RecentlyPlayed";
    public static final String Queue_TableName = "QueuePlaylist";
    public static final String Fav_TableName = "Favorites";
    public static final int DbVersion = 2;
    public static final String Separator =",";


    /**
     * Common Table Col. Name For Database
     * @param tableName
     * @return
     */
    public static String DefaultColumn(String tableName){
        return "CREATE TABLE " + tableName + " (" +
                DefaultColumn._ID + " INTEGER PRIMARY KEY," +
                DefaultColumn.SongId + " INTEGER UNIQUE" + Separator +
                DefaultColumn.SongTitle + " TEXT" + Separator +
                DefaultColumn.SongArtist + " TEXT" + Separator +
                DefaultColumn.SongAlbum + " TEXT" + Separator +
                DefaultColumn.SongAlbumId + " INTEGER" + Separator +
                DefaultColumn.SongNumber + " INTEGER" + Separator +
                DefaultColumn.SongPath + " TEXT" + " )";
    }

    /*
    Equalizer
     */
    public static final String BAND_LEVEL = "level";
    public static final String SAVE_PRESET = "preset";
    public static final String EQ_ENABLED = "enabled";
    public static final String BASS_ENABLED = "enabled";
    public static final String PRESET_ENABLED = "enabled";
    public static final String VIRTUAL_ENABLED = "enabled";
    public static final int GAIN_MAX = 1000;
    public static final String SAVE_EQ = "Equalizers";
    public static final String BASS_BOOST = "BassBoost";
    public static final String VIRTUAL_BOOST = "VirtualBoost";
    public static final String LOUD_BOOST = "Loud";
    public static final String PRESET_BOOST = "PresetReverb";
    public static final short BASSBOOST_STRENGTH = 1000;
    public static final short Virtualizer_STRENGTH = 1000;
    public static final String AUDIO_ID = "audio_id";
    public static final String OPEN_EFFECTS = "open_effects";
    public static final String CLOSE_EFFECTS = "close_effects";
    public static final String SAVEDBASS = "SavedBass";
    public static final String SAVEDLOUD = "SavedLoud";
    public static final String SAVEDVIRTUALIZER = "SavedVir";
    public static final String SAVEDREVERB = "SavedReverb";

    /**
     * Developer Properties
     */
    public static final String DEVELOPER_NAME= "Rajneesh Singh";

}
