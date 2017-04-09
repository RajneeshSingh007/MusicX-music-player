package com.rks.musicx.ui.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;

import java.io.File;
import java.util.HashMap;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.R.id.artist;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;
import static com.rks.musicx.misc.utils.Constants.SONG_YEAR;
import static com.rks.musicx.misc.utils.Helper.editSongTags;

/**
 * Created by Coolalien on 4/3/2017.
 */
public class TagEditorFragment extends miniFragment implements ImageChooserListener {

    private TextInputEditText mTitleEditText, mArtistEditText, mAlbumEditText, mTrackEditText,mYearEditText;
    private Song song;
    private Toolbar toolbar;
    private FloatingActionButton saveTags;
    private ImageView albumArtwork;
    private String finalPath,mediaPath;
    private ChosenImage chosenImages;
    private ImageChooserManager imageChooserManager;

    public static TagEditorFragment getInstance(){
        return new TagEditorFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tageditor, container, false);
        ui(rootView);
        function();
        return rootView;
    }

    private void ui(View rootView) {
        mTitleEditText = (TextInputEditText) rootView.findViewById(R.id.title);
        mArtistEditText = (TextInputEditText) rootView.findViewById(artist);
        mAlbumEditText = (TextInputEditText) rootView.findViewById(R.id.album);
        mTrackEditText = (TextInputEditText) rootView.findViewById(R.id.track_number);
        mYearEditText = (TextInputEditText) rootView.findViewById(R.id.year);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        saveTags = (FloatingActionButton) rootView.findViewById(R.id.save_tag);
        albumArtwork = (ImageView) rootView.findViewById(R.id.album_artwork);
    }


    private void function() {
        song = new Song();
        toolbar.setTitle("Tag Editor");
        toolbar.setTitleTextColor(Color.WHITE);
        String title = Extras.getInstance().getTitle();
        String album = Extras.getInstance().getAlbum();
        String artist = Extras.getInstance().getArtist();
        String year = Extras.getInstance().getYear();
        int no = Extras.getInstance().getNo();
        long id = Extras.getInstance().getAlbumID();
        String path = Extras.getInstance().getPath();
        long songid = Extras.getInstance().getId();
        song.setTitle(title);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setTrackNumber(no);
        song.setmSongPath(path);
        song.setId(songid);
        song.setAlbumId(id);
        song.setYear(year);
        mTitleEditText.setText(song.getTitle());
        mArtistEditText.setText(song.getArtist());
        mAlbumEditText.setText(song.getAlbum());
        mTrackEditText.setText(String.valueOf(song.getTrackNumber()));
        mYearEditText.setText(song.getYear());
        saveTags.setImageBitmap(Helper.textAsBitmap("Save", 40, Color.WHITE));
        ArtworkUtils.ArtworkLoaderPalette(getContext(), title, id, albumArtwork, new palette() {
            @Override
            public void palettework(Palette palette) {
                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                toolbar.setBackgroundColor(colors[0]);
                if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                } else {
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                }
            }
        });
        int colorAccent = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        } else {
            getActivity().getWindow().setStatusBarColor(colorAccent);
            toolbar.setBackgroundColor(colorAccent);
        }
        saveTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Object, Object, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Object... params) {
                        return storeData(getContext(), song);
                    }

                    @Override
                    protected void onPostExecute(Boolean b) {
                        super.onPostExecute(b);
                        if (b) {
                            load();
                            Toast.makeText(getContext(), "Tag Edit Success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Tag Edit Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });
        albumArtwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickNupdateArtwork();
            }
        });
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "1600");
        sequence.setConfig(config);
        sequence.addSequenceItem(albumArtwork, "Tap to change Artwork", "GOT IT");
        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                materialShowcaseView.hide();
            }
        });
    }

    /**
     * Save tags
     */
    public boolean storeData(Context context, Song song) {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(SONG_TITLE, mTitleEditText.getText().toString());
        tags.put(SONG_ARTIST, mArtistEditText.getText().toString());
        tags.put(SONG_ALBUM, mAlbumEditText.getText().toString());
        tags.put(SONG_TRACK_NUMBER, mTrackEditText.getText().toString());
        tags.put(SONG_YEAR, mYearEditText.getText().toString());
        return editSongTags(context, song, tags);
    }

    @Override
    public void load() {
        File file = new File(song.getmSongPath());
        getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    private void pickNupdateArtwork() {
        imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            mediaPath = imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + "");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (imageChooserManager == null) {
                imageChooserManager = new ImageChooserManager(this, requestCode, true);
                imageChooserManager.setImageChooserListener(this);
                imageChooserManager.reinitialize(mediaPath);
            }
            imageChooserManager.submit(requestCode, data);
        }
    }

    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        chosenImages = chosenImage;
        finalPath = chosenImages.getFilePathOriginal();
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ChangeAlbumCover(finalPath);
            }
        });
    }

    private void ChangeAlbumCover(String finalPath) {
        if (chosenImages != null) {
            new updateAlbumArt(finalPath).execute();
        }
    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onImagesChosen(ChosenImages chosenImages) {

    }

    public class updateAlbumArt extends AsyncTask<Void, Void, Void> {

        private Uri albumCover;
        private ContentValues values;
        private String path;

        public updateAlbumArt(String path) {
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {
            albumCover = Uri.parse("content://media/external/audio/albumart");
            try {
                getContext().getContentResolver().delete(ContentUris.withAppendedId(albumCover, song.getAlbumId()), null, null);
                values = new ContentValues();
                values.put("album_id", song.getAlbumId());
                values.put("_data", path);
            } catch (Exception e) {
                Log.d("playing", "error", e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Uri newUri = TagEditorFragment.this.getContext().getContentResolver().insert(albumCover, values);
            if (newUri != null) {
                File file = new File("content://media/external/audio/albumart");
                Toast.makeText(getContext(), "AlbumArt Changed", Toast.LENGTH_LONG).show();
                Log.d("updateAlbumCover", "success hurray !!!");
                getContext().getContentResolver().notifyChange(albumCover, null);
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), song.getTitle(), path, new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        albumArtwork.setImageBitmap( bitmap);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {

                    }
                });
            } else {
                Toast.makeText(getContext(), "AlbumArt Failed", Toast.LENGTH_LONG).show();
                Log.d("updateAlbumCover", "failed lol !!!");
            }
        }
    }
}
