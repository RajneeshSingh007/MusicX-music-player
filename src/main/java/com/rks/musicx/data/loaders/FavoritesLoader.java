package com.rks.musicx.data.loaders;

import android.content.Context;

import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.Favorites;

import java.util.List;


public class FavoritesLoader extends BaseAsyncTaskLoader<List<Song>>{

    Favorites favorites;

    public FavoritesLoader(Context context) {
        super(context);
        favorites = new Favorites(context);
    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> songList = favorites.read();
        favorites.close();
        return songList;
    }

    public void clearDb(){
        favorites.removeAll();
    }
}
