package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Helper.getAnimator;


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

public class SongListAdapter extends BaseRecyclerViewAdapter<Song, SongListAdapter.SongViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layout = R.layout.song_list;
    private int duration = 300;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private SparseBooleanArray storeChecked = new SparseBooleanArray();
    private boolean isMultiselect;
    private int itemSelected;

    public SongListAdapter(@NonNull Context context) {
        super(context);
    }


    public void setLayoutId(int layoutId) {
        layout = layoutId;
    }

    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SongListAdapter.SongViewHolder holder, int position) {
        Song song = getItem(position);
        if (layout == R.layout.song_list) {
            holder.SongTitle.setText(song.getTitle());
            holder.SongArtist.setText(song.getArtist());
            ArtworkUtils.ArtworkLoader(getContext(), 300, 600, song.getAlbum(), song.getAlbumId(), new palette() {
                @Override
                public void palettework(Palette palette) {

                }
            }, holder.SongArtwork);
            holder.menu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
            Drawable drawable = holder.menu.getDrawable();
            int accentColor = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                drawable.setTint(Color.WHITE);
                holder.SongTitle.setTextColor(Color.WHITE);
                holder.SongArtist.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
                holder.itemView.setBackgroundColor(storeChecked.get(position) ? ContextCompat.getColor(getContext(), R.color.translucent_white_8p) : Color.TRANSPARENT);
            } else {
                drawable.setTint(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
                holder.SongTitle.setTextColor(Color.BLACK);
                holder.SongArtist.setTextColor(Color.DKGRAY);
                holder.itemView.setBackgroundColor(storeChecked.get(position) ? Helper.getColorWithAplha(accentColor, 0.7f) : Color.TRANSPARENT);
            }
        }
        if (layout == R.layout.detail_list) {
            holder.SongTitle.setText(song.getTitle());
            holder.SongArtist.setText(song.getArtist());
            holder.number.setText(position + 1 + ".");
            holder.menu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
            Drawable drawable = holder.menu.getDrawable();
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                drawable.setTint(Color.WHITE);
                holder.SongTitle.setTextColor(Color.WHITE);
                holder.number.setTextColor(Color.WHITE);
                holder.SongArtist.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            } else {
                drawable.setTint(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
                holder.SongTitle.setTextColor(Color.BLACK);
                holder.number.setTextColor(Color.BLACK);
                holder.SongArtist.setTextColor(Color.DKGRAY);
            }
        }
        if (layout == R.layout.item_grid_view || layout == R.layout.recent_list) {
            int pos = holder.getAdapterPosition();
            if (lastpos < pos) {
                for (Animator animator : getAnimator(holder.songView)) {
                    animator.setDuration(duration);
                    animator.setInterpolator(interpolator);
                    animator.start();
                }
            }
            holder.SongTitle.setText(song.getTitle());
            holder.SongArtist.setText(song.getArtist());
            ArtworkUtils.ArtworkLoader(getContext(), 300, 600, song.getAlbum(), song.getAlbumId(), new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    holder.songView.setBackgroundColor(colors[0]);
                    holder.SongTitle.setTextColor(Helper.getTitleTextColor(colors[0]));
                    holder.SongArtist.setTextColor(Helper.getTitleTextColor(colors[0]));
                    Helper.animateViews(getContext(), holder.itemView, colors[0]);
                }
            }, holder.songGridArtwork);
            holder.menu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
            holder.menu.setVisibility(View.VISIBLE);
            Drawable drawable = holder.menu.getDrawable();
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                drawable.setTint(Color.WHITE);
            }
        }
    }

    public int getLayout() {
        return layout;
    }

    @Override
    public List<Song> getSnapshot() {
        return super.getSnapshot();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getTitle().substring(0, 1);
    }

    public void setFilter(List<Song> songList) {
        data = new ArrayList<>();
        data.addAll(songList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != data ? data.size() : 0);
    }

    @Override
    public Song getItem(int position) {
        if (data == null || data.size() < 0 || data.size() == 0) {
            return null;
        }
        if (position < data.size() && position >= 0) {
            return data.get(position);
        } else {
            return null;
        }
    }

    public void exitMultiselectMode() {
        isMultiselect = false;
        itemSelected = 0;
        storeChecked.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(storeChecked.size());
        for (int i = 0; i < storeChecked.size(); ++i) {
            items.add(storeChecked.keyAt(i));
        }
        return items;
    }

    public boolean isMultiselect() {
        return isMultiselect;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView SongTitle, SongArtist, number;
        private CircleImageView SongArtwork;
        private ImageButton menu;
        private LinearLayout songView;
        private ImageView songGridArtwork;

        public SongViewHolder(View itemView) {
            super(itemView);

            if (layout == R.layout.song_list) {
                SongTitle = (TextView) itemView.findViewById(R.id.title);
                SongArtist = (TextView) itemView.findViewById(R.id.artist);
                SongArtwork = (CircleImageView) itemView.findViewById(R.id.artwork);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                songView = (LinearLayout) itemView.findViewById(R.id.item_view);
                itemView.setOnClickListener(this);
                menu.setOnClickListener(this);
                itemView.setLongClickable(true);
                itemView.setOnLongClickListener(this);
            }
            if (layout == R.layout.detail_list) {
                SongTitle = (TextView) itemView.findViewById(R.id.title);
                SongArtist = (TextView) itemView.findViewById(R.id.artist);
                number = (TextView) itemView.findViewById(R.id.number);
                songView = (LinearLayout) itemView.findViewById(R.id.item_view);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                menu.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
            if (layout == R.layout.item_grid_view || layout == R.layout.recent_list) {
                songGridArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
                SongTitle = (TextView) itemView.findViewById(R.id.album_name);
                SongArtist = (TextView) itemView.findViewById(R.id.artist_name);
                songView = (LinearLayout) itemView.findViewById(R.id.backgroundColor);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                menu.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
                songGridArtwork.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (isMultiselect) {
                boolean currentState = storeChecked.get(getAdapterPosition());
                storeChecked.put(getAdapterPosition(), !currentState);
                notifyItemChanged(getAdapterPosition());
                if (currentState) {
                    triggerOnItemClickListener(--itemSelected, v);
                    storeChecked.delete(position);
                } else {
                    triggerOnItemClickListener(++itemSelected, v);
                }
            } else {
                triggerOnItemClickListener(position, v);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (!isMultiselect) {
                isMultiselect = true;
                storeChecked.put(getAdapterPosition(), true);
                notifyItemChanged(getAdapterPosition());
                triggerOnLongClickListener(++itemSelected);
                return true;
            }
            return false;
        }
    }
}
