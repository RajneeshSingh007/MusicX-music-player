package com.rks.musicx.database;

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rks.musicx.interfaces.DefaultColumn;
import com.rks.musicx.misc.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Coolalien on 6/28/2017.
 */
public class SaveQueueDatabase extends SQLiteOpenHelper {

    private String tableName;
    private SQLiteDatabase sqLiteDatabase;

    public SaveQueueDatabase(Context context, String name) {
        super(context, name, null, Constants.DbVersion);
        this.tableName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Constants.DefaultColumn(tableName));

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF NOT EXISTS " + tableName);
        onCreate(sqLiteDatabase);
    }

    public void addQueueName(String queueName) {
        sqLiteDatabase = getWritableDatabase();
        try {
            storeQueueName(sqLiteDatabase, queueName);
        } finally {
            sqLiteDatabase.close();
        }
    }

    private void storeQueueName(SQLiteDatabase sqLiteDatabase, String name) {
        ContentValues values = new ContentValues();
        values.put(DefaultColumn.QueueName, name);
        sqLiteDatabase.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeAll() {
        sqLiteDatabase = getReadableDatabase();
        try {
            sqLiteDatabase.beginTransaction();
            sqLiteDatabase.delete(tableName, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
            sqLiteDatabase.close();
        }
    }

    public List<String> readAll() {
        List<String> QueueList = new ArrayList<>();
        sqLiteDatabase = getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.query(tableName, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int colRead = cursor.getColumnIndex(DefaultColumn.QueueName);
                do {
                    String gotName = cursor.getString(colRead);
                    QueueList.add(gotName);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } finally {
            sqLiteDatabase.close();
        }
        return QueueList;
    }

    public boolean isExist(String queueName) {
        boolean result = false;
        sqLiteDatabase = getReadableDatabase();
        try {
            Cursor cursor = sqLiteDatabase.query(getTableName(), null, DefaultColumn.QueueName + "= ?", new String[]{queueName}, null, null, null, "1");
            if (cursor != null && cursor.moveToNext()) {
                result = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } finally {
            sqLiteDatabase.close();
        }
        return result;
    }

    public void deleteQueueName(String queueName) {
        sqLiteDatabase = getWritableDatabase();
        try {
            sqLiteDatabase.delete(getTableName(), DefaultColumn.QueueName + "= ?", new String[]{queueName});
        } finally {
            sqLiteDatabase.close();
        }
    }

    public String getTableName() {
        return tableName;
    }
}
