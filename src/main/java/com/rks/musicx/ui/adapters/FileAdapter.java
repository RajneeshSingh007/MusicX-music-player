package com.rks.musicx.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.data.model.FolderModel;
import com.rks.musicx.misc.utils.Extras;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;


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

public class FileAdapter extends BaseRecyclerViewAdapter<FolderModel, FileAdapter.Fileviewholder> implements FastScrollRecyclerView.SectionedAdapter {

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
        FolderModel file = getItem(position);
        holder.thumbnail.setImageResource(R.drawable.folder);
        holder.filename.setText(file.getName());
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            holder.filename.setTextColor(Color.WHITE);
        } else {
            holder.filename.setTextColor(Color.BLACK);
        }
    }

    @Override
    public FolderModel getItem(int position) throws ArrayIndexOutOfBoundsException {
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
}
