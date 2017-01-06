package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.network.LastFmClients;
import com.rks.musicx.data.network.LastFmServices;
import com.rks.musicx.data.network.model.Artist__;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ArtistListAdapter extends BaseRecyclerViewAdapter<Artist, ArtistListAdapter.ArtistViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private int duration = 300;

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

    private Animator[] getAnimator(View view){
        return new Animator[]{
                ObjectAnimator.ofFloat(view,"translationY", view.getMeasuredHeight(),0)
        };
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        Artist artists = getItem(position);
        if(layoutID == R.layout.item_grid_view){
            int pos = holder.getAdapterPosition();
            if (lastpos < pos){
                for (Animator animator : getAnimator(holder.backgroundColor)){
                    animator.setDuration(duration).start();
                    animator.setInterpolator(interpolator);
                }
            }
            holder.ArtistName.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            holder.AlbumCount.setText(artists.getName());
            holder.ArtistsArtwork.setImageResource(R.mipmap.ic_launcher);
            if (!Extras.getInstance().saveData()){
                getArtistData artistData = new getArtistData(artists,holder,position);
                artistData.execute();
            }
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view){
            holder.ArtistListName.setText(artists.getName());
            holder.ArtistListAlbumCount.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            if (!Extras.getInstance().saveData()){
                getArtistData artistData = new getArtistData(artists,holder,position);
                artistData.execute();
            }
            if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("dark_theme", false)) {
                holder.ArtistListName.setTextColor(Color.WHITE);
                holder.ArtistListAlbumCount.setTextColor(ContextCompat.getColor(getContext(),R.color.darkthemeTextColor));
            }
            holder.ArtistListArtwork.setImageResource(R.mipmap.ic_launcher);
        }

    }

    @Override
    public Artist getItem(int position) throws ArrayIndexOutOfBoundsException {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getName().substring(0,1);
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView ArtistName,ArtistListAlbumCount,AlbumCount,ArtistListName;
        private ImageView ArtistsArtwork;
        private LinearLayout backgroundColor;
        private CircleImageView ArtistListArtwork;
        private ImageButton menu;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            if(layoutID == R.layout.item_grid_view){
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
                ArtistListArtwork = (CircleImageView) itemView.findViewById(R.id.album_listartwork);
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

    public void setFilter(List<Artist> artistList) {
        data = new ArrayList<>();
        data.addAll(artistList);
        notifyDataSetChanged();
    }

    /**
     * Class to get artistArtwork from last.fm
     */
    public class getArtistData extends AsyncTask<Void,Void,Void> {

        Call<com.rks.musicx.data.network.model.Artist> artistCall;
        Artist artist;
        ArtistViewHolder artistViewHolder;
        int pos;

        public getArtistData(Artist artist,ArtistViewHolder artistViewHolder, int pos){
            this.artist = artist;
            this.artistViewHolder = artistViewHolder;
            this.pos = pos;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            LastFmClients last = new LastFmClients(getContext());
            LastFmServices lastFmServices = last.createService(LastFmServices.class);
            artistCall = lastFmServices.getartist(artist.getName());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
                @Override
                public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
                    com.rks.musicx.data.network.model.Artist getartist = response.body();
                    if(response.isSuccessful() && getartist != null){
                        final Artist__ artist1 = getartist.getArtist();
                        if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                            for (Image_ artistArtwork : artist1.getImage()) {
                                if (artistArtwork.getSize().equals("large")){
                                    String ArtistArtwork = artistArtwork.getText();
                                    if (layoutID == R.layout.item_grid_view) {
                                        ArtworkUtils.ArtworkLoaderPalette(getContext(), ArtistArtwork, artistViewHolder.ArtistsArtwork, new palette() {
                                            @Override
                                            public void palettework(Palette palette) {
                                                final int[] colors = getAvailableColor(palette);
                                                artistViewHolder.backgroundColor.setBackgroundColor(colors[0]);
                                                artistViewHolder.ArtistName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
                                                artistViewHolder.AlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent2));
                                                animateViews(artistViewHolder, colors[0]);
                                            }
                                        });
                                    }
                                    if (layoutID == R.layout.item_list_view) {
                                        ArtworkUtils.ArtworkNetworkLoader(getContext(), ArtistArtwork, artistViewHolder.ArtistListArtwork);
                                    }
                                }
                            }
                        }else {
                            if (layoutID == R.layout.item_grid_view) {
                                artistViewHolder.ArtistsArtwork.setImageResource(R.mipmap.ic_launcher);
                            }
                            if (layoutID == R.layout.item_list_view){
                                artistViewHolder.ArtistListArtwork.setImageResource(R.mipmap.ic_launcher);
                            }
                        }
                    }else {
                        if (layoutID == R.layout.item_grid_view) {
                            artistViewHolder.ArtistsArtwork.setImageResource(R.mipmap.ic_launcher);
                        }
                        if (layoutID == R.layout.item_list_view){
                            artistViewHolder.ArtistListArtwork.setImageResource(R.mipmap.ic_launcher);
                        }
                    }
                }

                @Override
                public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
                }
            });
        }
    }
    private ValueAnimator colorAnimation;

    private void animateViews(ArtistViewHolder artistViewHolder, int colorBg) {
        colorAnimation = setAnimator(0xffe5e5e5,
                colorBg);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                artistViewHolder.backgroundColor.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private ValueAnimator setAnimator(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        long duration = 800;
        colorAnimation.setDuration(duration);
        return colorAnimation;
    }

    private int[] getAvailableColor(Palette palette) {
        int[] temp = new int[3]; //array with size 3
        if (palette.getDarkVibrantSwatch() != null) {
            temp[0] = palette.getDarkVibrantSwatch().getRgb();
            temp[1] = palette.getDarkVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getDarkVibrantSwatch().getBodyTextColor();
        } else if (palette.getDarkMutedSwatch() != null) {
            temp[0] = palette.getDarkMutedSwatch().getRgb();
            temp[1] = palette.getDarkMutedSwatch().getTitleTextColor();
            temp[2] = palette.getDarkMutedSwatch().getBodyTextColor();
        }else if (palette.getVibrantSwatch() != null) {
            temp[0] = palette.getVibrantSwatch().getRgb();
            temp[1] = palette.getVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getVibrantSwatch().getBodyTextColor();
        }else if (palette.getDominantSwatch() != null) {
            temp[0] = palette.getDominantSwatch().getRgb();
            temp[1] = palette.getDominantSwatch().getTitleTextColor();
            temp[2] = palette.getDominantSwatch().getBodyTextColor();
        }else if (palette.getMutedSwatch() != null) {
            temp[0] = palette.getMutedSwatch().getRgb();
            temp[1] = palette.getMutedSwatch().getTitleTextColor();
            temp[2] = palette.getMutedSwatch().getBodyTextColor();
        }else {
            String atkey = Helper.getATEKey(getContext());
            int accent = Config.accentColor(getContext(),atkey);
            temp[0] = accent;
            temp[1] = 0xffe5e5e5;
            temp[2] = accent;
        }
        return temp;
    }
}
