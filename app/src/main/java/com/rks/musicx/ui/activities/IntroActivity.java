package com.rks.musicx.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.rks.musicx.ui.fragments.Intro.FirstIntro;
import com.rks.musicx.ui.fragments.Intro.FourthIntro;
import com.rks.musicx.ui.fragments.Intro.SecondIntro;
import com.rks.musicx.ui.fragments.Intro.ThirdIntro;

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

public class IntroActivity extends AppIntro {

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

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSkipPressed() {
        loadMainActivity();
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    public void getStarted(View v) {
        loadMainActivity();
    }


}
