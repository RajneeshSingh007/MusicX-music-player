package com.rks.musicx.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.FileExtensionFilter;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.FileAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.One;
import static com.rks.musicx.misc.utils.Constants.Zero;

public class FolderFragment extends miniFragment implements LoaderManager.LoaderCallbacks<List<Song>> {


    private FastScrollRecyclerView folderrv;
    private File currentDir;
    private FileAdapter fileadapter;
    private String currentPath = "currentPath";
    private final HashMap<String, Integer> mListPositioins = new HashMap<>();
    private Intent intent;
    private final int trackloader = -1;
    private String folderName;
    private Helper helper;
    private SongListAdapter songListAdapter;
    private File externalStorage;

    /**
     * instance of this class
     * @return
     */
    public static FolderFragment newInstance(int pos) {
        Extras.getInstance().setTabIndex(pos);
        return new FolderFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.common_rv, container, false);
        ui(rootView);
        function();
        return rootView;
    }

    private void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
    }

    private void function() {
        intent = getActivity().getIntent();
        if (intent.hasExtra(currentPath)) {
            String startPath = intent.getStringExtra(currentPath);
            if (startPath != null && startPath.length() > 0) {
                File tmp = new File(startPath);
                if (tmp.exists() && tmp.isDirectory()) {
                    currentDir = tmp;
                    externalStorage = tmp;
                }
            }
        }
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getActivity());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        folderrv.setLayoutManager(customLayoutManager);
        folderrv.addItemDecoration(new DividerItemDecoration(getActivity(), 75));
        String atekey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(),atekey);
        folderrv.setPopupBgColor(colorAccent);
        folderrv.setItemAnimator(new DefaultItemAnimator());
        fileadapter = new FileAdapter(getContext());
        folderrv.setAdapter(fileadapter);
        fileadapter.setOnItemClickListener(onClik);
        helper = new Helper(getContext());
        if (Extras.getInstance().storageConfig().equals(Zero)){
            currentDir = Helper.getInternalStorage();
            scanDirectory(currentDir);
        }else if (Extras.getInstance().storageConfig().equals(One)){
            externalStorage = Helper.getExternalStorage(getContext());
            scanDirectory(externalStorage);
        }
        setHasOptionsMenu(true);
    }

    /**
     * OnClick
     */
    private BaseRecyclerViewAdapter.OnItemClickListener onClik = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.folder_view:
                    if (fileadapter.data.size() > 0 ){
                        String paths = fileadapter.getItem(position).getAbsolutePath();
                        File file = new File(paths);
                        mListPositioins.put(file.getPath(), position);
                        if (file.isDirectory()){
                            scanDirectory(file);
                        }
                    }
                    break;
            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener songOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(),position);
                    folderrv.smoothScrollToPosition(position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloader,FolderFragment.this,FolderFragment.this,((MainActivity) getActivity()),position,view,getContext(),songListAdapter);
                    break;

            }
        }
    };


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
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

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        TrackLoader trackLoaders = new TrackLoader(getContext());
        if (id == trackloader){
            String[] selectargs = new String[]{
                    "%"+folderName+"%"
            }; //filter folders
            String selection = MediaStore.Audio.Media.DATA + " like ? ";
            trackLoaders.setSortOrder(MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            trackLoaders.filteralbumsong(selection,selectargs);
            return trackLoaders;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data  == null){
            return;
        }
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        songListAdapter.notifyDataSetChanged();
    }

    /**
     * Read Directory with filter
     *
     */
    public void scanDirectory(File dirpath) {
        new AsyncTask<Void, Void, String>() {
            File[] files;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {
                fileadapter.data.clear();
                files = dirpath.listFiles(new FileExtensionFilter(true));
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    if (files != null) {
                        for (File file : files) {
                            fileadapter.data.add(file);
                            fileadapter.notifyDataSetChanged();
                            currentPath = file.getAbsolutePath();
                            folderName = file.getParent();
                            if (file.isFile()) {
                                songListAdapter = new SongListAdapter(getContext());
                                songListAdapter.setOnItemClickListener(songOnClick);
                                folderrv.setAdapter(songListAdapter);
                                getLoaderManager().initLoader(trackloader, null, FolderFragment.this);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader,null, this);
    }

}
