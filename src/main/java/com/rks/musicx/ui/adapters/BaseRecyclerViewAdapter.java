package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Coolalien on 9/12/2016.
 */

public abstract class BaseRecyclerViewAdapter<TData, TViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<TViewHolder>  {

    private final Context context;
    private final LayoutInflater inflater;
    public List<TData> data = new ArrayList<TData>();
    private OnItemClickListener mOnItemClickListener;

    public BaseRecyclerViewAdapter(@NonNull final Context context) {
        this.context = context.getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public BaseRecyclerViewAdapter(@NonNull final Context context, @NonNull List<TData> data) {
        this.context = context.getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        this.data = new ArrayList<>(data);
    }

    protected Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public TData getItem(final int position) throws ArrayIndexOutOfBoundsException {
        return data.get(position);
    }

    public boolean add(TData object) {
        return data.add(object);
    }

    public boolean remove(TData object) {
        return data.remove(object);
    }

    public TData remove(int position) {
        return data.remove(position);
    }

    public void clear() {
        data.clear();
    }

    public boolean addAll(@NonNull Collection<? extends TData> collection) {
        return data.addAll(collection);
    }

    public void addDataList(List<TData> tDataList){
        data = tDataList;
        notifyDataSetChanged();
    }

    public List<TData> getSnapshot() {
        return data;
    }

    protected LayoutInflater getInflater() {
        return inflater;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void triggerOnItemClickListener(int position, View view) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(position, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}
