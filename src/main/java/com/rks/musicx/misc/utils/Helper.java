package com.rks.musicx.misc.utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.model.Playlist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.data.network.LyricsData;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.rks.musicx.ui.fragments.FavFragment;
import com.rks.musicx.ui.fragments.PlayListPicker;
import com.rks.musicx.ui.fragments.TagEditorFragment;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static com.rks.musicx.R.string.file_size;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;
import static com.rks.musicx.misc.utils.Constants.SONG_YEAR;

/*
 * Created by Coolalien on 24/03/2017.
 */

public class Helper {

  private Context context;
  private ValueAnimator colorAnimation;

  public Helper(Context context) {
    this.context = context;
  }

  /**
   * Set Ringtone
   */
  public static void setRingTone(Context context, String path) {
    if (!permissionManager.isWriteSettingsGranted(context)) {
      if (path != null){
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
          Uri  newuri = ContentUris.withAppendedId(uri, Long.valueOf(id));
          try {
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE,  newuri);
            Toast.makeText(context, "Ringtone set", Toast.LENGTH_LONG).show();
          }catch (Throwable t){
            t.printStackTrace();
          }
          cursor.close();
        }
      }else {
        Log.d("Helper", "invalid path");
      }
    } else {
      Log.d("Helper", "Write Permission Not Granted on mashmallow+");
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
        Uri  newuri = ContentUris.withAppendedId(uri, Long.valueOf(id));
        try {
          RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE,  newuri);
          Toast.makeText(context, "Ringtone set", Toast.LENGTH_LONG).show();
        }catch (Throwable t){
          t.printStackTrace();
        }
        cursor.close();
      }
    }
  }

  /**
   * Share music
   */
  public static void shareMusic(String path, Context context) {
    if (path != null) {
      File file = new File(path);
      if (file.exists()) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
      } else {
        Log.d("Helper", "path not found");
      }
    } else {
      Log.d("Helper", "path not found");
    }
  }

  /**
   * Details of music
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
            context.getText(file_size) +
            String.valueOf(String.format("%.2f", cal / 1024)) +
            " MB";
        new MaterialDialog.Builder(context)
            .title(R.string.action_details)
            .content(content)
            .positiveText(R.string.okay)
            .onPositive((materialDialog, dialogAction) -> materialDialog.dismiss())
            .show();
      } else {
        Toast.makeText(context, "File path not found", Toast.LENGTH_LONG).show();
      }
    } else {
      Log.d("Helper", "path not found");
    }

  }


  /**
   * Set color transparency
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
   * return path of data from uri
   */
  public static String getRealPathFromUri(Context context, Uri contentUri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.Audio.Media.DATA};
      cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
      cursor.moveToFirst();
      return cursor.getString(column_index);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  /**
   * Edit songs tags
   */
  public static boolean editSongTags(Context context, Song song, Map<String, String> tags) {

    String newTitle = tags.get(SONG_TITLE) == null ? song.getTitle() : tags.get(SONG_TITLE);
    String newArtist = tags.get(SONG_ARTIST) == null ? song.getArtist() : tags.get(SONG_ARTIST);
    String newAlbum = tags.get(SONG_ALBUM) == null ? song.getAlbum() : tags.get(SONG_ALBUM);
    String newTrackNumber = tags.get(SONG_TRACK_NUMBER) == null ? String.valueOf(song.getTrackNumber()) : tags.get(SONG_TRACK_NUMBER);
    String newyear = tags.get(SONG_YEAR) == null ? null : tags.get(SONG_YEAR);
    Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());

    File f = new File(getRealPathFromUri(context, songUri));

    AudioFile audioFile = new AudioFile();
    try {
      audioFile = AudioFileIO.read(f);
      audioFile.setTag(new ID3v24Tag());
    } catch (CannotReadException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (TagException e) {
      e.printStackTrace();
    } catch (ReadOnlyFileException e) {
      e.printStackTrace();
    } catch (InvalidAudioFrameException e) {
      e.printStackTrace();
    }

    Tag tag = null;

    if (audioFile != null) {
      tag = audioFile.getTag();
    } else {
      Log.d("tag", "audiofile null");

    }

    if (tag != null) {
      Log.d("tag", "not null");
      ContentValues values = new ContentValues();
      if (!song.getTitle().equals(newTitle)) {
        try {
          tag.setField(FieldKey.TITLE, newTitle);
          audioFile.commit();
        } catch (FieldDataInvalidException | CannotWriteException e) {
          e.printStackTrace();
        }

        values.put(MediaStore.Audio.Media.TITLE, newTitle);
        Log.d("tag", "title");

      }
      if (!song.getYear().equals(newyear)) {
        try {
          tag.setField(FieldKey.YEAR, newyear);
          audioFile.commit();
        } catch (FieldDataInvalidException | CannotWriteException e) {
          e.printStackTrace();
        }
        values.put(Media.YEAR, newyear);
        Log.d("tag", "title");
      }
      if (!song.getArtist().equals(newArtist)) {
        try {
          tag.setField(FieldKey.ARTIST, newArtist);
          audioFile.commit();
        } catch (FieldDataInvalidException e) {
          e.printStackTrace();
        } catch (CannotWriteException e) {
          e.printStackTrace();
        }

        values.put(MediaStore.Audio.Media.ARTIST, newArtist);
        Log.d("tag", "artist");

      }
      if (!song.getAlbum().equals(newAlbum)) {
        try {
          tag.setField(FieldKey.ALBUM, newAlbum);
          audioFile.commit();
        } catch (FieldDataInvalidException e) {
          e.printStackTrace();
        } catch (CannotWriteException e) {
          e.printStackTrace();
        }

        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{BaseColumns._ID,
                                MediaStore.Audio.AlbumColumns.ALBUM, MediaStore.Audio.AlbumColumns.ALBUM_KEY,
                                MediaStore.Audio.AlbumColumns.ARTIST}, MediaStore.Audio.AlbumColumns.ALBUM + " = ?",
                        new String[]{newAlbum}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

        if (cursor != null && cursor.moveToFirst()) {

          long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

          values.put(MediaStore.Audio.Media.ALBUM_ID, id);

          Log.d("er", String.valueOf(id));


        } else {

          values.put(MediaStore.Audio.Media.ALBUM, newAlbum);
        }

        if (cursor != null) {
          cursor.close();
        }
        Log.d("tag", "album");

      }
      if (!String.valueOf(song.getTrackNumber()).equals(newTrackNumber)) {
        try {
          tag.setField(FieldKey.TRACK, newTrackNumber);
          audioFile.commit();
        } catch (FieldDataInvalidException e) {
          e.printStackTrace();
        } catch (CannotWriteException e) {
          e.printStackTrace();
        }

        values.put(MediaStore.Audio.Media.TRACK, newTrackNumber);
      }
      if (values.size() > 0) {

        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values,
                MediaStore.Audio.Media._ID + "=" + song.getId(), null);
      }
      return true;
    }

    return false;
  }
  /**
   * Translation
   */
  public static Animator[] getAnimator(View view) {
    return new Animator[]{
        ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
    };
  }

  /**
   * Fragment transition
   */
  @SafeVarargs
  @SuppressLint("NewApi")
  public static void setFragmentTransition(FragmentActivity activity, Fragment firstFragment,
      Fragment secondFragment, @Nullable Pair<View, String>... transitionViews) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      // Inflate transitions to apply
      Transition changeTransform = TransitionInflater.from(activity)
          .inflateTransition(R.transition.change_image_transform);
      Transition explodeTransform = TransitionInflater.from(activity)
          .inflateTransition(R.transition.change_image_transform);
      // Setup exit transition on first fragment
      firstFragment.setSharedElementReturnTransition(changeTransform);
      firstFragment.setExitTransition(explodeTransform);
      // Setup enter transition on second fragment
      secondFragment.setSharedElementEnterTransition(changeTransform);
      secondFragment.setEnterTransition(explodeTransform);
    }
    FragmentTransaction ft = activity.getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, secondFragment)
        .addToBackStack("transaction");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transitionViews != null) {

      for (Pair<View, String> tr : transitionViews) {
        ft.addSharedElement(tr.first, tr.second);
      }
    }
    ft.commit();
  }

  /**
   * String Filters
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
   * Save Lyrics
   * @param path
   * @param content
   */
  public static void saveLyrics(String path, String content) {
    try {
      if (!content.isEmpty() && !path.isEmpty() && content.length() >0 && path.length() > 0){
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
   * Create directory
   */
  public static String createAppDir(String direName) {
    File file = new File(Environment.getExternalStorageDirectory() + "/" + "MusicX", direName);
    if (!file.exists()) {
      file.mkdirs();
    }
    return null;
  }

  /**
   * Rotation ImageView
   */
  public static void rotationAnim(ImageView imageView) {
    RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    rotateAnimation1.setInterpolator(new LinearInterpolator());
    rotateAnimation1.setDuration(300);
    rotateAnimation1.setRepeatCount(0);
    imageView.startAnimation(rotateAnimation1);
  }

  /**
   * Return palette color
   */
  public static int[] getAvailableColor(Context context, Palette palette) {
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
      String atkey = Helper.getATEKey(context);
      int accent = Config.accentColor(context, atkey);
      temp[0] = accent;
      temp[1] = 0xffe5e5e5;
      temp[2] = accent;
    }
    return temp;
  }

  /**
   * return theme pref
   */
  public static String getATEKey(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DarkTheme, false)
        ? DarkTheme : LightTheme;
  }

  /**
   * Filter artist
   */
  public static List<Artist> filterArtist(List<Artist> artistlist, String query) {
    query = query.toLowerCase();
    final List<Artist> filterartistlist = new ArrayList<>();
    for (Artist artist : artistlist) {
      final String text = artist.getName().toLowerCase();
      if (text.contains(query)) {
        filterartistlist.add(artist);
      }
    }
    return filterartistlist;
  }

  /**
   * filter album
   */
  public static List<Album> filterAlbum(List<Album> albumList, String query) {
    query = query.toLowerCase();
    final List<Album> filteralbumlist = new ArrayList<>();
    for (Album album : albumList) {
      final String text = album.getAlbumName().toLowerCase();
      if (text.contains(query)) {
        filteralbumlist.add(album);
      }
    }
    return filteralbumlist;
  }

  /**
   * load guidlines html
   */
  public static void GuidLines(Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title("GuideLines");
    WebView webView = new WebView(context);
    webView.loadUrl("file:///android_asset/Guidlines.html");
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

  /**
   * show changelogs
   */
  public static void Changelogs(Context context) {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
    builder.title("Changelogs");
    WebView webView = new WebView(context);
    webView.loadUrl("file:///android_asset/app_changelogs.html");
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

  /**
   * show licenses
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

  /**
   * time format
   */
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
   * Rate Dailog
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
   * Storage
   */
  public static File getStorage(String location) {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      String musicfolderpath = Environment.getExternalStoragePublicDirectory(location).getPath();
      Log.d("Helper", musicfolderpath);
      File path = new File(musicfolderpath);
      return path;
    } else {
      return null;
    }
  }

  /**
   * Delete track
   */
  @SuppressLint("StringFormatInvalid")
  private void DeleteTrack(int id, LoaderManager.LoaderCallbacks<List<Song>> songLoaders,
      Fragment fragment, String name, String path, Context context) {
    MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
    dialog.title(name);
    dialog.content(context.getString(R.string.delete_music, name));
    dialog.positiveText(android.R.string.ok);
    dialog.onPositive(new MaterialDialog.SingleButtonCallback() {
      @Override
      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (path != null) {
          File file = new File(path);
          if (file.exists()) {
            if (file.delete()) {
              Log.e("-->", "file Deleted :" + path);
              MediaScannerConnection.scanFile(context,
                  new String[]{file.toString()}, null,
                  new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                      Log.i("ExternalStorage", "Scanned " + path + ":");
                      Log.i("ExternalStorage", "-> uri=" + uri);
                      fragment.getLoaderManager().restartLoader(id, null, songLoaders);
                    }
                  });
              fragment.getLoaderManager().restartLoader(id, null, songLoaders);
              Toast.makeText(context, "Track deleted", Toast.LENGTH_SHORT).show();
            } else {
              Log.e("-->", "file not Deleted :" + name);
              MediaScannerConnection.scanFile(context,
                  new String[]{file.toString()}, null,
                  new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                      Log.i("ExternalStorage", "Scanned " + path + ":");
                      Log.i("ExternalStorage", "-> uri=" + uri);
                      fragment.getLoaderManager().restartLoader(id, null, songLoaders);
                    }
                  });
              fragment.getLoaderManager().restartLoader(id, null, songLoaders);
              Toast.makeText(context, "failed to delete song", Toast.LENGTH_SHORT).show();
            }

          } else {
            MediaScannerConnection.scanFile(context,
                new String[]{Environment.getExternalStorageDirectory().toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                  public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                    fragment.getLoaderManager().restartLoader(id, null, songLoaders);
                  }
                });
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
   * Show overflow menu
   */
  public void showMenu(int id, LoaderManager.LoaderCallbacks<List<Song>> songLoaders, Fragment fragment, MainActivity activity, int position, View v, Context context, SongListAdapter songListAdapter) {
    PopupMenu popup = new PopupMenu(context, v);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.song_list_item, popup.getMenu());
    Song song = songListAdapter.getItem(position);
    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.action_add_to_queue:
            activity.addToQueue(song);
            Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show();
            break;
          case R.id.action_set_as_next_track:
            activity.setAsNextTrack(song);
            Toast.makeText(context, "Added to next", Toast.LENGTH_SHORT).show();
            break;
          case R.id.action_add_to_playlist:
            PlaylistChooser(fragment, context, song.getId());
            break;
          case R.id.action_addFav:
            new FavHelper(context).addFavorite(song.getId());
            Toast.makeText(context, "Added to fav", Toast.LENGTH_LONG).show();
            break;
          case R.id.action_edit_tags:
            Extras.getInstance().saveMetaData(song);
            activity.setFragment(TagEditorFragment.getInstance());
            break;
          case R.id.action_set_ringtone:
            setRingTone(context, songListAdapter.getItem(position).getmSongPath());
            Toast.makeText(context, "Ringtone set", Toast.LENGTH_SHORT).show();
            break;
          case R.id.action_delete:
            DeleteTrack(id, songLoaders, fragment, song.getTitle(), song.getmSongPath(), context);
            break;
          case R.id.action_details:
            detailMusic(context, song.getTitle(), song.getAlbum(), song.getArtist(),
                song.getTrackNumber(), song.getmSongPath());
            break;
          case R.id.action_share:
            Helper.shareMusic(song.getmSongPath(), context);
            break;
          case R.id.action_fav:
            activity.setFragment(FavFragment.newFavoritesFragment());
            break;
          case R.id.action_removeFav:
            if (new FavHelper(context).isFavorite(song.getId())) {
              new FavHelper(context).removeFromFavorites(song.getId());
              Toast.makeText(context, "Removed from fav", Toast.LENGTH_LONG).show();
            } else {
              Toast.makeText(context, "First add to fav", Toast.LENGTH_LONG).show();
            }
            break;
        }
        return false;
      }
    });
    popup.show();
  }

  /**
   * Convert text to bitmap
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
   * Playlist chooser
   */
  public void PlaylistChooser(Fragment fragment, Context context, long song) {
    PlayListPicker playListPicker = new PlayListPicker();
    playListPicker.setPicked(new playlistPicked() {
      @Override
      public void onPlaylistPicked(Playlist playlist) {
        addSongToPlaylist(context.getContentResolver(), playlist.getId(), song);
        Toast.makeText(context, "Song is added ", Toast.LENGTH_LONG).show();
      }
    });
    playListPicker.show(fragment.getFragmentManager(), null);
  }

  /**
   * Delete playlist
   */
  public void deletePlaylist(ContentResolver resolver, long playlistId) {
    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
    resolver.delete(uri, null, null);
    String filter = MediaStore.Audio.Playlists._ID + "=" + playlistId;
    resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, filter, null);
  }

  /**
   * Add songs to playlist
   */
  public void addSongToPlaylist(ContentResolver resolver, long playlistId, long songId) {
    Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
    final int base = getSongCount(resolver, uri);
    insert(resolver, uri, songId, base + 1);
  }

  /**
   * getsong count
   */
  private int getSongCount(ContentResolver resolver, Uri uri) {
    String[] cols = new String[]{"count(*)"};
    Cursor cur = resolver.query(uri, cols, null, null, null);
    if (cur != null) {
      cur.moveToFirst();
    }
    final int count = cur.getInt(0);
    cur.close();
    return count;
  }

  /**
   * create playlist
   */
  public Uri createPlaylist(ContentResolver resolver, String playlistName) {
    Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    ContentValues values = new ContentValues();
    values.put(MediaStore.Audio.Playlists.NAME, playlistName);
    return resolver.insert(uri, values);
  }

  /**
   * insert into playlist
   */
  private void insert(ContentResolver resolver, Uri uri, long songId, int index) {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, index);
    values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId);
    resolver.insert(uri, values);

  }

  /**
   * Search lyrics
   */
  public void searchLyrics(Context context, String title, String artist,TextView setlyrics) {
    View v = LayoutInflater.from(context).inflate(R.layout.search_lyrics, null);
    MaterialDialog.Builder searchLyrics = new MaterialDialog.Builder(context);
    searchLyrics.title("Search Lyrics");
    searchLyrics.positiveText(android.R.string.ok);
    TextInputEditText songeditText = (TextInputEditText) v.findViewById(R.id.lyricssong_name);
    TextInputEditText artisteditText = (TextInputEditText) v.findViewById(R.id.lyricsartist_name);
    songeditText.setText(title);
    artisteditText.setText(artist);
    searchLyrics.onPositive(new MaterialDialog.SingleButtonCallback() {
      @Override
      public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        new LyricsData(context, songeditText.getText().toString(), artisteditText.getText().toString(), setlyrics).execute("Executed");
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
   * load lyrics
   */
  public void LoadLyrics(String title, String artist, TextView lrcView) {
    if (title != null && artist != null) {
      File file = new File(loadLyrics(title));
      if (file.exists()){
        if (file.getName().equals(title)){
          readLyricsFromFile(file,lrcView);
        }else {
          Log.d("Helper", "not same file ");
        }
      }else {
        try {
          new LyricsData(context, title, artist, lrcView).execute("Executed");
        }catch (Exception e){
          e.printStackTrace();
        }finally {
          lrcView.setText("Lyrics not found");
        }
      }

    } else {
      Log.d("Helper", "Title, Artist is null");
    }
  }

  /**
   * load lyrics
   */
  public String loadLyrics(String name) {
    return getDirLocation() + setFileName(name);
  }

  /**
   * read lyrics from file
   */
  public void readLyricsFromFile(File file, TextView textView) {
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

  /**
   * load artistImage
   */
  public String loadArtistImage(String name) {
    return getArtistArtworkLocation() + setFileName(name) + ".jpeg";
  }

  /**
   * ArtistImage Location
   */
  public String getArtistArtworkLocation() {
    return Environment.getExternalStorageDirectory() + "/MusicX/" + ".ArtistArtwork/";
  }

  /**
   * Lyrics location
   */
  public String getDirLocation() {
    return Environment.getExternalStorageDirectory() + "/MusicX/" + "Lyrics/";
  }

  /**
   * Set fileName
   */
  public String setFileName(String title) {
    if (TextUtils.isEmpty(title)) {
      title = context.getString(R.string.unknown);
    }
    return title;
  }

  /**
   * Animate view
   */
  public void animateViews(View view, int colorBg) {
    colorAnimation = setAnimator(0xffe5e5e5, colorBg);
    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

      @Override
      public void onAnimationUpdate(ValueAnimator animator) {
        view.setBackgroundColor((Integer) animator.getAnimatedValue());
      }

    });
  }

  /**
   * animate view
   */
  private ValueAnimator setAnimator(int colorFrom, int colorTo) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    long duration = 300;
    colorAnimation.setDuration(duration);
    return colorAnimation;
  }

  /**
   * Filter songs
   */
  public List<Song> filter(List<Song> songList, String query) {
    query = query.toLowerCase().trim();
    final List<Song> filtersonglist = new ArrayList<>();
    for (Song song : songList) {
      final String text = song.getTitle().toLowerCase().trim();
      if (text.contains(query)) {
        filtersonglist.add(song);
      }
    }
    return filtersonglist;
  }

}

