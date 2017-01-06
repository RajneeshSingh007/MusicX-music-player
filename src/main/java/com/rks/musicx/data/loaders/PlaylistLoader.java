package com.rks.musicx.data.loaders;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;

import com.rks.musicx.data.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlaylistLoader extends BaseAsyncTaskLoader<List<Song>> {

    private static final String[] sProjection = {
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA
    };

    private long mPlaylistId;

    public PlaylistLoader(Context context, long playlistId) {
        super(context);
        mPlaylistId = playlistId;
    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> playlist = new ArrayList<>();
        String sortorder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED){
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", mPlaylistId), sProjection, null,null, sortorder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor
                        .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                if (idCol == -1) {
                    idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                }
                int titleCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int trackCol = cursor
                        .getColumnIndex(MediaStore.Audio.Media.TRACK);
                int datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                do {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    String album = cursor.getString(albumCol);
                    long albumId = cursor.getLong(albumIdCol);
                    int track = cursor.getInt(trackCol);
                    String mSongPath = cursor.getString(datacol);
                    Song song = new Song();
                    /*
                     Setup metadata of playlist
                    */
                    song.setAlbum(album);
                    song.setmSongPath(mSongPath);
                    song.setArtist(artist);
                    song.setId(id);
                    song.setAlbumId(albumId);
                    song.setTrackNumber(track);
                    song.setTitle(title);
                    playlist.add(song);
                } while (cursor.moveToNext());
                cursor.close();
            }
            if(cursor == null){
                Collections.emptyList();
            }
            return playlist;
        }else {
            return null;
        }
    }

}
