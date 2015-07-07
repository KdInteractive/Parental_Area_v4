package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.DataBase.Tables.ParentPreferencesTable;

/**
 * Created by lionel on 11/03/14.
 */
public class ParentPreferencesDataAccess {

    private ContentResolver mContentResolver;

    public ParentPreferencesDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public void setParentPreferences(String key, String value) {

        if (getParentPreferencesForKey(key) != null) {
            updateParentPreferences(key, value);
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ParentPreferencesTable.PARENT_PREFERENCES_SAVED_KEY, key);
            contentValues.put(ParentPreferencesTable.PARENT_PREFERENCES_SAVED_VALUE, value);
            mContentResolver.insert(DBUriManager.CONTENT_URI_PARENT_PREFERENCES, contentValues);
        }
    }

    public void updateParentPreferences(String key, String value) {
        String whereClause = ParentPreferencesTable.PARENT_PREFERENCES_SAVED_KEY + " = '" + key + '\'';

        ContentValues contentValues = new ContentValues();
        contentValues.put(ParentPreferencesTable.PARENT_PREFERENCES_SAVED_VALUE, value);

        mContentResolver.update(DBUriManager.CONTENT_URI_PARENT_PREFERENCES, contentValues, whereClause, null);
    }

    public boolean getParentPreferencesForKeyInBoolean(String key) {
        String isEnabled = getParentPreferencesForKey(key);
        return isEnabled != null && isEnabled.equals("true");
    }

    public String getParentPreferencesForKey(String key) {

        //informations we want
        String[] projection = {ParentPreferencesTable.PARENT_PREFERENCES_SAVED_KEY, ParentPreferencesTable.PARENT_PREFERENCES_SAVED_VALUE};
        String selection = ParentPreferencesTable.PARENT_PREFERENCES_SAVED_KEY + " = '" + key + '\'';
        String valueForKey = null;

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_PARENT_PREFERENCES, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            valueForKey = cursor.getString(cursor.getColumnIndexOrThrow(ParentPreferencesTable.PARENT_PREFERENCES_SAVED_VALUE));
        }

        if (cursor != null) {
            cursor.close();
        }

        return valueForKey;
    }

    public boolean isParentAlreadyRegistered() {
        String[] projection = {ParentPreferencesTable.PARENT_PREFERENCES_ID};

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_PARENT_PREFERENCES, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cursor.close();
                return true;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }
}
