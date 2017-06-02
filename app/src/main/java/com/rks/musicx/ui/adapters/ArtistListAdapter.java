package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.network.ArtistArtwork;
import com.rks.musicx.interfaces.bitmap;
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

public class ArtistListAdapter extends BaseRecyclerViewAdapter<Artist, ArtistListAdapter.ArtistViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private int duration = 300;
    private ValueAnimator colorAnimation;

    public ArtistListAdapter(@NonNull Context context) {
        super(context);
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        Artist artists = getItem(position);
        if (!Extras.getInstance().saveData()) {
            ArtistArtwork artistArtwork = new ArtistArtwork(getContext(), artists.getName());
            artistArtwork.execute();
        }
        if (layoutID == R.layout.item_grid_view) {
            int pos = holder.getAdapterPosition();
            if (lastpos < pos) {
                for (Animator animator : Helper.getAnimator(holder.backgroundColor)) {
                    animator.setDuration(duration).start();
                    animator.setInterpolator(interpolator);
                }
            }
            holder.ArtistName.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            holder.AlbumCount.setText(artists.getName());
            ArtworkUtils.ArtworkLoader(getContext(), null, ArtworkUtils.getArtistCoverPath(getContext(), artists.getName()).getAbsolutePath(), 0, new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    holder.backgroundColor.setBackgroundColor(colors[0]);
                    holder.ArtistName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
                    holder.AlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent2));
                    Helper.animateViews(getContext(), holder.backgroundColor, colors[0]);
                }
            }, new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    holder.ArtistsArtwork.setImageBitmap(bitmap);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    holder.ArtistsArtwork.setImageBitmap(bitmap);
                }
            });
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view) {
            holder.ArtistListName.setText(artists.getName());
            holder.ArtistListAlbumCount.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            ArtworkUtils.ArtworkLoader(getContext(), null, ArtworkUtils.getArtistCoverPath(getContext(), artists.getName()).getAbsolutePath(), 0, new palette() {
                @Override
                public void palettework(Palette palette) {

                }
            }, new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    holder.ArtistListArtwork.setImageBitmap(bitmap);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    holder.ArtistListArtwork.setImageBitmap(bitmap);
                }
            });
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                holder.ArtistListName.setTextColor(Color.WHITE);
                holder.ArtistListAlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            }
        }

    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getName().substring(0, 1);
    }

    public void setFilter(List<Artist> artistList) {
        data = new ArrayList<>();
        data.addAll(artistList);
        notifyDataSetChanged();
    }


    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView ArtistName, ArtistListAlbumCount, AlbumCount, ArtistListName;
        private ImageView ArtistsArtwork;
        private LinearLayout backgroundColor;
        private CircleImageView ArtistListArtwork;
        private ImageButton menu;

        @SuppressLint("CutPasteId")
        public ArtistViewHolder(View itemView) {
            super(itemView);
            if (layoutID == R.layout.item_grid_view) {
                ArtistsArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
                AlbumCount = (TextView) itemView.findViewById(R.id.album_name);
                ArtistName = (TextView) itemView.findViewById(R.id.artist_name);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                backgroundColor = (LinearLayout) itemView.findViewById(R.id.backgroundColor);
                ArtistsArtwork.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
            }
            if (layoutID == R.layout.item_list_view) {
                ArtistListArtwork = (CircleImageView) itemView.findViewById(R.id.album_artwork);
                ArtistListName = (TextView) itemView.findViewById(R.id.listalbumname);
                ArtistListAlbumCount = (TextView) itemView.findViewById(R.id.listartistname);
                ArtistListArtwork.setOnClickListener(this);
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
