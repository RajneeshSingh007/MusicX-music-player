package com.rks.musicx.misc.utils;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by Coolalien on 1/20/2017.
 */

public class FileExtensionFilter implements FileFilter {

    protected static final String TAG = "FileExtensionFilter";

    /**
     * allows Directories
     */
    private final boolean allowDirectories;

    public FileExtensionFilter( boolean allowDirectories) {
        this.allowDirectories = allowDirectories;
    }


    @Override
    public boolean accept(File f) {
        if ( f.isHidden() || !f.canRead() ) {
            return false;
        }
        if ( f.isDirectory() ) {
            return checkDirectory( f );
        }else if ( f.isFile() ) {
            return checkFileExtension(f);
        }
        return true;
    }

    private boolean checkFileExtension( File f ) {
        String ext = getFileExtension(f.getName());
        if ( ext == null) return false;
        try {
            if ( SupportedFileFormat.valueOf(ext.toUpperCase()) != null ) {
                return true;
            }
        } catch(IllegalArgumentException e) {
            //Not known enum value
            return false;
        }
        return false;
    }

    private boolean checkDirectory( File dir ) {
        if ( !allowDirectories ) {
            return false;
        } else {
            final ArrayList<File> subDirs = new ArrayList<File>();
            int songNumb = dir.listFiles( new FileFilter() {

                @Override
                public boolean accept(File file) {
                    if ( file.isFile() ) {
                        return !file.getName().equals(".nomedia") && checkFileExtension(file);
                    } else if ( file.isDirectory() ){
                        subDirs.add( file );
                        return false;
                    } else
                        return false;
                }
            } ).length;

            if ( songNumb > 0 ) {
                Log.d(TAG, "checkDirectory: dir " + dir.toString() + " return true con songNumb -> " + songNumb );
                return true;
            }
            for( File subDir: subDirs ) {
                if ( checkDirectory( subDir ) ) {
                    Log.d(TAG, "checkDirectory [for]: subDir " + subDir.toString() + " return true");
                    return true;
                }
            }
            return false;
        }
    }

    public String getFileExtension( String fileName ) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else
            return null;
    }
}

