package com.cide.interactive.parentalArea.DataBase.Tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.FileUtils;
import com.cide.interactive.parentalArea.R;

import java.util.ArrayList;

/**
 * Created by lionel on 28/02/14.
 */
public class AppCategoryTable {

    //AppCategoryActivity Strings
    private static final int INDEX_IN_APP_CATEGORY_STRING_PACKAGENAME = 0;
    private static final int INDEX_IN_APP_CATEGORY_STRING_ACTIVITYNAME = 1;
    private static final int INDEX_IN_APP_CATEGORY_STRING_CATEGORY_ID = 2;

    private static final String DATABASE_CREATE = "create table "
            + DBUriManager.APP_CATEGORY_TABLE
            + '('
            + DBUriManager.APP_CATEGORY_PACKAGE_NAME + " text,"
            + DBUriManager.APP_CATEGORY_ACTIVITY_NAME + " text,"
            + DBUriManager.APP_CATEGORY_ID + " text,"
            + DBUriManager.APP_CATEGORY_IS_NEW_INSTALL + " text,"
            + " UNIQUE (" + DBUriManager.APP_CATEGORY_PACKAGE_NAME + ", " + DBUriManager.APP_CATEGORY_ACTIVITY_NAME + ") ON CONFLICT REPLACE"
            + ");";

    public static void onCreate(SQLiteDatabase database, Context context) {
        database.execSQL(DATABASE_CREATE);
        ArrayList<String> appCategory = FileUtils.getTextFileAsArray(context, R.raw.appcategory);
        String[] splitedLine;
        ContentValues contentValues = new ContentValues();
        for (String line : appCategory) {

            splitedLine = line.split("@");
            contentValues.put(DBUriManager.APP_CATEGORY_ID, splitedLine[INDEX_IN_APP_CATEGORY_STRING_CATEGORY_ID]);
            contentValues.put(DBUriManager.APP_CATEGORY_PACKAGE_NAME, splitedLine[INDEX_IN_APP_CATEGORY_STRING_PACKAGENAME]);
            contentValues.put(DBUriManager.APP_CATEGORY_ACTIVITY_NAME, splitedLine[INDEX_IN_APP_CATEGORY_STRING_ACTIVITYNAME]);
            contentValues.put(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL, "false");
            database.insert(DBUriManager.APP_CATEGORY_TABLE, null, contentValues);

        }

    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (oldVersion < 54) {
            database.execSQL("ALTER TABLE " + DBUriManager.APP_CATEGORY_TABLE +
                    " ADD COLUMN " + DBUriManager.APP_CATEGORY_IS_NEW_INSTALL + " text default 'false'");
        }
    }
}
