package com.rks.musicx.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.FileAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolderFragment extends Fragment {


    private FastScrollRecyclerView folderrv;
    private File currentDir;
    private FileAdapter fileadapter;
    private final HashMap<String, Integer> mListPositioins = new HashMap<>();
    private String currentPath = "currentPath";
    private Intent intent;
    private List<Song> songList;

    public static FolderFragment newInstance() {
        return new FolderFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folder, container, false);
        ui(rootView);
        function();
        return rootView;
    }

    private void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.folderrv);
    }

    private void function() {
        intent = getActivity().getIntent();
        if (intent.hasExtra(currentPath)) {
            String startPath = intent.getStringExtra(currentPath);
            if (startPath != null && startPath.length() > 0) {
                File tmp = new File(startPath);
                if (tmp.exists() && tmp.isDirectory()) {
                    currentDir = tmp;
                }
            }
        }
        currentDir = new File("/");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            currentDir = Environment.getExternalStorageDirectory();
        }
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        folderrv.setLayoutManager(customLayoutManager);
        folderrv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
        String atekey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(),atekey);
        folderrv.setPopupBgColor(colorAccent);
        folderrv.setItemAnimator(new DefaultItemAnimator());
        setHasOptionsMenu(true);
        fileadapter = new FileAdapter(getContext());
        folderrv.setAdapter(fileadapter);
        fileadapter.setOnItemClickListener(onClik);
        readDirectory(currentDir);
    }

    private BaseRecyclerViewAdapter.OnItemClickListener onClik = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.folder_view:
                    File file = fileadapter.data.get(position);
                    if (file.isDirectory()) {
                        mListPositioins.put(file.getPath(), position);
                        readDirectory(file);
                    }else if (file.isFile() && currentPath.endsWith(".mp3")){
                        ((MainActivity) getActivity()).onSongSelected(songList,position);
                    }
                    folderrv.smoothScrollToPosition(position);
                    break;
            }
        }
    };

    public void readDirectory(File path) {
        fileadapter.data.clear();
        File[] files = path.listFiles(new FileExtensionFilter(true));
        if (files != null) {
            for (File file : files) {
                fileadapter.data.add(file);
                fileadapter.notifyDataSetChanged();
                currentPath = file.getAbsolutePath();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String atekey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), atekey, Config.primaryColor(getActivity(), atekey));
    }

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
            }
            return checkFileExtension( f );
        }

        private boolean checkFileExtension( File f ) {
            String ext = getFileExtension(f);
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
                            if ( file.getName().equals( ".nomedia" ) )
                                return false;

                            return checkFileExtension( file );
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


        public String getFileExtension( File f ) {
            return getFileExtension( f.getName() );
        }

        public String getFileExtension( String fileName ) {
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                return fileName.substring(i + 1);
            } else
                return null;
        }

    }
    /**
     * Files formats currently supported by Library
     */
    public enum SupportedFileFormat {
        M4A("m4a"),
        MP3("mp3"),
        WAV("wav"),
        AAC("aac"),
        OGG("ogg");

        private String filesuffix;

        SupportedFileFormat( String filesuffix ) {
            this.filesuffix = filesuffix;
        }

        public String getFilesuffix() {
            return filesuffix;
        }
    }

}
