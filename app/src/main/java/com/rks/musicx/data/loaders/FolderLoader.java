package com.rks.musicx.data.loaders;


import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.rks.musicx.base.BaseAsyncTaskLoader;
import com.rks.musicx.data.model.Folder;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

public class FolderLoader extends BaseAsyncTaskLoader<List<Folder>> {

    private File dir;

    public FolderLoader(Context context, File dir) {
        super(context);
        this.dir = dir;
    }


    @Override
    public List<Folder> loadInBackground() {
        List<Folder> folderList = new ArrayList<>();
        List<Song> songList = new ArrayList<>();
        if (PermissionChecker.checkCallingOrSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            FileExtensionFilter mFileExtensionFilter = new FileExtensionFilter(Constants.fileExtensions);
            if (dir != null) {
                File[] scanFolder = dir.listFiles(mFileExtensionFilter);
                if (scanFolder == null) {
                    return null;
                }
                for (File aScanFolder : scanFolder) {
                    Folder folder = new Folder();
                    Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.DATA}, MediaStore.Audio.Media.DATA + " like ? ", new String[]{"%" + aScanFolder.getAbsolutePath() + "%"}, null);
                    if (cursor != null) {
                        int count = cursor.getCount();
                        if (count != 0) {
                            if (!aScanFolder.isDirectory()) {
                                String path = aScanFolder.getAbsolutePath();
                                Song song = Helper.getSongData(Extras.getInstance().getSongSortOrder(), getContext(), path);
                                songList.add(song);
                            }
                            if (!aScanFolder.getAbsolutePath().startsWith("/d")) {
                                Log.e("FolderLoader", "Path --> " + aScanFolder.getAbsolutePath());
                                folder.setFile(aScanFolder);
                                folder.setFileCount(count);
                                folder.setSongList(songList);
                                folderList.add(folder);
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                Collections.sort(folderList, new Comparator<Folder>() {
                    @Override
                    public int compare(Folder f1, Folder f2) {
                        if ((f1.getFile().isDirectory() && f2.getFile().isDirectory()))
                            return f1.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
                        else if (f1.getFile().isDirectory() && !f2.getFile().isDirectory())
                            return -1;
                        else if (!f1.getFile().isDirectory() && f2.getFile().isDirectory())
                            return 1;
                        else if (!f1.getFile().isDirectory() && !f2.getFile().isDirectory())
                            return f1.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
                        else return 0;
                    }

                });
                if (!dir.getAbsolutePath().equals("/")) {
                    Folder folder = new Folder();
                    if (dir.getParentFile() != null) {
                        folder.setFile(dir.getParentFile());
                        Log.e("FolderLoader", dir.getParentFile().getAbsolutePath());
                        folderList.add(0, folder);
                    }
                }
            }

            return folderList;
        } else {
            Log.d("Folder", "Permission not granted");
            return Collections.emptyList();
        }
    }

    /**
     * Filter
     */
    private class FileExtensionFilter implements FilenameFilter {

        private String[] mExtensions;

        public FileExtensionFilter(String[] extensions) {
            mExtensions = extensions;
        }

        @Override
        public boolean accept(File dir, String filename) {
            File scan = new File(dir, filename);
            if (scan.isHidden() || !scan.canRead() || scan.getName().equals(".nomedia") || scan.getName().startsWith(".")) {
                return false;
            }
            if (scan.isDirectory()) {
                return true;
            }
            if (scan.isFile() && scan.exists()) {
                for (String ext : mExtensions) {
                    if (scan.getAbsolutePath().endsWith(ext)) {
                        return true;
                    }
                }
            }
            return false;
        }

    }


}
