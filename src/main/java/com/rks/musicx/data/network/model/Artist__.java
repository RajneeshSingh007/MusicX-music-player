package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Artist__ {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("image")
    @Expose
    private List<Image_> image = new ArrayList<Image_>();
    @SerializedName("streamable")
    @Expose
    private String streamable;
    @SerializedName("ontour")
    @Expose
    private String ontour;
    @SerializedName("stats")
    @Expose
    private Stats stats;
    @SerializedName("similar")
    @Expose
    private Similar similar;
    @SerializedName("tags")
    @Expose
    private Tags tags;
    @SerializedName("bio")
    @Expose
    private Bio bio;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Image_> getImage() {
        return image;
    }

    public void setImage(List<Image_> image) {
        this.image = image;
    }

    public String getStreamable() {
        return streamable;
    }

    public void setStreamable(String streamable) {
        this.streamable = streamable;
    }

    public String getOntour() {
        return ontour;
    }

    public void setOntour(String ontour) {
        this.ontour = ontour;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Similar getSimilar() {
        return similar;
    }

    public void setSimilar(Similar similar) {
        this.similar = similar;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Bio getBio() {
        return bio;
    }

    public void setBio(Bio bio) {
        this.bio = bio;
    }

}
