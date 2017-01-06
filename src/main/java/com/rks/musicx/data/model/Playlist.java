package com.rks.musicx.data.model;

/**
 * Created by Coolalien on 6/11/2016.
 */

public class Playlist {
    private long id;
    private String name;

    public Playlist(long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


}
