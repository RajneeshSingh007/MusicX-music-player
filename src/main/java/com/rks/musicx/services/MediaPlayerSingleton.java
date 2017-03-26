package com.rks.musicx.services;

import android.media.MediaPlayer;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class MediaPlayerSingleton {

    private static MediaPlayerSingleton instance = null;

    private final MediaPlayer sMediaPlayer;

    protected MediaPlayerSingleton() {
        sMediaPlayer = new MediaPlayer();
    }

    public static MediaPlayerSingleton getInstance() {
        if (instance == null) {
            instance = new MediaPlayerSingleton();
        }
        return instance;
    }

    public MediaPlayer getMediaPlayer() {
        return sMediaPlayer;
    }


}
