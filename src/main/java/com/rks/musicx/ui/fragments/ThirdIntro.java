package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rks.musicx.R;

/**
 * Created by Coolalien on 1/5/2017.
 */

public class ThirdIntro extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.third_intro, container, false);
        getActivity().getWindow().setStatusBarColor(Color.parseColor("#37474F"));
        return rootView;
    }
}
