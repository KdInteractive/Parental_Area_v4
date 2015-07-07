package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.TimeSlotSetting;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;
import com.cide.interactive.parentalArea.DataBase.Tables.TimeSlotSettingsTable;

import java.util.ArrayList;

/**
 * Created by lionel on 05/12/14.
 */
public class TimeSlotSettingsDataAccess {
    private ContentResolver mContentResolver;
    private Context mContext;

    public TimeSlotSettingsDataAccess(Context context) {
        mContext = context;
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public void createDefaultTimeSlotForTest(int userId) {
        String[] timeSlot = {"8:00", "20:00"};

        ArrayList<String[]> timeSlotList = new ArrayList<>();
        timeSlotList.add(timeSlot);

        ArrayList<TimeSlotSetting> timeSlotSettingList = new ArrayList<>();

        //0 to 7 : 0 for all day parameters
        // 1 to 7 for week days
        for (int i = 0; i < 8; i++) {
            TimeSlotSetting timeSlotSetting;
            if (i == 0) {
                timeSlotSetting = new TimeSlotSetting(i, userId, 30, 60, 30, timeSlotList, false, false, false);
            } else {
                timeSlotSetting
                        = new TimeSlotSetting(i, userId, 30, 60, 30, timeSlotList, false, false, false);
            }
            timeSlotSettingList.add(timeSlotSetting);
        }

        save(timeSlotSettingList);
    }

    public void createDefaultTimeSlotForDemoMode(int userId) {
        String[] timeSlot = {"8:00", "20:00"};

        ArrayList<String[]> timeSlotList = new ArrayList<>();
        timeSlotList.add(timeSlot);

        ArrayList<TimeSlotSetting> timeSlotSettingList = new ArrayList<>();

        //0 to 7 : 0 for all day parameters
        // 1 to 7 for week days
        for (int i = 0; i < 8; i++) {
            TimeSlotSetting timeSlotSetting;
            if (i == 0) {
                timeSlotSetting = new TimeSlotSetting(i, userId, 30, 60, 30, timeSlotList, false, false, false);
            } else {
                timeSlotSetting
                        = new TimeSlotSetting(i, userId, 30, 60, 30, timeSlotList, false, false, false);
            }
            timeSlotSettingList.add(timeSlotSetting);
        }

        save(timeSlotSettingList);
    }

    public void save(ArrayList<TimeSlotSetting> timeSlotSettingList) {

        delete(timeSlotSettingList.get(0).getChildId());
        ContentValues contentValues = new ContentValues();

        for (TimeSlotSetting timeSlotSetting : timeSlotSettingList) {

            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID
                    , String.valueOf(timeSlotSetting.getChildId()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY
                    , String.valueOf(timeSlotSetting.getDay()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME
                    , String.valueOf(timeSlotSetting.getSessionTime()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME
                    , String.valueOf(timeSlotSetting.getPlayTime()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME
                    , String.valueOf(timeSlotSetting.getRestTime()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT
                    , String.valueOf(timeSlotSetting.getTimeSlotAsString()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED
                    , String.valueOf(timeSlotSetting.isEnabled()));
            contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON
                    , String.valueOf(timeSlotSetting.isEducational()));

            mContentResolver.insert(DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, contentValues);
        }
        AnalyticsManager.getInstance().setNeedToSendAnalytics(mContext, true, DBUriManager.CHILDINFO_TABLE);
    }

    public void save(TimeSlotSetting timeSlotSetting) {

        delete(timeSlotSetting.getChildId(), timeSlotSetting.getDay());
        ContentValues contentValues = new ContentValues();

        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID
                , String.valueOf(timeSlotSetting.getChildId()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY
                , String.valueOf(timeSlotSetting.getDay()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME
                , String.valueOf(timeSlotSetting.getSessionTime()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME
                , String.valueOf(timeSlotSetting.getPlayTime()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME
                , String.valueOf(timeSlotSetting.getRestTime()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT
                , String.valueOf(timeSlotSetting.getTimeSlotAsString()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED
                , String.valueOf(timeSlotSetting.isEnabled()));
        contentValues.put(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON
                , String.valueOf(timeSlotSetting.isEducational()));

        mContentResolver.insert(DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, contentValues);
        AnalyticsManager.getInstance().setNeedToSendAnalytics(mContext, true, DBUriManager.CHILDINFO_TABLE);
    }

    public ArrayList<TimeSlotSetting> getTimeSlotSettingsForUser(int userId) {

        ArrayList<TimeSlotSetting> timeSlotSettings = new ArrayList<>();
        String[] projection = {
                TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EXCEPTION
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON
        };

        String where = TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID + " = '" + userId + '\'';
        Cursor cursor = mContentResolver.query(
                DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, projection, where, null, null);
        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TimeSlotSetting tss = new TimeSlotSetting();
                tss.setChildId(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID)))));
                tss.setDay(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY)))));
                tss.setSessionTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME)))));
                tss.setPlayTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME)))));
                tss.setRestTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME)))));
                tss.addTimeSlot(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT))));
                tss.setEnabled(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED)))));
                tss.setEducationalEnabed(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(
                        TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON)))));
                timeSlotSettings.add(tss);

                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return timeSlotSettings;
    }

    public TimeSlotSetting getTimeSlotSettingsForUser(int userId, int dayOfWeek) {

        TimeSlotSetting tss = new TimeSlotSetting();
        String[] projection = {TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EXCEPTION
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED
                , TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON};

        String where
                = TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID + " = '" + userId + "' AND " +
                TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY + " = '" + dayOfWeek + '\'';
        Cursor cursor = mContentResolver.query(
                DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, projection, where, null, null);

        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            tss.setChildId(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID)))));
            tss.setDay(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY)))));
            tss.setSessionTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_SESSION_TIME)))));
            tss.setPlayTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_PLAY_TIME)))));
            tss.setRestTime(Integer.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_REST_TIME)))));
            tss.addTimeSlot(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TIME_SLOT))));
            tss.setEnabled(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_ENABLED)))));
            tss.setEducationalEnabed(Boolean.valueOf(cursor.getString((cursor.getColumnIndex(
                    TimeSlotSettingsTable.TIME_SLOT_SETTINGS_EDUCATIONAL_ON)))));
        } else {
            tss = null;
        }

        if (cursor != null) {
            cursor.close();
        }

        return tss;
    }

    public void delete(int userId) {
        String where = TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID + " = '" + userId + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, where, null);
    }

    private void delete(int userId, int day) {
        String where = TimeSlotSettingsTable.TIME_SLOT_SETTINGS_CHILD_ID + "='" + userId + "' AND "
                + TimeSlotSettingsTable.TIME_SLOT_SETTINGS_DAY + "='" + day + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_TIME_SLOT_SETTINGS, where, null);
    }
}
