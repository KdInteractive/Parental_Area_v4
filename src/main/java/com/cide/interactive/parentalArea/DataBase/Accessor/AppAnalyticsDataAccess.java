package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Analytics.AppAnalyticsInfo;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;

import java.util.ArrayList;


/**
 * Created by lionel on 11/03/14.
 */
public class AppAnalyticsDataAccess {

    private ContentResolver mContentResolver;

    public AppAnalyticsDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    //return the numeric value for the specific field
    public int getValueForKey(String packageName, String key) {
        int value = -1;
        String[] projection = {key};
        String where = AppAnalyticsTable.APP_ANALYTICS_PACKAGE_NAME + " ='" + packageName + '\'';
        Cursor cursor;
        cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_ANALYTICS, projection, where, null, null);

        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(key));
        }

        if (cursor != null) {
            cursor.close();
        }

        return value;
    }

    public void increaseValueForKey(String packageName, String key) {
        ContentValues values = new ContentValues();
        int currentValue = getValueForKey(packageName, key);
        if (currentValue == -1) {
            createNewApp(packageName);
            currentValue = 1;
        } else {
            currentValue++;
        }

        values.put(key, currentValue);

        String where = AppAnalyticsTable.APP_ANALYTICS_PACKAGE_NAME + " = '" + packageName + '\'';

        mContentResolver.update(DBUriManager.CONTENT_URI_APP_ANALYTICS, values, where, null);
    }

    private void createNewApp(String packageName) {
        ContentValues values = new ContentValues();
        values.put(AppAnalyticsTable.APP_ANALYTICS_PACKAGE_NAME, packageName);
        mContentResolver.insert(DBUriManager.CONTENT_URI_APP_ANALYTICS, values);
    }

    public ArrayList<AppAnalyticsInfo> getAllValues() {
        ArrayList<AppAnalyticsInfo> infos = new ArrayList<>();
        String[] projection = {AppAnalyticsTable.APP_ANALYTICS_PACKAGE_NAME, AppAnalyticsTable.APP_ANALYTICS_INSTALL, AppAnalyticsTable.APP_ANALYTICS_UNINSTALL};

        Cursor cursor;

        cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_ANALYTICS, projection, null, null, null);

        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                AppAnalyticsInfo info = new AppAnalyticsInfo();
                info.setSerial(Utils.getSerialId());
                info.setPackageName(cursor.getString(cursor.getColumnIndex(AppAnalyticsTable.APP_ANALYTICS_PACKAGE_NAME)));
                info.setInstallCount(cursor.getInt(cursor.getColumnIndex(AppAnalyticsTable.APP_ANALYTICS_INSTALL)));
                info.setUninstallCount(cursor.getInt(cursor.getColumnIndex(AppAnalyticsTable.APP_ANALYTICS_UNINSTALL)));
                infos.add(info);

                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return infos;
    }

    public void resetValues() {
        ContentValues values = new ContentValues();
        values.put(AppAnalyticsTable.APP_ANALYTICS_INSTALL, 0);
        values.put(AppAnalyticsTable.APP_ANALYTICS_UNINSTALL, 0);

        mContentResolver.update(DBUriManager.CONTENT_URI_APP_ANALYTICS, values, null, null);
    }

}
