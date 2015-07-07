package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;

/**
 * Created by lionel on 11/03/14.
 */
public class UpdatedValuesTable {

    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.UPDATED_VALUES_TABLE
            + '('
            + DBUriManager.UPDATED_VALUES_KEY + " text,"
            + DBUriManager.UPDATED_VALUES_VALUE + " text,"
            + DBUriManager.UPDATED_VALUES_USER_ID + " text, "
            + " UNIQUE (" + DBUriManager.UPDATED_VALUES_KEY + ", " + DBUriManager.UPDATED_VALUES_USER_ID + ") ON CONFLICT REPLACE"
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
