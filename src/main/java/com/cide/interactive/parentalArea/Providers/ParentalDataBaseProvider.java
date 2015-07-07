package com.cide.interactive.parentalArea.Providers;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.parentalArea.DataBase.ParentalDatabaseHelper;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;
import com.cide.interactive.parentalArea.DataBase.Tables.ParentPreferencesTable;
import com.cide.interactive.parentalArea.DataBase.Tables.TimeSlotSettingsTable;
import com.cide.interactive.parentalArea.DataBase.Tables.WebListTable;

/**
 * Created by lionel on 12/02/14.
 */
public class ParentalDataBaseProvider extends DBUriManager {

    // database
    private ParentalDatabaseHelper database;

    // used for the UriMacher
    private static final int CHILDINFOS_MATCHER = 10;
    private static final int CHILDINFO_ID_MATCHER = 20;
    private static final int ACTIVITYSTATUS_MATCHER = 30;
    private static final int ACTIVITYSTATUS_ID_MATCHER = 40;
    private static final int LAYOUT_MATCHER = 90;
    private static final int LAYOUT_ID_MATCHER = 100;
    private static final int APPCATEGORY_MATCHER = 130;
    private static final int APPCATEGORY_ID_MATCHER = 140;
    private static final int PARENTPREFERENCES_MATCHER = 210;
    private static final int PARENTPREFERENCES_ID_MATCHER = 220;
    private static final int UPDATEDVALUES_MATCHER = 230;
    private static final int UPDATEDVALUES_ID_MATCHER = 240;
    private static final int LAYOUTFOLDER_MATCHER = 250;
    private static final int LAYOUTFOLDER_ID_MATCHER = 260;
    private static final int LAYOUTFOLDERACTIVITIES_MATCHER = 270;
    private static final int LAYOUTFOLDERACTIVITIES_ID_MATCHER = 280;
    private static final int TO_ADD_TO_CHILD_LAYOUT_MATCHER = 290;
    private static final int APP_ANALYTICS_MATCHER = 390;
    private static final int APP_ANALYTICS_ID_MATCHER = 400;
    private static final int TIMESLOTSETTINGS_MATCHER = 430;
    private static final int TIMESLOTSETTINGS_ID_MATCHER = 440;
    private static final int WEBLIST_ID_MATCHER = 450;
    private static final int WEBLIST_MATCHER = 460;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CHILD, CHILDINFOS_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_CHILD + "/#", CHILDINFO_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_ACTIVITY_STATUS, ACTIVITYSTATUS_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_ACTIVITY_STATUS + "/#", ACTIVITYSTATUS_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_TO_ADD_TO_CHILD_LAYOUT, TO_ADD_TO_CHILD_LAYOUT_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT, LAYOUT_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT + "/#", LAYOUT_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_APP_CATEGORY, APPCATEGORY_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_APP_CATEGORY + "/#", APPCATEGORY_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PARENT_PREFERENCES, PARENTPREFERENCES_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_PARENT_PREFERENCES + "/#", PARENTPREFERENCES_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_UPDATED_VALUES, UPDATEDVALUES_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_UPDATED_VALUES + "/#", UPDATEDVALUES_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT_FOLDER, LAYOUTFOLDER_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT_FOLDER + "/#", LAYOUTFOLDER_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT_FOLDER_ACTIVITIES, LAYOUTFOLDERACTIVITIES_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_LAYOUT_FOLDER_ACTIVITIES + "/#", LAYOUTFOLDERACTIVITIES_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_APP_ANALYTICS, APP_ANALYTICS_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_APP_ANALYTICS + "/#", APP_ANALYTICS_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_TIME_SLOT_SETTINGS, TIMESLOTSETTINGS_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_TIME_SLOT_SETTINGS + "/#", TIMESLOTSETTINGS_ID_MATCHER);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH_WEB_LIST, WEBLIST_MATCHER);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH_WEB_LIST + "/#", WEBLIST_ID_MATCHER);
    }

    @Override
    public boolean onCreate() {
        database = new ParentalDatabaseHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id;

        switch (uriType) {
            case CHILDINFOS_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.CHILDINFO_TABLE, selection,
                        selectionArgs);
                break;
            case CHILDINFO_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DBUriManager.CHILDINFO_TABLE,
                            DBUriManager.CHILDINFO_ID + '=' + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBUriManager.CHILDINFO_TABLE,
                            DBUriManager.CHILDINFO_ID + '=' + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;

            case ACTIVITYSTATUS_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.ACTIVITY_STATUS_TABLE, selection,
                        selectionArgs);
                break;
            case ACTIVITYSTATUS_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DBUriManager.ACTIVITY_STATUS_TABLE,
                            DBUriManager.ACTIVITY_STATUS_ID + '=' + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBUriManager.ACTIVITY_STATUS_TABLE,
                            DBUriManager.ACTIVITY_STATUS_ID + '=' + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;

            case TO_ADD_TO_CHILD_LAYOUT_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.TO_ADD_TO_CHILD_LAYOUT_TABLE, selection,
                        selectionArgs);
                break;
            case LAYOUT_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.LAYOUT_TABLE, selection,
                        selectionArgs);
                break;
            case LAYOUT_ID_MATCHER:

                break;


            case APPCATEGORY_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.APP_CATEGORY_TABLE, selection,
                        selectionArgs);
                break;
            case APPCATEGORY_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DBUriManager.APP_CATEGORY_TABLE,
                            DBUriManager.APP_CATEGORY_ID + '=' + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBUriManager.APP_CATEGORY_TABLE,
                            DBUriManager.APP_CATEGORY_ID + '=' + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;

            case PARENTPREFERENCES_MATCHER:
                rowsDeleted = sqlDB.delete(ParentPreferencesTable.PARENT_PREFERENCES_TABLE, selection,
                        selectionArgs);
                break;
            case PARENTPREFERENCES_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ParentPreferencesTable.PARENT_PREFERENCES_TABLE,
                            ParentPreferencesTable.PARENT_PREFERENCES_ID + '=' + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ParentPreferencesTable.PARENT_PREFERENCES_TABLE,
                            ParentPreferencesTable.PARENT_PREFERENCES_ID + '=' + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;

            case UPDATEDVALUES_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.UPDATED_VALUES_TABLE, selection,
                        selectionArgs);
                break;
            case UPDATEDVALUES_ID_MATCHER:
                //TODO normally not needed
                break;

            case LAYOUTFOLDER_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.LAYOUT_FOLDER_TABLE, selection,
                        selectionArgs);
                break;
            case LAYOUTFOLDER_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(DBUriManager.LAYOUT_FOLDER_TABLE,
                            DBUriManager.LAYOUT_FOLDER_ID + '=' + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(DBUriManager.LAYOUT_FOLDER_TABLE,
                            DBUriManager.LAYOUT_FOLDER_ID + '=' + id
                                    + " and " + selection,
                            selectionArgs
                    );
                }
                break;

            case LAYOUTFOLDERACTIVITIES_MATCHER:
                rowsDeleted = sqlDB.delete(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_TABLE, selection,
                        selectionArgs);
                break;
            case LAYOUTFOLDERACTIVITIES_ID_MATCHER:
                //TODO normally not needed
                break;


            case APP_ANALYTICS_MATCHER:
                rowsDeleted = sqlDB.delete(AppAnalyticsTable.APP_ANALYTICS_TABLE, selection,
                        selectionArgs);
                break;
            case APP_ANALYTICS_ID_MATCHER:
                //TODO not needed
                break;

            case TIMESLOTSETTINGS_MATCHER:
                rowsDeleted = sqlDB.delete(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TABLE, selection,
                        selectionArgs);
                break;

            case WEBLIST_ID_MATCHER:
                //TODO not needed
                break;

            case WEBLIST_MATCHER:
                rowsDeleted = sqlDB.delete(WebListTable.WEB_LIST_TABLE, selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }


    @Override
    //return the uri to access to this row
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case CHILDINFOS_MATCHER:
                id = sqlDB.insert(DBUriManager.CHILDINFO_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_CHILDINFOS + "/" + id);

            case ACTIVITYSTATUS_MATCHER:
                id = sqlDB.insert(DBUriManager.ACTIVITY_STATUS_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_ACTIVITY_STATUS + "/" + id);

            case TO_ADD_TO_CHILD_LAYOUT_MATCHER:
                sqlDB.insert(DBUriManager.TO_ADD_TO_CHILD_LAYOUT_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return null;

            case LAYOUT_MATCHER:
                id = sqlDB.insert(DBUriManager.LAYOUT_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_LAYOUT + "/" + id);

            case APPCATEGORY_MATCHER:
                id = sqlDB.insert(DBUriManager.APP_CATEGORY_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_APP_CATEGORY + "/" + id);

            case PARENTPREFERENCES_MATCHER:
                id = sqlDB.insert(ParentPreferencesTable.PARENT_PREFERENCES_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_PARENT_PREFERENCES + "/" + id);

            case UPDATEDVALUES_MATCHER:
                id = sqlDB.insert(DBUriManager.UPDATED_VALUES_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_UPDATED_VALUES + "/" + id);

            case LAYOUTFOLDER_MATCHER:
                id = sqlDB.insert(DBUriManager.LAYOUT_FOLDER_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_LAYOUT_FOLDER + "/" + id);

            case LAYOUTFOLDERACTIVITIES_MATCHER:
                id = sqlDB.insert(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_LAYOUT_FOLDER_ACTIVITIES + "/" + id);

            case APP_ANALYTICS_MATCHER:
                id = sqlDB.insert(AppAnalyticsTable.APP_ANALYTICS_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_APP_ANALYTICS + "/" + id);

            case TIMESLOTSETTINGS_MATCHER:
                id = sqlDB.insert(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_TIME_SLOT_SETTINGS + "/" + id);

            case WEBLIST_MATCHER:
                id = sqlDB.insert(WebListTable.WEB_LIST_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CONTENT_URI_WEB_LIST + "/" + id);

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //TODO put that check  after tests are ok
        // check if the caller has requested a column which does not exists
        // checkColumns(projection);

        // Set the table
        // queryBuilder.setTables(ChildInfoTable.TABLE_CHILDINFO);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CHILDINFOS_MATCHER:
                queryBuilder.setTables(DBUriManager.CHILDINFO_TABLE);
                break;
            case CHILDINFO_ID_MATCHER:
                queryBuilder.setTables(DBUriManager.CHILDINFO_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DBUriManager.CHILDINFO_ID + '='
                        + uri.getLastPathSegment());
                break;

            case ACTIVITYSTATUS_MATCHER:
                queryBuilder.setTables(DBUriManager.ACTIVITY_STATUS_TABLE);
                break;

            case ACTIVITYSTATUS_ID_MATCHER:
                queryBuilder.setTables(DBUriManager.ACTIVITY_STATUS_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DBUriManager.ACTIVITY_STATUS_ID + '='
                        + uri.getLastPathSegment());
                break;

            case LAYOUT_MATCHER:
                queryBuilder.setTables(DBUriManager.LAYOUT_TABLE);
                break;

            case TO_ADD_TO_CHILD_LAYOUT_MATCHER:
                queryBuilder.setTables(DBUriManager.TO_ADD_TO_CHILD_LAYOUT_TABLE);
                break;

            case LAYOUT_ID_MATCHER:
                //TODO
                /*queryBuilder.setTables(DBUriManager.LAYOUT_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DBUriManager.LAYOUT_ID + "="
                        + uri.getLastPathSegment());*/
                break;


            case APPCATEGORY_MATCHER:
                queryBuilder.setTables(DBUriManager.APP_CATEGORY_TABLE);
                break;

            case APPCATEGORY_ID_MATCHER:
                queryBuilder.setTables(DBUriManager.APP_CATEGORY_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DBUriManager.APP_CATEGORY_ID + '='
                        + uri.getLastPathSegment());
                break;

            case PARENTPREFERENCES_MATCHER:
                queryBuilder.setTables(ParentPreferencesTable.PARENT_PREFERENCES_TABLE);
                break;

            case PARENTPREFERENCES_ID_MATCHER:
                queryBuilder.setTables(ParentPreferencesTable.PARENT_PREFERENCES_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(ParentPreferencesTable.PARENT_PREFERENCES_ID + '='
                        + uri.getLastPathSegment());
                break;

            case UPDATEDVALUES_MATCHER:
                queryBuilder.setTables(DBUriManager.UPDATED_VALUES_TABLE);
                break;

            case UPDATEDVALUES_ID_MATCHER:
                queryBuilder.setTables(DBUriManager.UPDATED_VALUES_TABLE);
                break;

            case LAYOUTFOLDER_MATCHER:
                queryBuilder.setTables(DBUriManager.LAYOUT_FOLDER_TABLE);
                break;

            case LAYOUTFOLDER_ID_MATCHER:
                queryBuilder.setTables(DBUriManager.LAYOUT_FOLDER_TABLE);
                // adding the ID to the original query
                queryBuilder.appendWhere(DBUriManager.LAYOUT_FOLDER_ID + '='
                        + uri.getLastPathSegment());
                break;

            case LAYOUTFOLDERACTIVITIES_MATCHER:
                queryBuilder.setTables(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_TABLE);
                break;

            case LAYOUTFOLDERACTIVITIES_ID_MATCHER:
                break;

            case APP_ANALYTICS_MATCHER:
                queryBuilder.setTables(AppAnalyticsTable.APP_ANALYTICS_TABLE);
                break;

            case TIMESLOTSETTINGS_MATCHER:
                queryBuilder.setTables(TimeSlotSettingsTable.TIME_SLOT_SETTINGS_TABLE);
                break;

            case WEBLIST_MATCHER:
                queryBuilder.setTables(WebListTable.WEB_LIST_TABLE);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }


        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        String id;

        switch (uriType) {

            case LAYOUT_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.LAYOUT_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LAYOUTFOLDER_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.LAYOUT_FOLDER_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LAYOUTFOLDERACTIVITIES_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CHILDINFOS_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.CHILDINFO_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CHILDINFO_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(DBUriManager.CHILDINFO_TABLE,
                            values,
                            DBUriManager.CHILDINFO_ID + '=' + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(DBUriManager.CHILDINFO_TABLE,
                            values,
                            DBUriManager.CHILDINFO_ID + '=' + id
                                    + " and "
                                    + selection,
                            selectionArgs
                    );
                }
                break;

            case PARENTPREFERENCES_MATCHER:
                rowsUpdated = sqlDB.update(ParentPreferencesTable.PARENT_PREFERENCES_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PARENTPREFERENCES_ID_MATCHER:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ParentPreferencesTable.PARENT_PREFERENCES_TABLE,
                            values,
                            ParentPreferencesTable.PARENT_PREFERENCES_ID + '=' + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ParentPreferencesTable.PARENT_PREFERENCES_TABLE,
                            values,
                            ParentPreferencesTable.PARENT_PREFERENCES_ID + '=' + id
                                    + " and "
                                    + selection,
                            selectionArgs
                    );
                }
                break;
            case ACTIVITYSTATUS_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.ACTIVITY_STATUS_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case APP_ANALYTICS_MATCHER:
                rowsUpdated = sqlDB.update(AppAnalyticsTable.APP_ANALYTICS_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;

            case APPCATEGORY_MATCHER:
                rowsUpdated = sqlDB.update(DBUriManager.APP_CATEGORY_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;

            case WEBLIST_MATCHER:
                rowsUpdated = sqlDB.update(WebListTable.WEB_LIST_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals(DBUriManager.METHOD_UPDATE_TABLE)) {
            //we have to use this version of execSQL, the other one doesn't work for update &co.
            database.getWritableDatabase().execSQL(arg);
        }
        return null;
    }
}