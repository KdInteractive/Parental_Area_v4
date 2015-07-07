package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;

import java.util.ArrayList;
import java.util.HashMap;


public class ChildInfoDataAccess {

    private ContentResolver mContentResolver = null;

    public ChildInfoDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }


    public static ArrayList<Integer> getAllUserId(Context context) {

        ContentResolver contentResolver = Utils.getOwnerContext(context).getContentResolver();

        String[] projection = {DBUriManager.CHILDINFO_UID};

        ArrayList<Integer> allUserId = new ArrayList<>();

        Cursor cursor = contentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                allUserId.add(cursor.getInt(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_UID)));
                cursor.moveToNext();
            }

        }
        if (cursor != null) {
            cursor.close();
        }
        return allUserId;
    }

    public static String getAnalyticsUIDForUserId(Context context, int userId) {

        ContentResolver contentResolver = Utils.getOwnerContext(context).getContentResolver();

        String[] projection = {DBUriManager.CHILDINFO_ANALYTICS_UID};
        String where = DBUriManager.CHILDINFO_UID + " = " + userId;

        String analyticsUID = "";

        Cursor cursor = contentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, where, null, null);

        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                analyticsUID = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_ANALYTICS_UID));
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        return analyticsUID;
    }

    public void createChildInDB(int userId, String defaultTheme, String name) {
        ContentValues values = new ContentValues();
        values.put(DBUriManager.CHILDINFO_UID, userId);
        values.put(DBUriManager.CHILDINFO_THEME, defaultTheme);
        values.put(DBUriManager.CHILDINFO_NAME, name);
        mContentResolver.insert(DBUriManager.CONTENT_URI_CHILDINFOS, values);
    }


    public HashMap<String, String> selectAllValuesFromDB(int userId) {
        String selection = DBUriManager.CHILDINFO_UID + " = '" + userId + '\'';
        return getAllValuesFromDB(selection);
    }

    /**
     * load data for child from DB
     *
     * @param selection
     */
    private HashMap<String, String> getAllValuesFromDB(String selection) {

        HashMap<String, String> valuesFromDB = new HashMap<>();

        //informations we want
        String[] projection = {DBUriManager.CHILDINFO_ID, DBUriManager.CHILDINFO_UID, DBUriManager.CHILDINFO_NAME, DBUriManager.CHILDINFO_AGE,
                DBUriManager.CHILDINFO_GENDER, DBUriManager.CHILDINFO_THEME, DBUriManager.CHILDINFO_BIRTH,
                DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER, DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION, DBUriManager.CHILDINFO_TIME_CONTROL_ON,
                DBUriManager.CHILDINFO_FILTER_ON, DBUriManager.CHILDINFO_WEB_LIST_ON, DBUriManager.CHILDINFO_LOCKED_INTERFACE,
                DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION, DBUriManager.CHILDINFO_INTERNET_ACCESS_ON, DBUriManager.CHILDINFO_PROFILE_TYPE,
                DBUriManager.CHILDINFO_DEMO_MODE, DBUriManager.CHILDINFO_PROFILE_CHANGED, DBUriManager.CHILDINFO_ANALYTICS_UID, DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS};

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();

            valuesFromDB.put(DBUriManager.CHILDINFO_GENDER, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_GENDER)));
            valuesFromDB.put(DBUriManager.CHILDINFO_THEME, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_THEME)));
            valuesFromDB.put(DBUriManager.CHILDINFO_NAME, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_NAME)));
            valuesFromDB.put(DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER)));
            valuesFromDB.put(DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION)));
            valuesFromDB.put(DBUriManager.CHILDINFO_TIME_CONTROL_ON, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_TIME_CONTROL_ON)));
            valuesFromDB.put(DBUriManager.CHILDINFO_WEB_LIST_ON, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_WEB_LIST_ON)));
            valuesFromDB.put(DBUriManager.CHILDINFO_FILTER_ON, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_FILTER_ON)));
            valuesFromDB.put(DBUriManager.CHILDINFO_LOCKED_INTERFACE, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_LOCKED_INTERFACE)));
            valuesFromDB.put(DBUriManager.CHILDINFO_BIRTH, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_BIRTH)));
            valuesFromDB.put(DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION)));
            valuesFromDB.put(DBUriManager.CHILDINFO_INTERNET_ACCESS_ON, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_INTERNET_ACCESS_ON)));
            valuesFromDB.put(DBUriManager.CHILDINFO_PROFILE_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_PROFILE_TYPE)));
            valuesFromDB.put(DBUriManager.CHILDINFO_DEMO_MODE, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_DEMO_MODE)));
            valuesFromDB.put(DBUriManager.CHILDINFO_PROFILE_CHANGED, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_PROFILE_CHANGED)));
            valuesFromDB.put(DBUriManager.CHILDINFO_ANALYTICS_UID, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_ANALYTICS_UID)));
            valuesFromDB.put(DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS, cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS)));
        }

        if (cursor != null) {
            cursor.close();
        }

        return valuesFromDB;
    }


    //called to save data in db
    public void saveDataInDB(HashMap<String, String> valuesForDB, int userId) {
        String where = DBUriManager.CHILDINFO_UID + " = '" + userId + '\'';

        ContentValues values = new ContentValues();

        for (String key : valuesForDB.keySet()) {
            values.put(key, valuesForDB.get(key));
        }

        mContentResolver.update(DBUriManager.CONTENT_URI_CHILDINFOS, values, where, null);
    }

    public void deleteUser(int userId) {

        String where = DBUriManager.CHILDINFO_UID + " = '" + userId + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_CHILDINFOS, where, null);
        deleteActivityStatusForUser(userId);
    }


    public void savePackageStatusInDB(ArrayList<ResolveInfo> packageNamesThatNeedToBeEnabled,
                                      ArrayList<ResolveInfo> packageNamesThatNeedToBeDisabled, int userId) {
        ContentValues values;

        String[] projection = new String[]{DBUriManager.ACTIVITY_STATUS_ENABLED};
        String whereClause = DBUriManager.ACTIVITY_STATUS_UID + " = ? "
                + " and " + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME + " = ? "
                + " and " + DBUriManager.ACTIVITY_STATUS_NAME + " = ? ";
        String[] whereParams;

        for (ResolveInfo ri : packageNamesThatNeedToBeEnabled) {
            whereParams = new String[]{String.valueOf(userId), ri.activityInfo.packageName, ri.activityInfo.name};
            Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, whereClause, whereParams, null);

            values = new ContentValues();
            values.put(DBUriManager.ACTIVITY_STATUS_ENABLED, Boolean.toString(true));

            if (cursor != null && cursor.moveToFirst()) {
                if (!Boolean.parseBoolean(cursor.getString(0))) {
                    mContentResolver.update(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, values, whereClause, whereParams);
                }
            } else {
                values.put(DBUriManager.ACTIVITY_STATUS_UID, userId);
                values.put(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME, ri.activityInfo.packageName);
                values.put(DBUriManager.ACTIVITY_STATUS_NAME, ri.activityInfo.name);
                mContentResolver.insert(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, values);
            }

            if (cursor != null) {
                cursor.close();
            }
        }

        for (ResolveInfo ri : packageNamesThatNeedToBeDisabled) {
            whereParams = new String[]{String.valueOf(userId), ri.activityInfo.packageName, ri.activityInfo.name};

            Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, whereClause, whereParams, null);

            values = new ContentValues();
            values.put(DBUriManager.ACTIVITY_STATUS_ENABLED, Boolean.toString(false));

            if (cursor != null && cursor.moveToFirst()) {
                if (Boolean.parseBoolean(cursor.getString(0))) {
                    mContentResolver.update(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, values, whereClause, whereParams);
                }
            } else {
                values.put(DBUriManager.ACTIVITY_STATUS_UID, userId);
                values.put(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME, ri.activityInfo.packageName);
                values.put(DBUriManager.ACTIVITY_STATUS_NAME, ri.activityInfo.name);
                mContentResolver.insert(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, values);
            }

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteActivityStatusForUser(int userId) {
        //delete all reference to the user
        String whereClause = DBUriManager.ACTIVITY_STATUS_UID + " = '" + userId + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, whereClause, null);
    }

    public void duplicateActivityStatusFromUserToUser(int otherChildId, int toUserId) {
        deleteActivityStatusForUser(toUserId);

        String[] projection = new String[]{DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME,
                DBUriManager.ACTIVITY_STATUS_NAME,
                DBUriManager.ACTIVITY_STATUS_ENABLED,
                DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY};
        String whereClause = DBUriManager.ACTIVITY_STATUS_UID + " = " + otherChildId;
        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, whereClause, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int indexPackageName = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME);
                int indexActivityName = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_NAME);
                int indexEnabled = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_ENABLED);
                int indexLastKnowCategory = cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY);

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBUriManager.ACTIVITY_STATUS_UID, toUserId);
                do {
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME, cursor.getString(indexPackageName));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_NAME, cursor.getString(indexActivityName));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_ENABLED, cursor.getString(indexEnabled));
                    contentValues.put(DBUriManager.ACTIVITY_STATUS_LAST_KNOW_CATEGORY, cursor.getString(indexLastKnowCategory));
                    mContentResolver.insert(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, contentValues);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    public ArrayList<String> getActivityStatusPackageName(int userId, boolean enabledActivity) {
        ArrayList<String> activityStatus = new ArrayList<>();
        String[] projection = new String[]{
                DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME
        };

        String where = DBUriManager.ACTIVITY_STATUS_UID + " = '" + userId + "' and " + DBUriManager.ACTIVITY_STATUS_ENABLED + " = '" + enabledActivity + "'";

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, where, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                String appKey = cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME));
                activityStatus.add(appKey);
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return activityStatus;
    }

    public int getChildCount() {
        int count = 0;
        String[] projection = {DBUriManager.CHILDINFO_ID, DBUriManager.CHILDINFO_UID};

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, null, null, null);

        if (cursor != null) {
            count = cursor.getCount();
        }

        if (cursor != null) {
            cursor.close();
        }

        return count;
    }
}
