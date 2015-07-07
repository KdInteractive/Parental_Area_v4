package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.cide.interactive.kuriolib.CategoryLayout;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Utils;

import java.util.ArrayList;

/**
 * Created by lionel on 20/03/14.
 */
public class LayoutInfoDataAccess {

    //String used to parse the txt file
    public static final String TAG_IN_DESCRIPTION_STRING_BEGIN_FOLDER = "beginFolder";
    public static final String TAG_IN_DESCRIPTION_STRING_END_FOLDER = "endFolder";
    public static final String TAG_IN_DESCRIPTION_STRING_BEGIN_CATEGORY = "categoryId";
    //Layout
    private static final int INDEX_IN_ACTIVITY_STRING_PACKAGENAME = 0;
    private static final int INDEX_IN_ACTIVITY_STRING_ACTIVITYNAME = 1;
    private static final int INDEX_IN_ACTIVITY_STRING_X = 2;
    private static final int INDEX_IN_ACTIVITY_STRING_Y = 3;
    //index for folders
    private static final int INDEX_IN_FOLDER_STRING_NAME = 1;
    private static final int INDEX_IN_FOLDER_STRING_X = 2;
    private static final int INDEX_IN_FOLDER_STRING_Y = 3;
    private ContentResolver mContentResolver;

    public LayoutInfoDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public CategoryLayout getLayout(int userId, String categoryId) {

        CategoryLayout categoryLayout = new CategoryLayout();

        String[] projection = {
                DBUriManager.LAYOUT_USER_ID
                , DBUriManager.LAYOUT_PACKAGE_NAME
                , DBUriManager.LAYOUT_ACTIVITY_NAME
                , DBUriManager.LAYOUT_FOLDER_ID
                , DBUriManager.LAYOUT_CATEGORY_ID
                , DBUriManager.LAYOUT_POSITION_X
                , DBUriManager.LAYOUT_POSITION_Y
        };

        String where = DBUriManager.LAYOUT_USER_ID + " = " + userId;
        if (categoryId != null) {
            where += " and " + DBUriManager.LAYOUT_CATEGORY_ID + " = " + categoryId;
        }

        Cursor cursor = mContentResolver.query(
                DBUriManager.CONTENT_URI_LAYOUT, projection, where, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_PACKAGE_NAME));
                String activityName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_ACTIVITY_NAME));
                String folderId = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_FOLDER_ID));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_CATEGORY_ID));
                int x = cursor.getInt(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_POSITION_X));
                int y = cursor.getInt(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_POSITION_Y));
                categoryLayout.addAppInfo(packageName, activityName, x, y, category, folderId);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return categoryLayout;
    }

    public void createDefaultLayout(int userId, ArrayList<String> layoutFileLines) {

        ContentValues contentValuesFolder, contentValuesLayout, contentValuesFolderActivities;

        String deleteWhereClause = DBUriManager.LAYOUT_USER_ID + " = ? and " + DBUriManager.LAYOUT_CATEGORY_ID + " <> ?";
        String[] deleteWhereParams = new String[]{String.valueOf(userId), Kurio.APP_CATEGORY_FAVORITES};

        mContentResolver.delete(DBUriManager.CONTENT_URI_LAYOUT, deleteWhereClause, deleteWhereParams);

        String currentCategoryId = Kurio.APP_CATEGORY_LICENCED;

        int indexLine = 0;
        int layoutFileLinesSize = layoutFileLines.size();

        while (indexLine < layoutFileLinesSize && layoutFileLines.get(indexLine).startsWith("#")) {
            indexLine++;
        }

        String[] splitedLine;
        for (; indexLine < layoutFileLinesSize; indexLine++) {
            splitedLine = layoutFileLines.get(indexLine).split("@");
            switch (splitedLine[0]) {
                case TAG_IN_DESCRIPTION_STRING_BEGIN_CATEGORY:
                    currentCategoryId = splitedLine[INDEX_IN_FOLDER_STRING_NAME];
                    break;
                case TAG_IN_DESCRIPTION_STRING_BEGIN_FOLDER:
                    contentValuesFolder = new ContentValues();
                    contentValuesFolder.put(DBUriManager.LAYOUT_FOLDER_NAME, splitedLine[INDEX_IN_FOLDER_STRING_NAME]);
                    Uri uriId = mContentResolver.insert(DBUriManager.CONTENT_URI_LAYOUT_FOLDER, contentValuesFolder);
                    long folderId = ContentUris.parseId(uriId);

                    contentValuesLayout = new ContentValues();
                    contentValuesLayout.put(DBUriManager.LAYOUT_USER_ID, userId);
                    contentValuesLayout.put(DBUriManager.LAYOUT_CATEGORY_ID, currentCategoryId);
                    contentValuesLayout.put(DBUriManager.LAYOUT_FOLDER_ID, folderId);
                    contentValuesLayout.put(DBUriManager.LAYOUT_POSITION_X, splitedLine[INDEX_IN_FOLDER_STRING_X]);
                    contentValuesLayout.put(DBUriManager.LAYOUT_POSITION_Y, splitedLine[INDEX_IN_FOLDER_STRING_Y]);
                    mContentResolver.insert(DBUriManager.CONTENT_URI_LAYOUT, contentValuesLayout);

                    for (indexLine++; indexLine < layoutFileLinesSize; indexLine++) {
                        splitedLine = layoutFileLines.get(indexLine).split("@");
                        if (splitedLine[0].equals(TAG_IN_DESCRIPTION_STRING_END_FOLDER)) {
                            break;
                        } else {
                            contentValuesFolderActivities = new ContentValues();
                            contentValuesFolderActivities.put(DBUriManager.LAYOUT_FOLDER_ID, folderId);
                            contentValuesFolderActivities.put(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_PACKAGE_NAME, splitedLine[INDEX_IN_ACTIVITY_STRING_PACKAGENAME]);
                            contentValuesFolderActivities.put(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_ACTIVITY_NAME, splitedLine[INDEX_IN_ACTIVITY_STRING_ACTIVITYNAME]);
                            contentValuesFolderActivities.put(DBUriManager.LAYOUT_FOLDER_ACTIVITIES_POSITION_X, splitedLine[INDEX_IN_ACTIVITY_STRING_X]);
                            mContentResolver.insert(DBUriManager.CONTENT_URI_LAYOUT_FOLDER_ACTIVITIES, contentValuesFolderActivities);
                        }
                    }
                    break;
                default:
                    contentValuesLayout = new ContentValues();
                    contentValuesLayout.put(DBUriManager.LAYOUT_USER_ID, userId);
                    contentValuesLayout.put(DBUriManager.LAYOUT_CATEGORY_ID, currentCategoryId);
                    contentValuesLayout.put(DBUriManager.LAYOUT_PACKAGE_NAME, splitedLine[INDEX_IN_ACTIVITY_STRING_PACKAGENAME]);
                    contentValuesLayout.put(DBUriManager.LAYOUT_ACTIVITY_NAME, splitedLine[INDEX_IN_ACTIVITY_STRING_ACTIVITYNAME]);
                    contentValuesLayout.put(DBUriManager.LAYOUT_POSITION_X, splitedLine[INDEX_IN_ACTIVITY_STRING_X]);
                    contentValuesLayout.put(DBUriManager.LAYOUT_POSITION_Y, splitedLine[INDEX_IN_ACTIVITY_STRING_Y]);
                    mContentResolver.insert(DBUriManager.CONTENT_URI_LAYOUT, contentValuesLayout);
                    break;
            }
        }
    }

    public void deleteLayoutForUser(int userId) {

        String[] projection = {DBUriManager.LAYOUT_FOLDER_ID};

        String selectionFolderId = DBUriManager.LAYOUT_USER_ID + " = '" + userId + "' and "
                + DBUriManager.LAYOUT_FOLDER_ID + " notnull";

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_LAYOUT, projection, selectionFolderId, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String whereDeleteFolders = DBUriManager.LAYOUT_FOLDER_ID + " = '"
                        + Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.LAYOUT_FOLDER_ID)))
                        + '\'';

                mContentResolver.delete(DBUriManager.CONTENT_URI_LAYOUT_FOLDER_ACTIVITIES, whereDeleteFolders, null);
                mContentResolver.delete(DBUriManager.CONTENT_URI_LAYOUT_FOLDER, whereDeleteFolders, null);

                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        //deleteLayoutForUser all reference to the user
        String whereClause = DBUriManager.LAYOUT_USER_ID + " = '" + userId + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_LAYOUT, whereClause, null);
    }
}
