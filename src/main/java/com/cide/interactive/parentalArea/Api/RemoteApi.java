package com.cide.interactive.parentalArea.Api;

import android.content.pm.ResolveInfo;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by leehack on 31/10/14.
 */
public class RemoteApi extends IRemoteApi.Stub {

    private final RemoteApiService apiService;

    public RemoteApi(RemoteApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public boolean deleteUser(int userId) {
        return apiService.deleteUser(userId);
    }

    @Override
    public List<Integer> getAllUserId() throws RemoteException {
        return apiService.getAllUserId();
    }

    @Override
    public void authorizeAppsForUsers(List activityInfoToEnable, List appsToDisable, int userId, boolean forDefaultList) throws RemoteException {
        apiService.authorizeAppsForUsers((ArrayList) activityInfoToEnable, appsToDisable, userId, forDefaultList);
    }

    //app Category

    public void setCategoryForApp(String packageName, String activityName, String categroyId) throws RemoteException {
        apiService.setCategoryForApp(packageName, activityName, categroyId);
    }

    public ArrayList<String[]> getAppCategory() throws RemoteException {
        return apiService.getAppCategory();
    }

    public ArrayList<String[]> getAppForCategory(String categoryId) throws RemoteException {
        return apiService.getAppForCategory(categoryId);
    }

    public String getCategoryIdForApp(String packageName, String activityName) throws RemoteException {
        return apiService.getCategoryIdForApp(packageName, activityName);
    }

    public HashMap<String, String> getCategories() throws RemoteException {
        return apiService.getCategories();
    }

    public ArrayList<ResolveInfo> getDefaultAppsToEnable() throws RemoteException {
        return apiService.getDefaultAppsToEnable();
    }

    public ArrayList<ResolveInfo> getDefaultAppsToDisable() throws RemoteException {
        return apiService.getDefaultAppsToDisable();
    }

    public ArrayList<ResolveInfo> getAppsToCategorize() throws RemoteException {
        return apiService.getAppsToCategorize();
    }

    //PArentPreference
    public void setParentPreferences(String key, String value) throws RemoteException {
        apiService.setParentPreferences(key, value);
    }

    public String getParentPreferencesForKey(String key) throws RemoteException {
        return apiService.getParentPreferencesForKey(key);
    }


    public void needToReload(String keyToReload, int userId) throws RemoteException {
        apiService.needToReload(keyToReload, userId);
    }

    public void importAppsFromOtherChild(int currentChildId, int otherChildId) throws RemoteException {
        apiService.importAppsFromOtherChild(currentChildId, otherChildId);
    }

    public int createChildFromJson(String childInfoAsJson) throws RemoteException {
        return apiService.createChildFromJson(childInfoAsJson);
    }

    public String getChildAsJson(int userId) throws RemoteException {
        return apiService.getChildAsJson(userId);
    }

    public void saveChildInfoFromJson(String childAsJson, int userId) throws RemoteException {
        apiService.saveChildInfoFromJson(childAsJson, userId);
    }

    public String getTimeSlotForUser(int userId) throws RemoteException {

        return apiService.getTimeSlotForUser(userId);
    }

    public void saveTimeSlotsForUser(String timeSlot) throws RemoteException {
        apiService.saveTimeSlotsForUser(timeSlot);
    }

    public void saveTimeSlotForUser(String timeSlot) throws RemoteException {
        apiService.saveTimeSlotForUser(timeSlot);
    }

    public void importTimeSlots(int fromUserId, int toUserId) throws RemoteException {
        apiService.importTimeSlots(fromUserId, toUserId);
    }

    public String getWebListForUser(int userId) throws RemoteException {
        return apiService.getWebListForUser(userId);
    }

    public void addWebListForUsers(String webListInfoAsJson) throws RemoteException {
        apiService.addWebListForUsers(webListInfoAsJson);
    }

    public void deleteWebList(String webListInfoAsJson) throws RemoteException {
        apiService.deleteWebList(webListInfoAsJson);
    }

    public void deleteWebListForUser(int userId) throws RemoteException {
        apiService.deleteWebListForUser(userId);
    }

    public void updateWebList(String webListInfoAsJson, int userId) throws RemoteException {
        apiService.updateWebList(webListInfoAsJson, userId);
    }

    public void updateWebListUrl(String oldUrl, String newUrl) throws RemoteException {
        apiService.updateWebListUrl(oldUrl, newUrl);
    }

    public void importWebListFromChild(int fromChild, int toChild) {
        apiService.importWebListFromChild(fromChild, toChild);
    }

    public void enabledGoogleAccount(boolean enabled, int userId) throws RemoteException {
        apiService.enabledGoogleAccount(enabled, userId);
    }

    public boolean isGoogleAccountEnabled(int userId) throws RemoteException {
        return apiService.isGoogleAccountEnabled(userId);
    }

    public boolean appsNeedGoogleAccount(List appsToCheck) throws RemoteException {
        return apiService.appsNeedGoogleAccount((ArrayList)appsToCheck);
    }

    public void increaseAnalyticsUsage(String usageKey) {
        apiService.increaseAnalyticsUsage(usageKey);
    }

    public void timeManagementUsedOnce() {
        apiService.timeManagementUsedOnce();
    }

    public void increaseAppAnalyticsValueForKey(String packageName, String key) {
        apiService.increaseAppAnalyticsValueForKey(packageName, key);
    }
}
