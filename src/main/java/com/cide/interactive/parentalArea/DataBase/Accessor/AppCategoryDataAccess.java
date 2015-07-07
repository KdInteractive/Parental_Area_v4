package com.cide.interactive.parentalArea.DataBase.Accessor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.FileUtils;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Created by lionel on 11/03/14.
 */
public class AppCategoryDataAccess {
    private static final String TAG = "AppCategoryDataAccess";
    private ContentResolver mContentResolver;

    public AppCategoryDataAccess(Context context) {
        mContentResolver = Utils.getOwnerContext(context).getContentResolver();
    }

    public static HashMap<String, String> getCategoriesAttribuable(Context context) {
        HashMap<String, String> allCategories = null;
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
        allCategories = appCategoryDataAccess.getCategories(context);

        HashMap<String, String> categories = new HashMap<>();
        for (String key : allCategories.keySet()) {
            if (!key.equals(Kurio.APP_CATEGORY_UNCATEGORIZED_ID) &&
                    !key.equals(Kurio.APP_CATEGORY_LICENCED) &&
                    !key.equals(Kurio.APP_CATEGORY_ALL_APPS) &&
                    !key.equals(Kurio.APP_CATEGORY_FAVORITES)) {
                categories.put(key, allCategories.get(key));
            }
        }

        return categories;
    }

    //used to categorized as uncategorized all launchable apps on the device
    public void checkAppCategory(Context context) {
        List<ResolveInfo> riList = AppManagementUtil.findAllActivities(context, PackageManager.GET_UNINSTALLED_PACKAGES);

        ArrayList<ResolveInfo> riUncategorized = getAppForUnCategorized((ArrayList) riList);
        for (ResolveInfo ri : riUncategorized) {
            setCategoryForApp(ri.activityInfo.packageName, ri.activityInfo.name, Kurio.APP_CATEGORY_UNCATEGORIZED_ID);
        }
    }

    //if the app already exist, update else insert as new app
    public void setCategoryForApp(String packageName, String activityName, String categoryId) {
        setCategoryForAppAsNew(packageName, activityName, categoryId, "false");
    }

    //for apps already installed once, pass the flag to true (newApp) to appear in the popup
    public void setCategoryAsNewApp(String packageName, String activityName, String categoryId) {
        setCategoryForAppAsNew(packageName, activityName, categoryId, "true");
    }

    private void setCategoryForAppAsNew(String packageName, String activityName, String categoryId, String asNew) {

        if (getCategoryIdForPackage(packageName).equals(Kurio.APP_CATEGORY_UNKNOW)) {
            ContentValues values = new ContentValues();
            values.put(DBUriManager.APP_CATEGORY_PACKAGE_NAME, packageName);
            values.put(DBUriManager.APP_CATEGORY_ACTIVITY_NAME, activityName);
            values.put(DBUriManager.APP_CATEGORY_ID, categoryId);
            values.put(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL, asNew);
            mContentResolver.insert(DBUriManager.CONTENT_URI_APP_CATEGORY, values);
        } else {
            String where = DBUriManager.APP_CATEGORY_PACKAGE_NAME + "='" + packageName + "'";

            ContentValues values = new ContentValues();
            values.put(DBUriManager.APP_CATEGORY_ID, categoryId);
            values.put(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL, asNew);
            mContentResolver.update(DBUriManager.CONTENT_URI_APP_CATEGORY, values, where, null);
        }
    }

    public void setCategoryForNewApp(String packageName, String activityName) {
        removeCategoryForApp(packageName, activityName);
        ContentValues values = new ContentValues();
        values.put(DBUriManager.APP_CATEGORY_PACKAGE_NAME, packageName);
        values.put(DBUriManager.APP_CATEGORY_ACTIVITY_NAME, activityName);
        values.put(DBUriManager.APP_CATEGORY_ID, Kurio.APP_CATEGORY_UNCATEGORIZED_ID);
        values.put(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL, "true");
        mContentResolver.insert(DBUriManager.CONTENT_URI_APP_CATEGORY, values);
    }

    public void removeCategoryForApp(String packageName, String activityName) {
        String whereClause = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = ? and " + DBUriManager.APP_CATEGORY_ACTIVITY_NAME + " = ?";
        String[] whereParams = new String[]{packageName, activityName};
        mContentResolver.delete(DBUriManager.CONTENT_URI_APP_CATEGORY, whereClause, whereParams);
    }

    // remove an app in appcategory only if it's uncategorized and new app
    public void removeUnCategorizedApp(String packageName, String activityName) {
        String whereClause = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = ? and " + DBUriManager.APP_CATEGORY_ACTIVITY_NAME
                + " = ? and " + DBUriManager.APP_CATEGORY_ID + " = ? and " + DBUriManager.APP_CATEGORY_IS_NEW_INSTALL + " = ?";
        String[] whereParams = new String[]{packageName, activityName, Kurio.APP_CATEGORY_UNCATEGORIZED_ID, "true"};
        mContentResolver.delete(DBUriManager.CONTENT_URI_APP_CATEGORY, whereClause, whereParams);
    }

    //return an array list of string array
    // 0 for packageName
    // 1 for activityName
    // 2 for categoryId
    // 3 for new Install
    public ArrayList<String[]> getAppCategory() {

        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME, DBUriManager.APP_CATEGORY_IS_NEW_INSTALL};

        ArrayList<String[]> appCategory = new ArrayList<>();

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, null, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_PACKAGE_NAME));
                String activityName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ACTIVITY_NAME));
                String categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ID));
                String isNewInstall = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL));

                String[] valuesArray = new String[4];
                valuesArray[0] = packageName;
                valuesArray[1] = activityName;
                valuesArray[2] = categoryId;
                valuesArray[3] = isNewInstall;

                appCategory.add(valuesArray);

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return appCategory;
    }

    public ArrayList<ResolveInfo> getDefaultAppsToEnableAsResolveInfo(Context context) {
        ArrayList<ResolveInfo> defaultAppToEnable = new ArrayList<>();
        ArrayList<String> appCategory = FileUtils.getTextFileAsArrayWihtoutComment(context.getResources(), R.raw.appcategory);
        String[] splitedLine;
        for (String line : appCategory) {
            splitedLine = line.split("@");
            String packageName = splitedLine[0];
            if (splitedLine[3].equals("1")) {
                List<ResolveInfo> ri = AppManagementUtil.findLaunchableActivitiesForPackage(context, packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                if (ri.size() == 0) {
                    ri = AppManagementUtil.findMainActivitiesForPackage(context, packageName, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES);
                }

                if (ri.size() > 0) {
                    defaultAppToEnable.addAll(ri);
                }
            }
        }

        return defaultAppToEnable;
    }

    public ArrayList<ResolveInfo> getDefaultAppsToDisableAsResolveInfo(Context context) {
        ArrayList<ResolveInfo> defaultAppToEnable = new ArrayList<>();
        ArrayList<String> appCategory = FileUtils.getTextFileAsArrayWihtoutComment(context.getResources(), R.raw.appcategory);
        String[] splitedLine;
        for (String line : appCategory) {
            splitedLine = line.split("@");
            String packageName = splitedLine[0];
            if (splitedLine[3].equals("0")) {
                List<ResolveInfo> ri = AppManagementUtil.findLaunchableActivitiesForPackage(context, packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                if (ri.size() == 0) {
                    ri = AppManagementUtil.findMainActivitiesForPackage(context, packageName, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES);
                }

                if (ri.size() > 0) {
                    defaultAppToEnable.addAll(ri);
                }
            }
        }

        return defaultAppToEnable;
    }

    public String getCategoryIdByPackageName(String packageName) {

        String whereClauseToRetrieveCategoryFromSubActivity = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = ?";
        String[] whereParamsToRetrieveCategoryFromSubActivity = new String[1];
        String[] projectionToRetrieveCategoryFromSubActivity = new String[]{DBUriManager.APP_CATEGORY_ID};
        String category = null;

        whereParamsToRetrieveCategoryFromSubActivity[0] = packageName;
        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projectionToRetrieveCategoryFromSubActivity, whereClauseToRetrieveCategoryFromSubActivity, whereParamsToRetrieveCategoryFromSubActivity, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                category = cursor.getString(0);
            }
            cursor.close();
        }
        if (cursor != null) {
            cursor.close();
        }

        return category;
    }


    public ArrayList<ResolveInfo> getAppForUnCategorized(ArrayList<ResolveInfo> allApps) {
        ArrayList<ResolveInfo> unCategorizedApp = new ArrayList<>();
        ArrayList<String[]> allCategories = getAppCategory();

        for (ResolveInfo ri : allApps) {
            boolean found = false;
            for (String[] appItem : allCategories) {
                String packageName = appItem[0];

                if (packageName.equals(ri.activityInfo.packageName)) {
                    found = true;
                    break;
                }
            }

            //not categorized (not exist in db)
            if (!found) {
                unCategorizedApp.add(ri);
            }
        }
        return unCategorizedApp;
    }

    //return apps uncategorized and new cause uncategorized and not new are not installed or not need
    // to be displayed
    public ArrayList<String[]> getAppUnCategorized() {

        ArrayList<String[]> unCategorizedApp = new ArrayList<>();
        ArrayList<String[]> allCategories = getAppForCategory(Kurio.APP_CATEGORY_UNCATEGORIZED_ID);

        for (String[] appItem : allCategories) {

            boolean isNewApp = Boolean.valueOf(appItem[2]);
            if (isNewApp) {
                unCategorizedApp.add(appItem);
            }
        }

        ArrayList<String> appsToNotDisplay = new ArrayList<>(Arrays.asList(Kurio.APP_TO_NOT_DISPLAY));

        Iterator<String[]> i = unCategorizedApp.iterator();
        while (i.hasNext()) {
            String[] appListPackageName = i.next();

            if (appsToNotDisplay.contains(appListPackageName[0])) {
                i.remove();
            }
        }

        return unCategorizedApp;
    }

    //return uncategorized and new apps, remove apps to not display
    //used to display the notification for new apps
    public ArrayList<ResolveInfo> getAppsToCategorize(Context context) {
        ArrayList<String[]> appList = new ArrayList<>();

        appList.addAll(getAppUnCategorized());
//        appList.addAll(getNewAppsCategorized());

        ArrayList<String> appsToNotDisplay = new ArrayList<>(Arrays.asList(Kurio.APP_TO_NOT_DISPLAY));

        Iterator<String[]> i = appList.iterator();
        while (i.hasNext()) {
            String[] appListPackageName = i.next();

            if (appsToNotDisplay.contains(appListPackageName[0])) {
                i.remove();
            }
        }

        ArrayList<ResolveInfo> appsInQueue = new ArrayList<>();
        for (String[] app : appList) {
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.setClassName(app[0], app[1]);
            ResolveInfo ri = context.getPackageManager().resolveActivity(mainIntent, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (ri != null) {
                appsInQueue.add(ri);
            }
        }

        return appsInQueue;
    }

    //return ArrayList<String[]> to avoid case when package have more than one activity name
    // 0 for packageName
    //1 for activityName
    public ArrayList<String[]> getAppForCategory(String categoryId) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME, DBUriManager.APP_CATEGORY_IS_NEW_INSTALL};
        String selection = DBUriManager.APP_CATEGORY_ID + " = '" + categoryId + '\'';

        ArrayList<String[]> appForCategory = new ArrayList<>();

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_PACKAGE_NAME));
                String activityName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ACTIVITY_NAME));
                String isNewApp = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL));

                String[] appItem = new String[3];
                appItem[0] = packageName;
                appItem[1] = activityName;
                appItem[2] = isNewApp;

                appForCategory.add(appItem);

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return appForCategory;
    }

    //return only categorized new apps
    public ArrayList<String[]> getNewAppsCategorized() {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME, DBUriManager.APP_CATEGORY_IS_NEW_INSTALL};

        String selection = DBUriManager.APP_CATEGORY_IS_NEW_INSTALL + " = 'true' and " + DBUriManager.APP_CATEGORY_ID + " != '" + Kurio.APP_CATEGORY_UNCATEGORIZED_ID + "'";

        ArrayList<String[]> newApps = new ArrayList<>();

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String packageName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_PACKAGE_NAME));
                String activityName = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ACTIVITY_NAME));
                String appCategory = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_IS_NEW_INSTALL));

                String[] appItem = new String[3];
                appItem[0] = packageName;
                appItem[1] = activityName;
                appItem[2] = appCategory;

                newApps.add(appItem);

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return newApps;
    }

    public ArrayList<ResolveInfo> getAppResolveInfoForCategory(Context context, String categoryId) {

        String[] projection = {DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};
        String selection = DBUriManager.APP_CATEGORY_ID + " = '" + categoryId + '\'';

        ArrayList<ResolveInfo> appForCategory = new ArrayList<>();

        PackageManager packageManager = Utils.getOwnerContext(context).getPackageManager();

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, selection, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            int indexPackageName = cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_PACKAGE_NAME);
            int indexActivityName = cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ACTIVITY_NAME);
            String packageName, activityName;
            List<ResolveInfo> resolveInfos;
            Intent intent;
            while (!cursor.isAfterLast()) {
                packageName = cursor.getString(indexPackageName);
                activityName = cursor.getString(indexActivityName);

                intent = new Intent();
                intent.setClassName(packageName, activityName);

                resolveInfos = packageManager.queryIntentActivities(intent, 0);
                if (resolveInfos != null && resolveInfos.size() > 0) {
                    appForCategory.add(resolveInfos.get(0));
                }

                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return appForCategory;
    }

    public String getCategoryIdForApp(ResolveInfo resolveInfo) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};

        String select = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + resolveInfo.activityInfo.packageName + "' and " +
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME + " = '" + resolveInfo.activityInfo.name + '\'';

        String categoryId = null;

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, select, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ID));

        }

        if (cursor != null) {
            cursor.close();
        }

        return categoryId;
    }

    public String getCategoryIdForPackage(String packageName) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};

        String select = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + packageName + "'";

        String categoryId = Kurio.APP_CATEGORY_UNKNOW;

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, select, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ID));

        }

        if (cursor != null) {
            cursor.close();
        }

        return categoryId;
    }

    public String getCategoryIdForApp(String packageName, String activityName) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};

        String select = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + packageName + "' and " +
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME + " = '" + activityName + '\'';

        String categoryId = Kurio.APP_CATEGORY_UNKNOW;

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, select, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ID));

        }

        if (cursor != null) {
            cursor.close();
        }

        return categoryId;
    }

    public boolean checkAppIsThisCategory(String packageName, String category) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};

        String select = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + packageName + '\'';

        boolean result = false;

        Cursor cursor = mContentResolver.query(
                DBUriManager.CONTENT_URI_APP_CATEGORY, projection, select, null, null);
        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String cat = cursor.getString(
                        cursor.getColumnIndexOrThrow(DBUriManager.APP_CATEGORY_ID));
                if (cat.equals(category)) {
                    result = true;
                    break;
                }
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }


    // also if the app is uncategorized it return true
    public boolean isAppCategorized(ResolveInfo resolveInfo) {
        String[] projection = {DBUriManager.APP_CATEGORY_ID, DBUriManager.APP_CATEGORY_PACKAGE_NAME,
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME};

        String select = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + resolveInfo.activityInfo.packageName + "' and " +
                DBUriManager.APP_CATEGORY_ACTIVITY_NAME + " = '" + resolveInfo.activityInfo.name + '\'';

        boolean isCategorized = false;

        Cursor cursor = mContentResolver.query(DBUriManager.CONTENT_URI_APP_CATEGORY, projection, select, null, null);
        if (cursor != null && !cursor.isAfterLast() && cursor.moveToFirst()) {
            isCategorized = true;
        }

        if (cursor != null) {
            cursor.close();
        }

        return isCategorized;
    }

    public HashMap<String, String> getCategories(Context context) {
        Resources res = context.getResources();
        ArrayList<String> categoryIds = FileUtils.getTextFileAsArrayWihtoutComment(res, R.raw.category);

        HashMap<String, String> categories = new HashMap<>();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (String id : categoryIds) {
                String line[] = id.split("@");
                String categoryID = line[0];
                String categoryLabel = null;
                if (res != null) {
                    int resourceId = FileUtils.getExternalResourceId(
                            res, Kurio.PACKAGE_KURIO_SERVICE,
                            "category_" + categoryID, "string");
                    if (resourceId != 0) {
                        categoryLabel = res.getString(resourceId);
                    }
                }

                categories.put(categoryID, categoryLabel);
            }
        }
        return categories;
    }

}
