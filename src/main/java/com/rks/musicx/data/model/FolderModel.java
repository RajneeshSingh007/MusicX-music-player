package com.rks.musicx.data.model;

import java.io.File;
import java.io.FilenameFilter;
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
        mFileExtensionFilter = new FileExtensionFilter(fileExtensions);
    }


    public FolderModel(String filePath) {
        mFile = new File(filePath);

        mFileModelComparator = new FileModelComparator();
        mFileExtensionFilter = new FileExtensionFilter(fileExtensions);
    }


    public long getLastModified() {
        return mFile.lastModified();
    }


    public String getName() {
        return mFile.getName();
    }


    public String getPath() {
        return mFile.getPath();
    }


    public String getURLString() {
        return mFile.getPath();
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

    public boolean isFileExists(){
        return mFile.exists();
    }

    public List<FolderModel> listFilesSorted() {
        List<FolderModel> files = new ArrayList<>();
        File[] filesArray = mFile.listFiles(mFileExtensionFilter);

        if (filesArray == null) {
            return files;
        }
        for (File file : filesArray) {
            if (file.getName().equals(".nomedia")) {
                files.clear();
                break;
            }
            files.add(new FolderModel(file));
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

    public File getmFile() {
        return mFile;
    }

    private class FileExtensionFilter implements FilenameFilter {

        private List<String> mExtensions;

        public FileExtensionFilter(List<String> extensions) {
            mExtensions = extensions;
        }

        @Override
        public boolean accept(File dir, String filename) {

            if (new File(dir, filename).isHidden() || !new File(dir, filename).canRead()) {
                return false;
            }
            if (new File(dir, filename).isDirectory()) {
                return true;
            }

           if (new File(dir, filename).isFile() && new File(dir, filename).exists()){
               if (!mExtensions.isEmpty()) {
                   String ext = getFileExtension(filename);
                   if (mExtensions.contains(ext)) {
                       return true;
                   }
               }
           }
            return false;
        }

        private String getFileExtension(String filename) {

            String ext = null;
            int i = filename.lastIndexOf('.');
            if (i != -1 && i < filename.length()) {
                ext = filename.substring(i + 1).toLowerCase();
            }
            return ext;
        }

    }

}
