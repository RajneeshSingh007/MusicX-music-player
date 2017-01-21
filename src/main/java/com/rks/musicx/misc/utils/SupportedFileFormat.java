package com.rks.musicx.misc.utils;

/**
 * Created by Coolalien on 1/20/2017.
 */

public enum  SupportedFileFormat {

    M4A("m4a"),
    MP3("mp3"),
    WAV("wav"),
    AAC("aac"),
    OGG("ogg");

    private String filesuffix;

    SupportedFileFormat( String filesuffix ) {
        this.filesuffix = filesuffix;
    }

    public String getFilesuffix() {
        return filesuffix;
    }
}
