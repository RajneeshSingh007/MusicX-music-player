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
import android.util.Log;
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
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class ArtistListAdapter extends BaseRecyclerViewAdapter<Artist, ArtistListAdapter.ArtistViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private int duration = 300;
    private Call<com.rks.musicx.data.network.model.Artist> artistCall;
    private LastFmClients lastFmClients;
    private LastFmServices lastFmServices;
    private InputStream inputStream;
    private OutputStream outputStream;
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
        lastFmClients = new LastFmClients(getContext());
        lastFmServices = lastFmClients.createService(LastFmServices.class);
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
            if (!Extras.getInstance().saveData()) {
                artistCall = lastFmServices.getartist(artists.getName());
                artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
                    @Override
                    public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
                        com.rks.musicx.data.network.model.Artist getartist = response.body();
                        if (response.isSuccessful() && getartist != null) {
                            final Artist__ artist1 = getartist.getArtist();
                            if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                                String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
                                File file = new File(artistImagePath);
                                for (Image_ artistArtwork : artist1.getImage()) {
                                    if (Extras.getInstance().hqArtistArtwork()) {
                                        if (file.exists()) {
                                            AndroidNetworking.delete(file.getAbsolutePath());
                                        }
                                        AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artists.getName() + ".jpeg")
                                                .setTag("DownloadingArtistImage")
                                                .setPriority(Priority.MEDIUM)
                                                .build()
                                                .startDownload(new DownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        Log.d("Artist", "successfully downloaded");
                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {
                                                        Log.d("Artist", "failed");
                                                    }
                                                });

                                    } else {
                                        if (file.exists()) {
                                            AndroidNetworking.delete(file.getAbsolutePath());
                                        }
                                        AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artists.getName() + ".jpeg")
                                                .setTag("DownloadingArtistImage")
                                                .setPriority(Priority.MEDIUM)
                                                .build()
                                                .startDownload(new DownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        Log.d("Artist", "successfully downloaded");
                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {
                                                        Log.d("Artist", "failed");
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Log.d("haha", "downloading failed");
                            }
                        } else {
                            Log.d("haha", "downloading failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
                        Log.d("ArtistAdapter", "error", t);
                    }
                });
            }
            String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
            File file = new File(artistImagePath);
            if (file.exists()) {
                ArtworkUtils.ArtworkLoaderPalette(getContext(), artists.getName(), file.getAbsolutePath(), holder.ArtistsArtwork, new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = getAvailableColor(palette);
                        holder.backgroundColor.setBackgroundColor(colors[0]);
                        holder.ArtistName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
                        holder.AlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent2));
                        animateViews(holder, colors[0]);
                    }
                });
            } else {
                holder.ArtistsArtwork.setImageResource(R.mipmap.ic_launcher);
            }
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view) {
            holder.ArtistListName.setText(artists.getName());
            holder.ArtistListAlbumCount.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            if (!Extras.getInstance().saveData()) {
                artistCall = lastFmServices.getartist(artists.getName());
                artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
                    @Override
                    public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
                        com.rks.musicx.data.network.model.Artist getartist = response.body();
                        if (response.isSuccessful() && getartist != null) {
                            final Artist__ artist1 = getartist.getArtist();
                            if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                                String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
                                File file = new File(artistImagePath);
                                for (Image_ artistArtwork : artist1.getImage()) {
                                    if (Extras.getInstance().hqArtistArtwork()) {
                                        if (file.exists()) {
                                            AndroidNetworking.delete(file.getAbsolutePath());
                                        }
                                        AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artists.getName() + ".jpeg")
                                                .setTag("DownloadingArtistImage")
                                                .setPriority(Priority.MEDIUM)
                                                .build()
                                                .startDownload(new DownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        Log.d("Artist", "successfully downloaded");
                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {
                                                        Log.d("Artist", "failed");
                                                    }
                                                });

                                    } else {
                                        if (file.exists()) {
                                            AndroidNetworking.delete(file.getAbsolutePath());
                                        }
                                        AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artists.getName() + ".jpeg")
                                                .setTag("DownloadingArtistImage")
                                                .setPriority(Priority.MEDIUM)
                                                .build()
                                                .startDownload(new DownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        Log.d("Artist", "successfully downloaded");
                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {
                                                        Log.d("Artist", "failed");
                                                    }
                                                });
                                    }
                                }
                            } else {
                                Log.d("haha", "downloading failed");
                            }
                        } else {
                            Log.d("haha", "downloading failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
                        Log.d("ArtistAdapter", "error", t);
                    }
                });
            }
            String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
            File file = new File(artistImagePath);
            if (file.exists()) {
                ArtworkUtils.ArtworkNetworkLoader(getContext(), artists.getName(), file.getAbsolutePath(), holder.ArtistListArtwork);
            } else {
                holder.ArtistListArtwork.setImageResource(R.mipmap.ic_launcher);
            }
            if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                holder.ArtistListName.setTextColor(Color.WHITE);
                holder.ArtistListAlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            }
        }

    }

    public String artworkQuality(Image_ artistArtwork) {
        if (artistArtwork.getSize().equals("large")) {
            return artistArtwork.getText();
        } else if (artistArtwork.getSize().equals("mega")) {
            return artistArtwork.getText();
        } else {
            return artistArtwork.getText();
        }
    }

    @Override
    public Artist getItem(int position) throws ArrayIndexOutOfBoundsException {
        return super.getItem(position);
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
        } else if (palette.getVibrantSwatch() != null) {
            temp[0] = palette.getVibrantSwatch().getRgb();
            temp[1] = palette.getVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getVibrantSwatch().getBodyTextColor();
        } else if (palette.getDominantSwatch() != null) {
            temp[0] = palette.getDominantSwatch().getRgb();
            temp[1] = palette.getDominantSwatch().getTitleTextColor();
            temp[2] = palette.getDominantSwatch().getBodyTextColor();
        } else if (palette.getMutedSwatch() != null) {
            temp[0] = palette.getMutedSwatch().getRgb();
            temp[1] = palette.getMutedSwatch().getTitleTextColor();
            temp[2] = palette.getMutedSwatch().getBodyTextColor();
        } else {
            String atkey = Helper.getATEKey(getContext());
            int accent = Config.accentColor(getContext(), atkey);
            temp[0] = accent;
            temp[1] = 0xffe5e5e5;
            temp[2] = accent;
        }
        return temp;
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView ArtistName, ArtistListAlbumCount, AlbumCount, ArtistListName;
        private ImageView ArtistsArtwork;
        private LinearLayout backgroundColor;
        private CircleImageView ArtistListArtwork;
        private ImageButton menu;

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
}
