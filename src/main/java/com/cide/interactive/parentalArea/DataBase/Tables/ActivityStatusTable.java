package com.cide.interactive.parentalArea.DataBase.Tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 12/02/14.
 */
public class ActivityStatusTable {

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.ACTIVITY_STATUS_TABLE
            + '('
            + DBUriManager.ACTIVITY_STATUS_ID + " integer primary key autoincrement, "
            + DBUriManager.ACTIVITY_STATUS_UID + " text,"
            + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME + " text,"
            + DBUriManager.ACTIVITY_STATUS_NAME + " text,"
            + DBUriManager.ACTIVITY_STATUS_ENABLED + " text, "
            + DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT + " int default 0, "
            + DBUriManager.ACTIVITY_STATUS_TIME_SPENT + " int default 0,"
            + DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY + " text"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 47) {
            database.execSQL("ALTER TABLE " + DBUriManager.ACTIVITY_STATUS_TABLE + " RENAME TO " + DBUriManager.ACTIVITY_STATUS_TABLE + "_old");

            onCreate(database);

            String[] projection = new String[]{DBUriManager.ACTIVITY_STATUS_UID,
                    DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME,
                    DBUriManager.ACTIVITY_STATUS_NAME,
                    DBUriManager.ACTIVITY_STATUS_ENABLED,
                    DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY};

            Cursor cursor = database.query(DBUriManager.ACTIVITY_STATUS_TABLE + "_old", projection, "", null, "", "", "");
            if (cursor != null && cursor.moveToFirst()) {
                int indexUserId = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_UID);
                int indexPackageName = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME);
                int indexActivityName = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_NAME);
                int indexEnabled = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_ENABLED);
                int indexKnowCategory = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY);
                ContentValues contentValues = new ContentValues();
                while (!cursor.isAfterLast()) {
                    contentValues.clear();
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_UID, cursor.getString(indexUserId));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME, cursor.getString(indexPackageName));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_NAME, cursor.getString(indexActivityName));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_ENABLED, cursor.getString(indexEnabled));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY, cursor.getString(indexKnowCategory));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT, 0);
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_TIME_SPENT, 0);

                    database.insert(DBUriManager.ACTIVITY_STATUS_TABLE, null, contentValues);

                    cursor.moveToNext();
                }
            }
            if(cursor != null) {
                cursor.close();
            }

            database.execSQL("DROP TABLE " + DBUriManager.ACTIVITY_STATUS_TABLE + "_old");
        }
    }

}
