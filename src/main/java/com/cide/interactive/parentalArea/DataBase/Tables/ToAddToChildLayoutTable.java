package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 28/02/14.
 */
public class ToAddToChildLayoutTable {

    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_TABLE
            + '('
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_USER_ID + " int,"
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_PACKAGE_NAME + " text,"
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_ACTIVITY_NAME + " text,"
            + " UNIQUE (" + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_USER_ID + ", "
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_ACTIVITY_NAME + ", "
            + DBUriManager.TO_ADD_TO_CHILD_LAYOUT_PACKAGE_NAME + ") ON CONFLICT REPLACE"
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
