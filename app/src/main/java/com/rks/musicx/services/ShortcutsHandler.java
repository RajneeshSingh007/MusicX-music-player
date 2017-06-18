package com.rks.musicx.services;

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

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.ui.activities.ShortcutActivity;

import java.util.ArrayList;

import static com.rks.musicx.misc.utils.Constants.SHORTCUTS_TYPES;

/**
 * Created by Coolalien on 6/4/2017.
 */
public class ShortcutsHandler {

    /**
     * Shortucts features on android N
     * @param context
     */
    public static void create(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            Icon pause = Icon.createWithResource(context, R.drawable.ic_shortcut_aw_ic_pause);
            ShortcutInfo pauses = new ShortcutInfo.Builder(context, Constants.PAUSE_SHORTCUTS)
                    .setShortLabel("Pause")
                    .setIcon(pause)
                    .setIntent(shortcut(context,2))
                    .build();

            Icon play = Icon.createWithResource(context, R.drawable.ic_shortcut_aw_ic_play);
            ShortcutInfo plays = new ShortcutInfo.Builder(context, Constants.PLAY_SHORTCUTS)
                    .setShortLabel("Play")
                    .setIcon(play)
                    .setIntent(shortcut(context, 1))
                    .build();
            ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
            shortcuts.add(plays);
            shortcuts.add(pauses);
            shortcutManager.setDynamicShortcuts(shortcuts);
        }
    }

    
    public static Intent shortcut(Context context, final int type) {
        Intent intent1 = new Intent(context, ShortcutActivity.class);
        intent1.setAction(Intent.ACTION_VIEW);
        Bundle bundle = new Bundle();
        bundle.putInt(SHORTCUTS_TYPES, type);
        intent1.putExtras(bundle);
        return intent1;
    }
}
