package com.rks.musicx.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.rks.musicx.ui.fragments.FirstIntro;
import com.rks.musicx.ui.fragments.FourthIntro;
import com.rks.musicx.ui.fragments.SecondIntro;
import com.rks.musicx.ui.fragments.ThirdIntro;

/**
 * Created by Coolalien on 7/7/2016.
 */
public class IntroActivity extends AppIntro{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(new FirstIntro());
        addSlide(new SecondIntro());
        addSlide(new ThirdIntro());
        addSlide(new FourthIntro());
        setProgressButtonEnabled(true);
        setVibrate(true);
        setVibrateIntensity(30);
        setFadeAnimation();
    }

    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        loadMainActivity();}

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    public void getStarted(View v){
        loadMainActivity();
    }


}
