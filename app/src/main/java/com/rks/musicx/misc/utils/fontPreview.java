package com.rks.musicx.misc.utils;

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use getContext() file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.rks.musicx.R;

/**
 * Created by Coolalien on 6/11/2017.
 */
public class fontPreview extends ListPreference {

    private int mClickedDialogEntryIndex;

    public fontPreview(Context context) {
        super(context);
    }

    public fontPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public fontPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (getEntries() == null || getEntryValues() == null) {
            super.onPrepareDialogBuilder(builder);
            return;
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.preview_font, getEntries()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Typeface tf = null;
                CheckedTextView view = (CheckedTextView) convertView;
                if (view == null) {
                    view = (CheckedTextView) View.inflate(getContext(), R.layout.preview_font, null);
                }
                switch (position) {
                    case 0:
                        tf = Helper.getTypeface(getContext(), "RobotoLight.ttf");
                        break;
                    case 1:
                        tf = Helper.getTypeface(getContext(), "Raleway.ttf");
                        break;
                    case 2:
                        tf = Helper.getTypeface(getContext(), "Knul.otf");
                        break;
                    case 3:
                        tf = Helper.getTypeface(getContext(), "CutiveMono.ttf");
                        break;
                    case 4:
                        tf = Helper.getTypeface(getContext(), "Timber.ttf");
                        break;
                    case 5:
                        tf = Helper.getTypeface(getContext(), "Snippet.ttf");
                        break;
                    case 6:
                        tf = Helper.getTypeface(getContext(), "Trench.ttf");
                        break;
                    case 7:
                        tf = Helper.getTypeface(getContext(), "Monad.otf");
                        break;
                    case 8:
                        tf = Helper.getTypeface(getContext(), "Rex.ttf");
                        break;
                    case 9:
                        tf = Helper.getTypeface(getContext(), "ExodusStriped.otf");
                        break;
                    case 10:
                        tf = Helper.getTypeface(getContext(), "GogiaRegular.otf");
                        break;
                    case 11:
                        tf = Helper.getTypeface(getContext(), "MavenPro.ttf");
                        break;
                    case 12:
                        tf = Helper.getTypeface(getContext(), "Vow.ttf");
                        break;
                    case 13:
                        tf = Helper.getTypeface(getContext(), "Nunito.ttf");
                        break;
                    case 14:
                        tf = Helper.getTypeface(getContext(), "Circled.ttf");
                        break;
                    case 15:
                        tf = Helper.getTypeface(getContext(), "Franks.otf");
                        break;
                    case 16:
                        tf = Helper.getTypeface(getContext(), "Mountain.otf");
                        break;
                    case 17:
                        tf = Helper.getTypeface(getContext(), "Jakarta.ttf");
                        break;
                    case 18:
                        tf = Helper.getTypeface(getContext(), "Abyssopelagic.otf");
                        break;
                    case 19:
                        tf = Helper.getTypeface(getContext(), "Tesla.ttf");
                        break;
                    case 20:
                        tf = Typeface.DEFAULT;
                       break;

                }
                view.setText(getEntries()[position]);
                view.setTypeface(tf, Typeface.NORMAL);
                return view;
            }
        };

        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(adapter, mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;
                dialog.dismiss();
                Extras.getInstance().getmPreferences().edit().putString(Constants.TextFonts, String.valueOf(which)).commit();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String val = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (callChangeListener(val)) {
                setValue(val);
            }
        }
    }
}
