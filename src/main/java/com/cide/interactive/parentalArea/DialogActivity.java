package com.cide.interactive.parentalArea;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.UserManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.parentalArea.Analytics.AnalyticsManager;
import com.cide.interactive.parentalArea.Api.RemoteApiService;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.DBRequestHelper;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.Receivers.PackageInfoReceiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * Created by lionel on 12/12/13.
 * <p/>
 * manage pop up when an app is installed to categorized it
 * the pop up change if its already displayed and a new app nedd to be categorized
 */
public class DialogActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "DialogActivity";
    private static final int MAX_CHILD_PER_ROW = 4;

    ResolveInfo mCurrentApp;
    ArrayList<ResolveInfo> mCurrentAppsInQueue = new ArrayList<>(); // store only different packages
    ArrayList<ResolveInfo> mAllAppsInQueue = new ArrayList<>(); // store all launchable activities
    List<ResolveInfo> mBrowserApps;

    HashMap<String, String> mCategories = new HashMap<>();

    LinearLayout mFirstBtnLL;
    LinearLayout mChildProfilesLLFirstLine;
    LinearLayout mChildProfilesLLSecondLine;

    ArrayList<UserInfo> mChildSelected;

    List<UserInfo> mUsersInfo;
    UserManager mUserManager;

    CategoryButtonCheck mBtnPreviousSelectedCategory;
    CategoryButtonCheck mBtnCurrentSelectedCategory;

    TextView mTVTitle;
    ImageView mIVAppCategory;
    TextView mWarningBrowser;
    PackageManager mpm;
    AppCategoryDataAccess mAppCategoryDataAccess;
    boolean mIsCancelClicked = false;

    private static boolean findActivitiesForPackage(Context context,
                                                    String packageName, int userId) {
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivitiesAsUser(
                mainIntent, 0, userId);
        return apps != null && apps.size() > 0;
    }

    private void createAppListToCategorized() {

        AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());
        mAllAppsInQueue = appCategoryDataAccess.getAppsToCategorize(getApplicationContext());

        for (ResolveInfo ri : mAllAppsInQueue) {
            boolean found = false;
            for (ResolveInfo resolveInfo : mCurrentAppsInQueue) {
                if (resolveInfo.activityInfo.packageName.equals(ri.activityInfo.packageName)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                mCurrentAppsInQueue.add(ri);
            }
        }

        mBrowserApps = AppManagementUtil.findBrowserActivities(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_category_dialog);
        mAppCategoryDataAccess = new AppCategoryDataAccess(getApplicationContext());

        mChildSelected = new ArrayList<>();

        mUserManager = (UserManager) getSystemService(Context.USER_SERVICE);

        mUsersInfo = mUserManager.getUsers();

        //sort child in same order as profile selection
        Collections.sort(mUsersInfo, new Comparator<UserInfo>() {

            @Override
            public int compare(UserInfo lhs, UserInfo rhs) {
                if (lhs.creationTime >= rhs.creationTime) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        mpm = getPackageManager();

        createAppListToCategorized();

        if (mCurrentAppsInQueue.size() == 0) {
            finish();
            return;
        }

        mCurrentApp = mCurrentAppsInQueue.remove(mCurrentAppsInQueue.size() - 1);

        mFirstBtnLL = (LinearLayout) findViewById(R.id.llFirstLineBtnCategory);
        mChildProfilesLLFirstLine = (LinearLayout) findViewById(R.id.llChildProfilesFirstLine);
        mChildProfilesLLSecondLine = (LinearLayout) findViewById(R.id.llChildProfilesSecondLine);
        mTVTitle = (TextView) findViewById(R.id.tvTitle);
        mIVAppCategory = (ImageView) findViewById(R.id.ivAppCategory);
        mWarningBrowser = (TextView) findViewById(R.id.warning_browser);

        //set Text for one app
        if (mCurrentApp != null) {
            createIconAndTitleForApp(mCurrentApp);
            checkForBrowserWarning(mCurrentApp);
        }

        mCategories = AppCategoryDataAccess.getCategoriesAttribuable(getApplicationContext());

        createButtonForCategory();
        createChildIconSelection();

        findViewById(R.id.negative_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsCancelClicked = true;
                checkQueue();
                mChildSelected.clear();
            }
        });

        findViewById(R.id.positive_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag;

                for (ResolveInfo ri : mAllAppsInQueue) {
                    if (ri.activityInfo.packageName.equals(mCurrentApp.activityInfo.packageName)) {

                        //to categorize all apps for this package
                        List<ResolveInfo> allAppsForPackage = AppManagementUtil.findLaunchableActivitiesForPackage(getApplicationContext(),
                                ri.activityInfo.packageName, PackageManager.GET_DISABLED_COMPONENTS);

                        //if a category is selected
                        if (mBtnCurrentSelectedCategory != null) {
                            tag = (String) mBtnCurrentSelectedCategory.getTag();

                            if (tag.equals(Kurio.APP_CATEGORY_UNCATEGORIZED_ID)) {
                                mAppCategoryDataAccess.removeCategoryForApp(ri.activityInfo.packageName,
                                        ri.activityInfo.name);
                            } else {

                                for (ResolveInfo riApp : allAppsForPackage) {
                                    mAppCategoryDataAccess.removeCategoryForApp(riApp.activityInfo.packageName,
                                            riApp.activityInfo.name);

                                    mAppCategoryDataAccess.setCategoryForApp(riApp.activityInfo.packageName,
                                            riApp.activityInfo.name, tag);
                                }
                            }

                            DBRequestHelper dbRequestHelper = new DBRequestHelper(getApplicationContext());
                            for (int userId : RemoteApiService.getAllUserId(getApplicationContext())) {
                                dbRequestHelper.needToReload(Kurio.RELOAD_LAUNCHER_APPS, userId);
                            }
                        }

                        ChildInfoDataAccess childInfoDataAccess = new ChildInfoDataAccess(getApplicationContext());
                        for (UserInfo userInfo : mUsersInfo) {
                            if (ChildInfoCore.isChild(getApplicationContext(), userInfo.id)) {
                                if (mChildSelected.contains(userInfo)) {
                                    for (ResolveInfo riApp : allAppsForPackage) {
                                        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userInfo.id);
                                        childInfoCore.enableAppFromPopupCategory(riApp);
                                    }
                                } else {
                                    for (ResolveInfo riApp : allAppsForPackage) {
                                        ChildInfoCore childInfoCore = new ChildInfoCore(getApplicationContext(), userInfo.id);
                                        childInfoCore.disableActivity(
                                                riApp.activityInfo.packageName, riApp.activityInfo.name, true);
                                    }

                                    childInfoDataAccess.savePackageStatusInDB((ArrayList) allAppsForPackage, new ArrayList<ResolveInfo>(), userInfo.id);
                                }
                                AnalyticsManager.getInstance().setNeedToSendAnalytics(getApplicationContext(), true, DBUriManager.ACTIVITY_STATUS_TABLE);
                            }
                        }
                        break;
                    }
                }
                mChildSelected.clear();
                checkQueue();
            }
        });
    }

    private void checkQueue() {
        if (mCurrentAppsInQueue.size() > 0) {

            mCurrentApp = mCurrentAppsInQueue.remove(mCurrentAppsInQueue.size() - 1);

            createIconAndTitleForApp(mCurrentApp);
            createButtonForCategory();
            createChildIconSelection();

            checkForBrowserWarning(mCurrentApp);

        } else {
            //if no more apps to categorized and the user hasnt press cancel at least one time
            // we remove the notification
            if (!mIsCancelClicked) {
                clearNotification();
            }
            finish();
        }
    }

    private void createIconAndTitleForApp(ResolveInfo ri) {
        mTVTitle.setText(ri.activityInfo.applicationInfo.loadLabel(mpm));

        Bitmap tmpIcon = ((BitmapDrawable) ri.activityInfo.applicationInfo.loadIcon(mpm)).getBitmap();
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int density = activityManager.getLauncherLargeIconDensity();

        if (tmpIcon.getDensity() > density) {
            Bitmap newIcon = tmpIcon.createScaledBitmap(tmpIcon, density, density, true);
            tmpIcon = newIcon;
        }

        mIVAppCategory.setImageBitmap(tmpIcon);
    }

    private void createButtonForCategory() {

        mFirstBtnLL.removeAllViews();

        for (String key : mCategories.keySet()) {

            CategoryButtonCheck categoryBtn = new CategoryButtonCheck(this, key);
            int name = getResources().getIdentifier("category_" + key, "string", getApplicationContext().getPackageName());
            categoryBtn.setIconName(getResources().getString(name));
            categoryBtn.setTag(key);
            categoryBtn.setOnClickListener(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            categoryBtn.setLayoutParams(lp);

            String categoryId = mAppCategoryDataAccess.getCategoryIdForApp(mCurrentApp.activityInfo.packageName, mCurrentApp.activityInfo.name);

            if (key.equals(categoryId)) {
                categoryBtn.callOnClick();
            }

            mFirstBtnLL.addView(categoryBtn);
        }
    }

    private void createChildIconSelection() {

        mChildProfilesLLFirstLine.removeAllViews();
        mChildProfilesLLSecondLine.removeAllViews();

        int count = 0;

        for (int i = 0; i < mUsersInfo.size(); i++) {

            if (!ChildInfoCore.isChild(getApplicationContext(), mUsersInfo.get(i).id)) {
                continue;
            }

            ChildButtonCheck childBtn = new ChildButtonCheck(this, mUserManager.getUserIcon(mUsersInfo.get(i).id));
            childBtn.setIconName(mUsersInfo.get(i).name);
            childBtn.setTag(i);
            childBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tag = (Integer) v.getTag();

                    if (mChildSelected.contains(mUsersInfo.get(tag))) {
                        mChildSelected.remove(mUsersInfo.get(tag));
                        ((ChildButtonCheck) v).setCheck(false);
                    } else {
                        mChildSelected.add(mUsersInfo.get(tag));
                        ((ChildButtonCheck) v).setCheck(true);
                    }
                }
            });

            if (findActivitiesForPackage(getApplicationContext(), mCurrentApp.activityInfo.packageName,
                    mUsersInfo.get(i).id)) {
                childBtn.callOnClick();
            }

            if (count < MAX_CHILD_PER_ROW) {
                mChildProfilesLLFirstLine.addView(childBtn);
            } else {
                mChildProfilesLLSecondLine.addView(childBtn);
            }

            count++;
        }

        if (count == 0) {
            findViewById(R.id.tvChildSelection).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        //used to change color of displayed categorized btn
        mBtnCurrentSelectedCategory = (CategoryButtonCheck) v;
        mBtnCurrentSelectedCategory.setCheck(true);

        if (mBtnPreviousSelectedCategory != null &&
                mBtnPreviousSelectedCategory != mBtnCurrentSelectedCategory) {
            mBtnPreviousSelectedCategory.setCheck(false);
        }

        mBtnPreviousSelectedCategory = mBtnCurrentSelectedCategory;
    }

    private void clearNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(null, PackageInfoReceiver.NOTIF_ID);
    }


    private boolean isAppEnabled(String packageName, int userId) {
        return false;
    }

    private boolean checkForBrowserWarning(ResolveInfo resolveInfo) {
        boolean isWarning = false;
        for (ResolveInfo browser : mBrowserApps) {
            if (!browser.activityInfo.packageName.equalsIgnoreCase(Kurio.PACKAGE_DEFAULT_BROWSER) &&
                    browser.activityInfo.packageName.equals(resolveInfo.activityInfo.packageName)) {
                isWarning = true;
            }
        }

        if (isWarning) {
            mWarningBrowser.setVisibility(View.VISIBLE);
        } else {
            mWarningBrowser.setVisibility(View.GONE);
        }

        return isWarning;
    }
}
