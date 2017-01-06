package com.rks.musicx.misc.utils;

import com.rks.musicx.data.model.Song;

import java.util.List;

/**
 * Created by Coolalien on 12/26/2016.
 */

public interface MetaDatas {

    void onSongSelected(List<Song> songList, int pos);

    void onShuffleRequested(List<Song> songList, boolean play);

    void addToQueue(Song song);

    void setAsNextTrack(Song song);
}
