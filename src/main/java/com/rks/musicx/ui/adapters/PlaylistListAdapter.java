package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
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

/**
 * Created by Andry on 29/10/15.
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
        holder.deletePlaylist.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_menu));
        Drawable drawable = holder.deletePlaylist.getDrawable();
        drawable.mutate();
        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dark_theme", false)) {
            drawable.setTint(Color.WHITE);
            holder.PlaylistName.setTextColor(Color.WHITE);
        } else {
            drawable.setTint(ContextCompat.getColor(getContext(),R.color.MaterialGrey));
        }
    }

    @Override
    public Playlist getItem(int position) throws ArrayIndexOutOfBoundsException {
        return super.getItem(position);
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
