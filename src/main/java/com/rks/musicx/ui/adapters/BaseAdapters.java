package com.rks.musicx.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Coolalien on 10/20/2016.
 */

public abstract class BaseAdapters <V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V>{

    private OnItemClickListener onItemClick;

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        onItemClick = listener;
    }

    void triggerOnItemClickListener(int position, View view)
    {
        if(onItemClick != null)
        {
            onItemClick.onItemClick(position, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}
