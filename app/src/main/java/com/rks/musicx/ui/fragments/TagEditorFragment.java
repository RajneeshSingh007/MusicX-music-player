package com.rks.musicx.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
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
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.changeAlbumArt;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.LyricsHelper;
import com.rks.musicx.misc.widgets.updateAlbumArt;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static android.app.Activity.RESULT_OK;
import static com.rks.musicx.R.id.artist;

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

public class TagEditorFragment extends Fragment implements ImageChooserListener {

    private TextInputEditText mTitleEditText, mArtistEditText, mAlbumEditText, mTrackEditText, mYearEditText, mLyricsEditText;
    private Song song;
    private Toolbar toolbar;
    private FloatingActionButton saveTags;
    private ImageView albumArtwork;
    private String finalPath;
    private ChosenImage chosenImages;
    private ImageChooserManager imageChooserManager;
    private MediaScannerConnection mediaScannerConnection;
    private updateAlbumArt updatealbumArt;

    public static TagEditorFragment getInstance() {
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
        mLyricsEditText = (TextInputEditText) rootView.findViewById(R.id.lyrics);
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
        song.setLyrics(LyricsHelper.getInbuiltLyrics(path));
        mTitleEditText.setText(song.getTitle());
        mArtistEditText.setText(song.getArtist());
        mAlbumEditText.setText(song.getAlbum());
        mTrackEditText.setText(String.valueOf(song.getTrackNumber()));
        mYearEditText.setText(song.getYear());
        mLyricsEditText.setText(song.getLyrics());
        saveTags.setImageBitmap(Helper.textAsBitmap("Save", 40, Color.WHITE));
        ArtworkUtils.ArtworkLoader(getContext(), 300, 600, album, id, new palette() {
            @Override
            public void palettework(Palette palette) {
                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                if (getActivity() == null || getActivity().getWindow() == null) {
                    return;
                }
                Helper.setColor(getActivity(), colors[0], toolbar);
            }
        }, new bitmap() {
            @Override
            public void bitmapwork(Bitmap bitmap) {
                albumArtwork.setImageBitmap(bitmap);
            }

            @Override
            public void bitmapfailed(Bitmap bitmap) {
                albumArtwork.setImageBitmap(bitmap);
            }
        });
        int colorAccent = Config.accentColor(getContext(), Helper.getATEKey(getContext()));
        if (getActivity() == null || getActivity().getWindow() == null) {
            return;
        }
        Helper.setColor(getActivity(), colorAccent, toolbar);
        saveTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new saveData(getContext(), song).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        Helper.rotateFab(saveTags);
    }

    public boolean storeData(Context context) {
        Song newData = new Song();
        newData.setAlbum(mAlbumEditText.getText().toString());
        newData.setTitle(mTitleEditText.getText().toString());
        newData.setYear(mYearEditText.getText().toString());
        newData.setArtist(mArtistEditText.getText().toString());
        newData.setTrackNumber(Helper.parseToInt(mTrackEditText.getText().toString(), song.getTrackNumber()));
        newData.setLyrics(mLyricsEditText.getText().toString());
        newData.setmSongPath(song.getmSongPath());
        newData.setId(song.getId());
        newData.setAlbumId(song.getAlbumId());
        return Helper.editSongTags(context, newData);
    }

    private void pickNupdateArtwork() {
        imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            finalPath = imageChooserManager.choose();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + "");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (imageChooserManager == null) {
                imageChooserManager = new ImageChooserManager(this, requestCode, true);
                imageChooserManager.setImageChooserListener(this);
                imageChooserManager.reinitialize(finalPath);
            }
            imageChooserManager.submit(requestCode, data);
        }

    }

    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        chosenImages = chosenImage;
        finalPath = chosenImages.getFilePathOriginal();
        song.setAlbumArt(Uri.parse(finalPath));
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ChangeAlbumCover(finalPath, song.getmSongPath(), song.getAlbumId());
            }
        });
    }

    private void ChangeAlbumCover(String finalPath, String path, long key) {
        if (chosenImages != null) {
            updatealbumArt = new updateAlbumArt(finalPath, path, getContext(), key, new changeAlbumArt() {
                @Override
                public void onPostWork() {
                    ArtworkUtils.ArtworkLoader(getContext(), 300, 600, finalPath, song.getAlbumId(), new palette() {
                        @Override
                        public void palettework(Palette palette) {
                            final int[] colors = Helper.getAvailableColor(getContext(), palette);
                            if (getActivity() == null || getActivity().getWindow() == null) {
                                return;
                            }
                            Helper.setColor(getActivity(), colors[0], toolbar);
                            Helper.animateViews(getContext(), toolbar, colors[0]);
                        }
                    }, new bitmap() {
                        @Override
                        public void bitmapwork(Bitmap bitmap) {
                            albumArtwork.setImageBitmap(bitmap);
                        }

                        @Override
                        public void bitmapfailed(Bitmap bitmap) {
                            albumArtwork.setImageBitmap(bitmap);
                        }
                    });
                }
            });
            updatealbumArt.execute();
        }
    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onImagesChosen(ChosenImages chosenImages) {

    }

    @Override
    public void onStop() {
        if (updatealbumArt != null) {
            updatealbumArt.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (updatealbumArt != null){
            updatealbumArt.cancel(true);
        }
        super.onDestroy();
    }

    private class saveData extends AsyncTask<Object, Object, Boolean> {

        private Song song;
        private Context context;

        public saveData(Context context, Song song){
            this.context = context;
            this.song = song;
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            return storeData(context);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (b) {
                Toast.makeText(context, "Tag Edit Success", Toast.LENGTH_SHORT).show();
                mediaScannerConnection = new MediaScannerConnection(getContext(),
                        new MediaScannerConnection.MediaScannerConnectionClient() {

                            public void onScanCompleted(String path, Uri uri) {
                                mediaScannerConnection.disconnect();
                            }

                            public void onMediaScannerConnected() {
                                mediaScannerConnection.scanFile(song.getmSongPath(), "audio/*");
                            }
                        });
            } else {
                Toast.makeText(context, "Tag Edit Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
