package com.cide.interactive.parentalArea.Api;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserManager;

import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.TimeSlotSetting;
import com.cide.interactive.kuriolib.WebListInfo;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.TimeSlotSettingsDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.WebListDataAccess;
import com.cide.interactive.parentalArea.DataBase.DBRequestHelper;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.Receivers.PackageInfoReceiver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RemoteApiService extends Service {
    private static final String TAG = "RemoteApiService";
    private static final String KURIO_INTENT_ACTION_BIND_API_SERVICE = "com.cide.interactive.intent.action.bindApiService";


    public RemoteApiService() {
    }

    public static List<Integer> getAllUserId(Context context) {
        ArrayList<Integer> childIdFromDb = ChildInfoDataAccess.getAllUserId(context);
        UserManager userManager = (UserManager) context.getSystemService(USER_SERVICE);
        List<UserInfo> users = userManager.getUsers();

        for (int i = 0; i < childIdFromDb.size(); i++) {
            boolean found = false;
            for (UserInfo userInfo : users) {

                if (userInfo.id == childIdFromDb.get(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                childIdFromDb.remove(i);
                i--;
            }
        }

        return childIdFromDb;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (KURIO_INTENT_ACTION_BIND_API_SERVICE.equals(intent.getAction())) {
            Log.d(TAG, "onBind()");
            return new RemoteApi(this);
        }
        return null;
    }

    public boolean deleteUser(int userId) {
        UserManager userManager = UserManager.get(getApplicationContext());
        if (userManager.removeUser(userId)) {
            ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userId);
            childInfoCore.deleteUser();
            return true;
        }
        return false;
    }

    //we remove user present in db but not in the system (timing issue while delete profile)
    public List<Integer> getAllUserId() {
        return getAllUserId(getApplicationContext());
    }

    public void authorizeAppsForUsers(ArrayList<ResolveInfo> appsToEnable, List<ResolveInfo> appsToDisable, int userId, boolean forDefaultList) {
        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userId);
        childInfoCore.authorizeAppsForUsers(appsToEnable, appsToDisable);
    }

    public void setCategoryForApp(String packageName, String activityName, String categroyId) throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        DBRequestHelper dbRequestHelper = new DBRequestHelper(getApplicationContext());
        for (int userId : getAllUserId()) {
            dbRequestHelper.needToReload(Kurio.RELOAD_LAUNCHER_APPS, userId);
        }
        appCategoryDataAccess.setCategoryForApp(packageName, activityName, categroyId);
        clearCategorizationNotificationIfNeeded();
    }

    private void clearCategorizationNotificationIfNeeded() {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        ArrayList<ResolveInfo> uncategorized = appCategoryDataAccess.getAppsToCategorize(getApplicationContext());

        if (uncategorized.isEmpty()) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(PackageInfoReceiver.NOTIF_ID);
        }
    }

    public ArrayList<String[]> getAppCategory() throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getAppCategory();
    }

    public ArrayList<String[]> getAppForCategory(String categoryId) throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getAppForCategory(categoryId);
    }

    public String getCategoryIdForApp(String packageName, String activityName) throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getCategoryIdForApp(packageName, activityName);
    }

    public HashMap<String, String> getCategories() throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getCategories(getApplicationContext());
    }

    public ArrayList<ResolveInfo> getDefaultAppsToEnable() throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getDefaultAppsToEnableAsResolveInfo(getApplicationContext());
    }

    public ArrayList<ResolveInfo> getDefaultAppsToDisable() throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getDefaultAppsToDisableAsResolveInfo(getApplicationContext());
    }

    public ArrayList<ResolveInfo> getAppsToCategorize() throws RemoteException {
        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        return appCategoryDataAccess.getAppsToCategorize(getApplicationContext());
    }

    //PArentPreference
    public void setParentPreferences(String key, String value) throws RemoteException {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(getApplicationContext());
        parentPreferencesDataAccess.setParentPreferences(key, value);
    }

    public String getParentPreferencesForKey(String key) throws RemoteException {
        ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(getApplicationContext());
        return parentPreferencesDataAccess.getParentPreferencesForKey(key);
    }

    /**
     * relace dbrequesthelper
     */


    void needToReload(String keyToReload, int userId) throws RemoteException {
        DBRequestHelper dbRequestHelper = new DBRequestHelper(getApplicationContext());
        dbRequestHelper.needToReload(keyToReload, userId);
    }

    public void importAppsFromOtherChild(int currentChildId, int otherChildId) throws RemoteException {
        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), currentChildId);
        childInfoCore.importAppsFromOtherChild(getApplicationContext(), otherChildId);

    }

    public int createChildFromJson(String childInfoAsJson) throws RemoteException {
        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext());
        return childInfoCore.createFromJson(childInfoAsJson);
    }

    public String getChildAsJson(int userId) throws RemoteException {
        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userId);
        return childInfoCore.toJson();
    }

    public void saveChildInfoFromJson(String childAsJson, int userId) throws RemoteException {
        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userId);
        childInfoCore.initChildFromJson(childAsJson);
        childInfoCore.saveChildInfoInDB();
    }

    /**
     * time slots
     */
    public String getTimeSlotForUser(int userId) throws RemoteException {
        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess(getApplicationContext());
        ArrayList<TimeSlotSetting> timeSlotSettings = timeSlotSettingsDataAccess.getTimeSlotSettingsForUser(userId);

        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("timeSlot", gson.toJsonTree(timeSlotSettings));

        return gson.toJson(jsonObject);
    }

    public void saveTimeSlotsForUser(String timeSlot) throws RemoteException {
        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess(getApplicationContext());

        Gson gson = new GsonBuilder().create();
        try {
            JSONObject jsonObject = new JSONObject(timeSlot);
            ArrayList<TimeSlotSetting> timeSlotSettings = gson.fromJson(jsonObject.get("timeSlot").toString(), new TypeToken<ArrayList<TimeSlotSetting>>() {
            }.getType());

            timeSlotSettingsDataAccess.save(timeSlotSettings);

        } catch (JSONException e) {
            Log.e("TimeSlotSettings : createFromJson", e.getMessage());
        }
    }

    public void saveTimeSlotForUser(String timeSlot) throws RemoteException {
        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess(getApplicationContext());

        Gson gson = new GsonBuilder().create();
        TimeSlotSetting timeSlotSetting = gson.fromJson(timeSlot, TimeSlotSetting.class);
        timeSlotSettingsDataAccess.save(timeSlotSetting);
    }

    public void importTimeSlots(int fromUserId, int toUserId) throws RemoteException {
        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess((getApplicationContext()));
        timeSlotSettingsDataAccess.delete(toUserId);
        ArrayList<TimeSlotSetting> newTimeSlot = timeSlotSettingsDataAccess.getTimeSlotSettingsForUser(fromUserId);
        for (TimeSlotSetting ts : newTimeSlot) {
            ts.setChildId(toUserId);
        }
        timeSlotSettingsDataAccess.save(newTimeSlot);
    }

    public String getWebListForUser(int userId) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        ArrayList<WebListInfo> webList = webListDataAccess.getWebListForUser(userId);

        Gson gson = new GsonBuilder().create();
        return gson.toJson(webList);
    }

    public void addWebListForUsers(String webListInfoAsJson) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        Gson gson = new GsonBuilder().create();

        for (int i : ChildInfoDataAccess.getAllUserId(getApplicationContext())) {
            webListDataAccess.addWebListForUser(gson.fromJson(webListInfoAsJson, WebListInfo.class), i);
        }

    }

    public void deleteWebList(String webListInfoAsJson) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        Gson gson = new GsonBuilder().create();

        webListDataAccess.deleteWebList(gson.fromJson(webListInfoAsJson, WebListInfo.class));
    }

    public void deleteWebListForUser(int userId) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        webListDataAccess.deleteListForUser(userId);
    }

    public void updateWebList(String webListInfoAsJson, int userId) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        Gson gson = new GsonBuilder().create();

        webListDataAccess.updateWebList(gson.fromJson(webListInfoAsJson, WebListInfo.class), userId);
    }

    public void updateWebListUrl(String oldUrl, String newUrl) throws RemoteException {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        webListDataAccess.updateWebListUrl(oldUrl, newUrl);
    }

    public void importWebListFromChild(int fromChild, int toChild) {
        WebListDataAccess webListDataAccess = new WebListDataAccess(getApplicationContext());
        webListDataAccess.importWebListFromChild(fromChild, toChild);
    }

    public void enabledGoogleAccount(boolean enabled, int userId) throws RemoteException {
        ChildInfoCore.enabledGoogleAccount(enabled, userId);
    }

    public boolean isGoogleAccountEnabled(int userId) throws RemoteException {
        return ChildInfoCore.isGoogleAccountEnabled(getApplicationContext(), userId);
    }

    public boolean appsNeedGoogleAccount(ArrayList<ResolveInfo> appsToCheck) throws RemoteException {
        return ChildInfoCore.appsNeedGoogleAccount(getApplicationContext(), appsToCheck);
    }

    public void increaseAnalyticsUsage(String usageKey) {
        AnalyticsManager.getInstance().increaseUsage(getApplicationContext(), usageKey);
    }

    public void timeManagementUsedOnce() {
        AnalyticsManager.getInstance().timeManagementUsedOnce(getApplicationContext());
    }

    public void increaseAppAnalyticsValueForKey(String packageName, String key) {
        AnalyticsManager.getInstance().increaseAppAnalyticsValueForKey(getApplicationContext(), packageName, key);
    }
}
