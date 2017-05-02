package com.rks.musicx.services;


import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

    public static void lockscreenMedia(MediaSessionCompat mediaSessionCompat, MusicXService musicXService, String what){
        if (musicXService == null){
            return;
        }
        if (PLAYSTATE_CHANGED.equals(what)){
            if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()){
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING , musicXService.getPlayerPos(), 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
            }else {
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED , 0, 0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build());
            }
        }
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicXService.getsongArtistName());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicXService.getsongAlbumName());
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicXService.getsongTitle());
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicXService.getDuration());
        if (META_CHANGED.equals(what)){
            Glide.with(musicXService)
                    .load(ArtworkUtils.uri(musicXService.getsongAlbumID()))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            if (resource != null){
                                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource);
                            }else {
                                builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, ArtworkUtils.getDefaultArtwork(musicXService));
                            }
                        }
                    });
            mediaSessionCompat.setMetadata(builder.build());
        }
    }
}
