package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Similar {

    @SerializedName("artist")
    @Expose
    private List<Artist__> artist = new ArrayList<Artist__>();

    public List<Artist__> getArtist() {
        return artist;
    }

    public void setArtist(List<Artist__> artist) {
        this.artist = artist;
    }

}
