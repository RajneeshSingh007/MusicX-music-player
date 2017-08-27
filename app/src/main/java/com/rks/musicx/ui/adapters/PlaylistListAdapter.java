package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.PlaylistHelper;

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

public class PlaylistListAdapter extends BaseRecyclerViewAdapter<Playlist, PlaylistListAdapter.PlaylistViewHolder> {

    public PlaylistListAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_list, parent, false);
        return new PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
        Playlist playlist = getItem(position);
        holder.PlaylistName.setText(playlist.getName());
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.getId());
        int songCount = PlaylistHelper.getSongCount(getContext().getContentResolver(), uri);
        holder.SongCount.setText(String.valueOf(songCount) + " " + getContext().getString(R.string.titles));
        holder.deletePlaylist.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        Drawable drawable = holder.deletePlaylist.getDrawable();
        drawable.mutate();
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            drawable.setTint(Color.WHITE);
            holder.PlaylistName.setTextColor(Color.WHITE);
            holder.SongCount.setTextColor(Color.WHITE);
        } else {
            drawable.setTint(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
            holder.SongCount.setTextColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
            holder.PlaylistName.setTextColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
        }
    }

    @Override
    public int getItemCount() {
        return (null != data ? data.size() : 0);
    }

    @Override
    public Playlist getItem(int position) {
        if (data == null || data.size() < 0 || data.size() == 0) {
            return null;
        }
        if (position < data.size() && position >= 0) {
            return data.get(position);
        } else {
            return null;
        }
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView PlaylistName, SongCount;
        private ImageButton deletePlaylist;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            PlaylistName = (TextView) itemView.findViewById(R.id.name);
            deletePlaylist = (ImageButton) itemView.findViewById(R.id.delete_playlist);
            SongCount = (TextView) itemView.findViewById(R.id.song_count);
            itemView.setOnClickListener(this);
            deletePlaylist.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
        }

    }
}
