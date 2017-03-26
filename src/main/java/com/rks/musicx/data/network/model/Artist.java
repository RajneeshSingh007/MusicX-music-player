package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Artist {

    @SerializedName("artist")
    @Expose
    private Artist__ artist;

    public Artist__ getArtist() {
        return artist;
    }

    public void setArtist(Artist__ artist) {
        this.artist = artist;
    }

}
