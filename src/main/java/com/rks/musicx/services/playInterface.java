package com.rks.musicx.services;

import com.rks.musicx.data.model.Song;

import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public interface playInterface {

    /*
    *@playing properties
    */
    void play();

    void pause();

    void playnext(boolean torf);

    void playprev(boolean torf);

    void shuffle();

    void toggle();

    void returnHome();

    void forwardPlayingView();

    int getDuration();

    int getPlayerPos();

    void seekto(int seek);

    void setPlaylist(List<Song> songList, int pos, boolean play);

    void smartplaylist(List<Song> smartplaylist);

    void trackingstart();

    void trackingstop();

    void startCurrentTrack(Song song);

    void setPlaylistandShufle(List<Song> songList, boolean play);

    void addToQueue(Song song);

    void setAsNextTrack(Song song);

    void fastplay(boolean torf, Song song);

    int getnextPos(boolean yorno);

    int getprevPos(boolean yorno);

    void checkTelephonyState();

    void headsetState();

    void stopMediaplayer();

    void mediaLockscreen();

    void updatemediaLockscreen(String update);

    void updateService(String updateservices);

    void buildNotification();

    int getNextrepeatMode();

    void setdataPos(int pos, boolean play);

    int returnpos();

    void refreshWidget();

    boolean getAudioFocus();

    void abandonAudioFocus();

    void clearQueue();

    /*
    *@return song MetaData
    */
    String getsongTitle();

    long getsongId();

    String getsongAlbumName();

    String getsongArtistName();

    String getsongData();

    long getsongAlbumID();

    int getsongNumber();

}
