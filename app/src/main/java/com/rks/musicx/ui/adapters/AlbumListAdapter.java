package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
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

public class AlbumListAdapter extends BaseRecyclerViewAdapter<Album, AlbumListAdapter.AlbumViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private int duration = 300;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;

    public AlbumListAdapter(@NonNull Context context) {
        super(context);
    }

    public void setLayoutID(int layout) {
        layoutID = layout;
    }

    @Override
    public AlbumListAdapter.AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlbumListAdapter.AlbumViewHolder holder, int position) {
        Album albums = getItem(position);
        if (layoutID == R.layout.item_grid_view || layoutID == R.layout.recent_list) {
            int pos = holder.getAdapterPosition();
            if (lastpos < pos) {
                for (Animator animator : Helper.getAnimator(holder.backgroundColor)) {
                    animator.setDuration(duration);
                    animator.setInterpolator(interpolator);
                    animator.start();
                }
            }
            holder.AlbumArtwork.setTransitionName("TransitionArtwork" + position);
            holder.AlbumName.setText(albums.getAlbumName());
            holder.ArtistName.setText(albums.getArtistName());
            ArtworkUtils.ArtworkLoader(getContext(), 300, 600, albums.getAlbumName(), albums.getId(), new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    holder.backgroundColor.setBackgroundColor(colors[0]);
                    holder.AlbumName.setTextColor(Helper.getTitleTextColor(colors[0]));
                    holder.ArtistName.setTextColor(Helper.getTitleTextColor(colors[0]));
                    Helper.animateViews(getContext(), holder.backgroundColor, colors[0]);
                }
            }, holder.AlbumArtwork);
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view) {
            holder.AlbumListName.setText(albums.getAlbumName());
            holder.ArtistListName.setText(albums.getArtistName());
            ArtworkUtils.ArtworkLoader(getContext(), 300, 600, albums.getAlbumName(), albums.getId(), new palette() {
                @Override
                public void palettework(Palette palette) {
                }
            }, holder.AlbumListArtwork);
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                holder.AlbumListName.setTextColor(Color.WHITE);
                holder.ArtistListName.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            }
        }
    }

    @Override
    public Album getItem(int position) {
        if (data == null || data.size() < 0 || data.size() == 0) {
            return null;
        }
        if (position < data.size() && position >= 0) {
            return data.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return (null != data ? data.size() : 0);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getAlbumName().substring(0, 1);
    }

    public void setFilter(List<Album> albumList) {
        data = new ArrayList<>();
        data.addAll(albumList);
        notifyDataSetChanged();
    }


    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView AlbumArtwork;
        private TextView ArtistName, AlbumName, AlbumListName, ArtistListName;
        private LinearLayout backgroundColor;
        private CircleImageView AlbumListArtwork;
        private ImageButton menu;

        @SuppressLint("CutPasteId")
        public AlbumViewHolder(View itemView) {
            super(itemView);

            if (layoutID == R.layout.item_grid_view || layoutID == R.layout.recent_list) {
                AlbumArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
                AlbumName = (TextView) itemView.findViewById(R.id.album_name);
                ArtistName = (TextView) itemView.findViewById(R.id.artist_name);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                backgroundColor = (LinearLayout) itemView.findViewById(R.id.backgroundColor);
                AlbumArtwork.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
            }
            if (layoutID == R.layout.item_list_view) {
                AlbumListArtwork = (CircleImageView) itemView.findViewById(R.id.album_artwork);
                AlbumListName = (TextView) itemView.findViewById(R.id.listalbumname);
                ArtistListName = (TextView) itemView.findViewById(R.id.listartistname);
                AlbumListArtwork.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
        }
    }

}
