package com.rks.musicx.data.network.VagModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Mu {

    @SerializedName("text")
    @Expose
    public String text;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("lang")
    @Expose
    private Integer lang;
    @SerializedName("translate")
    @Expose
    private List<Translate> translate = new ArrayList<Translate>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Integer getLang() {
        return lang;
    }

    public void setLang(Integer lang) {
        this.lang = lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Translate> getTranslate() {
        return translate;
    }

    public void setTranslate(List<Translate> translate) {
        this.translate = translate;
    }


}
