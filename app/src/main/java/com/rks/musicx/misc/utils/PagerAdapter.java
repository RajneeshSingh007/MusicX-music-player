package com.rks.musicx.misc.utils;

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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Coolalien on 6/18/2017.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    private Map<Integer, String> mFragmentTags;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> tabTitles = new ArrayList<>();
    private Fragment fragment;

    public PagerAdapter(Fragment fragment,FragmentManager fm, List<Fragment> fragmentList, List<String> tabTitles) {
        super(fm);
        this.fragment = fragment;
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
        if (mFragmentList.contains(object)) return mFragmentList.indexOf(object);
        else return POSITION_NONE;
    }

    public Fragment getFragment(int position) {
        String tag = mFragmentTags.get(position);
        if (tag == null) {
            return null;
        }
        if (fragment == null){
            return null;
        }
        return fragment.getChildFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (position <= getCount()) {
            FragmentManager manager = ((Fragment) object).getFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove((Fragment) object);
            trans.commit();
        }
    }

    public void removeTabPage(int position) {
        if (position < mFragmentList.size() && position < tabTitles.size()){
            mFragmentList.remove(position);
            tabTitles.remove(position);
            notifyDataSetChanged();
        }
    }

}
