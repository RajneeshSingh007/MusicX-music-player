package com.rks.musicx.ui.adapters;

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
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Coolalien on 6/26/2017.
 */
public class RemoveTabAdapter extends BaseRecyclerViewAdapter<String, RemoveTabAdapter.removeViewholder> implements SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final SimpleItemTouchHelperCallback.OnStartDragListener mDragStartListener;
    private List<Integer> removeList = new ArrayList<>();

    public RemoveTabAdapter(@NonNull Context context, SimpleItemTouchHelperCallback.OnStartDragListener mDragStartListener) {
        super(context);
        this.mDragStartListener = mDragStartListener;
        notifyDataSetChanged();
    }

    @Override
    public RemoveTabAdapter.removeViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_list, parent, false);
        return new removeViewholder(view);
    }

    @Override
    public void onBindViewHolder(RemoveTabAdapter.removeViewholder holder, int position) {
        String name = getItem(position);
        holder.tabName.setTypeface(Helper.getFont(getContext()));
        holder.tabName.setText(name);
        holder.tabName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            holder.tabName.setTextColor(Color.WHITE);
        } else {
            holder.tabName.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return (null != data ? data.size() : 0);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public String getItem(int position) {
        if (data == null || data.size() < 0 || data.size() == 0) {
            return null;
        }
        if (position < data.size() && position >= 0) {
            return data.get(position);
        } else {
            return null;
        }
    }

    @Override
    public void onItemDismiss(int position) {
        data.remove(position);
        removeList.add(position);
        Extras.getInstance().saveRemoveTab(removeList);
        notifyItemChanged(position);
        notifyDataSetChanged();
    }

    public class removeViewholder extends RecyclerView.ViewHolder {

        private TextView tabName;

        public removeViewholder(View itemView) {
            super(itemView);

            tabName = (TextView) itemView.findViewById(R.id.remove_title);
        }
    }
}
