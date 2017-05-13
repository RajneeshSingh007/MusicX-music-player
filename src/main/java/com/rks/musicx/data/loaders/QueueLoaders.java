package com.rks.musicx.data.loaders;

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

import android.content.Context;

import com.rks.musicx.data.model.Song;
import com.rks.musicx.services.MusicXService;

import java.util.List;

/**
 * Created by Coolalien on 5/12/2017.
 */
public class QueueLoaders extends BaseAsyncTaskLoader<List<Song>> {

    private MusicXService musicXService;


    public QueueLoaders(Context context, MusicXService musicXService) {
        super(context);
        this.musicXService = musicXService;
    }

    @Override
    public List<Song> loadInBackground() {
        if (musicXService == null){
            return null;
        }
        List<Song> queueList = musicXService.getPlayList();
        if (queueList.size() > 0){
            return queueList;
        }
        return null;
    }
}
