
package com.rks.musicx.data.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Links {

    @SerializedName("link")
    @Expose
    private Link link;

    /**
     * 
     * @return
     *     The link
     */
    public Link getLink() {
        return link;
    }

    /**
     * 
     * @param link
     *     The link
     */
    public void setLink(Link link) {
        this.link = link;
    }

}
