package com.rks.musicx.data.network.VagModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Vag {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("art")
    @Expose
    private Art art;
    @SerializedName("mus")
    @Expose
    private List<Mu> mus = new ArrayList<Mu>();
    @SerializedName("badwords")
    @Expose
    private Boolean badwords;
    @SerializedName("extra")
    @Expose
    private List<Alb> alb = new ArrayList<Alb>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Art getArt() {
        return art;
    }

    public void setArt(Art art) {
        this.art = art;
    }

    public List<Mu> getMus() {
        return mus;
    }

    public void setMus(List<Mu> mus) {
        this.mus = mus;
    }

    public Boolean getBadwords() {
        return badwords;
    }

    public void setBadwords(Boolean badwords) {
        this.badwords = badwords;
    }

    public List<Alb> getAlb() {
        return alb;
    }

    public void setAlb(List<Alb> alb) {
        this.alb = alb;
    }

}
