package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rks.musicx.R;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class FourthIntro extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fourth_intro, container, false);
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
        return v;
    }
}
