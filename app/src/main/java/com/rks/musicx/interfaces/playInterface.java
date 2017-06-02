package com.rks.musicx.interfaces;

import com.rks.musicx.data.model.Song;

import java.util.List;

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

    void updateService(String updateservices);

    int getNextrepeatMode();

    void setdataPos(int pos, boolean play);

    int returnpos();

    void clearQueue();

    void forceStop();

    void receiverCleanup();

    int audioSession();

    void restorePos();

    int fadeDurationValue();

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

    long getArtistID();

    String getYear();


}
