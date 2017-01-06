package com.rks.musicx.data.model;

/**
 * Created by Coolalien on 6/11/2016.
 */

public class Album {

    private long id;
    private String albumName;
    private String artistName;
    private int year;
    private int trackCount;

    /**
     * Getter
     * @return
     */
    public long getId() {
        return id;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getYear() {
        return year;
    }

    public int getTrackCount() {
        return trackCount;
    }

    /**
     * Setter
     */

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
