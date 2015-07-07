package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 11/03/14.
 */
public class ParentPreferencesTable {


    /**
     * PARENT PREFERENCES
     * for this table, we can set a key and an associate value
     * exemple : we can store email; key:GlobalSetting.EMAIL_KEY (email), value:popo@popo.com
     * or a pin pwd : key:GlobalSetting.PIN_CODE_KEY(pincode), value:2472
     */

    public static final String PARENT_PREFERENCES_TABLE = "parentpreferences";
    public static final String PARENT_PREFERENCES_ID = "_id";
    public static final String PARENT_PREFERENCES_SAVED_KEY = "key";
    public static final String PARENT_PREFERENCES_SAVED_VALUE = "value";

    private static final String DATABASE_CREATE = "create table "
            + PARENT_PREFERENCES_TABLE
            + '('
            + PARENT_PREFERENCES_ID + " integer primary key autoincrement,"
            + PARENT_PREFERENCES_SAVED_KEY + " text,"
            + PARENT_PREFERENCES_SAVED_VALUE + " text"
            + ");";


    public static void onCreate(SQLiteDatabase database) {

        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {

        if (oldVersion < 46) {
            //update ParentPreference table to send analytics data when we update the DB
            database.execSQL("INSERT INTO " + PARENT_PREFERENCES_TABLE +
                    "( " + PARENT_PREFERENCES_SAVED_KEY + ',' +
                    PARENT_PREFERENCES_SAVED_VALUE + " )" +
                    " VALUES ( '" + DBUriManager.ACTIVITY_STATUS_TABLE + "', " +
                    '\'' + String.valueOf(true) + "')");

            database.execSQL("INSERT INTO " + PARENT_PREFERENCES_TABLE +
                    "( " + PARENT_PREFERENCES_SAVED_KEY + ',' +
                    PARENT_PREFERENCES_SAVED_VALUE + " )" +
                    " VALUES ( '" + AppAnalyticsTable.APP_ANALYTICS_TABLE + "', " +
                    '\'' + String.valueOf(true) + "')");

            database.execSQL("INSERT INTO " + PARENT_PREFERENCES_TABLE +
                    "( " + PARENT_PREFERENCES_SAVED_KEY + ',' +
                    PARENT_PREFERENCES_SAVED_VALUE + " )" +
                    " VALUES ( '" + DBUriManager.CHILDINFO_TABLE + "', " +
                    '\'' + String.valueOf(true) + "')");
        }
    }
}
