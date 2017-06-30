package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.rks.musicx.R;
import com.rks.musicx.base.BaseRefreshFragment;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.PagerAdapter;
import com.rks.musicx.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/*
 * Created by Coolalien on 6/28/2016.
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

public class MainFragment extends BaseRefreshFragment {

    private ViewPager mViewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private List<Fragment> fragmentList;
    private List<String> tabTitles;
    private RecentFragment recentFragment;
    private FolderFragment folderFragment;
    private SongListFragment songListFragment;
    private AlbumListFragment albumListFragment;
    private ArtistListFragment artistListFragment;
    private PlaylistListFragment playlistListFragment;
    private boolean added = false;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private void addPages(){
        // Add fragments
        fragmentList.add(recentFragment);
        fragmentList.add(folderFragment);
        fragmentList.add(songListFragment);
        fragmentList.add(albumListFragment);
        fragmentList.add(artistListFragment);
        fragmentList.add(playlistListFragment);

        // Add Tab Titles
        Locale l = Locale.getDefault();
        tabTitles.add(getString(R.string.recentlists).toUpperCase(l));
        tabTitles.add(getString(R.string.folder).toUpperCase(l));
        tabTitles.add(getString(R.string.titles).toUpperCase(l));
        tabTitles.add(getString(R.string.albums).toUpperCase(l));
        tabTitles.add(getString(R.string.artists).toUpperCase(l));
        tabTitles.add(getString(R.string.playlists).toUpperCase(l));

        added = true;
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
    public void onPause() {
        super.onPause();
        Extras.getInstance().setTabIndex(mViewPager.getCurrentItem());
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_main;
    }

    @Override
    protected void ui(View view) {
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    }

    @Override
    protected void funtion() {
        folderFragment = new FolderFragment();
        songListFragment = new SongListFragment();
        recentFragment = new RecentFragment();
        albumListFragment = new AlbumListFragment();
        artistListFragment = new ArtistListFragment();
        playlistListFragment = new PlaylistListFragment();

        fragmentList = new ArrayList<>();
        tabTitles = new ArrayList<>();
        addPages();
        pagerAdapter = new PagerAdapter(MainFragment.this, getChildFragmentManager(), fragmentList, tabTitles);
        mViewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        toolbar.setTitle("");
        mViewPager.setOffscreenPageLimit(6);
        toolbar.showOverflowMenu();
        DrawerLayout mDrawerLayout = ((MainActivity) getActivity()).getDrawerLayout();
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        if (Extras.getInstance().restoreLastTab()) {
            mViewPager.setCurrentItem(Integer.valueOf(Extras.getInstance().getTabIndex()), true);
        }
        if (added) {
            for (int pos : Extras.getInstance().getRemoveTab()) {
                pagerAdapter.removeTabPage(pos);
                Log.e("Main", String.valueOf(pos));
            }
        }
    }

    @Override
    public void load() {
        int fragmentCount = pagerAdapter.getCount();
        for (int pos = 0; pos < fragmentCount; pos++) {
            BaseRefreshFragment fragment = (BaseRefreshFragment) pagerAdapter.getFragment(pos);
            if (fragment != null) {
                Log.d("fragment", fragment.getClass().getCanonicalName());
                fragment.load();
            }
        }
    }

}
