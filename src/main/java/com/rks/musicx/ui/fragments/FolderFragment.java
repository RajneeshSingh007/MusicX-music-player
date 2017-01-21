package com.rks.musicx.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.FileExtensionFilter;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.FileAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolderFragment extends Fragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Song>> {


    private FastScrollRecyclerView folderrv;
    private File currentDir;
    private FileAdapter fileadapter;
    private final HashMap<String, Integer> mListPositioins = new HashMap<>();
    private String currentPath = "currentPath";
    private Intent intent;
    private final int trackloader = -1;
    private String folderName;
    private Helper helper;
    private SearchView searchView;
    private List<Song> songList;
    private SongListAdapter songListAdapter;
    private Toolbar toolbar;

    /**
     * instance of this class
     * @return
     */
    public static FolderFragment newInstance() {
        return new FolderFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_folder, container, false);
        ui(rootView);
        function();
        setHasOptionsMenu(true);
        return rootView;
    }

    private void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.folderrv);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
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
        helper = new Helper(getContext());
        songList = new ArrayList<>();
        toolbar.setTitle(getString(R.string.folder));
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
                        File file = fileadapter.data.get(position);
                        if (file.isDirectory()) {
                            mListPositioins.put(file.getPath(), position);
                            readDirectory(file);
                        }else {
                            Toast.makeText(getContext(),"Empty Directory",Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    };


    /**
     * Read Directory with filter
     * @param path
     */
    public void readDirectory(File path) {
        fileadapter.data.clear();
        File[] files = path.listFiles(new FileExtensionFilter(true));
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
                    getLoaderManager().initLoader(trackloader, null, this);
                }
            }
        }
    }

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
        songList = data;
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        songListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu,menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.folder_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Song> filterlist = helper.filter(songList,newText);
        songListAdapter.setFilter(filterlist);
        return true;
    }
}
