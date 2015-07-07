package com.cide.interactive.parentalArea.DataBase;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Utils;

import java.util.ArrayList;

/**
 * Created by lionel on 21/02/14.
 */

//used to have some specific interaction with db not linked with specific objects
public class DBRequestHelper {

    private ContentResolver mContentResolver;

    public DBRequestHelper(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public boolean isUserExistInDB(int userId) {

        String[] projection = {DBUriManager.CHILDINFO_ID, DBUriManager.CHILDINFO_UID};

        String whereClause = DBUriManager.CHILDINFO_UID + " = '" + userId + '\'';

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, whereClause, null, null);

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


    //used to update DB when launcher need to reload some specific part (Layout, app category etc)
    //we can call it setKeyToUpdateForChild
    public void needToReload(String keyToReload, int userId) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBUriManager.UPDATED_VALUES_KEY, keyToReload);
        contentValues.put(DBUriManager.UPDATED_VALUES_VALUE, Boolean.toString(true));
        contentValues.put(DBUriManager.UPDATED_VALUES_USER_ID, userId);

        mContentResolver.insert(DBUriManager.CONTENT_URI_UPDATED_VALUES, contentValues);
    }

    public void removeNeedToReload(String keyToReload) {
        String where = DBUriManager.UPDATED_VALUES_KEY + " = '" + keyToReload + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_UPDATED_VALUES, where, null);
    }

    public void needToReloadForAllChild(String keyToReload, ArrayList<Integer> allUserId) {
        for (Integer userId : allUserId) {
            needToReload(keyToReload, userId);
        }
    }

    public ArrayList<String> getKeysUpdatedForChild(int userId) {

        String selection = DBUriManager.UPDATED_VALUES_USER_ID + " = '" + userId + "' and "
                + DBUriManager.UPDATED_VALUES_VALUE + " = 'true'";

        String[] projection = {DBUriManager.UPDATED_VALUES_USER_ID, DBUriManager.UPDATED_VALUES_KEY,
                DBUriManager.UPDATED_VALUES_VALUE};

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_UPDATED_VALUES, projection, selection, null, null);

        ArrayList<String> updatedValues = new ArrayList<>();

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                updatedValues.add(cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.UPDATED_VALUES_KEY)));

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return updatedValues;
    }

    public int getFolderIdFromDB(String folderNAme) {
        String[] projection = {DBUriManager.LAYOUT_FOLDER_ID, DBUriManager.LAYOUT_FOLDER_NAME};
        String whereClause = DBUriManager.LAYOUT_FOLDER_NAME + " = '" + folderNAme + '\'';

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_LAYOUT_FOLDER, projection, whereClause, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_FOLDER_ID));
            }
        }

        return -1;
    }
}
