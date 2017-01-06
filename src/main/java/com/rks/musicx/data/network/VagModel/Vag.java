
package com.rks.musicx.data.network.VagModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * 
     * @return
     *     The type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The art
     */
    public Art getArt() {
        return art;
    }

    /**
     * 
     * @param art
     *     The art
     */
    public void setArt(Art art) {
        this.art = art;
    }

    /**
     * 
     * @return
     *     The mus
     */
    public List<Mu> getMus() {
        return mus;
    }

    /**
     * 
     * @param mus
     *     The mus
     */
    public void setMus(List<Mu> mus) {
        this.mus = mus;
    }

    /**
     * 
     * @return
     *     The badwords
     */
    public Boolean getBadwords() {
        return badwords;
    }

    /**
     * 
     * @param badwords
     *     The badwords
     */
    public void setBadwords(Boolean badwords) {
        this.badwords = badwords;
    }

    /**
     *
     * @return
     *     The alb
     */
    public List<Alb> getAlb() {
        return alb;
    }

    /**
     *
     * @param alb
     *     The alb
     */
    public void setAlb(List<Alb> alb) {
        this.alb = alb;
    }

}
