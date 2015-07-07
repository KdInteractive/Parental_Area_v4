package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.FileUtils;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.kuriolib.WebListInfo;
import com.cide.interactive.parentalArea.DataBase.Tables.WebListTable;
import com.cide.interactive.parentalArea.R;

import java.util.ArrayList;

/**
 * Created by lionel on 12/01/15.
 */
public class WebListDataAccess {

    ContentResolver mContentResolver;

    public WebListDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public void createWebList(int userId) {

        ArrayList<WebListInfo> webList = new ArrayList<>();
        String[] projection = {WebListTable.WEB_LIST_URL};

        // SELECT DISTINCT isn't possible with a ContentResolver so we're obliged to do it manually (see the list below)
        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_WEB_LIST, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String url = cursor.getString(cursor.getColumnIndex(WebListTable.WEB_LIST_URL));

                boolean addUrl = true;
                for (WebListInfo wi : webList) {
                    if (wi.getUrl().equals(url)) {
                        addUrl = false;
                        break;
                    }
                }
                if (addUrl) {
                    WebListInfo webListInfo = new WebListInfo();
                    webListInfo.setUrl(url);
                    webListInfo.setBookmarked(false);
                    webListInfo.setEnabled(false);
                    webListInfo.setWhiteList(false);
                    webList.add(webListInfo);
                    addWebListForUser(webListInfo, userId);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public ArrayList<WebListInfo> getWebListForUser(int userId) {

        ArrayList<WebListInfo> webList = new ArrayList<>();
        String[] projection = {WebListTable.WEB_LIST_URL, WebListTable.WEB_LIST_USER_ID, WebListTable.WEB_LIST_ENABLED,
                WebListTable.WEB_LIST_IS_WHITE_LIST, WebListTable.WEB_LIST_BOOKMARKED, WebListTable.WEB_LIST_ID_BIB};

        String selection = WebListTable.WEB_LIST_USER_ID + " = '" + userId + '\'';
        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_WEB_LIST, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            int index_URL = cursor.getColumnIndexOrThrow(WebListTable.WEB_LIST_URL);
            int index_ENABLED = cursor.getColumnIndexOrThrow(WebListTable.WEB_LIST_ENABLED);
            int index_IS_WHITE_LIST = cursor.getColumnIndexOrThrow(WebListTable.WEB_LIST_IS_WHITE_LIST);
            int index_BOOKMARKED = cursor.getColumnIndexOrThrow(WebListTable.WEB_LIST_BOOKMARKED);
            int index_ID_BIB = cursor.getColumnIndexOrThrow(WebListTable.WEB_LIST_ID_BIB);

            while (!cursor.isAfterLast()) {
                WebListInfo webListInfo = new WebListInfo();
                webListInfo.setChildId(userId);
                webListInfo.setUrl(cursor.getString(index_URL));
                webListInfo.setEnabled(Boolean.valueOf(cursor.getString(index_ENABLED)));
                webListInfo.setWhiteList(Boolean.valueOf(cursor.getString(index_IS_WHITE_LIST)));
                webListInfo.setBookmarked(Boolean.valueOf(cursor.getString(index_BOOKMARKED)));
                webListInfo.setIdBookmarkInBrowser(cursor.getInt(index_ID_BIB));

                webList.add(webListInfo);
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return webList;
    }

    public void addWebListForUser(WebListInfo webListInfo, int userId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WebListTable.WEB_LIST_URL, webListInfo.getUrl());
        contentValues.put(WebListTable.WEB_LIST_USER_ID, String.valueOf(userId));
        contentValues.put(WebListTable.WEB_LIST_ENABLED, String.valueOf(webListInfo.isEnabled()));
        contentValues.put(WebListTable.WEB_LIST_IS_WHITE_LIST, String.valueOf(webListInfo.isWhiteList()));
        contentValues.put(WebListTable.WEB_LIST_BOOKMARKED, String.valueOf(webListInfo.isBookmarked()));
        contentValues.put(WebListTable.WEB_LIST_ID_BIB, String.valueOf(webListInfo.getIdBookmarkInBrowser()));
        mContentResolver.insert(DBUriManager.CONTENT_URI_WEB_LIST, contentValues);
    }

    public void deleteWebList(WebListInfo webListInfo) {
        String where = WebListTable.WEB_LIST_URL + " = '" + webListInfo.getUrl() + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_WEB_LIST, where, null);
    }

    public void deleteListForUser(int userId) {
        String where = WebListTable.WEB_LIST_USER_ID + " = '" + userId + "'";
        mContentResolver.delete(DBUriManager.CONTENT_URI_WEB_LIST, where, null);
    }

    public void updateWebList(WebListInfo webListInfo, int userId) {

        String where = WebListTable.WEB_LIST_URL + " = '" + webListInfo.getUrl() + "' and " + WebListTable.WEB_LIST_USER_ID + " = '" +
                userId + '\'';
        ContentValues contentValues = new ContentValues();
        contentValues.put(WebListTable.WEB_LIST_URL, webListInfo.getUrl());
        contentValues.put(WebListTable.WEB_LIST_USER_ID, webListInfo.getChildId());
        contentValues.put(WebListTable.WEB_LIST_ENABLED, String.valueOf(webListInfo.isEnabled()));
        contentValues.put(WebListTable.WEB_LIST_IS_WHITE_LIST, String.valueOf(webListInfo.isWhiteList()));
        contentValues.put(WebListTable.WEB_LIST_BOOKMARKED, String.valueOf(webListInfo.isBookmarked()));
        contentValues.put(WebListTable.WEB_LIST_ID_BIB, webListInfo.getIdBookmarkInBrowser());
        mContentResolver.update(DBUriManager.CONTENT_URI_WEB_LIST, contentValues, where, null);
    }

    public void updateWebListUrl(String oldUrl, String newUrl) {
        String where = WebListTable.WEB_LIST_URL + " = '" + oldUrl + '\'';
        ContentValues contentValues = new ContentValues();
        contentValues.put(WebListTable.WEB_LIST_URL, newUrl);
        mContentResolver.update(DBUriManager.CONTENT_URI_WEB_LIST, contentValues, where, null);
    }

    public void importWebListFromChild(int fromChild, int toChild) {
        ArrayList<WebListInfo> newWebList = getWebListForUser(fromChild);
        for (WebListInfo wi : newWebList) {
            wi.setChildId(toChild);
            updateWebList(wi, toChild);
        }
    }

    public void createDefaultListForTest(Context context, int userId) {
        ArrayList<String> urlList = FileUtils.getTextFileAsArrayWihtoutComment(context.getResources(), R.raw.white_list_text);
        for (String url : urlList) {
            WebListInfo webListInfo = new WebListInfo();
            webListInfo.setUrl(url);
            webListInfo.setBookmarked(false);
            webListInfo.setEnabled(true);
            webListInfo.setWhiteList(true);
            addWebListForUser(webListInfo, userId);
        }

        urlList = FileUtils.getTextFileAsArrayWihtoutComment(context.getResources(), R.raw.block_list_text);
        for (String url : urlList) {
            WebListInfo webListInfo = new WebListInfo();
            webListInfo.setUrl(url);
            webListInfo.setBookmarked(false);
            webListInfo.setEnabled(true);
            webListInfo.setWhiteList(false);
            addWebListForUser(webListInfo, userId);
        }
    }
}
