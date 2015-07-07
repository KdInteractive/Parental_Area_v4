package com.cide.interactive.parentalArea.Library;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.provider.Settings;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.ChildInfo;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.FileUtils;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.kuriolib.WebFilterUtil;
import com.cide.interactive.kuriolib.WebListInfo;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.LayoutInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.TimeSlotSettingsDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.WebListDataAccess;
import com.cide.interactive.parentalArea.DataBase.DBRequestHelper;
import com.cide.interactive.parentalArea.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Created by lionel on 06/11/14.
 */
public class ChildInfoCore extends ChildInfo {
    public static final String ACTION_GET_THEME_APPLICATION = "com.cide.interactive.getthemeapplication";
    private static ChildInfoCore sChildInfo = null;
    private Context mOwnerContext;
    private ContentResolver mContentResolver;
    private UserManager mUserManager;
    private ChildInfoDataAccess mChildInfoDataAccess;

    //used to create a new user
    public ChildInfoCore(Context context) {
        super(context);
        mOwnerContext = Utils.getOwnerContext(context);
        mContentResolver = mOwnerContext.getContentResolver();
        mChildInfoDataAccess = new ChildInfoDataAccess(context);
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);

    }

    //used for existing users
    public ChildInfoCore(Context context, int userId) {
        super(context, userId);
        mOwnerContext = Utils.getOwnerContext(context);
        mContentResolver = mOwnerContext.getContentResolver();
        mChildInfoDataAccess = new ChildInfoDataAccess(context);
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        selectAllValuesFromDB();

    }

    public static ChildInfoCore getCurrentChildInfo(Context context, boolean renew) {
        if (sChildInfo == null || renew) {
            sChildInfo = new ChildInfoCore(context, context.getUserId());
        }
        return sChildInfo;
    }

    public static boolean isChild(Context context, int userId) {
        return ChildInfoDataAccess.getAllUserId(context).contains(userId);
    }

    public static void enabledGoogleAccount(boolean enabled, int userId) {
        IPackageManager pmForUser = AppGlobals.getPackageManager();

        Utils.disableInstallationCheckForUser(userId);
        Intent i = new Intent("android.accounts.AccountAuthenticator");

        List<ResolveInfo> resolveInfo = null;
        try {
            resolveInfo = pmForUser
                    .queryIntentServices(i, null, PackageManager.GET_UNINSTALLED_PACKAGES
                            | PackageManager.GET_DISABLED_COMPONENTS, userId);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (resolveInfo != null) {
            for (ResolveInfo r : resolveInfo) {
                PackageInfo pi = null;
                try {
                    pi = pmForUser.getPackageInfo(r.serviceInfo.packageName
                            , PackageManager.GET_UNINSTALLED_PACKAGES, userId);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                if (pi != null && "com.google".equals(pi.restrictedAccountType)) {
                    try {
                        if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)
                                != ApplicationInfo.FLAG_INSTALLED) {
                            try {
                                pmForUser.installExistingPackageAsUser(r.serviceInfo.packageName, userId);
                            } catch (RemoteException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }

                        if (enabled) {
                            pmForUser.setComponentEnabledSetting(
                                    new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name),
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP, userId);
                        } else {
                            pmForUser.setComponentEnabledSetting(
                                    new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name),
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP, userId);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
        Utils.enableInstallationCheckForUser(userId);
    }

    //query all app that need authentication
    // check if one need/use google account
    // check the status
    public static boolean isGoogleAccountEnabled(Context context, int userId) {
        boolean activated = false;
        IPackageManager pmForUser = AppGlobals.getPackageManager();

        Intent i = new Intent("android.accounts.AccountAuthenticator");

        List<ResolveInfo> resolveInfo = context.getPackageManager()
                .queryIntentServicesAsUser(i, PackageManager.GET_UNINSTALLED_PACKAGES
                        | PackageManager.GET_DISABLED_COMPONENTS, userId);

        for (ResolveInfo r : resolveInfo) {
            PackageInfo pi = null;
            try {
                pi = pmForUser.getPackageInfo(r.serviceInfo.packageName
                        , PackageManager.GET_UNINSTALLED_PACKAGES, userId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (pi != null && "com.google".equals(pi.restrictedAccountType)) {
                try {
                    int enabled = pmForUser.getComponentEnabledSetting(new ComponentName(r.serviceInfo.packageName, r.serviceInfo.name), userId);
                    if (enabled == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ||
                            enabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        activated = true;
                        break;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                break;
            }
        }
        return activated;
    }

    private static void orderAppsSystemFirst(Context context, List<ResolveInfo> packageToSort) {

        PackageManager pm = context.getPackageManager();
        for (int i = 0; i < packageToSort.size(); i++) {
            ResolveInfo app = packageToSort.get(i);
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(app.activityInfo.packageName, 0);
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    packageToSort.remove(i);
                    packageToSort.add(0, app);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }

    public static boolean appsNeedGoogleAccount(Context context, ArrayList<ResolveInfo> appsToCheck) {


        boolean needGoogleAccount = false;

        for(int i = 0; i < appsToCheck.size() && !needGoogleAccount; i++) {
            ResolveInfo resolveInfo = appsToCheck.get(i);
            needGoogleAccount = appsNeedGoogleAccount(context, resolveInfo.activityInfo.packageName);
        }
        return needGoogleAccount;
    }

    public static boolean appsNeedGoogleAccount(Context context, String packageName) {
        PackageInfo packageInfo;

        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);

            if((packageInfo.requiredAccountType != null
                        && (packageInfo.requiredAccountType.equals("*") || packageInfo.requiredAccountType.equals("com.google")))
                    || (packageInfo.restrictedAccountType != null
                        && (packageInfo.restrictedAccountType.equals("*") || packageInfo.restrictedAccountType.equals("com.google")))) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return false;
    }


    public int createFromJson(String childInfoAsJson) {

        initChildFromJson(childInfoAsJson);
//we query all visible activities, and set category as uncategorised for apps not in the file
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(mContext);
        appCategoryDataAccess.checkAppCategory(mContext);

        if (createChildAccount()) {

            createDefaultCustomTimeControl();
            createWebList();
            enabledGoogleAccount(false, mUserId);
            saveChildInfoInDB();
            return mUserId;
        }

        return -1;
    }

    protected void selectAllValuesFromDB() {
        if (mChildInfoDataAccess == null) {
            mChildInfoDataAccess = new ChildInfoDataAccess(mOwnerContext);
        }
        HashMap<String, String> valuesFromDB = mChildInfoDataAccess.selectAllValuesFromDB(mUserId);

        if (valuesFromDB.size() > 0) {
            mSexBoy = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_GENDER));
            mTheme = valuesFromDB.get(DBUriManager.CHILDINFO_THEME);
            mName = valuesFromDB.get(DBUriManager.CHILDINFO_NAME);
            mDemoMode = Boolean.parseBoolean(valuesFromDB.get(DBUriManager.CHILDINFO_DEMO_MODE));
            mActivateAdsFilter = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER));
            mAllowUsbConnection = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION));
            mIsWebFilterOn = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_FILTER_ON));
            mIsWebListOn = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_WEB_LIST_ON));
            mLockedInterface = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_LOCKED_INTERFACE));
            mBirth = valuesFromDB.get(DBUriManager.CHILDINFO_BIRTH);
            mAutoAuthorizeApplication = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION));
            mIsWebAccessOn = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_INTERNET_ACCESS_ON));
            mProfileChanged = Boolean.valueOf(valuesFromDB.get(DBUriManager.CHILDINFO_PROFILE_CHANGED));
            mAnalyticsUID = valuesFromDB.get(DBUriManager.CHILDINFO_ANALYTICS_UID);
            mFilterInfo = valuesFromDB.get(DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS);
        }
    }

    // in the broadcast receiver but for now, if there is error with the profile creation,
    // userinfo == null
    private boolean createChildAccount() {

        final UserInfo userInfo = mUserManager.createUser(mName, 0);
        //user is not created
        if (userInfo == null) {
            return false;
        }

        //disable google setup
        Settings.Secure.putIntForUser(mOwnerContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 1, userInfo.id);

        createChildInDB(userInfo.id);

        AnalyticsManager.getInstance().setNeedToSendAnalytics(mContext, true, DBUriManager.CHILDINFO_TABLE);
        return true;

    }

    public void createChildInDB(int userId) {

        ChildInfoDataAccess childInfoDataAccess = new ChildInfoDataAccess(mOwnerContext);
        mUserId = userId;
        childInfoDataAccess.deleteActivityStatusForUser(userId);
        childInfoDataAccess.createChildInDB(userId, mTheme, mName);
        mAnalyticsUID = UUID.randomUUID().toString();
    }

    public void saveChildInfoInDB() {
        mUserManager.setUserName(mUserId, mName);

        HashMap<String, String> valuesForDB = new HashMap<>();
        valuesForDB.put(DBUriManager.CHILDINFO_UID, String.valueOf(mUserId));
        valuesForDB.put(DBUriManager.CHILDINFO_GENDER, Boolean.toString(mSexBoy));
        valuesForDB.put(DBUriManager.CHILDINFO_THEME, mTheme);
        valuesForDB.put(DBUriManager.CHILDINFO_NAME, mName);
        valuesForDB.put(DBUriManager.CHILDINFO_BIRTH, mBirth);
        valuesForDB.put(DBUriManager.CHILDINFO_ACTIVATE_ADS_FILTER, Boolean.toString(mActivateAdsFilter));
        valuesForDB.put(DBUriManager.CHILDINFO_ALLOW_USB_CONNECTION, Boolean.toString(mAllowUsbConnection));
        valuesForDB.put(DBUriManager.CHILDINFO_FILTER_ON, Boolean.toString(mIsWebFilterOn));
        valuesForDB.put(DBUriManager.CHILDINFO_WEB_LIST_ON, Boolean.toString(mIsWebListOn));
        valuesForDB.put(DBUriManager.CHILDINFO_LOCKED_INTERFACE, Boolean.toString(mLockedInterface));
        valuesForDB.put(DBUriManager.CHILDINFO_AUTO_AUTHORIZE_APPLICATION, Boolean.toString(mAutoAuthorizeApplication));
        valuesForDB.put(DBUriManager.CHILDINFO_INTERNET_ACCESS_ON, Boolean.toString(mIsWebAccessOn));
        valuesForDB.put(DBUriManager.CHILDINFO_DEMO_MODE, mDemoMode ? "true" : "false");
        valuesForDB.put(DBUriManager.CHILDINFO_ANALYTICS_UID, mAnalyticsUID);
        valuesForDB.put(DBUriManager.CHILDINFO_FILTER_INFO_NUMBERS, mFilterInfo);

        mChildInfoDataAccess.saveDataInDB(valuesForDB, mUserId);
        AnalyticsManager.getInstance().setNeedToSendAnalytics(mOwnerContext, true, DBUriManager.CHILDINFO_TABLE);
    }

    public String getAnalyticsUID() {
        //for old version if the value doesnt exits, we create it the save it to db
        if (mAnalyticsUID == null) {
            mAnalyticsUID = UUID.randomUUID().toString();
            saveChildInfoInDB();
        }
        return mAnalyticsUID;
    }

    public void createDefaultCustomTimeControl() {

        //create a new time control with the new designed DB
        TimeSlotSettingsDataAccess timeSlotDataAccess = new TimeSlotSettingsDataAccess(mContext);
        if (mDemoMode) {
            timeSlotDataAccess.createDefaultTimeSlotForDemoMode(mUserId);
        } else {
            timeSlotDataAccess.createDefaultTimeSlotForTest(mUserId);
        }
    }

    public void createWebList() {
        WebListDataAccess webListDataAccess = new WebListDataAccess(mContext);
        webListDataAccess.createWebList(mUserId);
    }

    /**
     * we add and remove specifics apps to enable or disable
     *
     * @param activityInfoToEnable
     */
    public void authorizeAppsForUsers(ArrayList<ResolveInfo> activityInfoToEnable, List<ResolveInfo> appsToDisable) {
        ArrayList<ResolveInfo> packageNamesThatNeedToBeEnabled = new ArrayList<>(0);
        ArrayList<ResolveInfo> packageNamesThatNeedToBeDisabled = new ArrayList<>(0);
        final IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        Iterator<ResolveInfo> iterator = appsToDisable.iterator();
        //remove apps from appsTodisable if in appsToEnable
        while (iterator.hasNext()) {
            ResolveInfo ri = iterator.next();
            for (ResolveInfo riToEnable : activityInfoToEnable) {
                if (ri.activityInfo.packageName.equals(riToEnable.activityInfo.packageName)
                        && ri.activityInfo.name.equals(riToEnable.activityInfo.name)) {
                    iterator.remove();
                }
            }
        }

        orderAppsSystemFirst(mOwnerContext, appsToDisable);

        AnalyticsManager.getInstance().setNeedToSendAnalytics(mContext, true, DBUriManager.ACTIVITY_STATUS_TABLE);

        Utils.disableInstallationCheckForUser(mUserId);

        String removeShorcutWhereClause = DBUriManager.LAYOUT_USER_ID + " = " + mUserId
                + " and " + DBUriManager.LAYOUT_PACKAGE_NAME + " = ?"
                + " and " + DBUriManager.LAYOUT_ACTIVITY_NAME + " = ?";


        String[] removeShorcutWhereParams = new String[2];

        ArrayList<ResolveInfo> appsToEnable = new ArrayList<>(0);

        Iterator<ResolveInfo> i = activityInfoToEnable.iterator();

        while (i.hasNext()) {
            ResolveInfo ri = i.next();
            if (ri.activityInfo.packageName.contains("com.cide.interactive")) {
                appsToEnable.add(ri);
                i.remove();
            }
        }

        activityInfoToEnable.addAll(0, appsToEnable);

        for (ResolveInfo app : activityInfoToEnable) {
            boolean applicationWasEnabled = true;
            String packageName = app.activityInfo.packageName;
            String activityName = app.activityInfo.name;

            Log.e(TAG, "pname enabled : " + packageName + " / aName : " + activityName);

            //Get Info
            PackageInfo piUser = null;
            try {
                piUser = pmForUser.getPackageInfo(packageName
                        , PackageManager.GET_DISABLED_COMPONENTS
                        | PackageManager.GET_UNINSTALLED_PACKAGES, mUserId);
            } catch (RemoteException e) {
                Log.w(TAG, e);
            }

            //install the app
            if (piUser != null
                    && (piUser.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)
                    != ApplicationInfo.FLAG_INSTALLED) {
                applicationWasEnabled = false;
                try {
                    pmForUser.installExistingPackageAsUser(packageName, mUserId);
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                }
            }

            //check hidden status
            boolean wasHidden = AppManagementUtil.getApplicationHiddenAsUser(packageName, mUserId);
            if (piUser != null && wasHidden) {
                applicationWasEnabled = false;
                AppManagementUtil.setApplicationHiddenAsUser(packageName, false, mUserId);
            }

            //enable the activity and save in db
            if (piUser != null) {
                enableActivity(packageName
                        , activityName, applicationWasEnabled, false);
                packageNamesThatNeedToBeEnabled.add(app);
            }

            if (packageName.equals(Kurio.PACKAGE_KURIO_LAUNCHER)) {
                Utils.disableInstallationCheckForUser(mUserId);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_MAIN);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                intentFilter.addCategory(Intent.CATEGORY_HOME);
                ComponentName componentName = new ComponentName(packageName, activityName);
                try {
                    if (mContext.getUserId() == 0
                            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pmForUser.addPersistentPreferredActivity(intentFilter,
                                componentName, mUserId);
                    } else {
                        pmForUser.addPreferredActivity(intentFilter
                                , IntentFilter.MATCH_CATEGORY_EMPTY
                                , new ComponentName[]{componentName}, componentName
                                , mUserId);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            if (packageName.equals("com.android.settings")) {
                try {
                    pmForUser.setApplicationEnabledSetting("com.android.settings",
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, mUserId, null);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        for (ResolveInfo riActivity : appsToDisable) {
            removeShorcutWhereParams[0] = riActivity.activityInfo.packageName;
            removeShorcutWhereParams[1] = riActivity.activityInfo.name;
            mContentResolver.delete(DBUriManager.CONTENT_URI_LAYOUT, removeShorcutWhereClause
                    , removeShorcutWhereParams);

            disableActivity(riActivity.activityInfo.packageName, riActivity.activityInfo.name, false);
            packageNamesThatNeedToBeDisabled.add(riActivity);
        }

        installThemeAppsForUser(mOwnerContext);
        installLicensePackForUser(mOwnerContext);
        Utils.enableInstallationCheckForUser(mUserId);

        DBRequestHelper dbRequestHelper = new DBRequestHelper(mContext);
        dbRequestHelper.needToReload(Kurio.RELOAD_LAUNCHER_APPS, mUserId);

        mChildInfoDataAccess.savePackageStatusInDB(packageNamesThatNeedToBeEnabled
                , packageNamesThatNeedToBeDisabled, mUserId);
    }

    private void installThemeAppsForUser(Context context) {
        Intent intent = new Intent(ACTION_GET_THEME_APPLICATION);
        final PackageManager packageManager = context.getPackageManager();
        IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        final List<ResolveInfo> themeApps = packageManager.queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES);

        for (ResolveInfo resolveInfo : themeApps) {
            try {
                pmForUser.installExistingPackageAsUser(resolveInfo.activityInfo.packageName, mUserId);
            } catch (Exception e) {
                android.util.Log.w(TAG, e);
            }
        }
    }

    private void installLicensePackForUser(Context context) {
        Intent intent = new Intent(Kurio.ACTION_LICENCED_PACKAGE);
        final PackageManager packageManager = context.getPackageManager();
        IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        final List<ResolveInfo> licenseApps = packageManager.queryBroadcastReceivers(intent, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES);

        for (ResolveInfo resolveInfo : licenseApps) {
            try {
                pmForUser.installExistingPackageAsUser(resolveInfo.activityInfo.packageName, mUserId);
            } catch (Exception e) {
                android.util.Log.w(TAG, e);
            }
        }
    }

    public void deleteUser() {

        deleteTimeControl();
        deleteUpdatedValues();
        deleteWebList();
        mChildInfoDataAccess.deleteUser(mUserId);
        LayoutInfoDataAccess layoutInfoDataAccess = new LayoutInfoDataAccess(mContext);
        layoutInfoDataAccess.deleteLayoutForUser(getUserId());

        AnalyticsManager.getInstance().setNeedToSendAnalytics(mContext, true, DBUriManager.CHILDINFO_TABLE);
    }

    private void deleteWebList() {
        WebListDataAccess webListDataAccess = new WebListDataAccess(mContext);
        webListDataAccess.deleteListForUser(mUserId);
    }

    protected void deleteUpdatedValues() {
        String whereClause = DBUriManager.UPDATED_VALUES_USER_ID + " = '" + mUserId + '\'';
        mContentResolver.delete(DBUriManager.CONTENT_URI_UPDATED_VALUES, whereClause, null);
    }


    protected void deleteTimeControl() {
        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess(mContext);
        timeSlotSettingsDataAccess.delete(mUserId);
    }

    public void enableAppFromPopupCategory(ResolveInfo resolveInfo) {
        Utils.disableInstallationCheckForUser(mUserId);
        boolean applicationWasEnabled = true;
        IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        PackageInfo piUser;

        try {
            piUser = pmForUser.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES, mUserId);
            if ((piUser.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
                applicationWasEnabled = false;
                pmForUser.installExistingPackageAsUser(resolveInfo.activityInfo.packageName, mUserId);
            }

            if (AppManagementUtil.getApplicationHiddenAsUser(resolveInfo.activityInfo.packageName, mUserId)) {
                applicationWasEnabled = false;
                pmForUser.setApplicationHiddenSettingAsUser(resolveInfo.activityInfo.packageName, false, mUserId);
            }

            enableActivity(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name, applicationWasEnabled, false);
            ArrayList<ResolveInfo> packageToEnable = new ArrayList<>();
            packageToEnable.add(resolveInfo);

            mChildInfoDataAccess.savePackageStatusInDB(packageToEnable, new ArrayList<ResolveInfo>(), mUserId);

        } catch (RemoteException e) {
            Log.w(TAG, e);
        }
        Utils.enableInstallationCheckForUser(mUserId);
    }


    //used to enable an app already installed
    public void enableActivity(String packageName, String activityName, boolean applicationWasEnabled,
                               boolean sendBroadcast) {

        try {
            if (!applicationWasEnabled) {

                if (sendBroadcast) {
                    Intent intent = new Intent();
                    intent.setAction(Kurio.PACKAGE_ADDED);
                    intent.putExtra("package", packageName);
                    intent.putExtra("activity", activityName);
                    mContext.sendBroadcast(intent);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    public void disableActivity(String packageName, String activityName, boolean sendBroadcast) {
//        String[] projection = {};
//        String whereClauseActivity = DBUriManager.ACTIVITY_STATUS_UID + " = ? "
//                + " and " + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME + " = ?"
//                + " and " + DBUriManager.ACTIVITY_STATUS_ENABLED + " = \"true\"";
//        String[] whereParamsActivity = {String.valueOf(mUserId), packageName};
//
//        mContentResolver.query(DBUriManager.CONTENT_URI_ACTIVITY_STATUS, projection, whereClauseActivity, whereParamsActivity, null);

        AppManagementUtil.setApplicationHiddenAsUser(packageName, true, mUserId);
        if (sendBroadcast) {
            Intent intentDisable = new Intent(Kurio.PACKAGE_REMOVED);
            intentDisable.putExtra("package", packageName);
            intentDisable.putExtra("activity", activityName);
            mContext.sendBroadcast(intentDisable);
        }
    }

    public void importAppsFromOtherChild(Context context, int otherChildId) {
        mChildInfoDataAccess.duplicateActivityStatusFromUserToUser(otherChildId, mUserId);
//        mChildInfoDataAccess.duplicateLayoutFromUserToUser(otherChildId, mUserId);

        ArrayList<String> enabledApps = mChildInfoDataAccess.getActivityStatusPackageName(otherChildId, true);
        ArrayList<String> disabledApps = mChildInfoDataAccess.getActivityStatusPackageName(otherChildId, false);
        ArrayList<ResolveInfo> enabledAppsRI = new ArrayList<>();
        ArrayList<ResolveInfo> disabledAppsRI = new ArrayList<>();

        for (String packageName : enabledApps) {
            List<ResolveInfo> ri = AppManagementUtil.findLaunchableActivitiesForPackage(context, packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (ri.size() > 0) {
                enabledAppsRI.addAll(ri);
            }
        }

        for (String packageName : disabledApps) {
            List<ResolveInfo> ri = AppManagementUtil.findLaunchableActivitiesForPackage(context, packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (ri.size() > 0) {
                disabledAppsRI.addAll(ri);
            }
        }
        authorizeAppsForUsers(enabledAppsRI, disabledAppsRI);
    }

    public void saveAppWhiteListForCronLab(String pathFile) {
        ArrayList<String> appWhiteList = FileUtils.getTextFileAsArray(mContext, mContext.getPackageName(), "raw", String.valueOf(R.raw.private_file_app_w_list_package));
        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(pathFile));

            if (appWhiteList != null) {
                for (String packageName : appWhiteList) {
                    bufferedWriter.append(packageName);
                    bufferedWriter.newLine();
                }
            }

        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (bufferedWriter != null)
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
        }
    }

    public void saveWhiteListForCronLab(String pathFile) {

        WebListDataAccess webListDataAccess = new WebListDataAccess(mContext);
        ArrayList<WebListInfo> webListInfoArrayList = webListDataAccess.getWebListForUser(mUserId);
        ArrayList<WebListInfo> whiteList = new ArrayList<>();

        for (WebListInfo webListInfo : webListInfoArrayList) {
            if (webListInfo.isWhiteList()) {
                whiteList.add(webListInfo);
            }
        }
        saveListForCronLab(pathFile, whiteList);
    }

    public void saveBlackListForCronLab(String pathFile) {

        WebListDataAccess webListDataAccess = new WebListDataAccess(mContext);
        ArrayList<WebListInfo> webListInfoArrayList = webListDataAccess.getWebListForUser(mUserId);
        ArrayList<WebListInfo> blakcList = new ArrayList<>();

        for (WebListInfo webListInfo : webListInfoArrayList) {
            if (!webListInfo.isWhiteList()) {
                blakcList.add(webListInfo);
            }
        }
        saveListForCronLab(pathFile, blakcList);
    }

    private void saveListForCronLab(String pathFile, ArrayList<WebListInfo> webListInfoArrayList) {

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(pathFile));
            if (!mIsWebAccessOn) {
                bufferedWriter.newLine();
            } else if (mIsWebListOn) {
                webListInfoArrayList.addAll(getDefaultWhiteList(mContext));
                for (WebListInfo webListInfo : webListInfoArrayList) {
                    if (webListInfo.isEnabled()) {
                        URL tmpKeyUrl = new URL("http://" + webListInfo.getUrl());
                        String tmpkey = tmpKeyUrl.getHost().toLowerCase();
                        //add the end of the url
                        tmpkey = tmpkey.concat(tmpKeyUrl.getPath());
                        bufferedWriter.append(tmpkey);
                        bufferedWriter.newLine();
                    }
                }
            } else if (!mIsWebFilterOn) {
                bufferedWriter.newLine();
            } else if(!mIsWebListOn) {
                ArrayList<WebListInfo> defaultWhiteList = getDefaultWhiteList(mContext);
                for(WebListInfo webListInfo : defaultWhiteList) {
                    URL tmpKeyUrl = new URL("http://" + webListInfo.getUrl());
                    String tmpkey = tmpKeyUrl.getHost().toLowerCase();
                    //add the end of the url
                    tmpkey = tmpkey.concat(tmpKeyUrl.getPath());
                    bufferedWriter.append(tmpkey);
                    bufferedWriter.newLine();
                }
            }

        } catch (IOException e) {
            Log.w(TAG, "saveListForCronLab", e);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    private ArrayList<WebListInfo> getDefaultWhiteList(Context context) {
        ArrayList<String> defaultWitheList = FileUtils.getTextFileAsArray(mContext, R.raw.white_list_text);
        ArrayList<WebListInfo> whiteList = new ArrayList<>();

        for(String webInfo : defaultWitheList) {
            if(!webInfo.startsWith("#")) {
                WebListInfo webListInfo = new WebListInfo();
                webListInfo.setUrl(webInfo);
                webListInfo.setBookmarked(false);
                webListInfo.setEnabled(true);
                webListInfo.setWhiteList(true);
                whiteList.add(webListInfo);
            }
        }
        return whiteList;
    }

    public void saveFilterForCronLab(Context context, String pathFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(pathFile));
            if (mIsWebFilterOn && mIsWebAccessOn) {
                String[] splitFilters = mFilterInfo.split("@");
                if (splitFilters[0].charAt(0) == WebFilterUtil.AUTOMATIC_FILTER.charAt(0)) {
                    splitFilters = WebFilterUtil.createFilterInfo(context, getAge(), false).split("@");
                }

                for (String key : splitFilters) {
                    if (key.length() == 4) {
                        bufferedWriter.append(key);
                        bufferedWriter.newLine();
                    }
                }
            } else {
                bufferedWriter.append(WebFilterUtil.getAdultFilter());
                bufferedWriter.newLine();
            }

        } catch (IOException e) {
            Log.w(TAG, "saveFilterForCronLab", e);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }

            } catch (IOException e) {
                Log.w(TAG, e.getMessage());
            }
        }
    }
}

