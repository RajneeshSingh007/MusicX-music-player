package com.rks.musicx.misc.utils;

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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.data.network.NetworkHelper;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.rks.musicx.misc.utils.Helper.stringFilter;

/**
 * Created by Coolalien on 8/4/2017.
 */
public class LyricsHelper {


    /**
     * Insert Lyrics
     *
     * @param path
     * @param lyrics
     * @return
     */
    public static boolean insertLyrics(String path, String lyrics) {
        File f = new File(path);
        if (f.exists()) {
            try {
                AudioFile audioFile = AudioFileIO.read(f);
                if (audioFile == null) {
                    return false;
                }
                TagOptionSingleton.getInstance().setAndroid(true);
                Tag tag = audioFile.getTag();
                if (tag == null) {
                    return false;
                }
                tag.deleteField(FieldKey.LYRICS);
                tag.setField(FieldKey.LYRICS, lyrics);
                audioFile.setTag(tag);
                AudioFileIO.write(audioFile);
                return true;
            } catch (CannotReadException | CannotWriteException | InvalidAudioFrameException | TagException | IOException | ReadOnlyFileException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * Save Lyrics
     *
     * @param path
     * @param content
     */
    public static void writeLyrics(String path, String content) {
        try {
            if (!content.isEmpty() && !path.isEmpty() && content.length() > 0 && path.length() > 0) {
                FileWriter writer = new FileWriter(path);
                writer.flush();
                writer.write(stringFilter(content));
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get Inbuilt Lyrics
     *
     * @param path
     * @return
     */
    @NonNull
    public static String getInbuiltLyrics(String path) {
        String lyrics;
        if (path != null) {
            File file = new File(path);
            AudioFile audioFile = null;
            Tag tag = null;
            if (file.exists()) {
                try {
                    audioFile = AudioFileIO.read(file);
                    if (audioFile != null) {
                        tag = audioFile.getTag();
                        if (tag != null) {
                            lyrics = tag.getFirst(FieldKey.LYRICS);
                            //lyrics = lyrics.replaceAll("\n", "</br>");
                            return lyrics;
                        } else {
                            return "No Lyrics found";
                        }
                    }
                } catch (CannotReadException | ReadOnlyFileException | InvalidAudioFrameException | TagException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("LyricsHelper", "file not exists");
            }
        }
        return "No Lyrics found";
    }

    /**
     * Search Lyrics
     *
     * @param context
     * @param title
     * @param artist
     * @param path
     * @param setlyrics
     */
    public static void searchLyrics(Context context, String title, String artist, String album, String path, TextView setlyrics) {
        View v = LayoutInflater.from(context).inflate(R.layout.search_lyrics, new LinearLayout(context), false);
        MaterialDialog.Builder searchLyrics = new MaterialDialog.Builder(context);
        searchLyrics.title("Search Lyrics");
        searchLyrics.positiveText(android.R.string.ok);
        TextInputEditText songeditText = (TextInputEditText) v.findViewById(R.id.lyricssong_name);
        TextInputEditText artisteditText = (TextInputEditText) v.findViewById(R.id.lyricsartist_name);
        TextInputEditText albumeditText = (TextInputEditText) v.findViewById(R.id.lyricsalbum_name);
        songeditText.setText(title);
        artisteditText.setText(artist);
        albumeditText.setText(album);
        searchLyrics.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                LoadLyrics(context, songeditText.getText().toString(), artisteditText.getText().toString(), albumeditText.getText().toString(), path, setlyrics);
            }
        });
        searchLyrics.negativeText(android.R.string.cancel);
        searchLyrics.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                searchLyrics.autoDismiss(true);
            }
        });
        searchLyrics.customView(v, false);
        searchLyrics.show();
    }

    /**
     * Display Lyrics
     *
     * @param title
     * @param artist
     * @param path
     * @param lrcView
     */
    public static void LoadLyrics(Context context, String title, String artist, String album, String path, TextView lrcView) {
        if (title == null || artist == null || album == null || path == null) {
            return;
        }
        File file = new File(saveLyrics(title));
        if (file.exists()) {
            if (file.getName().equals(title)) {
                readLyricsFromFile(file, lrcView);
            }
        } else {
            try {
                if (!Extras.getInstance().saveData()) {
                    NetworkHelper.indicineLyrics(context, artist, title, album, path, lrcView);
                } else {
                    Log.d("LyricsHelper", "not allowed network");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lrcView.setText(getInbuiltLyrics(path));
            }
        }
    }

    /**
     * Location Of Lyrics
     *
     * @param name
     * @return
     */
    public static String saveLyrics(String name) {
        return Helper.getDirLocation() + Helper.setFileName(name);
    }

    /**
     * Read Lyrics from file
     *
     * @param file
     * @param textView
     */
    public static void readLyricsFromFile(File file, TextView textView) {
        if (file != null) {
            FileInputStream iStr;
            try {
                iStr = new FileInputStream(file);
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(iStr));
                String TextLine = "";
                String TextBuffer = "";
                try {
                    while ((TextLine = fileReader.readLine()) != null) {
                        TextBuffer += TextLine + "\n";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText(TextBuffer);
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Helper", "error");
        }
    }


}
