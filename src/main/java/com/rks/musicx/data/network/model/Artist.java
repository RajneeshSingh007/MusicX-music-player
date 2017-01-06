
package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Artist {

    @SerializedName("artist")
    @Expose
    private Artist__ artist;

    /**
     * 
     * @return
     *     The artist
     */
    public Artist__ getArtist() {
        return artist;
    }

    /**
     * 
     * @param artist
     *     The artist
     */
    public void setArtist(Artist__ artist) {
        this.artist = artist;
    }

}
