package com.cide.interactive.parentalArea.Analytics;

import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.UserHandle;

import com.android.internal.widget.LockPatternUtils;
import com.cide.interactive.kuriolib.ChildInfo;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppAnalyticsDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by lionel on 11/07/14.
 */

//used to store specific data in db
// used to calculate and format data to send them

public class AnalyticsManager {

    private static AnalyticsManager mInstance;

    public static AnalyticsManager getInstance() {
        if (mInstance == null) {
            mInstance = new AnalyticsManager();
        }
        return mInstance;
    }

    private AnalyticsManager() {

    }

    public void setChildProfileChanged(ContentResolver contentResolver, int userId) {

        String where = DBUriManager.CHILDINFO_UID + " = '" + userId + '\'';

        ContentValues values = new ContentValues();
        values.put(DBUriManager.CHILDINFO_PROFILE_CHANGED, Boolean.toString(true));
        contentResolver.update(DBUriManager.CONTENT_URI_CHILDINFOS, values, where, null);
    }

    public void increaseUsage(Context context, String usageKey) {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
        String value = parentPreferencesDataAccess.getParentPreferencesForKey(usageKey);
        if (value == null) {
            value = "1";
        } else {
            value = String.valueOf(Integer.valueOf(value) + 1);
        }

        parentPreferencesDataAccess.setParentPreferences(usageKey, value);
    }

    public void timeManagementUsedOnce(Context context) {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
        parentPreferencesDataAccess.setParentPreferences(Kurio.ANALYTICS_TIME_CONTROL_USED_ONCE, "true");
    }

    public void increaseAppAnalyticsValueForKey(Context context, String packageName, String key) {
        AppAnalyticsDataAccess appAnalyticsDataAccess = new AppAnalyticsDataAccess(context);
        appAnalyticsDataAccess.increaseValueForKey(packageName, key);

    }

    public void setNeedToSendAnalytics(Context context, boolean value, String tableToUpdate) {

        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
        parentPreferencesDataAccess.setParentPreferences(tableToUpdate, String.valueOf(value));

    }

    public boolean isChildProtectedByPassword(Context context, int userId) {
        LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
        lockPatternUtils.setCurrentUser(userId);
        boolean isPasswordSet = true;

        if (lockPatternUtils.getKeyguardStoredPasswordQuality() == 0 || (lockPatternUtils.getKeyguardStoredPasswordQuality() == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING
                && (!lockPatternUtils.isLockPatternEnabled() || !lockPatternUtils.savedPatternExists()))) {
            isPasswordSet = false;
        }
        lockPatternUtils.setCurrentUser(UserHandle.USER_NULL);

        return isPasswordSet;
    }

    public boolean needToUpdateAnalytics(Context context, String tableToCheck) {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
        String value = parentPreferencesDataAccess.getParentPreferencesForKey(tableToCheck);
        if (value == null) {
            value = "false";
        }
        return Boolean.valueOf(value);
    }

    public Bundle getAnalyticsFromParentPreferences(Context context) {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
        Bundle b = new Bundle();
        String value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_APP_MANAGEMENT_COUNT);
        if (value == null) {
            value = "0";
        }
        b.putString(Kurio.ANALYTICS_APP_MANAGEMENT_COUNT, value);

        value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_FAQ_COUNT);
        if (value == null) {
            value = "0";
        }
        b.putString(Kurio.ANALYTICS_FAQ_COUNT, value);

        value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_CONTACT_US_COUNT);
        if (value == null) {
            value = "0";
        }
        b.putString(Kurio.ANALYTICS_CONTACT_US_COUNT, value);

        value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_USER_MANUAL_COUNT);
        if (value == null) {
            value = "0";
        }
        b.putString(Kurio.ANALYTICS_USER_MANUAL_COUNT, value);

        value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_TIME_CONTROL_USED_ONCE);
        if (value == null) {
            value = "false";
        }
        b.putString(Kurio.ANALYTICS_TIME_CONTROL_USED_ONCE, value);

        value = parentPreferencesDataAccess.getParentPreferencesForKey(Kurio.ANALYTICS_LAST_PARENT_LOG);
        if (value == null) {
            value = "";
        }
        b.putString(Kurio.ANALYTICS_LAST_PARENT_LOG, "2015-05-18");

        return b;
    }

    //return json formatted string
    public String getActivityStatusAnalytics(Context context) {

        Gson gson = new GsonBuilder().create();

        ArrayList<ActivityStatusInfo> infos = getActivityStatusForUsers(context);

        String infosToString = null;
        if (infos.size() > 0) {
            infosToString = gson.toJson(infos);
        }

        return infosToString;
    }

    private ArrayList<ActivityStatusInfo> getActivityStatusForUsers(Context context) {
        ArrayList<ActivityStatusInfo> infos = new ArrayList<>();

        Cursor cursor;

        for (int userId : ChildInfoDataAccess.getAllUserId(context)) {

            String[] projection = {DBUriManager.ACTIVITY_STATUS_UID, DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME,
                    DBUriManager.ACTIVITY_STATUS_NAME, DBUriManager.ACTIVITY_STATUS_ENABLED, DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT,
                    DBUriManager.ACTIVITY_STATUS_TIME_SPENT};

            String where = DBUriManager.ACTIVITY_STATUS_UID + "='" + userId + '\'';

            cursor = context.getContentResolver().query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, where, null, null);

            if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    ActivityStatusInfo info = new ActivityStatusInfo();
                    info.setSerial(Utils.getSerialId());
                    String analyticsUID = ChildInfoDataAccess.getAnalyticsUIDForUserId(context, userId);
                    info.setAnalyticsUID(analyticsUID);
                    info.setActivityName(cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_NAME)));
                    info.setPackageName(cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME)));
                    info.setEnabled(cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_ENABLED)));
                    String launchCount = cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT));
                    if (launchCount == null) {
                        launchCount = "0";
                    }
                    info.setLaunchCount(Integer.valueOf(launchCount));

                    String timeSpent = cursor.getString(cursor.getColumnIndex(DBUriManager.ACTIVITY_STATUS_TIME_SPENT));
                    if (timeSpent == null) {
                        timeSpent = "0";
                    }
                    info.setTimeSpent(Integer.valueOf(timeSpent));

                    infos.add(info);
                    cursor.moveToNext();
                }
            }

            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }

    //return json formatted string
    public String getAppAnalytics(Context context) {

        AppAnalyticsDataAccess appAnalyticsDataAccess = new AppAnalyticsDataAccess(context);
        ArrayList<AppAnalyticsInfo> infos = appAnalyticsDataAccess.getAllValues();
        Gson gson = new GsonBuilder().create();
        String infosToString = null;
        if (infos.size() > 0) {
            infosToString = gson.toJson(infos);
        }

        return infosToString;
    }

    public Bundle getChildTimeControlAnalytics(ChildInfo childInfo) {
        Bundle b = new Bundle();

//        //if time control is on we check values of others functionnalities
//        if (childInfo.isTimeControlOn()) {
//            b.putString(DBUriManager.TIME_CONTROL_IS_TIME_SLOT_ENABLED, String.valueOf(childInfo.isTimeSlotActivated()));
//            //if the session is disable we send value 0 for time control session info
//            getChildSessionInfos(childInfo.isAdvancedSettingsActivated(), childInfo, b);
//        } else {
//            //if time control is off we put default values
//            b.putString(DBUriManager.TIME_CONTROL_IS_TIME_SLOT_ENABLED, String.valueOf(false));
//            getChildSessionInfos(false, childInfo, b);
//        }
//
//        b.putString(DBUriManager.SESSION_TIME_TIME_CONTROL_NAME_ID, childInfo.mTimeControlList.getNameId());

        return b;
    }

    private void getChildSessionInfos(boolean isEnabled, ChildInfo childInfo, Bundle b) {
//        b.putString(DBUriManager.TIME_CONTROL_IS_ADVANCED_SETTINGS_ENABLED, String.valueOf(isEnabled));

//        if (isEnabled) {
//            b.putString(DBUriManager.SESSION_TIME_DAILY_PLAY_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(childInfo.getSessionDailyPlaytime(GlobalSetting.TIME_CONTROL_WEEK_DAYS)));
//            b.putString(DBUriManager.SESSION_TIME_DAILY_PLAY_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(childInfo.getSessionDailyPlaytime(GlobalSetting.TIME_CONTROL_WEEK_END)));
//
//            b.putString(DBUriManager.SESSION_TIME_MAX_DURATION + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(childInfo.getSessionMaxDuration(GlobalSetting.TIME_CONTROL_WEEK_DAYS)));
//            b.putString(DBUriManager.SESSION_TIME_MAX_DURATION + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(childInfo.getSessionMaxDuration(GlobalSetting.TIME_CONTROL_WEEK_END)));
//
//            b.putString(DBUriManager.SESSION_TIME_REST_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(childInfo.getSessionRestTime(GlobalSetting.TIME_CONTROL_WEEK_DAYS)));
//            b.putString(DBUriManager.SESSION_TIME_REST_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(childInfo.getSessionRestTime(GlobalSetting.TIME_CONTROL_WEEK_END)));
//        } else {
//            b.putString(DBUriManager.SESSION_TIME_DAILY_PLAY_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(0));
//            b.putString(DBUriManager.SESSION_TIME_DAILY_PLAY_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(0));
//
//            b.putString(DBUriManager.SESSION_TIME_MAX_DURATION + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(0));
//            b.putString(DBUriManager.SESSION_TIME_MAX_DURATION + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(0));
//
//            b.putString(DBUriManager.SESSION_TIME_REST_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_DAYS,
//                    String.valueOf(0));
//            b.putString(DBUriManager.SESSION_TIME_REST_TIME + "_" + GlobalSetting.TIME_CONTROL_WEEK_END,
//                    String.valueOf(0));
//        }


    }

    private void clearTableValues(Context context) {
        AppAnalyticsDataAccess appAnalyticsDataAccess = new AppAnalyticsDataAccess(context);
        appAnalyticsDataAccess.resetValues();

    }

    public void dataSendForApps(Context context) {
        clearTableValues(context);
        setNeedToSendAnalytics(context, false, AppAnalyticsTable.APP_ANALYTICS_TABLE);
    }

    public void dataSendForActivityStatus(Context context) {
        setNeedToSendAnalytics(context, false, AppAnalyticsTable.APP_ANALYTICS_TABLE);
        clearActivityStatus(context);
    }

    private void clearActivityStatus(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("launch_count", 0);
        contentValues.put("time_spent", 0);

        context.getContentResolver().update(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, contentValues, null, null);
    }

    public String getActiveProfileAnlyticsUid(Context context) {
        ArrayList<String> activeProfiles = new ArrayList<>();
        ArrayList<Integer> childsId = ChildInfoDataAccess.getAllUserId(context);

        for (int id : childsId) {
            activeProfiles.add(ChildInfoDataAccess.getAnalyticsUIDForUserId(context, id));
        }

        Gson gson = new GsonBuilder().create();

        String dataToSend = gson.toJson(activeProfiles);

        return dataToSend;
    }
}
