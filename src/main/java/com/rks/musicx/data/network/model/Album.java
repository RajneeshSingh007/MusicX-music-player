package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Coolalien on 10/2/2016.
 */

public class Album {

    @SerializedName("album")
    @Expose
    private Album_ album;

    /**
     *
     * @return
     *     The artist
     */
    public Album_ getAlbum() {
        return album;
    }

    /**
     *
     * @param album
     *     The artist
     */
    public void setAlbum(Album_ album) {
        this.album = album;
    }

}
