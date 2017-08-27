package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.base.BaseRefreshFragment;
import com.rks.musicx.data.loaders.FolderLoader;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.model.Folder;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.interfaces.Action;
import com.rks.musicx.interfaces.ExtraCallback;
import com.rks.musicx.interfaces.RefreshData;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.FolderAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;


/*
 * Created by Coolalien on 03/24/2017.
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

public class FolderFragment extends BaseRefreshFragment implements SearchView.OnQueryTextListener {


    private final int folderloader = 2;
    private FastScrollRecyclerView folderrv;
    private FolderAdapter folderAdapter;
    private Helper helper;
    private SearchView searchView;
    private android.support.v7.view.ActionMode mActionMode;
    private File currentPath;
    private LoaderManager.LoaderCallbacks<List<Folder>> folderLoader = new LoaderManager.LoaderCallbacks<List<Folder>>() {
        @Override
        public Loader<List<Folder>> onCreateLoader(int id, Bundle args) {
            FolderLoader folderLoader = new FolderLoader(getContext(), getCurrentPath());
            if (id == folderloader) {
                return folderLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<Folder>> loader, List<Folder> data) {
            if (data == null) {
                return;
            }
            folderAdapter.clear();
            folderAdapter.addDataList(data);
        }

        @Override
        public void onLoaderReset(Loader<List<Folder>> loader) {
            loader.reset();
            folderAdapter.notifyDataSetChanged();
        }
    };
    private BaseRecyclerViewAdapter.OnLongClickListener onLongClick = new BaseRecyclerViewAdapter.OnLongClickListener() {
        @Override
        public void onLongItemClick(int position) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(Helper.getActionCallback(((MainActivity) getActivity()), getContext(), new Action() {
                @Override
                public void clear() {
                    if (mActionMode != null) {
                        mActionMode.setTitle("");
                        mActionMode.finish();
                        mActionMode = null;
                    }
                    folderAdapter.exitMultiselectMode();
                }

                @Override
                public Fragment currentFrag() {
                    return FolderFragment.this;
                }

                @Override
                public void refresh() {
                    getLoaderManager().restartLoader(folderloader, null, folderLoader);
                }
            }, false, new ExtraCallback() {
                @Override
                public SongListAdapter songlistAdapter() {
                    return null;
                }

                @Override
                public FolderAdapter folderAdapter() {
                    return folderAdapter;
                }
            }));

            Helper.setActionModeBackgroundColor(mActionMode, Config.primaryColor(getContext(), Helper.getATEKey(getContext())));
            if (position > 0) {
                if (mActionMode != null) {
                    mActionMode.setTitle(position + " selected");
                }
            } else {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        }
    };
    /**
     * Folder click
     */
    private BaseRecyclerViewAdapter.OnItemClickListener onClik = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            if (folderAdapter.isMultiselect()) {
                if (position > 0) {
                    if (mActionMode != null) {
                        mActionMode.setTitle(position + " selected");
                    }
                } else {
                    if (mActionMode != null) {
                        mActionMode.finish();
                    }
                }
            } else {
                if (position >= 0 && position < folderAdapter.getItemCount()) {
                    Folder folder = folderAdapter.getItem(position);
                    currentPath = folder.getFile();
                    switch (view.getId()) {
                        case R.id.item_view:
                            if (currentPath != null) {
                                if (currentPath.isDirectory()) {
                                    if (!currentPath.getAbsolutePath().equals("/")) {
                                        getLoaderManager().restartLoader(folderloader, null, folderLoader);
                                    } else {
                                        Toast.makeText(getContext(), "No directory found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    int index = Helper.getIndex(currentPath.getAbsolutePath(), folder.getSongList());
                                    if (index > -1) {
                                        ((MainActivity) getActivity()).onSongSelected(folder.getSongList(), index);
                                        Extras.getInstance().saveSeekServices(0);
                                    }
                                    Log.e("Folder", currentPath.getName() + " ---> " + String.valueOf(position));
                                }
                            }
                            break;
                        case R.id.menu_button:
                            if (currentPath != null) {
                                if (currentPath.isDirectory()) {
                                    Helper.showFolderMenu(getContext(), view, currentPath, new RefreshData() {
                                        @Override
                                        public void refresh() {
                                            String storagePath = Extras.getInstance().getFolderPath();
                                            if (storagePath != null) {
                                                currentPath = new File(storagePath);
                                            } else {
                                                currentPath = new File(Helper.getStoragePath());
                                            }
                                            getLoaderManager().restartLoader(folderloader, null, folderLoader);
                                        }

                                        @Override
                                        public Fragment currentFrag() {
                                            return FolderFragment.this;
                                        }
                                    });
                                } else {
                                    int index = Helper.getIndex(currentPath.getAbsolutePath(), folder.getSongList());
                                    if (index > -1) {
                                        Song song = folder.getSongList().get(index);
                                        helper.showMenu(false, new RefreshData() {
                                            @Override
                                            public void refresh() {
                                                getLoaderManager().restartLoader(folderloader, null, folderLoader);
                                            }

                                            @Override
                                            public Fragment currentFrag() {
                                                return FolderFragment.this;
                                            }
                                        }, ((MainActivity) getActivity()), view, getContext(), song);
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected int setLayout() {
        return R.layout.common_rv;
    }

    @Override
    protected void ui(View rootView) {
        folderrv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
    }

    @Override
    protected void funtion() {
        String atekey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), atekey);
        folderrv.setPopupBgColor(colorAccent);
        folderrv.setItemAnimator(new DefaultItemAnimator());
        helper = new Helper(getContext());
        setHasOptionsMenu(true);
        String storagePath = Extras.getInstance().getFolderPath();
        if (storagePath != null) {
            currentPath = new File(storagePath);
        } else {
            currentPath = new File(Helper.getStoragePath());
        }
        folderAdapter = new FolderAdapter(getContext());
        folderrv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        folderAdapter.setOnItemClickListener(onClik);
        folderAdapter.setOnLongClickListener(onLongClick);
        folderrv.setHasFixedSize(true);
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
        customLayoutManager.setSmoothScrollbarEnabled(true);
        folderrv.setLayoutManager(customLayoutManager);
        folderrv.setAdapter(folderAdapter);
        initLoader();
    }


    private void initLoader() {
        getLoaderManager().initLoader(folderloader, null, folderLoader);
    }

    public File getCurrentPath() {
        return currentPath;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        menu.findItem(R.id.grid_view).setVisible(false);
        menu.findItem(R.id.default_folder).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shuffle_all:
                if (folderAdapter.getSongList().size() > 0) {
                    ((MainActivity) getActivity()).onShuffleRequested(folderAdapter.getSongList(), true);
                } else {
                    Toast.makeText(getContext(), "Empty Data", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_sort_by_az:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                load();
                break;
            case R.id.menu_sort_by_year:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                load();
                break;
            case R.id.menu_sort_by_artist:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                load();
                break;
            case R.id.menu_sort_by_album:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                load();
                break;
            case R.id.menu_sort_by_duration:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                load();
                break;
            case R.id.menu_sort_by_date:
                Extras.getInstance().setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                load();
                break;
            case R.id.default_folder:
                setDefaultFolder();
                load();
                break;
            case R.id.menu_refresh:
                load();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setDefaultFolder() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.default_dir)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .autoDismiss(true)
                .typeface(Helper.getFont(getContext()), Helper.getFont(getContext()))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (getCurrentPath() != null) {
                            if (getCurrentPath().isDirectory()) {
                                try {
                                    Log.e("FolderFragment", getCurrentPath().getAbsolutePath());
                                    Extras.getInstance().saveFolderPath(getCurrentPath().getAbsolutePath());
                                } catch (Exception e) {
                                    // ignored
                                }
                            }
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    @Override
    public void load() {
        getLoaderManager().restartLoader(folderloader, null, folderLoader);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Folder> folderList = helper.filterFolder(getContext(), folderAdapter.getSnapshot(), newText);
        if (folderList.size() > 0) {
            folderAdapter.setFilter(folderList);
            return true;
        } else {
            Toast.makeText(getContext(), "No data found...", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
