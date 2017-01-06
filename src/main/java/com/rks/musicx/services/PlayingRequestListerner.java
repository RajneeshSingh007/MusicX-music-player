package com.rks.musicx.services;

import com.rks.musicx.data.model.Song;

import java.util.List;

/**
 * Created by Coolalien on 12/25/2016.
 */

public class PlayingRequestListerner {

    private static List<Song> mPlayList;
    private static int mIndex;
    private static boolean play;
    private static Song mNextTrack;
    private static Song mAddToQueue;


    public static void sendRequests(MusicXService musicXService) {
        if (musicXService == null) {
            return;
        }

        if (mPlayList != null) {
            musicXService.setPlaylist(mPlayList, mIndex, play);
            mPlayList = null;
        }

        if (mAddToQueue != null) {
            musicXService.addToQueue(mAddToQueue);
            mAddToQueue = null;
        }

        if (mNextTrack != null) {
            musicXService.setAsNextTrack(mNextTrack);
            mNextTrack = null;
        }
    }
}
