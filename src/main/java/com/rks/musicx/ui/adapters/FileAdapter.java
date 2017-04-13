package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class FileAdapter extends BaseRecyclerViewAdapter<File, FileAdapter.Fileviewholder> implements FastScrollRecyclerView.SectionedAdapter {

    // private FileModelComparator fileModelComparator = new FileModelComparator();

    public FileAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    public FileAdapter.Fileviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.folder_list, parent, false);
        return new Fileviewholder(rootView);
    }

    @Override
    public void onBindViewHolder(FileAdapter.Fileviewholder holder, int position) {
        File file = getItem(position);
        //Collections.sort(data, fileModelComparator);
        if (!TextUtils.isEmpty(Extras.getInstance().getFolderPath())) {
            String path = Extras.getInstance().getFolderPath();
            String foldername = Extras.getInstance().getFolderPath().substring(path.lastIndexOf("/") + 1);
            if (Extras.getInstance().gettrackFolderpath()) {
                holder.filename.setText(foldername);
            }
        }
        holder.thumbnail.setImageResource(R.drawable.folder);
        if (!Extras.getInstance().gettrackFolderpath()) {
            holder.filename.setText(file.getName());
        }
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            holder.filename.setTextColor(Color.WHITE);
        } else {
            holder.filename.setTextColor(Color.BLACK);
        }
    }

    @Override
    public File getItem(int position) throws ArrayIndexOutOfBoundsException {
        return data.size() > 0 ? data.get(position) : null;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getName().substring(0, 1);
    }

    public class Fileviewholder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView thumbnail;
        private TextView filename;

        public Fileviewholder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            filename = (TextView) itemView.findViewById(R.id.filename);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.folder_view).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
        }
    }

    /*public class FileModelComparator implements Comparator<File> {

        @Override
        public int compare(File file, File t1) {
            if (file.equals(t1)) {
                return 0;
            }

            if (file.isDirectory() && t1.isFile()) {
                // show directories above files
                return -1;
            }

            if (file.isFile() && t1.isDirectory()) {
                // show files below directories
                return 1;
            }

            // sort alphabetically, ignoring case
            return file.getName().compareToIgnoreCase(t1.getName());
        }
    }*/
}
