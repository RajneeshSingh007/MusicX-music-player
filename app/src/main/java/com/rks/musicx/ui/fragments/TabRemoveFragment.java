package com.rks.musicx.ui.fragments;

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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.ui.adapters.RemoveTabAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Coolalien on 6/26/2017.
 */
public class TabRemoveFragment extends android.app.DialogFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {

    private RemoveTabAdapter tabAdapter;
    private RecyclerView rv;
    private Button btn;
    private ItemTouchHelper mItemTouchHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.playlist_picker, new LinearLayout(getActivity()), false);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        btn = (Button) view.findViewById(R.id.create_playlist);
        btn.setVisibility(View.GONE);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getActivity());
        customlayoutmanager.setOrientation(LinearLayoutManager.VERTICAL);
        customlayoutmanager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customlayoutmanager);
        tabAdapter = new RemoveTabAdapter(getActivity(), this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(tabAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(rv);
        List<String> data = new ArrayList<String>();
        data.add("1. Recent");
        data.add("2. Folder");
        data.add("3. Song");
        data.add("4. Album");
        data.add("5. Artist");
        data.add("6.Playlist");
        tabAdapter.addDataList(data);
        rv.setAdapter(tabAdapter);
        builder.title("Tab Remove")
                .autoDismiss(true)
                .positiveText(R.string.okay)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .customView(view, false);
        return builder.show();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


}
