package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import com.rks.musicx.data.model.Album;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class AlbumListAdapter extends BaseRecyclerViewAdapter<Album, AlbumListAdapter.AlbumViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private int duration = 300;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private ValueAnimator colorAnimation;

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
                    animator.setDuration(duration).start();
                    animator.setInterpolator(interpolator);
                }
            }
            holder.AlbumName.setText(albums.getAlbumName());
            holder.ArtistName.setText(albums.getArtistName());
            //new getArt(albums,holder).execute();
            ArtworkUtils.ArtworkLoaderPalette(getContext(), albums.getAlbumName(), albums.getId(), holder.AlbumArtwork, new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(),palette);
                    holder.backgroundColor.setBackgroundColor(colors[0]);
                    holder.AlbumName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
                    holder.ArtistName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent2));
                    animateViews(holder, colors[0]);
                }
            });
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view) {
            holder.AlbumListName.setText(albums.getAlbumName());
            holder.ArtistListName.setText(albums.getArtistName());
            ArtworkUtils.ArtworkLoader(getContext(), albums.getAlbumName(), albums.getId(), holder.AlbumListArtwork);
            if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                holder.AlbumListName.setTextColor(Color.WHITE);
                holder.ArtistListName.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            }
        }
    }

    @Override
    public Album getItem(int position) throws ArrayIndexOutOfBoundsException {
        return super.getItem(position);
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

    private void animateViews(AlbumViewHolder albumViewHolder, int colorBg) {
        colorAnimation = setAnimator(0xffe5e5e5,
                colorBg);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                albumViewHolder.backgroundColor.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private ValueAnimator setAnimator(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        return colorAnimation;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView AlbumArtwork;
        private TextView ArtistName, AlbumName, AlbumListName, ArtistListName;
        private LinearLayout backgroundColor;
        private CircleImageView AlbumListArtwork;
        private ImageButton menu;

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
                AlbumListArtwork = (CircleImageView) itemView.findViewById(R.id.album_listartwork);
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

    /*public class getArt extends AsyncTask<Void,Void,Void>{

        Call<LyricsList> lyricsListCall;
        Album album;
        AlbumListAdapter.AlbumViewHolder albumViewHolder;

        public getArt(Album album, AlbumListAdapter.AlbumViewHolder albumViewHolder){
            this.album = album;
            this.albumViewHolder = albumViewHolder;
        }

        @Override
        protected Void doInBackground(Void... params) {
            VagalumeClient vagalumeClient = new VagalumeClient(getContext());
            VagalumeServices vagalumeServices = vagalumeClient.createService(VagalumeServices.class);
            lyricsListCall = vagalumeServices.getAlbumArt(album.getArtistName(),album.getAlbumName(),ApiKey);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lyricsListCall.enqueue(new Callback<LyricsList>() {
                @Override
                public void onResponse(Call<LyricsList> call, Response<LyricsList> response) {
                    LyricsList lyricsList = response.body();
                    if (response.isSuccessful()){
                        if (lyricsList !=null){
                            Picasso.with(getContext())
                                    .load(lyricsList.getArt().getUrl())
                                    .centerCrop()
                                    .resize(300,300)
                                    .error(R.mipmap.ic_launcher)
                                    .placeholder(R.mipmap.ic_launcher)
                                    .config(Bitmap.Config.ARGB_8888)
                                    .into(albumViewHolder.AlbumArtwork, PicassoPalette.with(vagList.get(0).getUrl(),albumViewHolder.AlbumArtwork).intoCallBack(new PicassoPalette.CallBack() {
                                        @Override
                                        public void onPaletteLoaded(Palette palette) {
                                            final int[] colors = getAvailableColor(palette);
                                            albumViewHolder.backgroundColor.setBackgroundColor(colors[0]);
                                            albumViewHolder.AlbumName.setTextColor(colors[1]);
                                            albumViewHolder.ArtistName.setTextColor(colors[2]);
                                            animateViews(albumViewHolder,colors[0], colors[1], colors[2]);
                                        }
                                    }));
                        }
                    }
                }

                @Override
                public void onFailure(Call<LyricsList> call, Throwable t) {

                }
            });
        }
    }*/

}
