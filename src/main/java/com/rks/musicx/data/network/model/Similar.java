
package com.rks.musicx.data.network.model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Similar {

    @SerializedName("artist")
    @Expose
    private List<Artist__> artist = new ArrayList<Artist__>();

    /**
     * 
     * @return
     *     The artist
     */
    public List<Artist__> getArtist() {
        return artist;
    }

    /**
     * 
     * @param artist
     *     The artist
     */
    public void setArtist(List<Artist__> artist) {
        this.artist = artist;
    }

}
