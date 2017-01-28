package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.MainActivity;

/**
 * Created by Coolalien on 10/9/2016.
 */

public class RecentFragment extends Fragment {

    private TextView recentName,recentPlayed,More,RecentlyAddedMore;

    /**
     * Instances of this fragment
     * @return
     */
    public static RecentFragment newInstance(int pos){
        Extras.getInstance().setTabIndex(pos);
        return new RecentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recent,container,false);
        setupInstance(rootView);
        return rootView;
    }

    /**
     * setup Views
     * @param rootView
     */
    private void setupInstance(View rootView) {
        recentName = (TextView) rootView.findViewById(R.id.recentAdded);
        recentPlayed = (TextView) rootView.findViewById(R.id.recentPlayed);
        More = (TextView) rootView.findViewById(R.id.recentPlayedMore);
        RecentlyAddedMore = (TextView) rootView.findViewById(R.id.recentAddedMore);
        RecentlyAddedMore.setOnClickListener(v -> {
            RecentlyAddedFragment recentlyAddedFragment = new RecentlyAddedFragment().newInstance("%s limit -1",true);
            ((MainActivity) getActivity()).setFragment(recentlyAddedFragment);
        });
        More.setOnClickListener(v -> {
            RecentPlayedFragment recentPlayedFragment = new RecentPlayedFragment().newInstance(-1,true);
            ((MainActivity) getActivity()).setFragment(recentPlayedFragment);
        });
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            recentName.setTextColor(Color.WHITE);
            recentPlayed.setTextColor(Color.WHITE);
            More.setTextColor(Color.WHITE);
            RecentlyAddedMore.setTextColor(Color.WHITE);
        }else {
            recentName.setTextColor(Color.BLACK);
            recentPlayed.setTextColor(Color.BLACK);
            More.setTextColor(Color.BLACK);
            RecentlyAddedMore.setTextColor(Color.BLACK);
        }
    }


    /**
     * Load recentlyPlayed fragment
     */
    private void recentlyPlayed() {
        RecentPlayedFragment recentPlayedFragment = new RecentPlayedFragment().newInstance(9,false);
        if (recentPlayedFragment != null){
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.recentplayedfragment,recentPlayedFragment).commitAllowingStateLoss();
        }
    }

    /**
     * Load recentlyAdded fragment
     */
    private void recentlyAdded(){
        RecentlyAddedFragment recentlyAddedFragment = new RecentlyAddedFragment().newInstance("%s limit 9",false);
        if (recentlyAddedFragment != null){
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.recentaddedfragment,recentlyAddedFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
        recentlyPlayed();
        recentlyAdded();
    }
}
