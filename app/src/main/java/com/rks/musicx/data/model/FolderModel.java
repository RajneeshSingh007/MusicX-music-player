package com.rks.musicx.data.model;

import android.text.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
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

public class FolderModel implements Comparable<FolderModel> {

    private static final List<String> fileExtensions = new ArrayList<>(Arrays.asList("aac", "mp4", "flac", "m4a", "mp3", "ogg"));

    private final File mFile;
    private final FileModelComparator mFileModelComparator;
    private final FileExtensionFilter mFileExtensionFilter;

    public FolderModel(File file) {
        mFile = file;
        mFileModelComparator = new FileModelComparator();
        mFileExtensionFilter = new FileExtensionFilter();
    }


    public FolderModel(String filePath) {
        mFile = new File(filePath);

        mFileModelComparator = new FileModelComparator();
        mFileExtensionFilter = new FileExtensionFilter();
    }


    public String getName() {
        return mFile.getName();
    }


    public String getPath() {
        return mFile.getAbsolutePath();
    }


    public boolean isDirectory() {
        return mFile.isDirectory();
    }


    public boolean isFile() {
        return mFile.isFile();
    }

    public String getParent() {
        return mFile.getParent();
    }

    public File getParentFile(){
        return mFile.getParentFile();
    }

    public static List<String> getFileExtensions() {
        return fileExtensions;
    }

    public boolean isFileExists() {
        return mFile.exists();
    }

    public List<FolderModel> listFilesSorted() {
        List<FolderModel> files = new ArrayList<>();
        File extra = new File(mFile.getAbsolutePath(), "..");
        files.add(new FolderModel(extra));
        File[] filesArray =mFile.listFiles(mFileExtensionFilter);
        if (filesArray == null) {
            return files;
        }
        for (int k =0; k< filesArray.length; k++){
            files.add(new FolderModel(filesArray[k]));
        }
        Collections.sort(files, mFileModelComparator);
        return files;
    }


    @Override
    public boolean equals(Object model) {
        if (!(model instanceof FolderModel)) {
            return false;
        }
        return mFile.equals(((FolderModel) model).mFile);
    }

    @Override
    public int compareTo(FolderModel model) {
        return mFile.compareTo(((FolderModel) model).mFile);
    }

    private class FileModelComparator implements Comparator<FolderModel> {

        @Override
        public int compare(FolderModel f1, FolderModel f2) {

            if (f1.equals(f2)) {
                return 0;
            }

            if (f1.isDirectory() && f2.isFile()) {
                // show directories above files
                return -1;
            }

            if (f1.isFile() && f2.isDirectory()) {
                // show files below directories
                return 1;
            }

            // sort alphabetically, ignoring case
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    private class FileExtensionFilter implements FileFilter {

        public FileExtensionFilter() {
        }


        @Override
        public boolean accept(File file) {
            if (file.isHidden() || !file.canRead()) {
                return false;
            }
            if (file.isFile()) {
                String name = file.getName();
                return getfileExtension(name);
            } else if (file.isDirectory()) {
                return checkDir(file);
            } else
                return false;
        }

        public  boolean getfileExtension(String name) {
            if (TextUtils.isEmpty(name)) {
                return false;
            }
            int p = name.lastIndexOf(".") + 1;
            if (p < 1) {
                return false;
            }
            String ext = name.substring(p).toLowerCase();
            for (String o : fileExtensions) {
                if (o.equals(ext)) {
                    return true;
                }
            }
            return false;
        }


        private  boolean checkDir(File dir) {
            return dir.exists() && dir.canRead() && !".".equals(dir.getName()) && dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    return !".".equals(name) && !"..".equals(name) && pathname.canRead() &&  (pathname.isDirectory()  || (pathname.isFile() && getfileExtension(name)));
                }

            }).length != 0;
        }
    }
}
