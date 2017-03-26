package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Album {

    @SerializedName("album")
    @Expose
    private Album_ album;

    public Album_ getAlbum() {
        return album;
    }

    public void setAlbum(Album_ album) {
        this.album = album;
    }

}
