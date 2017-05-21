package com.rks.musicx.services;


import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;

import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;

import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;

/*
 * Created by Coolalien on 02/05/2017.
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

public class MediaSession {

    public static void lockscreenMedia(MediaSessionCompat mediaSessionCompat, MusicXService musicXService, String what) {
        if (musicXService == null) {
            return;
        }
        if (PLAYSTATE_CHANGED.equals(what)) {
            if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, musicXService.getPlayerPos(), 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
            } else {
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
            }
        }
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        if (META_CHANGED.equals(what)) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicXService.getsongTitle());
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicXService.getDuration());
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicXService.getsongArtistName());
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicXService.getsongAlbumName());
            ArtworkUtils.ArtworkLoaderBitmapPalette(musicXService, musicXService.getsongAlbumName(), musicXService.getsongAlbumID(), new palette() {
                @Override
                public void palettework(Palette palette) {

                }
            }, new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                    mediaSessionCompat.setMetadata(builder.build());
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                    mediaSessionCompat.setMetadata(builder.build());
                }
            });
        }
    }
}
