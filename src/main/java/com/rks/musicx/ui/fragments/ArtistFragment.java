package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.appthemeengine.Config;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.palette.BitmapPalette;
import com.palette.GlidePalette;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.data.network.LastFmClients;
import com.rks.musicx.data.network.LastFmServices;
import com.rks.musicx.data.network.model.Artist__;
import com.rks.musicx.data.network.model.Image_;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import java.io.File;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class ArtistFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>> {

    private final int trackLoader = 1;
    private FastScrollRecyclerView rv;
    private ImageView artworkView;
    private Artist artist;
    private SongListAdapter songListAdapter;
    private Toolbar toolbar;
    private Helper helper;
    private FloatingActionButton fab;
    private RequestManager mRequestManager;
    private FrameLayout bioView;
    private TextView artistBio;
    private boolean bio;

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = (position, view) -> {
        switch (view.getId()) {
            case R.id.item_view:
                ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                break;
            case R.id.menu_button:
                helper.showMenu(trackLoader, this, ArtistFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                break;
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shuffle_fab:
                    ((MainActivity) getActivity()).onShuffleRequested(songListAdapter.getSnapshot(), true);
                    break;
            }
        }
    };

    public static ArtistFragment newInstance(Artist artist) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ARTIST_ID, artist.getId());
        args.putString(Constants.ARTIST_NAME, artist.getName());
        args.putInt(Constants.ARTIST_ALBUM_COUNT, artist.getAlbumCount());
        args.putInt(Constants.ARTIST_TRACK_COUNT, artist.getTrackCount());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        setHasOptionsMenu(true);
        if (args != null) {
            long id = args.getLong(Constants.ARTIST_ARTIST_ID);
            String name = args.getString(Constants.ARTIST_NAME);
            int albumCount = args.getInt(Constants.ARTIST_ALBUM_COUNT);
            int trackCount = args.getInt(Constants.ARTIST_TRACK_COUNT);
            artist = new Artist(id, name, albumCount, trackCount);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_artist, container, false);
        ui(rootView);
        funtion();
        return rootView;
    }

    private void ui(View rootView) {
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.songrv);
        artworkView = (ImageView) rootView.findViewById(R.id.artist_artwork);
        fab = (FloatingActionButton) rootView.findViewById(R.id.shuffle_fab);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        artistBio = (TextView) rootView.findViewById(R.id.artist_bio);
        bioView = (FrameLayout) rootView.findViewById(R.id.artistview_bio);
    }

    private void funtion() {
        CustomLayoutManager c = new CustomLayoutManager(getContext());
        c.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(c);
        songListAdapter = new SongListAdapter(getContext());
        songListAdapter.setLayoutId(R.layout.song_list);
        songListAdapter.setOnItemClickListener(mOnClick);
        rv.setAdapter(songListAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
        fab.setOnClickListener(mOnClickListener);
        toolbar.setTitle(artist.getName());
        toolbar.setTitleTextColor(Color.WHITE);
        helper = new Helper(getContext());
        loadTrak();
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        } else {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        }
        mRequestManager = Glide.with(this);
        String artistImagePath = new Helper(getContext()).loadArtistImage(artist.getName());
        File file = new File(artistImagePath);
        if (file.exists()) {
            mRequestManager.load(file.getAbsolutePath())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(300, 300)
                    .listener(GlidePalette.with(file.getAbsolutePath()).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            final int[] colors = Helper.getAvailableColor(getContext(), palette);
                            toolbar.setBackgroundColor(colors[0]);
                            if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                                getActivity().getWindow().setStatusBarColor(colors[0]);
                            } else {
                                getActivity().getWindow().setStatusBarColor(colors[0]);
                            }
                        }
                    }))
                    .into(artworkView);
        } else {
            artworkView.setImageResource(R.mipmap.ic_launcher);
        }
        bioView.setVisibility(View.GONE);
      /**
       * Swipe Listerner
       */
      final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
          return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
          if (bio){
            bioView.setVisibility(View.VISIBLE);
          }else {
            bioView.setVisibility(View.GONE);
          }
          return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

          return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
          super.onLongPress(e);
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
          return false;
        }
      });
      artworkView.setOnTouchListener((v, event) -> {
        gesture.onTouchEvent(event);
        bio = true;
        return true;
      });
      bioView.setOnTouchListener((view, motionEvent) -> {
        gesture.onTouchEvent(motionEvent);
        bio = false;
        return true;
      });
      artistBio.setOnTouchListener((view, motionEvent) -> {
        gesture.onTouchEvent(motionEvent);
        bio = false;
        return true;
      });
      artistBio();
      /**
       * Show Case
       */
      ShowcaseConfig config = new ShowcaseConfig();
      MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "1400");
      sequence.setConfig(config);
      sequence.addSequenceItem(artworkView, "Tap to view avaiable artist Bio", "GOT IT");
      sequence.start();
      sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
        @Override
        public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
          materialShowcaseView.hide();
        }
      });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!Extras.getInstance().saveData()) {
            String artistImagePath = new Helper(getContext()).loadArtistImage(artist.getName());
            File file = new File(artistImagePath);
            if (!file.exists()) {
                ArtistCover();
            }
        }
    }

    /*
    *
    * ArtistCover
    */
    private void ArtistCover() {
        LastFmClients last = new LastFmClients(getContext());
        LastFmServices lastFmServices = last.createService(LastFmServices.class);
        Call<com.rks.musicx.data.network.model.Artist> artistCall = lastFmServices.getartist(artist.getName());
        artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
            @Override
            public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
                com.rks.musicx.data.network.model.Artist getartist = response.body();
                if (response.isSuccessful() && getartist != null) {
                    final Artist__ artist1 = getartist.getArtist();
                    if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
                        String artistImagePath = new Helper(getContext()).loadArtistImage(artist.getName());
                        File file = new File(artistImagePath);
                        for (Image_ artistArtwork : artist1.getImage()) {
                            if (Extras.getInstance().hqArtistArtwork()) {
                                if (file.exists()) {
                                    AndroidNetworking.delete(file.getAbsolutePath());
                                }
                                AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artist.getName() + ".jpeg")
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
                                AndroidNetworking.download(artworkQuality(artistArtwork), new Helper(getContext()).getArtistArtworkLocation(), artist.getName() + ".jpeg")
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
                Log.d("ArtistFrag", "error", t);
            }
        });
    }

    private void artistBio(){
      LastFmClients last = new LastFmClients(getContext());
      LastFmServices lastFmServices = last.createService(LastFmServices.class);
      Call<com.rks.musicx.data.network.model.Artist> artistCall = lastFmServices.getartist(artist.getName());
      artistCall.enqueue(new Callback<com.rks.musicx.data.network.model.Artist>() {
        @Override
        public void onResponse(Call<com.rks.musicx.data.network.model.Artist> call, Response<com.rks.musicx.data.network.model.Artist> response) {
          com.rks.musicx.data.network.model.Artist getartist = response.body();
          if (response.isSuccessful() && getartist != null) {
            final Artist__ artist1 = getartist.getArtist();
            if (artist1 != null && artist1.getImage() != null && artist1.getImage().size() > 0) {
              artistBio.setText(artist1.getBio().getSummary());
            } else {
              Log.d("haha", "bio load failed");
              artistBio.setText("No bio found");
            }
          } else {
            Log.d("haha", "bio load failed");
          }
        }

        @Override
        public void onFailure(Call<com.rks.musicx.data.network.model.Artist> call, Throwable t) {
          Log.d("ArtistFrag", "error", t);
        }
      });
    }

  /**
   * Artwork quality
   * @param artistArtwork
   * @return
   */
    public String artworkQuality(Image_ artistArtwork) {
        if (artistArtwork.getSize().equals("large")) {
            return artistArtwork.getText();
        } else if (artistArtwork.getSize().equals("mega")) {
            return artistArtwork.getText();
        } else {
            return artistArtwork.getText();
        }
    }

    /*
    reload track
   */
    public void reload() {
        getLoaderManager().restartLoader(trackLoader, null, this);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        TrackLoader tracksloader = new TrackLoader(getActivity());
        if (id == trackLoader) {
            tracksloader.filteralbumsong(MediaStore.Audio.Media.ARTIST_ID + "=?", new String[]{String.valueOf(artist.getId())});
            tracksloader.setSortOrder(MediaStore.Audio.Media.TRACK);
            return tracksloader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        songListAdapter.notifyDataSetChanged();
    }

    private void loadTrak() {
        getLoaderManager().initLoader(trackLoader, null, this);
    }


}
