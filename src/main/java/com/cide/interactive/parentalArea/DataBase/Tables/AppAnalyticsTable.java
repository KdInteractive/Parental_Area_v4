package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by lionel on 15/07/14.
 */
public class AppAnalyticsTable {

    public static final String APP_ANALYTICS_TABLE = "app_analytics";
    public static final String APP_ANALYTICS_ID = "_id";
    public static final String APP_ANALYTICS_PACKAGE_NAME = "package_name";
    public static final String APP_ANALYTICS_INSTALL = "install_count";
    public static final String APP_ANALYTICS_UNINSTALL = "uninstall_count";
    private static final String APP_ANALYTICS_RESTORED_FROM_PRELOAD = "restored_from_preload";
    private static final String APP_ANALYTICS_NOT_RESTORED_FROM_PRELOAD = "not_restored_from_preload";

    private static final String DATABASE_CREATE = "create table "
            + APP_ANALYTICS_TABLE
            + '('
            + APP_ANALYTICS_ID + " integer primary key autoincrement,"
            + APP_ANALYTICS_PACKAGE_NAME + " text,"
            + APP_ANALYTICS_INSTALL + " integer default 0,"
            + APP_ANALYTICS_UNINSTALL + " integer default 0"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        //this table is not in the main version
        // so if the version is very old, create the table
        // if the version 
        if (oldVersion < 46) {
            onCreate(database);
        } else {
            if (oldVersion < 49) {
                database.execSQL("ALTER TABLE " + APP_ANALYTICS_TABLE +
                        " DROP " + APP_ANALYTICS_RESTORED_FROM_PRELOAD);
                database.execSQL("ALTER TABLE " + APP_ANALYTICS_TABLE +
                        " DROP " + APP_ANALYTICS_NOT_RESTORED_FROM_PRELOAD);
            }
        }
    }
}
