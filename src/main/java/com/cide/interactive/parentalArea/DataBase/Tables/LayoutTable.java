package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 28/02/14.
 */
public class LayoutTable {

    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.LAYOUT_TABLE
            + '('
            + DBUriManager.LAYOUT_ID + " integer primary key autoincrement,"
            + DBUriManager.LAYOUT_USER_ID + " int,"
            + DBUriManager.LAYOUT_PACKAGE_NAME + " text,"
            + DBUriManager.LAYOUT_ACTIVITY_NAME + " text,"
            + DBUriManager.LAYOUT_FOLDER_ID + " int,"
            + DBUriManager.LAYOUT_CATEGORY_ID + " text,"
            + DBUriManager.LAYOUT_POSITION_X + " integer,"
            + DBUriManager.LAYOUT_POSITION_Y + " integer"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {

        switch (oldVersion) {
            case 45:
                break;
        }
    }

}
