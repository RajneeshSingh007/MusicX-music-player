package com.rks.musicx.ui.fragments;

import android.content.Context;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class MainFragment extends miniFragment {

  private ViewPager mViewPager;
  private PagerAdapter pagerAdapter;
  private TabLayout tabLayout;
  private Toolbar toolbar;

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

    pagerAdapter = new PagerAdapter(getChildFragmentManager(), getContext());
    mViewPager.setAdapter(pagerAdapter);
    tabLayout.setupWithViewPager(mViewPager);
    if (Extras.getInstance().restoreLastTab()) {
      mViewPager.setCurrentItem(Integer.valueOf(Extras.getInstance().getTabIndex()), true);
    }
    toolbar.setTitle("");
    mViewPager.setOffscreenPageLimit(6);
    toolbar.showOverflowMenu();
    DrawerLayout mDrawerLayout = ((MainActivity) getActivity()).getDrawerLayout();
    ((MainActivity) getActivity()).setSupportActionBar(toolbar);
    ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(mDrawerToggle);
    mDrawerToggle.syncState();
    return rootView;
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


  /**
   * ViewPager Adapter
   */
  public class PagerAdapter extends FragmentStatePagerAdapter {

    private Map<Integer, String> mFragmentTags;

    public PagerAdapter(FragmentManager fm, Context context) {
      super(fm);
      mFragmentTags = new HashMap<>();
    }


    @Override
    public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RecentFragment.newInstance(0);
                case 1:
                    return FolderFragment.newInstance(1);
                case 2:
                    if (!Extras.getInstance().songView()) {
                        return SongListFragment.newInstance(2);
                    } else {
                        return SongGridFragment.newInstance(2);
                    }
                case 3:
                    if (Extras.getInstance().albumView()) {
                        return AlbumListFragment.newInstance(3);
                    } else if (!Extras.getInstance().albumView()) {
                        return AlbumGridFragment.newInstance(3);
                    }
                case 4:
                    if (Extras.getInstance().artistView()) {
                        return ArtistListFragment.newInstance(4);
                    } else if (!Extras.getInstance().artistView()) {
                        return ArtistGridFragment.newInstance(4);
                    }
                case 5:
                    return PlaylistListFragment.newInstance(5);
            }
      return null;
    }


    @Override
    public int getCount() {
      return 6;
    }

    @Override
    public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.recentlists).toUpperCase(l);
                case 1:
                    return getString(R.string.folder).toUpperCase(l);
                case 2:
                    return getString(R.string.titles).toUpperCase(l);
                case 3:
                    return getString(R.string.albums).toUpperCase(l);
                case 4:
                    return getString(R.string.artists).toUpperCase(l);
                case 5:
                    return getString(R.string.playlists).toUpperCase(l);
            }
      return null;
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
