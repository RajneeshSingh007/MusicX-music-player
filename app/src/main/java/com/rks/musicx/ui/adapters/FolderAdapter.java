package com.rks.musicx.ui.adapters;

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
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Folder;
import com.rks.musicx.data.model.Song;
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

public class FolderAdapter extends BaseRecyclerViewAdapter<Folder, FolderAdapter.Folderviewholder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<Song> songList = new ArrayList<>();
    private SparseBooleanArray storeChecked = new SparseBooleanArray();
    private boolean isMultiselect;
    private int itemSelected;

    public FolderAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    public FolderAdapter.Folderviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.song_list, parent, false);
        return new Folderviewholder(rootView);
    }

    @Override
    public void onBindViewHolder(FolderAdapter.Folderviewholder holder, int position) {
        Folder folder = getItem(position);
        holder.filename.setTypeface(Helper.getFont(getContext()));
        holder.extraParam.setTypeface(Helper.getFont(getContext()));
        holder.menu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        Drawable drawable = holder.menu.getDrawable();
        int accentColor = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            holder.filename.setTextColor(Color.WHITE);
            drawable.setTint(Color.WHITE);
            holder.extraParam.setTextColor(Color.WHITE);
        } else {
            holder.filename.setTextColor(Color.BLACK);
            holder.extraParam.setTextColor(Color.BLACK);
            drawable.setTint(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
        }
        if (folder.getFile().isDirectory()) {
            Glide.with(getContext())
                    .load(R.drawable.folder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .crossFade()
                    .placeholder(R.drawable.folder)
                    .error(R.drawable.folder)
                    .into(holder.thumbnail);
            holder.thumbnail.setBorderColor(Color.TRANSPARENT);
            holder.thumbnail.setBorderWidth(0);
            int count = folder.getFileCount();
            if (count == 0) {
                holder.extraParam.setVisibility(View.GONE);
            } else {
                holder.extraParam.setText(String.valueOf(folder.getFileCount()) + " files");
            }
            if (position == 0) {
                holder.filename.setText("..");
                holder.menu.setVisibility(View.GONE);
            } else {
                holder.filename.setText(folder.getFile().getName());
                holder.menu.setVisibility(View.VISIBLE);
            }
        } else {
            songList = folder.getSongList();
            if (songList.size() > 0) {
                String currentPath = folder.getFile().getAbsolutePath();
                int index = Helper.getIndex(currentPath, songList);
                if (index > -1) {
                    Song song = songList.get(index);
                    holder.filename.setText(song.getTitle());
                    holder.extraParam.setText(song.getArtist());
                    holder.extraParam.setVisibility(View.VISIBLE);
                    ArtworkUtils.ArtworkLoader(getContext(), 300, 600, song.getAlbum(), song.getAlbumId(), new palette() {
                        @Override
                        public void palettework(Palette palette) {

                        }
                    }, holder.thumbnail);
                    if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                        holder.itemView.setBackgroundColor(storeChecked.get(index) ? ContextCompat.getColor(getContext(), R.color.translucent_white_8p) : Color.TRANSPARENT);
                    } else {
                        holder.itemView.setBackgroundColor(storeChecked.get(index) ? Helper.getColorWithAplha(accentColor, 0.7f) : Color.TRANSPARENT);
                    }
                }
            }
        }
    }

    public void setFilter(List<Folder> filter) {
        data = new ArrayList<>();
        data.addAll(filter);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != data ? data.size() : 0);
    }

    @Override
    public Folder getItem(int position) {
        if (data == null || data.size() < 0 || data.size() == 0) {
            return null;
        }
        if (position < data.size() && position >= 0) {
            return data.get(position);
        } else {
            return null;
        }
    }

    public List<Song> getSongList() {
        return songList;
    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        if (data.get(position).getFile().isDirectory()) {
            return data.get(position).getFile().getName().substring(0, 1);
        } else {
            if (songList.size() > 0) {
                int index = Helper.getIndex(data.get(position).getFile().getAbsolutePath(), songList);
                if (index > -1) {
                    return getItem(position).getSongList().get(index).getTitle().substring(0, 1);
                } else {
                    return "#";
                }
            } else {
                return "#";
            }
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

    public class Folderviewholder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private CircleImageView thumbnail;
        private TextView filename, extraParam;
        private ImageButton menu;

        public Folderviewholder(View itemView) {
            super(itemView);
            thumbnail = (CircleImageView) itemView.findViewById(R.id.artwork);
            filename = (TextView) itemView.findViewById(R.id.title);
            menu = (ImageButton) itemView.findViewById(R.id.menu_button);
            extraParam = (TextView) itemView.findViewById(R.id.artist);
            itemView.setOnClickListener(this);
            menu.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            menu.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (isMultiselect) {
                if (position < data.size() || position >= 0) {
                    if (!data.get(position).getFile().isDirectory()) {
                        String path = data.get(position).getFile().getAbsolutePath();
                        int index = Helper.getIndex(path, getSongList());
                        boolean currentState = storeChecked.get(index);
                        storeChecked.put(index, !currentState);
                        notifyItemChanged(index);
                        if (currentState) {
                            triggerOnItemClickListener(--itemSelected, v);
                            storeChecked.delete(index);
                        } else {
                            triggerOnItemClickListener(++itemSelected, v);
                        }
                    }
                }
            } else {
                triggerOnItemClickListener(position, v);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (!isMultiselect) {
                int position = getAdapterPosition();
                if (position < data.size() || position >= 0) {
                    if (!data.get(position).getFile().isDirectory()) {
                        isMultiselect = true;
                        String path = data.get(position).getFile().getAbsolutePath();
                        int index = Helper.getIndex(path, getSongList());
                        if (index > -1) {
                            storeChecked.put(index, true);
                            notifyItemChanged(index);
                            triggerOnLongClickListener(++itemSelected);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}



