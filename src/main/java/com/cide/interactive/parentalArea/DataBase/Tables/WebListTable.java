package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by lionel on 12/01/15.
 */
public class WebListTable {

    public static final String WEB_LIST_TABLE = "web_list";
    public static final String WEB_LIST_ID = "_id";
    public static final String WEB_LIST_URL = "url";
    public static final String WEB_LIST_USER_ID = "user_id";
    public static final String WEB_LIST_ENABLED = "enabled";
    public static final String WEB_LIST_BOOKMARKED = "bookmarked";
    public static final String WEB_LIST_IS_WHITE_LIST = "is_white_list";
    public static final String WEB_LIST_ID_BIB = "id_bookmark_in_browser";


    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + WEB_LIST_TABLE
            + '('
            + WEB_LIST_ID + " integer primary key autoincrement, "
            + WEB_LIST_URL + " text,"
            + WEB_LIST_USER_ID + " text,"
            + WEB_LIST_ENABLED + " text,"
            + WEB_LIST_IS_WHITE_LIST + " text,"
            + WEB_LIST_BOOKMARKED + " text,"
            + WEB_LIST_ID_BIB + " integer"
            + ");";

    public static void onCreate(SQLiteDatabase database) {

        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (oldVersion < 57) {
            onCreate(database);
        }

    }

}
