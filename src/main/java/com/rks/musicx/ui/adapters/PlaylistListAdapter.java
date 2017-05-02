package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.misc.utils.Extras;

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
        holder.deletePlaylist.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        Drawable drawable = holder.deletePlaylist.getDrawable();
        drawable.mutate();
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            drawable.setTint(Color.WHITE);
            holder.PlaylistName.setTextColor(Color.WHITE);
        } else {
            drawable.setTint(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
            holder.PlaylistName.setTextColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
        }
    }

    @Override
    public Playlist getItem(int position) throws ArrayIndexOutOfBoundsException {
        return data.size() > 0 ? data.get(position) : null;
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView PlaylistName;
        ImageButton deletePlaylist;
        LinearLayout PlaylistView;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            PlaylistName = (TextView) itemView.findViewById(R.id.name);
            deletePlaylist = (ImageButton) itemView.findViewById(R.id.delete_playlist);
            PlaylistView = (LinearLayout) itemView.findViewById(R.id.item_view);
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
