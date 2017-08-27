package com.rks.musicx.misc.utils;

import android.Manifest;

import com.rks.musicx.interfaces.DefaultColumn;


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

public class Constants {

    /*
    permission properties
     */
    public static final int PERMISSIONS_REQ = 10;
    public static final int OVERLAY_REQ = 1;
    public static final int WRITESETTINGS = 2;
    public static final int EQ = 3445;
    public static final int NAV = 56660;
    public static final int IMAGE_PICKER = 25350;

    /*
    Sorting properties
     */
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    public static final String ARTIST_ALBUM_SORT = "artist_album_sort";
    public static final String PLAYLIST_SORT_ORDER = "playlist_sort";
    /*
    Playlist  & fav. properties
     */
    public static final String PARAM_PLAYLIST_NAME = "playlist_name";
    public static final String PARAM_PLAYLIST_ID = "playlist_id";
    /*
    Floating Widget properties
     */
    public static final String KEY_POSITION_X = "position_x";
    public static final String KEY_POSITION_Y = "position_y";
    /*
    Preferences
     */
    public static final String PlayingView = "playing_selection";
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
    public static final String ClearQueue = "clear_queuedb";
    public static final String SAVE_DATA = "save_internet";
    public static final String HIDE_NOTIFY = "hide_notification";
    public static final String HIDE_LOCKSCREEEN = "hide_lockscreenMedia";
    public static final String REORDER_TAB = "tab_selection";
    public static final String RESTORE_LASTTAB = "restore_lasttab";
    public static final String HQ_ARTISTARTWORK = "hqartist_artwork";
    public static final String EQSWITCH = "eqswitch";
    public static final String ARTWORKCOLOR = "artwork_adaptive";
    public static final String FOLDERPATH = "folderpath";
    public static final String ALBUMGRID = "albumgrid";
    public static final String ARTISTGRID = "artistgrid";
    public static final String SONGGRID = "songgrid";
    public static final String WIDGETTRACK = "widgettrack";
    public static final String PLAYLIST_ID = "playlistId";
    public static final String FADEINOUT_DURATION = "fadein_fadeout_seekbar";
    public static final String FADETRACK = "fade_inout";
    public static final String REMOVETABS = "remove_tabs";
    public static final String TRYPEFACE_PATH = "font_path";
    public static final String WIDGET_COLOR = "widget_color";
    public static final String PLAYINGVIEW_TRACK = "isplayingView3";
    public static final String SETTINGS_TRACK = "settings_track";
    public static final String HD_ARTWORK = "hd_artwork";
    public static final String DOWNLOADED_ARTWORK = "downloaded_artwork";
    public static final String REMOVE_TABLIST = "removeTablist";
    public static final String TAG_METADATA = "MetaData";
    public static final String AUDIO_FILTER = "audio_filter";
    public static final String NAV_SETTINGS = "nav_settings";

    /**
     * Files filter
     */
    public static final String fileExtensions[] = new String[]{
            ".aac", ".mp3", ".wav", ".ogg", ".midi", ".3gp", ".mp4", ".m4a", ".amr", ".flac"
    };

    /*
    Choices
     */
    public static final String Zero = "0";
    public static final String One = "1";
    public static final String Two = "2";
    public static final String Three = "3";
    public static final String Four = "4";
    public static final String Five = "5";
    /*
    Theming Properties
     */
    public static final String LightTheme = "light_theme";
    public static final String DarkTheme = "dark_theme";
    public static final String BlackTheme = "black_theme";
    /*
    Database Properties
     */
    public static final String RecentlyPlayed_TableName = "RecentlyPlayed";
    public static final String Queue_TableName = "QueuePlaylist";
    public static final String Fav_TableName = "Favorites";
    public static final String Queue_Store_TableName = "QueueStore";

    public static final int DbVersion = 2;
    public static final String Separator = ",";

    public static final String DOWNLOAD_ARTWORK = "DownloadAlbum";
    public static final String DOWNLOAD_ARTWORK2 = "DownloadArtwork";

    /*
    Equalizer
     */
    public static final String BAND_LEVEL = "level";
    public static final String SAVE_PRESET = "preset";
    public static final int GAIN_MAX = 100;
    public static final String SAVE_EQ = "Equalizers";
    public static final String BASS_BOOST = "BassBoost";
    public static final String VIRTUAL_BOOST = "VirtualBoost";
    public static final String LOUD_BOOST = "Loud";
    public static final String PRESET_BOOST = "PresetReverb";
    public static final String PRESET_POS = "spinner_position";
    public static final short BASSBOOST_STRENGTH = 1000;
    public static final short Virtualizer_STRENGTH = 1000;
    /**
     * Developer Name
     */
    public static final String DEVELOPER_NAME = "Rajneesh Singh";
    public static final String PACKAGENAME = "com.rks.musicx.";
    /*
    album properties
     */
    public static final String ALBUM_ID = PACKAGENAME + "id";
    public static final String ALBUM_NAME = PACKAGENAME + "name";
    public static final String ALBUM_ARTIST = PACKAGENAME + "artist";
    public static final String ALBUM_YEAR = PACKAGENAME + "year";
    public static final String ALBUM_TRACK_COUNT = PACKAGENAME + "track_count";
    /*
    artist properties
     */
    public static final String ARTIST_ARTIST_ID = PACKAGENAME + "artist_id";
    public static final String ARTIST_NAME = PACKAGENAME + "artist_name";
    public static final String ARTIST_ALBUM_COUNT = PACKAGENAME + "album_count";
    public static final String ARTIST_TRACK_COUNT = PACKAGENAME + "track_count";
    /*
    song properties
     */
    public static final String SONG_ID = PACKAGENAME + "song_id";
    public static final String SONG_TITLE = PACKAGENAME + "song_title";
    public static final String SONG_ARTIST = PACKAGENAME + "song_artist";
    public static final String SONG_ALBUM = PACKAGENAME + "song_album";
    public static final String SONG_ALBUM_ID = PACKAGENAME + "song_album_id";
    public static final String SONG_TRACK_NUMBER = PACKAGENAME + "song_track_number";
    public static final String SONG_PATH = PACKAGENAME + "song_path";
    public static final String SONG_YEAR = PACKAGENAME + "song_year";
    /*
    playing propertiess
     */
    public static final String PREF_AUTO_PAUSE = PACKAGENAME + "AUTO_PAUSE";
    public static final String ACTION_PLAY = PACKAGENAME + "ACTION_PLAY";
    public static final String ACTION_PAUSE = PACKAGENAME + "ACTION_PAUSE";
    public static final String ACTION_CHANGE_STATE = PACKAGENAME + "ACTION_CHANGE_STATE";
    public static final String ACTION_TOGGLE = PACKAGENAME + "ACTION_TOGGLE";
    public static final String ACTION_NEXT = PACKAGENAME + "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = PACKAGENAME + "ACTION_PREVIOUS";
    public static final String ACTION_STOP = PACKAGENAME + "ACTION_STOP";
    public static final String ACTION_CHOOSE_SONG = PACKAGENAME + "ACTION_CHOOSE_SONG";
    public static final String META_CHANGED = PACKAGENAME + "META_CHANGED";
    public static final String PLAYSTATE_CHANGED = PACKAGENAME + "PLAYSTATE_CHANGED";
    public static final String QUEUE_CHANGED = PACKAGENAME + "QUEUE_CHANGED";
    public static final String POSITION_CHANGED = PACKAGENAME + "POSITION_CHANGED";
    public static final String ITEM_ADDED = PACKAGENAME + "ITEM_ADDED";
    public static final String ORDER_CHANGED = PACKAGENAME + "ORDER_CHANGED";
    public static final String REPEAT_MODE_CHANGED = PACKAGENAME + "REPEAT_MODE_CHANGED";
    public static final String REPEATMODE = PACKAGENAME + "repeatMode";
    public static final String SHUFFLEMODE = PACKAGENAME + "shuffle";
    public static final String PLAYINGSTATE = PACKAGENAME + "playingState";
    public static final String CURRENTPOS = PACKAGENAME + "position";
    public static final String ACTION_PLAYINGVIEW = PACKAGENAME + "PLAYING_VIEW";
    public static final String ACTION_COMMAND = PACKAGENAME + "command";
    public static final String ACTION_COMMAND1 = PACKAGENAME + "command1";
    public static final String ACTION_COMMAND2 = PACKAGENAME + "command2";
    public static final String ACTION_FAV = PACKAGENAME + "widget_fav";
    public static final String PLAYER_POS = PACKAGENAME + "player_pos";
    public static final String PAUSE_SHORTCUTS = PACKAGENAME + "pause_shortcuts";
    public static final String PLAY_SHORTCUTS = PACKAGENAME + "pause_shortcuts";
    public static final String SHORTCUTS_TYPES = PACKAGENAME + "shortcuts_type";

    public static final String SHOW_ALBUM = "show_album";
    public static final String SHOW_ARTIST = "show_artist";
    public static final String SHOW_TAG = "show_tag";

    /**
     * Permissions Array
     */
    public static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.SYSTEM_ALERT_WINDOW};

    /**
     * network link
     */
    public static String lastFmUrl = "http://ws.audioscrobbler.com/2.0/";

    public static String vagUrl = "https://www.vagalume.com.br/";
    public static String indicineUrl = "http://www.indicine.com/";
    public static String lyricsedUrl = "http://lyricsed.com/";
    public static String songlyricsUrl = "http://www.songlyrics.com/";
    public static String atozUrl = "http://www.azlyrics.com/lyrics/";
    public static String metroUrl = "http://www.metrolyrics.com/";
    public static String directUrl = "https://www.directlyrics.com/";
    public static String hindigeetUrl = "http://www.hindigeetmala.net/";
    public static String lyricsondemandUrl = "https://www.lyricsondemand.com/";
    public static String absolutelyricsUrl = "http://www.absolutelyrics.com/lyrics/view/";
    public static String lyricsbogieUrl = "https://www.lyricsbogie.com/movies/";

    /**
     * Database table
     *
     * @param tableName
     * @return
     */
    public static String DefaultColumn(String tableName, boolean torf) {
        if (torf) {
            return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    DefaultColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + Separator +
                    DefaultColumn.SongId + " INTEGER UNIQUE" + Separator +
                    DefaultColumn.SongTitle + " TEXT" + Separator +
                    DefaultColumn.SongArtist + " TEXT" + Separator +
                    DefaultColumn.SongAlbum + " TEXT" + Separator +
                    DefaultColumn.SongAlbumId + " INTEGER" + Separator +
                    DefaultColumn.SongNumber + " INTEGER" + Separator +
                    DefaultColumn.SongPath + " TEXT" + " )";
        } else {
            return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    DefaultColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + Separator +
                    DefaultColumn.ArtistId + " INTEGER UNIQUE" + Separator +
                    DefaultColumn.ArtistTitle + " TEXT" + Separator +
                    DefaultColumn.ArtistAlbumCount + " INTEGER" + Separator +
                    DefaultColumn.ArtistTrackCount + " INTEGER" + " )";
        }
    }

    /**
     * Database to store queueName
     * @param tableName
     * @return
     */
    public static String DefaultColumn(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                DefaultColumn._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + Separator +
                DefaultColumn.QueueName + " TEXT" + " )";
    }
}
