package com.rks.musicx.data.network.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Album_ {

    private String name;
    private String artist;

    private String mbid;

    @SerializedName("image")
    private List<Image_> imageList;

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getMbid() {
        return mbid;
    }

    public List<Image_> getImageList() {
        return imageList;
    }
}
