package com.rks.musicx.data.loaders;

import android.content.Context;

import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.RecentlyPlayed;

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

public class RecentlyPlayedLoader extends BaseAsyncTaskLoader<List<Song>> {

    private RecentlyPlayed recentlyPlayed;
    private int limit;

    public RecentlyPlayedLoader(Context context, int limit) {
        super(context);
        recentlyPlayed = new RecentlyPlayed(context);
        this.limit = limit;
    }

    @Override
    public List<Song> loadInBackground() {
        List<Song> songList = recentlyPlayed.readLimit(limit);
        recentlyPlayed.close();
        return songList;
    }

    public void clearDb() {
        recentlyPlayed.removeAll();
    }
}
