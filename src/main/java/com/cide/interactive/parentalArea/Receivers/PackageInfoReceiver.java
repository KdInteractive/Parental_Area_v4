package com.cide.interactive.parentalArea.Receivers;

import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v4.app.NotificationCompat;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;
import com.cide.interactive.parentalArea.Api.RemoteApiService;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;
import com.cide.interactive.parentalArea.DialogActivity;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.ParentCheckActivity;
import com.cide.interactive.parentalArea.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alexandre on 24/03/14.
 */
public class PackageInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageInstalledOrRemoved";
    public static int NOTIF_ID = 1709;
    private static int sCurrentUserId = -1;
    private static PackageManager sPackageManager = null;
    private static ContentResolver sContentResolver = null;
    private static ArrayList<String> sIgnoredPackagename = new ArrayList<>();

    static {
        sIgnoredPackagename.add(Kurio.PACKAGE_KURIO_LAUNCHER);
        sIgnoredPackagename.add(Kurio.PACKAGE_KURIO_SERVICE);
        sIgnoredPackagename.add(Kurio.PACKAGE_KURIO_SETTINGS);
        sIgnoredPackagename.add(Kurio.PACKAGE_KURIO_PRELOADER);
        sIgnoredPackagename.add(Kurio.PACKAGE_PLAY_STORE);
    }

    private static void installPackageForParent(String packageName) {
        final IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        try {
            pmForUser.installExistingPackageAsUser(packageName, 0);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        sCurrentUserId = context.getUserId();

        if (sPackageManager == null) {
            sPackageManager = context.getPackageManager();
        }

        final String packageName = intent.getData().getSchemeSpecificPart();
        Log.e("----------------------------------- RECEIVE : ", packageName + " - " + action + " / " + String.valueOf(context.getUserId()));
        if (sContentResolver == null) {
            sContentResolver = Utils.getOwnerContext(context).getContentResolver();
        }

        if (sIgnoredPackagename.contains(packageName) || AppManagementUtil.GOOGLE_ACCOUNT_PACKAGE_NAME.contains(packageName)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ResolveInfo> mLaunchableActivities = AppManagementUtil.findLaunchableActivitiesForPackage(context, packageName, 0);

                if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                    AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
                    for (ResolveInfo resolveInfo : mLaunchableActivities) {
                        //check if the app exist in DB and create it if needed
                        if (!isAppExistInDB(context, resolveInfo)) {
                            createNewAppInDb(context, resolveInfo, null);
                        } else {
                            // if already exist, we reassign the last known category and put this app as new
                            createNewAppInDb(context, resolveInfo, appCategoryDataAccess.getCategoryIdForPackage(packageName));
                        }
                    }

                    if (sCurrentUserId != 0) {
                        installPackageForParent(packageName);
                    }

                    if (sCurrentUserId != 0 && isLegitimateInstallation(context, packageName)) {
                        //enable activities for child
                        boolean wasEnabled = wasEnabled(packageName);
                        for (ResolveInfo resolveInfo : mLaunchableActivities) {
                            if (wasEnabled) {
                                ChildInfoCore.getCurrentChildInfo(context, false).enableActivity(packageName, resolveInfo.activityInfo.name, false, true);
                            } else {
                                ChildInfoCore.getCurrentChildInfo(context, false).disableActivity(
                                        packageName,
                                        resolveInfo.activityInfo.name,
                                        true
                                );
                            }
                        }
                        ChildInfoDataAccess childInfoDataAccess = new ChildInfoDataAccess(context);
                        if (wasEnabled) {
                            childInfoDataAccess.savePackageStatusInDB((ArrayList) mLaunchableActivities, new ArrayList<ResolveInfo>(), sCurrentUserId);
                        } else {
                            childInfoDataAccess.savePackageStatusInDB(new ArrayList<ResolveInfo>(), (ArrayList) mLaunchableActivities, sCurrentUserId);
                        }
                    } else if (sCurrentUserId != 0) {
                        //its not a legitimate install
                        for (ResolveInfo ri : mLaunchableActivities) {
                            ChildInfoCore.getCurrentChildInfo(context, false).disableActivity(ri.activityInfo.packageName,
                                    ri.activityInfo.name, true);
                        }
                    }


                    ArrayList<ResolveInfo> appsToCategorize = appCategoryDataAccess.getAppsToCategorize(context);

                    if (appsToCategorize.isEmpty()) {
                        clearNotification(context);
                    } else {
                        displayNotification(context);
                    }


                    AnalyticsManager.getInstance().increaseAppAnalyticsValueForKey(context, packageName, AppAnalyticsTable.APP_ANALYTICS_INSTALL);
                } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {

                    AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
                    ArrayList<ResolveInfo> uncategorizedApps = appCategoryDataAccess.getAppsToCategorize(context);
                    removeCurrentPackage(packageName, uncategorizedApps);

                    if (uncategorizedApps.isEmpty()) {
                        clearNotification(context);
                    } else {
                        displayNotification(context);
                    }

                    // if the removed app was actually uncategorized and "new", we can delete it from the database
                    //the condition is in the removeUnCategorizedApp in sql
                    //we change the status only if the package is removed for parent
                    if (sCurrentUserId == 0) {
                        List<ResolveInfo> ri = AppManagementUtil.findLaunchableActivitiesForPackage(context,
                                packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                        if (ri.size() > 0) {
                            appCategoryDataAccess.setCategoryForApp(packageName, ri.get(0).activityInfo.name, appCategoryDataAccess.getCategoryIdForPackage(packageName));
                        } else {
                            appCategoryDataAccess.setCategoryForApp(packageName, "null", appCategoryDataAccess.getCategoryIdForPackage(packageName));
                        }
                    }

                    Intent intentChange = new Intent(Kurio.PACKAGE_REMOVED);
                    intentChange.putExtra("package", packageName);
                    context.sendBroadcast(intentChange);
                    AnalyticsManager.getInstance().increaseAppAnalyticsValueForKey(context, packageName, AppAnalyticsTable.APP_ANALYTICS_UNINSTALL);
                } else if (action.equals(Intent.ACTION_PACKAGE_CHANGED) && sCurrentUserId != 0) {

                    //just add new activities in the db with enable as the old package for all launchable activities --> no
                    // we need to get old parameters and add again this packageName / activityName in the DB;

                    AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
                    String categoryId = appCategoryDataAccess.getCategoryIdByPackageName(packageName);

                    boolean wasEnabled = wasEnabled(packageName);

                    //remove app in the activity status
                    removeAppInDb(packageName);

                    for (ResolveInfo resolveInfo : mLaunchableActivities) {
                        createNewAppInDb(context, resolveInfo, categoryId);

                        if (wasEnabled) {
                            ChildInfoCore.getCurrentChildInfo(context, false).enableActivity(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, false, true);
                        } else {
                            ChildInfoCore.getCurrentChildInfo(context, false).disableActivity(resolveInfo.activityInfo.packageName,
                                    resolveInfo.activityInfo.name, true);
                        }
                    }
                }

                AnalyticsManager.getInstance().setNeedToSendAnalytics(context, true, AppAnalyticsTable.APP_ANALYTICS_TABLE);
            }
        }).start();

    }


    private void removeCurrentPackage(String packageName, ArrayList<ResolveInfo> appList) {
        Iterator<ResolveInfo> iterator = appList.iterator();
        while (iterator.hasNext()) {
            ResolveInfo current = iterator.next();
            if (current.activityInfo.packageName.equals(packageName)) {
                iterator.remove();
            }
        }
    }

    private void displayNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            if (context.getUserId() != ActivityManagerNative.getDefault().getCurrentUser().id) {
                return;
            }
        } catch (RemoteException e) {
            return;
        }

        Intent intentDialog = new Intent();

        if (sCurrentUserId == 0) {
            Log.e("user : " + String.valueOf(sCurrentUserId), "for parent... ");
            intentDialog.setClass(context, DialogActivity.class);
        } else {
            Log.e("user : " + String.valueOf(sCurrentUserId), "for child");
            intentDialog.setClass(context, ParentCheckActivity.class);
            intentDialog.putExtra("fromCategoryNotif", true);
            intentDialog.putExtra("CHILD_ID", sCurrentUserId);
        }

        intentDialog.setAction(Intent.ACTION_VIEW);
        //intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIF_ID, intentDialog, 0);

        int messageIcon = R.drawable.ic_app_default;

        Notification.Builder notifBuilder = new Notification.Builder(context)
                .setContentTitle(context.getResources().getString(R.string.app_category_dialog_title))
                .setContentText(context.getResources().getString(R.string.app_category_notifications_categorize_one_app))
                .setSmallIcon(messageIcon)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIF_ID, notifBuilder.build());
    }

    private void clearNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID);
    }

    private boolean isLegitimateInstallation(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(context, false);


        if (!childInfo.isAutoAuthorizeApplication()) {

            //allow system apps
            String applicationName;
            try {
                PackageInfo applicationInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES);
                if ((applicationInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    return true;
                }
                applicationName = packageManager.getPackageInfo(packageName, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES).applicationInfo.loadLabel(packageManager).toString();
            } catch (PackageManager.NameNotFoundException e) {
                applicationName = packageName;
                Log.w(TAG, e);
            }

            //notify the child that is not allow to install app
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
            notificationBuilder.setSmallIcon(R.drawable.notification_installation_blocked);
            String textTitle = String.format(context.getString(R.string.notification_installation_blocked_title), applicationName);
            notificationBuilder.setContentTitle(textTitle);

            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notification_installation_blocked)));
//            notificationBuilder.setTicker(context.getString(R.string.notification_installation_blocked));
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentText(context.getString(R.string.notification_installation_blocked));
            notificationBuilder.setWhen(System.currentTimeMillis());
            Intent notificationIntent = new Intent();
            notificationIntent.setClassName("com.cide.interactive.kuriosettings", "com.cide.interactive.kuriosettings.MainActivity");
            notificationIntent.putExtra("CHILD_ID", sCurrentUserId);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, PackageInfoReceiver.class.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(PackageInfoReceiver.class.hashCode(), notificationBuilder.build());

            return false;
        }
        return true;
    }

    private void createNewAppInDb(Context context, ResolveInfo resolveInfo, String defaultCategory) {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);

        if (defaultCategory == null) {
            appCategoryDataAccess.setCategoryForNewApp(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
        } else {
            appCategoryDataAccess.setCategoryAsNewApp(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, defaultCategory);
        }
    }

    //test if the app exist in DB to get the category
    // if not, we try to get the category for the package
    // if exist set the category of the app as the package.
    private boolean isAppExistInDB(Context context, ResolveInfo resolveInfo) {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
        String category = appCategoryDataAccess.getCategoryIdForApp(resolveInfo);
        boolean exist = true;

        if (category == null) {
            category = appCategoryDataAccess.getCategoryIdByPackageName(resolveInfo.activityInfo.packageName);

            if (category == null) {
                exist = false;
            } else {
                appCategoryDataAccess.setCategoryForApp(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name, category);
            }

        }
        return exist;
    }

    private boolean wasEnabled(String packageName) {

        String[] projection = {DBUriManager.ACTIVITY_STATUS_ENABLED};
        boolean enabled = true;

        String where = DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME + " = '" + packageName + "' and user_id = '" +
                sCurrentUserId + "'";

        Cursor cursor = sContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, where, null, null);

        if (cursor != null && !cursor.isAfterLast()) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                enabled = enabled && Boolean.valueOf(cursor.getString(0));
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return enabled;
    }

    public void removeAppInDb(String packageName) {
        //delete for child
        String where = DBUriManager.ACTIVITY_STATUS_UID + " = '" + sCurrentUserId + "' and " + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME + " = '" + packageName + "'";
        sContentResolver.delete(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, where, null);

        //delete for category
        String whereCategory = DBUriManager.APP_CATEGORY_PACKAGE_NAME + " = '" + packageName + "' and package_name = '" + packageName + "'";
        sContentResolver.delete(DBUriManager.CONTENT_URI_APP_CATEGORY, whereCategory, null);

    }

}
