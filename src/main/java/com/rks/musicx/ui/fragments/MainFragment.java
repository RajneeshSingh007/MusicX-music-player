package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


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

public class MainFragment extends miniFragment {

    private ViewPager mViewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private List<Fragment> fragmentList;
    private List<String> tabTitles;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        function();
        return rootView;
    }

    private void function() {
        fragmentList = new ArrayList<>();
        tabTitles = new ArrayList<>();

        /// Add fragments
        fragmentList.add(RecentFragment.newInstance());
        fragmentList.add(FolderFragment.newInstance());
        fragmentList.add(SongListFragment.newInstance());
        fragmentList.add(AlbumListFragment.newInstance());
        fragmentList.add(ArtistListFragment.newInstance());
        fragmentList.add(PlaylistListFragment.newInstance());

        // Add Tab Titles
        Locale l = Locale.getDefault();
        tabTitles.add(getString(R.string.recentlists).toUpperCase(l));
        tabTitles.add(getString(R.string.folder).toUpperCase(l));
        tabTitles.add(getString(R.string.titles).toUpperCase(l));
        tabTitles.add(getString(R.string.albums).toUpperCase(l));
        tabTitles.add(getString(R.string.artists).toUpperCase(l));
        tabTitles.add(getString(R.string.playlists).toUpperCase(l));

        pagerAdapter = new PagerAdapter(getChildFragmentManager(), fragmentList, tabTitles);
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
    }

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
    public void onPause() {
        super.onPause();
        Extras.getInstance().setTabIndex(mViewPager.getCurrentItem());
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void load() {
        int fragmentCount = pagerAdapter.getCount();
        for (int pos = 0; pos < fragmentCount; pos++) {
            miniFragment fragment = (miniFragment) pagerAdapter.getFragment(pos);
            if (fragment != null) {
                Log.d("fragment", fragment.getClass().getCanonicalName());
                fragment.load();
            }
        }
    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        private Map<Integer, String> mFragmentTags;
        private List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> tabTitles = new ArrayList<>();

        public PagerAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> tabTitles) {
            super(fm);
            mFragmentTags = new HashMap<>();
            this.mFragmentList = fragmentList;
            this.tabTitles = tabTitles;
        }


        @Override
        public Fragment getItem(int position) {
            Extras.getInstance().setTabIndex(position);
            return mFragmentList.get(position);
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }

        public Fragment getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null) {
                return null;
            }
            return getChildFragmentManager().findFragmentByTag(tag);
        }
    }

}
