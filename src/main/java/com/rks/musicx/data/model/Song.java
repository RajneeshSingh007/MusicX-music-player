package com.rks.musicx.data.model;

/**
 * Created by Coolalien on 6/11/2016.
 */


public class Song {

    private long id;
    private String title;
    private String artist;
    private String album;
    private int trackNumber;
    private long albumId;
    private String genre;
    private String mSongPath;
    /*
    Getter
     */
    public long getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public String getGenre() {
        return genre;
    }

    public String getmSongPath() {
        return mSongPath;
    }

    /*
    Setter
     */

    public void setmSongPath(String mSongPath) {
        this.mSongPath = mSongPath;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }
}


