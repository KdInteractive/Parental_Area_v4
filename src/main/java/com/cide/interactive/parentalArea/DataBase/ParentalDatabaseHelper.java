package com.cide.interactive.parentalArea.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cide.interactive.parentalArea.DataBase.Tables.ActivityStatusTable;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;
import com.cide.interactive.parentalArea.DataBase.Tables.AppCategoryTable;
import com.cide.interactive.parentalArea.DataBase.Tables.ChildInfoTable;
import com.cide.interactive.parentalArea.DataBase.Tables.LayoutFolderActivitiesTable;
import com.cide.interactive.parentalArea.DataBase.Tables.LayoutFolderTable;
import com.cide.interactive.parentalArea.DataBase.Tables.LayoutTable;
import com.cide.interactive.parentalArea.DataBase.Tables.ParentPreferencesTable;
import com.cide.interactive.parentalArea.DataBase.Tables.TimeSlotSettingsTable;
import com.cide.interactive.parentalArea.DataBase.Tables.ToAddToChildLayoutTable;
import com.cide.interactive.parentalArea.DataBase.Tables.UpdatedValuesTable;
import com.cide.interactive.parentalArea.DataBase.Tables.WebListTable;

/**
 * Created by lionel on 12/02/14.
 */
public class ParentalDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String DATABASE_NAME = "/data/system/parental.db";
    private static final int DATABASE_VERSION = 57;

    public ParentalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ChildInfoTable.onCreate(db);
        ActivityStatusTable.onCreate(db);
        ToAddToChildLayoutTable.onCreate(db);
        LayoutTable.onCreate(db);
        LayoutFolderTable.onCreate(db);
        LayoutFolderActivitiesTable.onCreate(db);
        AppCategoryTable.onCreate(db, mContext);
        ParentPreferencesTable.onCreate(db);
        UpdatedValuesTable.onCreate(db);
        AppAnalyticsTable.onCreate(db);
        TimeSlotSettingsTable.onCreate(db);
        WebListTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ChildInfoTable.onUpgrade(db, oldVersion, newVersion);
        ActivityStatusTable.onUpgrade(db, oldVersion, newVersion);
        ToAddToChildLayoutTable.onUpgrade(db, oldVersion, newVersion);
        LayoutTable.onUpgrade(db, oldVersion, newVersion);
        LayoutFolderTable.onUpgrade(db, oldVersion, newVersion);
        LayoutFolderActivitiesTable.onUpgrade(db, oldVersion, newVersion);
        AppCategoryTable.onUpgrade(db, oldVersion, newVersion);
        ParentPreferencesTable.onUpgrade(db, oldVersion, newVersion);
        UpdatedValuesTable.onUpgrade(db, oldVersion, newVersion);
        AppAnalyticsTable.onUpgrade(db, oldVersion, newVersion);
        TimeSlotSettingsTable.onUpgrade(db, oldVersion, newVersion);
        WebListTable.onUpgrade(db, oldVersion, newVersion);
    }
}