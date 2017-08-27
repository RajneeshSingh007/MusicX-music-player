package com.rks.musicx.misc.utils;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.support.v7.view.ActionMode;
import android.support.v7.view.StandaloneActionMode;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.MusicXApplication;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.DefaultSongLoader;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.model.Folder;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.database.SaveQueueDatabase;
import com.rks.musicx.interfaces.Action;
import com.rks.musicx.interfaces.ExtraCallback;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.FolderAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.rks.musicx.ui.fragments.AlbumFragment;
import com.rks.musicx.ui.fragments.ArtistFragment;
import com.rks.musicx.ui.fragments.FavFragment;
import com.rks.musicx.ui.fragments.TagEditorFragment;

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.rks.musicx.misc.utils.Constants.BlackTheme;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.Four;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.One;
import static com.rks.musicx.misc.utils.Constants.Three;
import static com.rks.musicx.misc.utils.Constants.Two;
import static com.rks.musicx.misc.utils.Constants.Zero;
import static com.rks.musicx.misc.utils.Constants.fileExtensions;

/*
 * Created by Coolalien on 24/03/2017.
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

public class Helper {

    private static ValueAnimator colorAnimation;
    private static HashMap<String, Typeface> fontCache = new HashMap<>();
    private Context context;

    public Helper(Context context) {
        this.context = context;
    }

    public static void setRingTone(Context context, String path) {
        if (permissionManager.isWriteSettingsGranted(context)) {
            setRingtone(context, path);
            Toast.makeText(context, "Ringtone set", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Helper", "Write Permission Not Granted on mashmallow+");
            Toast.makeText(context, "Settings write permission denied", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set Ringtone
     *
     * @param context
     * @param path
     */
    private static void setRingtone(Context context, String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaColumns.DATA, file.getAbsolutePath());
        String filterName = path.substring(path.lastIndexOf("/") + 1);
        contentValues.put(MediaColumns.TITLE, filterName);
        contentValues.put(MediaColumns.MIME_TYPE, "audio/mp3");
        contentValues.put(MediaColumns.SIZE, file.length());
        contentValues.put(Media.IS_RINGTONE, true);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        Cursor cursor = context.getContentResolver().query(uri, null, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            String id = cursor.getString(0);
            contentValues.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            context.getContentResolver().update(uri, contentValues, MediaStore.MediaColumns.DATA + "=?", new String[]{path});
            Uri newuri = ContentUris.withAppendedId(uri, Long.valueOf(id));
            try {
                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newuri);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            cursor.close();
        }
    }

    /**
     * Share Music
     *
     * @param id
     * @param context
     */
    public static void shareMusic(long id, Context context) {
        if (permissionManager.isExternalReadStorageGranted(context)) {
            if (id == 0) {
                return;
            }
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri trackUri = Uri.parse(uri.toString() + "/" + id);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, trackUri);
            intent.setType("audio/*");
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
        } else {
            Log.d("Helper", "Permission failed");
        }
    }

    /**
     * Song Details
     *
     * @param context
     * @param title
     * @param album
     * @param artist
     * @param trackno
     * @param data
     */
    public static void detailMusic(Context context, String title, String album, String artist,
                                   int trackno, String data) {
        if (data != null) {
            File file = new File(data);
            if (file.exists()) {
                float cal = (file.length() / 1024);
                String content = context.getText(R.string.song_Name) +
                        title +
                        "\n\n" +
                        context.getText(R.string.album_name) +
                        album +
                        "\n\n" +
                        context.getString(R.string.artist_name) +
                        artist +
                        "\n\n" +
                        context.getText(R.string.trackno) +
                        trackno +
                        "\n\n" +
                        context.getText(R.string.file_path) +
                        data +
                        "\n\n" +
                        context.getText(R.string.file_size) +
                        String.valueOf(String.format("%.2f", cal / 1024)) +
                        " MB";
                new MaterialDialog.Builder(context)
                        .title(R.string.action_details)
                        .content(content)
                        .positiveText(R.string.okay)
                        .typeface(getFont(context), getFont(context))
                        .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
                        .show();
            } else {
                Toast.makeText(context, "File path not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Helper", "path not found");
        }

    }


    /**
     * Return alpha color
     *
     * @param color
     * @param ratio
     * @return
     */
    public static int getColorWithAplha(int color, float ratio) {
        int transColor;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        transColor = Color.argb(alpha, r, g, b);
        return transColor;
    }

    /**
     * Edit Song Tags
     *
     * @param context
     * @param song
     * @return
     */
    public static boolean editSongTags(Context context, Song song) {
        File f = new File(song.getmSongPath());
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
                String year = song.getYear();
                String title = song.getTitle();
                String album = song.getAlbum();
                String artist = song.getArtist();
                String lyrics = song.getLyrics();
                tag.deleteField(FieldKey.LYRICS);
                tag.setField(FieldKey.LYRICS, Html.fromHtml(lyrics).toString());
                ContentValues values = new ContentValues();
                if (title != null){
                    tag.setField(FieldKey.TITLE, title);
                    values.put(MediaStore.Audio.Media.TITLE, title);
                }
                if (artist != null){
                    tag.setField(FieldKey.ARTIST, artist);
                    values.put(MediaStore.Audio.Media.ARTIST, artist);
                }
                if (album != null){
                    tag.setField(FieldKey.ALBUM, album);
                    Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{BaseColumns._ID,
                                    MediaStore.Audio.AlbumColumns.ALBUM, MediaStore.Audio.AlbumColumns.ALBUM_KEY,
                                    MediaStore.Audio.AlbumColumns.ARTIST}, MediaStore.Audio.AlbumColumns.ALBUM + " = ?",
                            new String[]{album}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

                    if (cursor != null && cursor.moveToFirst()) {
                        long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                        values.put(MediaStore.Audio.Media.ALBUM_ID, id);
                        cursor.close();
                    } else {
                        values.put(MediaStore.Audio.Media.ALBUM, album);
                    }
                }
                if (song.getTrackNumber() != -1){
                    tag.setField(FieldKey.TRACK, String.valueOf(song.getTrackNumber()));
                    values.put(MediaStore.Audio.Media.TRACK, song.getTrackNumber());
                }
                if (year != null && year.length() > 0){
                    tag.setField(FieldKey.YEAR,  "" + year);
                    values.put(MediaStore.Audio.Media.YEAR, year);
                }
                if (values.size() > 0) {
                    context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, android.provider.MediaStore.Audio.Media._ID + "=?", new String[]{String.valueOf(song.getId())});
                }else {
                    return false;
                }
                audioFile.setTag(tag);
                AudioFileIO.write(audioFile);
            } catch (CannotReadException | CannotWriteException | InvalidAudioFrameException | TagException | IOException | ReadOnlyFileException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Animate View
     *
     * @param view
     * @return
     */
    public static Animator[] getAnimator(View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
        };
    }

    /**
     * Fragment Transition
     *
     * @param activity
     * @param firstFragment
     * @param secondFragment
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setFragmentTransition(MainActivity activity, Fragment firstFragment, Fragment secondFragment, View view, String name, String tag) {
        if (activity == null) {
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate(tag, 0);

        if (fragmentPopped) {
            // fragment is pop from backStack
        } else {
            Transition transitionFade = TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform);
            Transition transitionImage = TransitionInflater.from(activity).inflateTransition(R.transition.change_image_transform);

            firstFragment.setSharedElementReturnTransition(transitionImage);
            firstFragment.setEnterTransition(transitionFade);
            firstFragment.setExitTransition(transitionFade);

            secondFragment.setSharedElementEnterTransition(transitionImage);
            secondFragment.setEnterTransition(transitionFade);
            secondFragment.setExitTransition(transitionFade);

            fragmentManager.beginTransaction()
                    .replace(R.id.container, secondFragment)
                    .addSharedElement(view, name)
                    .addToBackStack(tag)
                    .commit();
        }
    }

    /**
     * Filter String
     *
     * @param str
     * @return
     */
    public static String stringFilter(String str) {
        if (str == null) {
            return null;
        }
        Pattern lineMatcher = Pattern.compile("\\n[\\\\/:*?\\\"<>|]((\\[\\d\\d:\\d\\d\\.\\d\\d\\])+)(.+)");
        Matcher m = lineMatcher.matcher(str);
        return m.replaceAll("").trim();
    }


    /**
     * Create App directory
     *
     * @param direName
     * @return
     */
    public static String createAppDir(String direName) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + "MusicX", direName);
        if (!file.exists()) {
            file.mkdirs();
        }
        return null;
    }

    /**
     * Rotate ImageView
     *
     * @param view
     */
    public static void rotationAnim(@NonNull View view) {
        RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation1.setInterpolator(new LinearInterpolator());
        rotateAnimation1.setDuration(300);
        rotateAnimation1.setRepeatCount(0);
        view.startAnimation(rotateAnimation1);
    }

    /**
     * Return Array with palette color
     *
     * @param context
     * @param palette
     * @return
     */
    public static int[] getAvailableColor(Context context, Palette palette) {
        int[] temp = new int[3]; //array with size 3
        if (palette.getVibrantSwatch() != null) {
            temp[0] = palette.getVibrantSwatch().getRgb();
            temp[1] = palette.getVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getVibrantSwatch().getBodyTextColor();
        } else if (palette.getMutedSwatch() != null) {
            temp[0] = palette.getMutedSwatch().getRgb();
            temp[1] = palette.getMutedSwatch().getTitleTextColor();
            temp[2] = palette.getMutedSwatch().getBodyTextColor();
        } else if (palette.getDarkVibrantSwatch() != null) {
            temp[0] = palette.getDarkVibrantSwatch().getRgb();
            temp[1] = palette.getDarkVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getDarkVibrantSwatch().getBodyTextColor();
        } else if (palette.getDarkMutedSwatch() != null) {
            temp[0] = palette.getDarkMutedSwatch().getRgb();
            temp[1] = palette.getDarkMutedSwatch().getTitleTextColor();
            temp[2] = palette.getDarkMutedSwatch().getBodyTextColor();
        } else if (palette.getDominantSwatch() != null) {
            temp[0] = palette.getDominantSwatch().getRgb();
            temp[1] = palette.getDominantSwatch().getTitleTextColor();
            temp[2] = palette.getDominantSwatch().getBodyTextColor();
        } else {
            String atkey = Helper.getATEKey(context);
            int accent = Config.accentColor(context, atkey);
            temp[0] = accent;
            temp[1] = 0xffe5e5e5;
            temp[2] = accent;
        }
        return temp;
    }

    /**
     * Theme Config
     * @param context
     * @return
     */
    public static String getATEKey(Context context) {
        if (MusicXApplication.getmPreferences() == null) {
            return null;
        }
        Boolean theme = MusicXApplication.getmPreferences().getBoolean(DarkTheme, false);
        Boolean blacktheme = MusicXApplication.getmPreferences().getBoolean(BlackTheme, false);
        if (theme) {
            SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
            editor.putBoolean(BlackTheme, false);
            editor.apply();
            return DarkTheme;
        } else if (blacktheme) {
            SharedPreferences.Editor editor = MusicXApplication.getmPreferences().edit();
            editor.putBoolean(DarkTheme, false);
            editor.apply();
            return BlackTheme;
        } else {
            return LightTheme;
        }
    }

    /**
     * Filter Artist ArrayList
     *
     * @param artistlist
     * @param query
     * @return
     */
    public static List<Artist> filterArtist(List<Artist> artistlist, String query) {
        query = query.toLowerCase().trim();
        final List<Artist> filterartistlist = new ArrayList<>();
        for (Artist artist : artistlist) {
            final String text = artist.getName().toLowerCase().trim();
            if (text.contains(query)) {
                filterartistlist.add(artist);
            }
        }
        return filterartistlist;
    }

    /**
     * Filter Album
     *
     * @param albumList
     * @param query
     * @return
     */
    public static List<Album> filterAlbum(List<Album> albumList, String query) {
        query = query.toLowerCase();
        final List<Album> filteralbumlist = new ArrayList<>();
        for (Album album : albumList) {
            final String text = album.getAlbumName().toLowerCase().trim();
            if (text.contains(query)) {
                filteralbumlist.add(album);
            }
        }
        return filteralbumlist;
    }

    /**
     * GuideLines Dialog
     *
     * @param context
     */
    public static void GuidLines(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("GuideLines");
        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/Guidlines.html");
        builder.positiveText(android.R.string.ok);
        builder.typeface(getFont(context),getFont(context));
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        builder.customView(webView, false);
        builder.build();
        builder.show();
    }

    /**
     * GuideLines Dialog
     *
     * @param context
     */
    public static void LyricsApi(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Lyrics Api");
        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/Lyrics_api.html");
        builder.positiveText(android.R.string.ok);
        builder.typeface(getFont(context), getFont(context));
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        builder.customView(webView, false);
        builder.build();
        builder.show();
    }
    /**
     * ChangeLogs Dialog
     *
     * @param context
     */
    public static void Changelogs(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Changelogs");
        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/app_changelogs.html");
        builder.positiveText(android.R.string.ok);
        builder.typeface(getFont(context), getFont(context));
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        builder.customView(webView, false);
        builder.build();
        builder.show();
    }

    /**
     * Licenses Dialog
     *
     * @param context
     */
    public static void Licenses(Context context) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.title("Licenses");
        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/licenses.html");
        builder.negativeText(android.R.string.cancel);
        builder.positiveText(android.R.string.ok);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                builder.autoDismiss(true);
            }
        });
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                builder.cancelable(true);
            }
        });
        builder.customView(webView, false);
        builder.build();
        builder.show();
    }

    public static String shortTime(Context context, long secs) {
        long hours, mins;
        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;
        final String durationFormat = context
                .getString(hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    /**
     * Show Rate Dailog
     *
     * @param mContext
     */
    public static void showRateDialog(final Context mContext) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
        String appName = mContext.getString(R.string.app_name);
        builder.title("Rate " + appName);
        builder.content("If you enjoy using " + appName
                + ", please take a moment to rate it. Thanks for your support!");
        builder.negativeText(mContext.getString(android.R.string.cancel));
        builder.positiveText(mContext.getString(android.R.string.ok));
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.rks.musicx")));
            }
        });
        builder.typeface(getFont(mContext),getFont(mContext));
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                builder.autoDismiss(true);
            }
        });
        builder.dismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Return Storage Path
     *
     * @return
     */
    public static String getStoragePath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String musicfolderpath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d("Helper", musicfolderpath);
            return musicfolderpath;
        } else {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        }
    }

    /**
     * Return text As bitmap
     *
     * @param text
     * @param textSize
     * @param textColor
     * @return
     */
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize); //text size
        paint.setColor(textColor); //text color
        paint.setTextAlign(Paint.Align.LEFT); //align center
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint); //draw text
        return image;
    }

    /**
     * Duration Calculator
     * @param id
     * @return
     */
    public static String durationCalculator(long id) {
        String finalTimerString = "";
        String secondsString = "";
        String mp3Minutes = "";
        // Convert total duration into time

        int minutes = (int) (id % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((id % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        if (minutes < 10) {
            mp3Minutes = "0" + minutes;
        } else {
            mp3Minutes = "" + minutes;
        }
        finalTimerString = finalTimerString + mp3Minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }

    /**
     * Multi file delete
     * @param songList
     * @param context
     */
    public static void multiDeleteTrack(Action action, List<Song> songList, Context context) {
        if (songList.size() == 0) {
            return;
        }
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
        dialog.title("Delete Tracks");
        dialog.content(context.getString(R.string.delete_music));
        dialog.positiveText(android.R.string.ok);
        dialog.typeface(getFont(context), getFont(context));
        dialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                boolean confirm = false;
                for (Song song : songList) {
                    String path = song.getmSongPath();
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            if (file.delete()) {
                                confirm = true;
                                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"audio/*"}, new MediaScannerConnection.MediaScannerConnectionClient() {
                                    @Override
                                    public void onMediaScannerConnected() {

                                    }

                                    @Override
                                    public void onScanCompleted(String s, Uri uri) {
                                        action.refresh();
                                    }
                                });
                            } else {
                                confirm = false;
                            }

                        }
                    }
                }
                if (confirm) {
                    Log.e("Helper", "files are Deleted");
                    action.refresh();
                    Toast.makeText(context, "All Songs deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Helper", "files are not Deleted");
                    action.refresh();
                    Toast.makeText(context, "Failed to delete song", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.negativeText(R.string.cancel);
        dialog.show();
    }

    /**
     * Folder Menu
     *
     * @param context
     * @param view
     */
    public static void showFolderMenu(Context context, View view, File file, RefreshData refreshData) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.folder_menu, popupMenu.getMenu());
        if (file == null) {
            return;
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.exclude_folder:
                        excludeFolder(context, file, refreshData);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * Exclude folder
     *
     * @param context
     * @param file
     */
    private static void excludeFolder(Context context, File file, RefreshData refreshData) {
        if (permissionManager.writeExternalStorageGranted(context)) {
            if (file == null) {
                return;
            }
            String path = file.getAbsolutePath();
            int lastIndexOf = path.lastIndexOf("/");
            path = path.substring(0, lastIndexOf);
            Log.e("Helper", path);
            String exlude = path + "/" + "." + file.getName();
            File dist = new File(exlude);
            Log.e("Helper", file.getName());
            Log.e("Helper", dist.getName());
            Log.e("Helper", file.getAbsolutePath());
            Log.e("Helper", dist.getAbsolutePath());
            boolean exclude = file.renameTo(dist);
            if (exclude) {
                Toast.makeText(context, "Folder excluded", Toast.LENGTH_SHORT).show();
                refreshData.refresh();
            } else {
                Toast.makeText(context, "Folder exclude failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static int parseToInt(String maybeInt, int defaultValue) {
        if (maybeInt == null) return defaultValue;
        maybeInt = maybeInt.trim();
        if (maybeInt.isEmpty()) return defaultValue;
        return Integer.parseInt(maybeInt);
    }

    /**
     * Return Index of songList in the folderSection
     *
     * @param path
     * @param songList
     * @return
     */
    public static int getIndex(@NonNull String path, @NonNull List<Song> songList) {
        int index = -1;
        for (int i = 0; i < songList.size(); i++) {
            Song song = songList.get(i);
            if (song.getmSongPath().contains(path)) {
                index = i;
                Log.e("Helper", String.valueOf(index));
                break;
            }
        }
        return index;
    }

    /**
     * AlbumArtwork Location
     *
     * @return
     */
    public static String getAlbumArtworkLocation() {
        return Environment.getExternalStorageDirectory() + "/MusicX/" + ".AlbumArtwork/";
    }

    /**
     * ArtistArtwork Location
     *
     * @return
     */
    public static String getArtistArtworkLocation() {
        return Environment.getExternalStorageDirectory() + "/MusicX/" + ".ArtistArtwork/";
    }

    /**
     * Return Lyrics Directory
     *
     * @return
     */
    public static String getDirLocation() {
        return Environment.getExternalStorageDirectory() + "/MusicX/" + "Lyrics/";
    }

    /**
     * Set fileName
     *
     * @param title
     * @return
     */
    static String setFileName(String title) {
        if (TextUtils.isEmpty(title)) {
            title = "unknown";
        }
        return title;
    }

    /**
     * Animate view's background color
     *
     * @param view
     * @param colorBg
     */
    public static void animateViews(Context context, View view, int colorBg) {
        colorAnimation = setAnimator(0xffe5e5e5, colorBg);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private static ValueAnimator setAnimator(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        long duration = 300;
        colorAnimation.setDuration(duration);
        return colorAnimation;
    }

    /**
     * Font
     * @param context
     * @param path
     */
    public static void getCalligraphy(Context context, String path) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(path)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                .setFontAttrId(R.attr.fontPath)
                .build());
        Extras.getInstance().saveTypeface(path);
    }

    /**
     * Start Activity
     * @param context
     * @param sClass
     * @param <S>
     */
    public static <S> void startActivity(Activity context, Class<S> sClass){
        if (context == null){
            return;
        }
        Intent intent = new Intent(context, sClass);
        context.startActivity(intent);
    }

    /**
     * Delete Directory
     * @param fileOrDirectory
     */
    public static void deleteRecursive(Context context, File fileOrDirectory) {
        if (permissionManager.writeExternalStorageGranted(context)) {
            if (fileOrDirectory != null) {
                if (fileOrDirectory.isDirectory()) {
                    boolean issuccess = fileOrDirectory.delete();
                    if (issuccess) {
                        Log.d("Helper", "delete Success");
                    } else {
                        Log.d("Helper", "delete failed");
                    }
                }
            }
       }
    }

    /**
     * return Typeface
     * @param context
     * @return
     */
    public static Typeface getFont(Context context) {
        String path = Extras.getInstance().getTypeface();
        return getTypeface(context, path);
    }

    /**
     * Typeface
     * @param customFont
     * @return
     */
    public static Typeface getTypeface(Context context, String customFont) {
        Typeface tf = fontCache.get(customFont);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), customFont);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(customFont, tf);
        }
        return tf;
    }

    /**
     * Rotate view 360
     * @param view
     */
    public static void rotateFab(@NonNull View view){
        ViewCompat.animate(view).
                rotation(360f).
                withLayer().
                setDuration(300).
                setInterpolator(new FastOutSlowInInterpolator()).
                start();
    }

    /**
     * check activity/class/package present or not in the device
     * @param context
     * @param intent
     * @return
     */
    public static boolean isActivityPresent(Context context, Intent intent){
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Set color to nav and status bar
     * @param activity
     * @param v
     */
    public static void setColor(@NonNull Activity activity, int color,View v){
        if (activity.getWindow() == null){
            return;
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            activity.getWindow().setStatusBarColor(color);
            v.setBackgroundColor(color);
            activity.getWindow().setNavigationBarColor(color);
        } else {
            activity.getWindow().setStatusBarColor(color);
            v.setBackgroundColor(color);
            activity.getWindow().setNavigationBarColor(color);
        }
    }

    /***
     * Filter AudioFile Length
     * @return
     */
    public static String filterAudio(){
        String filterAudio = "15000";
        switch (Extras.getInstance().getAudioFilter()){
            case Zero:
                filterAudio = "30000"; //30sec
                break;
            case One:
                filterAudio = "60000"; //1min
                break;
            case Two:
                filterAudio = "120000"; //2min
                break;
            case Three:
                filterAudio = "180000"; //3min
                break;
            case Four:
                filterAudio = "240000"; //4min
                break;
            case "5":
                filterAudio = "300000"; //5min
                break;
        }
        return filterAudio;
    }

    /**
     * Set background color os actionmode using reflection
     * @param actionMode
     * @param color
     */
    public static void setActionModeBackgroundColor(ActionMode actionMode, int color) {
        try {
            StandaloneActionMode standaloneActionMode = (StandaloneActionMode) actionMode;
            Field mContextView = StandaloneActionMode.class.getDeclaredField("mContextView");
            mContextView.setAccessible(true);
            Object value = mContextView.get(standaloneActionMode);
            ((View) value).setBackground(new ColorDrawable(color));
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
    }
    
    /**
     * return song metadata from path
     * @param context
     * @param path
     * @return
     */
    public static List<Song> getSongMetaData(Context context, String path) {
        if (path == null){
            return null;
        }
        List<Song> songList = new ArrayList<>();
        DefaultSongLoader defaultSongLoader = new DefaultSongLoader(context);
        defaultSongLoader.setProvider(true);
        defaultSongLoader.setUri(Uri.parse(String.valueOf(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)));
        defaultSongLoader.setQueryTable(null);
        for (String ext : fileExtensions) {
            if (path.toLowerCase().endsWith(ext)) {
                defaultSongLoader.setSelection(MediaStore.Audio.Media.DATA + " like ? ");
                defaultSongLoader.setQueryTable2(new String[]{"%" + path + "%"});
                defaultSongLoader.setSortOrder(null);
                songList.add(defaultSongLoader.getSongData());
            }
        }
        return songList;
    }

    public static Song getSongData(String sortOrder, @NonNull Context context, String path) {
        if (path == null) {
            return null;
        }
        Song song = new Song();
        if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int datacol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                /**
                 * @return songs metadata
                 */
                long id = cursor.getLong(idCol);
                String title = cursor.getString(titleCol);
                String artist = cursor.getString(artistCol);
                String album = cursor.getString(albumCol);
                long albumId = cursor.getLong(albumIdCol);
                int track = cursor.getInt(trackCol);
                String mSongPath = cursor.getString(datacol);

                song.setAlbum(album);
                song.setmSongPath(mSongPath);
                song.setArtist(artist);
                song.setId(id);
                song.setAlbumId(albumId);
                song.setTrackNumber(track);
                song.setTitle(title);
            }
            if (cursor != null) {
                cursor.close();
            }
        } else {
            Log.e("DefaultSongLoader", "No read permissions");
        }
        return song;
    }

    /**
     * ActionMode Config
     * @param mainActivity
     * @param context
     * @param action
     * @return
     */
    public static ActionMode.Callback getActionCallback(MainActivity mainActivity, Context context, Action action, boolean whichAdapter, ExtraCallback extraCallback) {
        if (mainActivity == null){
            return null;
        }
        android.support.v7.view.ActionMode.Callback mActionModeCallback = new android.support.v7.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.multi_select, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_add_to_playlist:
                        if (whichAdapter) {
                            PlaylistHelper.PlaylistMultiChooser(action.currentFrag(), context, getSelectedSong(extraCallback.songlistAdapter()));
                        } else {
                            PlaylistHelper.PlaylistMultiChooser(action.currentFrag(), context, getSelectedSong(extraCallback.folderAdapter()));
                        }
                        break;
                    case R.id.action_add_to_queue:
                        try {
                            if (whichAdapter) {
                                for (Song song : getSelectedSong(extraCallback.songlistAdapter())) {
                                    mainActivity.addToQueue(song);
                                }
                            } else {
                                for (Song song : getSelectedSong(extraCallback.folderAdapter())) {
                                    mainActivity.addToQueue(song);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_play:
                        if (whichAdapter) {
                            mainActivity.onShuffleRequested(getSelectedSong(extraCallback.songlistAdapter()), true);
                        } else {
                            mainActivity.onShuffleRequested(getSelectedSong(extraCallback.folderAdapter()), true);
                        }
                        break;
                    case R.id.action_delete:
                        if (whichAdapter) {
                            multiDeleteTrack(action, getSelectedSong(extraCallback.songlistAdapter()), context);
                        } else {
                            multiDeleteTrack(action, getSelectedSong(extraCallback.folderAdapter()), context);
                        }
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
                action.clear();
            }


        };
        return mActionModeCallback;
    }

    /**
     * MultiSelection SongList
     * @return
     */
    private static List<Song> getSelectedSong(SongListAdapter songListAdapter) {
        if (songListAdapter == null || songListAdapter.getSelectedItems().size() == 0){
            return null;
        }
        List<Integer> selectedPos = songListAdapter.getSelectedItems();
        List<Song> songList = new ArrayList<>();
        int pos;
        for (int i = selectedPos.size() -1; i>=0; i--){
            pos = selectedPos.get(i);
            Song song = songListAdapter.getItem(pos);
            songList.add(song);
        }
        return songList;
    }

    /**
     * MultiSelection SongList
     *
     * @return
     */
    private static List<Song> getSelectedSong(FolderAdapter folderAdapter) {
        if (folderAdapter == null || folderAdapter.getSelectedItems().size() == 0) {
            return null;
        }
        List<Integer> selectedPos = folderAdapter.getSelectedItems();
        List<Song> songList = new ArrayList<>();
        int pos;
        for (int i = selectedPos.size() - 1; i >= 0; i--) {
            pos = selectedPos.get(i);
            Song song = folderAdapter.getSongList().get(pos);//getItem(pos);
            songList.add(song);
        }
        return songList;
    }

    /**
     * Return Saved QuequeList
     * @param context
     * @return
     */
    public static List<String> getSavedQueueList(Context context){
        List<String> queueList = new ArrayList<>();
        SaveQueueDatabase queueDatabase = new SaveQueueDatabase(context, Constants.Queue_Store_TableName);
        queueList = queueDatabase.readAll();
        queueDatabase.close();
        if (queueList.size() > 0){
            return queueList;
        }else {
            return null;
        }
    }

    /**
     * Filter space EditText
     *
     * @return
     */
    @NonNull
    public static InputFilter inputFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
                String filter = "";
                for (int k = start; k < end; k++) {
                    char chz = charSequence.charAt(k);
                    if (!Character.isWhitespace(chz)) {
                        filter += chz;
                    }
                }
                return filter;
            }
        };
    }

    /**
     * Color Helper
     */

    @ColorInt
    public static int getTitleTextColor(@ColorInt int color) {
        double darkness = 1.0D - (0.299D * (double) Color.red(color) + 0.587D * (double) Color.green(color) + 0.114D * (double) Color.blue(color)) / 255.0D;
        return darkness < 0.35D ? getDarkerColor(color, 0.25F) : -1;
    }

    @ColorInt
    public static int getBodyTextColor(@ColorInt int color) {
        int title = getTitleTextColor(color);
        return setColorAlpha(title, 0.7F);
    }

    @ColorInt
    public static int getDarkerColor(@ColorInt int color, @FloatRange(from = 0.0D, to = 1.0D) float transparency) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= transparency;
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int setColorAlpha(@ColorInt int color, @FloatRange(from = 0.0D, to = 1.0D) float alpha) {
        int alpha2 = Math.round((float) Color.alpha(color) * alpha);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha2, red, green, blue);
    }

    public static ColorStateList getColorStateList(@ColorInt int color) {
        int[][] states = new int[][]{{16842919}, {16842908}, new int[0]};
        int[] colors = new int[]{getDarkerColor(color, 0.8F), getDarkerColor(color, 0.8F), color};
        return new ColorStateList(states, colors);
    }

    public static boolean isValidColor(String string) {
        try {
            Color.parseColor(string);
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    @NonNull
    public static Intent imagePicker(Context context) {
        if (permissionManager.isExternalReadStorageGranted(context)) {
            Intent chooser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            chooser.setType("image/*");
            Intent intent = Intent.createChooser(chooser, "Choose image");
            return intent;
        } else {
            Toast.makeText(context, "Permission grant failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * return real path of image
     *
     * @param uri
     * @param context
     * @return
     */
    public static String getRealPathFromURI(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

    @NonNull
    public static Animator getCircularShowAnimtion(@NonNull View view) {
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        int finalRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animation.setDuration(500);
        return animation;
    }

    @NonNull
    public static Animator getCircularHideAnimtion(@NonNull View view) {
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        int finalRadius = (int) Math.hypot(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        animation.setDuration(500);
        return animation;
    }

    /**
     * Delete Track
     *
     * @param name
     * @param path
     * @param context
     */
    public void DeleteTrack(RefreshData refreshData, String name, String path, Context context) {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
        dialog.title(name);
        dialog.content(R.string.delete_music);
        dialog.positiveText(android.R.string.ok);
        dialog.typeface(getFont(context),getFont(context));
        dialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.e("-->", "file Deleted :" + path);
                            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"audio/*"}, new MediaScannerConnection.MediaScannerConnectionClient() {
                                @Override
                                public void onMediaScannerConnected() {

                                }

                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    refreshData.refresh();
                                }
                            });
                            Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("-->", "file not Deleted :" + name);
                            refreshData.refresh();
                            Toast.makeText(context, "Failed to delete song", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        refreshData.refresh();
                    }
                } else {
                    Log.d("Helper", "Path not found");
                }

            }
        });
        dialog.negativeText(R.string.cancel);
        dialog.show();
    }

    /**
     * Song Menu Options
     *
     * @param torf
     * @param activity
     * @param v
     * @param context
     */
    public void showMenu(boolean torf, RefreshData refreshData, MainActivity activity, View v, Context context, Song song) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.song_list_item, popup.getMenu());
        // Song song = songListAdapter.getItem(position);
        FavHelper favHelper = new FavHelper(context);
        popup.getMenu().findItem(R.id.action_remove_playlist).setVisible(torf);
        if (activity == null){
            return;
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_remove_playlist:
                        PlaylistHelper.deletePlaylistTrack(context, Extras.getInstance().getPlaylistId(), song.getId());
                        Toast.makeText(context, "Removed from playlist", Toast.LENGTH_SHORT).show();
                        refreshData.refresh();
                        break;
                    case R.id.action_add_to_queue:
                        activity.addToQueue(song);
                        Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_set_as_next_track:
                        activity.setAsNextTrack(song);
                        Toast.makeText(context, "Added to next", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_add_to_playlist:
                        PlaylistHelper.PlaylistChooser(refreshData.currentFrag(), context, song.getId());
                        break;
                    case R.id.action_addFav:
                        new FavHelper(context).addFavorite(song.getId());
                        Toast.makeText(context, "Added to fav", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_edit_tags:
                        Extras.getInstance().saveMetaData(song);
                        activity.setFragment(TagEditorFragment.getInstance());
                        break;
                    case R.id.action_set_ringtone:
                        setRingTone(context, song.getmSongPath());
                        Toast.makeText(context, "Ringtone set", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_delete:
                        DeleteTrack(refreshData, song.getTitle(), song.getmSongPath(), context);
                        break;
                    case R.id.action_details:
                        detailMusic(context, song.getTitle(), song.getAlbum(), song.getArtist(),
                                song.getTrackNumber(), song.getmSongPath());
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(song.getId(), context);
                        break;
                    case R.id.action_fav:
                        activity.setFragment(FavFragment.newFavoritesFragment());
                        break;
                    case R.id.action_removeFav:
                        if (favHelper.isFavorite(song.getId())) {
                            favHelper.removeFromFavorites(song.getId());
                            Toast.makeText(context, "Removed from fav", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "First add to fav", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.go_to_album:
                        Album album = new Album();
                        album.setAlbumName(song.getAlbum());
                        album.setArtistName(song.getArtist());
                        album.setYear(0);
                        album.setTrackCount(song.getTrackNumber());
                        album.setId(song.getAlbumId());
                        activity.setFragment(AlbumFragment.newInstance(album));
                        break;
                    case R.id.go_to_artist:
                        Artist artist = new Artist(song.getArtistId(), song.getArtist(), 0, 0);
                        activity.setFragment(ArtistFragment.newInstance(artist));
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    // End of color Helper

    /**
     * ArtistImage  load
     *
     * @param name
     * @return
     */
    public String loadArtistImage(String name) {
        return getArtistArtworkLocation() + setFileName(name) + ".jpeg";
    }

    /**
     * AlbumImage  load
     *
     * @param name
     * @return
     */
    public String loadAlbumImage(String name) {
        return getAlbumArtworkLocation() + setFileName(name) + ".jpeg";
    }

    /**
     * Filter Song List
     *
     * @param songList
     * @param query
     * @return
     */
    public List<Song> filter(List<Song> songList, String query) {
        query = query.toLowerCase().trim();
        final List<Song> filtersonglist = new ArrayList<>();
        for (Song song : songList) {
            final String text = song.getTitle().toLowerCase().trim();
            if (text.contains(query)) {
                Log.e("Helper", "Query --> search song");
                filtersonglist.add(song);
            }
        }
        return filtersonglist;
    }

    /**
     * Filter Folder
     *
     * @param fileList
     * @param query
     * @return
     */
    public List<Folder> filterFolder(@NonNull Context context,  List<Folder> fileList, String query) {
        query = query.toLowerCase().trim();
        final List<Folder> filterFolder = new ArrayList<>();
        final List<Song> songList = new ArrayList<>();
        for (Folder folder : fileList) {
            Folder folders = new Folder();
            if (!folder.getFile().isDirectory()) {
                Song song = Helper.getSongData(Extras.getInstance().getSongSortOrder(), context, folder.getFile().getAbsolutePath());
                final String text = song.getTitle().toLowerCase().trim();
                if (text.contains(query)) {
                    songList.add(song);
                }
            }
            final String text1 = folder.getFile().getName().toLowerCase().trim();
            if (text1.contains(query)) {
                folders.setFile(folder.getFile());
                folders.setSongList(songList);
                filterFolder.add(folders);
            }
        }
        return filterFolder;
    }
}

