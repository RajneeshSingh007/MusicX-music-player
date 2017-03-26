package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Image_ {

    @SerializedName("#text")
    @Expose
    private String text;
    @SerializedName("size")
    @Expose
    private String size;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
