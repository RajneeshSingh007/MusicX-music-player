package com.rks.musicx.data.loaders;


import android.content.Context;
import android.util.Log;

import com.rks.musicx.data.model.FolderModel;
import com.rks.musicx.misc.utils.permissionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public class FolderLoader extends BaseAsyncTaskLoader<List<FolderModel>>{

    private FolderModel folderModels;

    public FolderLoader(Context context, FolderModel folderModel) {
        super(context);
        folderModels = folderModel;
    }

    @Override
    public List<FolderModel> loadInBackground() {
        List<FolderModel> files = new ArrayList<>();
        if (permissionManager.isExternalReadStorageGranted(getContext())) {
            files = folderModels.listFilesSorted();
            return files;
        } else {
            Log.d("Folder", "Permission not granted");
            return Collections.emptyList();
        }
    }


}
