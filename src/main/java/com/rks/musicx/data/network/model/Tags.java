package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Tags {

    @SerializedName("tag")
    @Expose
    private List<Tag> tag = new ArrayList<Tag>();

    public List<Tag> getTag() {
        return tag;
    }

    public void setTag(List<Tag> tag) {
        this.tag = tag;
    }

}
