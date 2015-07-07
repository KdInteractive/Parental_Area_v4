package com.cide.interactive.parentalArea.DataBase.Tables;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by lionel on 06/03/14.
 */
public class TimeSlotSettingsTable {

    public static final String TIME_SLOT_SETTINGS_TABLE = "time_slot_settings";
    public static final String TIME_SLOT_SETTINGS_ID = "_id";
    public static final String TIME_SLOT_SETTINGS_CHILD_ID = "child_id";
    public static final String TIME_SLOT_SETTINGS_DAY = "day"; // name of the file
    public static final String TIME_SLOT_SETTINGS_SESSION_TIME = "session_time";
    public static final String TIME_SLOT_SETTINGS_PLAY_TIME = "play_time";
    public static final String TIME_SLOT_SETTINGS_REST_TIME = "rest_time";
    public static final String TIME_SLOT_SETTINGS_TIME_SLOT = "time_slot";
    public static final String TIME_SLOT_SETTINGS_EXCEPTION = "exception";
    public static final String TIME_SLOT_SETTINGS_ENABLED = "enabled";
    public static final String TIME_SLOT_SETTINGS_EDUCATIONAL_ON = "educational_on";

    private static final String DATABASE_CREATE = "create table "
            + TIME_SLOT_SETTINGS_TABLE
            + '('
            + TIME_SLOT_SETTINGS_ID + " integer primary key autoincrement, "
            + TIME_SLOT_SETTINGS_CHILD_ID + " text, "
            + TIME_SLOT_SETTINGS_DAY + " text,"
            + TIME_SLOT_SETTINGS_SESSION_TIME + " text,"
            + TIME_SLOT_SETTINGS_PLAY_TIME + " text, "
            + TIME_SLOT_SETTINGS_REST_TIME + " text, "
            + TIME_SLOT_SETTINGS_TIME_SLOT + " text, "
            + TIME_SLOT_SETTINGS_EXCEPTION + " text,"
            + TIME_SLOT_SETTINGS_ENABLED + " text,"
            + TIME_SLOT_SETTINGS_EDUCATIONAL_ON + " text"
            + "); ";


    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (oldVersion < 53) {
            onCreate(database);
        } else if (oldVersion < 56) {
            database.execSQL("ALTER TABLE " + TIME_SLOT_SETTINGS_TABLE +
                    " ADD COLUMN " + TIME_SLOT_SETTINGS_EDUCATIONAL_ON + " text default 'false'");
        }
    }
}
