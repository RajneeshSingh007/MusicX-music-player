package com.rks.musicx.data.loaders;

import android.content.Context;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.interfaces.DefaultColumn;
import com.rks.musicx.misc.utils.Constants;

import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FavoritesLoader extends BaseAsyncTaskLoader<List<Song>> {

    private CommonDatabase commonDatabase;
    private String order = DefaultColumn._ID + " DESC";
    private int limit;

    public FavoritesLoader(Context context, int limit) {
        super(context);
        commonDatabase = new CommonDatabase(context, Constants.Fav_TableName, true);
        this.limit = limit;
    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> songList = commonDatabase.readLimit(limit, order);
        commonDatabase.close();
        return songList;
    }

    public void clearDb() {
        commonDatabase.removeAll();
        commonDatabase.close();
    }
}
