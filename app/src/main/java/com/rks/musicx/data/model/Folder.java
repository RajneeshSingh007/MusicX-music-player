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

public class Folder implements Comparable<Folder> {

    private static final List<String> fileExtensions = new ArrayList<>(Arrays.asList("aac", "mp4", "flac", "m4a", "mp3", "ogg"));
    private final File mFile;
    private final FileModelComparator mFileModelComparator;
    private final FileExtensionFilter mFileExtensionFilter;

    public Folder(File file) {
        mFile = file;
        mFileModelComparator = new FileModelComparator();
        mFileExtensionFilter = new FileExtensionFilter(fileExtensions);
    }


    public Folder(String filePath) {
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

    public File getmFile() {
        return mFile;
    }

    public List<Folder> listFilesSorted() {
        List<Folder> files = new ArrayList<>();
        File[] filesArray = mFile.listFiles(mFileExtensionFilter);

        if (filesArray == null) {
            return null;
        }
        for (File file : filesArray) {
            if (file.getName().equals(".nomedia")) {
                files.clear();
                break;
            }
            files.add(new Folder(file));
        }
        Collections.sort(files, mFileModelComparator);
        return files;
    }


    @Override
    public boolean equals(Object model) {
        if (!(model instanceof Folder)) {
            return false;
        }
        return mFile.equals(((Folder) model).mFile);
    }

    @Override
    public int compareTo(Folder model) {
        return mFile.compareTo(((Folder) model).mFile);
    }

    private class FileModelComparator implements Comparator<Folder> {

        @Override
        public int compare(Folder f1, Folder f2) {

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


    private class FileExtensionFilter implements FilenameFilter {

        private List<String> mExtensions;

        public FileExtensionFilter(List<String> extensions) {
            mExtensions = extensions;
        }

        @Override
        public boolean accept(File dir, String filename) {
            File scan = new File(dir, filename);
            if (scan.isHidden() || !scan.canRead()) {
                return false;
            }
            if (scan.isDirectory()) {
                return true;
            }
            if (scan.isFile() && scan.exists()){
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
