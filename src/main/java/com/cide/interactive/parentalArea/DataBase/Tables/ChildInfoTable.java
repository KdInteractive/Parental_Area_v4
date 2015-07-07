package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 12/02/14.
 */
public class ChildInfoTable {

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.CHILDINFO_TABLE
            + '('
            + DBUriManager.CHILDINFO_ID + " integer primary key autoincrement, "
            + DBUriManager.CHILDINFO_UID + " text,"
            + DBUriManager.CHILDINFO_ANALYTICS_UID + " text,"
            + DBUriManager.CHILDINFO_NAME + " text,"
            + DBUriManager.CHILDINFO_AGE + " text,"
            + DBUriManager.CHILDINFO_GENDER + " text,"
            + DBUriManager.CHILDINFO_THEME + " text not null default theme2,"
            + DBUriManager.CHILDINFO_BIRTH + " text,"
            + DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER + " text,"
            + DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION + " text,"
            + DBUriManager.CHILDINFO_TIME_CONTROL_ON + " text,"
            + DBUriManager.CHILDINFO_LOCKED_INTERFACE + " text,"
            + DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION + " text,"
            + DBUriManager.CHILDINFO_INTERNET_ACCESS_ON + " text default \"false\","
            + DBUriManager.CHILDINFO_PROFILE_TYPE + " text,"
            + DBUriManager.CHILDINFO_DEMO_MODE + " text default \"false\","
            + DBUriManager.CHILDINFO_PROFILE_CHANGED + " text default \"false\","
            + DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS + " text, "
            + DBUriManager.CHILDINFO_WEB_LIST_ON + " text default \"false\","
            + DBUriManager.CHILDINFO_FILTER_ON + " text default \"false\""
            + ");";

    public static void onCreate(SQLiteDatabase database) {

        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {

        if (oldVersion < 46) {
            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_PROFILE_CHANGED + " text default \"false\"");
        }

        if (oldVersion < 48) {
            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_ANALYTICS_UID + " text");
        }

        if (oldVersion < 52) {
            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS + " text");
        }

        if (oldVersion < 55) {
            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_INTERNET_ACCESS_ON + " text default \"false\"");

            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_WEB_LIST_ON + " text default \"false\"");

            database.execSQL("ALTER TABLE " + DBUriManager.CHILDINFO_TABLE +
                    " ADD COLUMN " + DBUriManager.CHILDINFO_FILTER_ON + " text default \"false\"");
        }
    }
}
