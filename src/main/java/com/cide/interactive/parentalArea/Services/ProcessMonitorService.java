package com.cide.interactive.parentalArea.Services;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.widget.Toast;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.R;
import com.cide.interactive.parentalArea.TimeControl.TimeController;

import java.util.Calendar;

public class ProcessMonitorService extends Service {
    private static final String TAG = "ProcessMonitor";
    private static String mPreviousPackage = null;

    private static TimeController mTimeController;
    private static boolean mServiceIsRunning = false;
    private static long lastTimechecked = 0;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private BroadcastReceiver mScreenOnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (action.equals(Intent.ACTION_USER_BACKGROUND)
                            || action.equals(Intent.ACTION_SCREEN_OFF)) {
                        mTimeController.startRecording();
                        long spent = Calendar.getInstance().getTimeInMillis() - lastTimechecked;
                        if (mPreviousPackage != null) {
                            updateTimeSpent(mPreviousPackage, spent);
                            mPreviousPackage = null;
                        }
                    }
                }
            }).start();

        }
    };
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {

        @Override
        public void onForegroundActivitiesChanged(int i, int i2, boolean b) throws RemoteException {
            frontActivityChanged();
        }

        public void onImportanceChanged(int i, int i2, int i3) throws RemoteException {

        }

        @Override
        public void onProcessDied(int i, int i2) throws RemoteException {

        }

        public void onProcessStateChanged(int i, int i2, int i3) throws RemoteException {

        }

    };

    public ProcessMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the sService.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!mServiceIsRunning) {
            mServiceIsRunning = true;
            mTimeController = new TimeController(ProcessMonitorService.this);
            IntentFilter filter = new IntentFilter(Intent.ACTION_USER_BACKGROUND);
            filter.addAction(Intent.ACTION_USER_FOREGROUND);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(mScreenOnOffReceiver, filter);
            try {
                ActivityManagerNative.getDefault().registerProcessObserver(mProcessObserver);
            } catch (RemoteException e) {
                Log.w(TAG, e.getMessage(), e);
            }
        }
        frontActivityChanged();


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mServiceIsRunning = false;
        unregisterReceiver(mScreenOnOffReceiver);
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(mProcessObserver);
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage(), e);
        }
        super.onDestroy();
    }

    private void disableSettings() {
        PackageManager pm = getPackageManager();
//        pm.setComponentEnabledSetting(new ComponentName(this, BlockSettingActivity.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        pm.setApplicationEnabledSetting("com.android.settings",
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
    }

    private void updateLaunchCount(final String packageName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver =
                        Utils.getOwnerContext(getApplicationContext()).getContentResolver();

                String request = "update " + DBUriManager.ACTIVITY_STATUS_TABLE + " set "
                        + DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT
                        + " = " + DBUriManager.ACTIVITY_STATUS_LAUNCH_COUNT + " + 1 "
                        + " where " + DBUriManager.ACTIVITY_STATUS_UID
                        + " = " + getApplicationContext().getUserId()
                        + " and " + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME
                        + " = " + DatabaseUtils.sqlEscapeString(packageName);
                contentResolver.call(DBUriManager.CONTENT_URI_CALL,
                        DBUriManager.METHOD_UPDATE_TABLE, request, null);

                ParentPreferencesDataAccess parentPreferencesDataAccess =
                        new ParentPreferencesDataAccess(getApplicationContext());
                parentPreferencesDataAccess.setParentPreferences(
                        DBUriManager.ACTIVITY_STATUS_TABLE, String.valueOf(true));
            }
        }).start();
    }

    private void updateTimeSpent(final String packageName, final long timeSpentInMillis) {
        Log.d(TAG, "Time Spent:" + timeSpentInMillis / 1000 + "s on " + packageName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver contentResolver =
                        Utils.getOwnerContext(getApplicationContext()).getContentResolver();

                String request = "update " + DBUriManager.ACTIVITY_STATUS_TABLE + " set "
                        + DBUriManager.ACTIVITY_STATUS_TIME_SPENT + " = "
                        + DBUriManager.ACTIVITY_STATUS_TIME_SPENT + " + "
                        + Math.round(timeSpentInMillis / 1000f)
                        + " where " + DBUriManager.ACTIVITY_STATUS_UID
                        + " = " + getApplicationContext().getUserId()
                        + " and " + DBUriManager.ACTIVITY_STATUS_PACKAGE_NAME
                        + " = " + DatabaseUtils.sqlEscapeString(packageName);
                contentResolver.call(DBUriManager.CONTENT_URI_CALL,
                        DBUriManager.METHOD_UPDATE_TABLE, request, null);

                ParentPreferencesDataAccess parentPreferencesDataAccess =
                        new ParentPreferencesDataAccess(getApplicationContext());
                parentPreferencesDataAccess.setParentPreferences(
                        DBUriManager.ACTIVITY_STATUS_TABLE, String.valueOf(true));

            }
        }).start();
    }

    private void frontActivityChanged() {
        try {
            if (getUserId() != ActivityManagerNative.getDefault().getCurrentUser().id) {
                return;
            }
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage(), e);
            return;
        }

        // Renew Global ChildInfo
        ChildInfoCore.getCurrentChildInfo(getApplicationContext(), true);

        String currentPackage = mTimeController.getFrontActivity().getPackageName();

        if (!ChildInfoCore.isGoogleAccountEnabled(getApplicationContext(), getUserId()) &&
                ChildInfoCore.appsNeedGoogleAccount(getApplicationContext(), currentPackage)) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), getString(R.string.launch_app_google_account_deactivate), 2).show();
                }
            });
        }

        mTimeController.startRecording();

        Log.d(TAG, "FrontPackage:" + mTimeController.getFrontActivity().toString());
        if (currentPackage != null && !currentPackage.equals(mPreviousPackage)) {
            long nowInMillis = Calendar.getInstance().getTimeInMillis();

            // Disable Android Setting when user came back to launcher
            if (mTimeController.isLauncher(currentPackage)) {
                disableSettings();
            }

            if (mPreviousPackage != null) {
                updateTimeSpent(mPreviousPackage, nowInMillis - lastTimechecked);
            }

            updateLaunchCount(currentPackage);
            lastTimechecked = nowInMillis;
            mPreviousPackage = currentPackage;
        }
    }
}