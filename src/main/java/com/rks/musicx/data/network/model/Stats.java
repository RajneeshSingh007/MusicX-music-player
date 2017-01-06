
package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Stats {

    @SerializedName("listeners")
    @Expose
    private String listeners;
    @SerializedName("playcount")
    @Expose
    private String playcount;

    /**
     * 
     * @return
     *     The listeners
     */
    public String getListeners() {
        return listeners;
    }

    /**
     * 
     * @param listeners
     *     The listeners
     */
    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    /**
     * 
     * @return
     *     The playcount
     */
    public String getPlaycount() {
        return playcount;
    }

    /**
     * 
     * @param playcount
     *     The playcount
     */
    public void setPlaycount(String playcount) {
        this.playcount = playcount;
    }

}
