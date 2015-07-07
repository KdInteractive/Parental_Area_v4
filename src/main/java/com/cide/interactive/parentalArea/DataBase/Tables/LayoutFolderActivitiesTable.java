package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by alexandre on 18/03/14.
 */
public class LayoutFolderActivitiesTable {

    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.LAYOUT_FOLDER_ACTIVITIES_TABLE
            + '('
            + DBUriManager.LAYOUT_FOLDER_ID + " int,"
            + DBUriManager.LAYOUT_FOLDER_ACTIVITIES_PACKAGE_NAME + " text, "
            + DBUriManager.LAYOUT_FOLDER_ACTIVITIES_ACTIVITY_NAME + " text, "
            + DBUriManager.LAYOUT_FOLDER_ACTIVITIES_POSITION_X + " int"
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
