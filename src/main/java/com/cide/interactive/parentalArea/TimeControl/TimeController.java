package com.cide.interactive.parentalArea.TimeControl;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.RemoteException;
import android.view.WindowManagerGlobal;

import com.cide.interactive.kuriolib.AppManagementUtil;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.TimeSlotSetting;
import com.cide.interactive.parentalArea.BuildConfig;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.TimeSlotSettingsDataAccess;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeController {

    public final static int MINUTES_IN_MILLIS = 60000; // 60*1000
    public static final String EXTRA_TIME = "EXTRA_TIME";
    private static final String TAG = "TimeController";
    private static final String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";
    private static final String LAST_CHECKED_TIME = "LAST_CHECKED_TIME";
    private static final String PLAYED_TIME = "PLAYED_TIME";
    private static final String PLAYED_SESSION_TIME = "PLAYED_SESSION_TIME";
    private static final String PAUSED_TIME = "PAUSED_TIME";
    private static final String SESSION_LOCKED = "SESSION_LOCKED";
    private static final String IS_PAUSED = "IS_PAUSED";
    private static SharedPreferences mSharedPreferences;
    private static Intent mTimeLockIntent = null, mTimeWarningIntent = null;
    private static Context mContext;
    private static Handler timeLockTimer = new Handler();
    private static ActivityManager activityManager;

    public TimeController(Context context) {

        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static ComponentName getFrontActivity() {

        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName currentPackage = null;
        if (taskInfo != null) {
            currentPackage = taskInfo.get(0).topActivity;
        }
        return currentPackage;
    }

    public static boolean isLauncher(String packageName) {
        List<ResolveInfo> launchers = AppManagementUtil.findAllLauncher(mContext);
        for (ResolveInfo launcher : launchers) {
            if (packageName.equals(launcher.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isKurioSetting(String packageName) {
        final Intent intent = new Intent("com.cide.interactive.action.KURIO_SETTING");
        PackageManager pm = mContext.getPackageManager();
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (packageName.equals(info.activityInfo.packageName)) {
                return true;
            }

        }
        return false;
    }

    public static boolean isPreloader(String packageName) {
        final Intent intent = new Intent("com.cide.interactive.action.PRELOADER");
        PackageManager pm = mContext.getPackageManager();
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (packageName.equals(info.activityInfo.packageName)) {
                return true;
            }

        }
        return false;
    }

    private static boolean needToPause(ComponentName frontPackageName, boolean forEducational) {
        if (frontPackageName != null) {
            // Launcher is allowed even it's locked
            if (isLauncher(frontPackageName.getPackageName())) {
                return true;
            }
            // Parental Setting is allowed even it's locked
            if (isKurioSetting(frontPackageName.getPackageName())) {
                return true;
            }
            // Preloader is allowed even it's locked
            if (isPreloader(frontPackageName.getPackageName())) {
                return true;
            }
            // Parental Core is allowed even it's locked
            if (mContext.getPackageName().equals(frontPackageName.getPackageName())) {
                return true;
            }
            // Android setting is allowed even it's locked
            if (frontPackageName.getPackageName().equals("com.android.settings")) {
                return true;
            }
            if (frontPackageName.getPackageName().equals("android")) {
                return true;
            }
            if (frontPackageName.getPackageName().equals("com.android.systemui")) {
                return true;
            }
        }

        try {
            if (WindowManagerGlobal.getWindowManagerService().isKeyguardLocked()) {
                return true;
            }
        } catch (RemoteException e) {
            Log.w(TAG, e.getMessage(), e);
        }

        if (forEducational) {
            AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(mContext);
            if (frontPackageName != null && appCategoryDataAccess.checkAppIsThisCategory(frontPackageName.getPackageName(), Kurio.APP_CATEGORY_EDUCATIONAL)) {
                return true;
            }
        }

        return false;
    }

    private static void savePreference(int day, long lastChecked, long playedTime, long sessionPlayed,
                                       long pausedTime, boolean isPaused) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(CURRENT_DAY_OF_WEEK, day);
        editor.putLong(LAST_CHECKED_TIME, lastChecked);
        editor.putLong(PLAYED_TIME, playedTime);
        editor.putLong(PLAYED_SESSION_TIME, sessionPlayed);
        editor.putLong(PAUSED_TIME, pausedTime);
        editor.putBoolean(IS_PAUSED, isPaused);

        if (BuildConfig.DEBUG) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(lastChecked);
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
            Log.d(TAG, "CURRENT_DAY_OF_WEEK:" + day);
            Log.d(TAG, "LAST_CHECKED_TIME:" + format.format(cal.getTime()));
            Log.d(TAG, "PLAYED_TIME:" + playedTime / 1000 + 's');
            Log.d(TAG, "PLAYED_SESSION_TIME:" + sessionPlayed / 1000 + 's');
            Log.d(TAG, "PAUSED_TIME:" + pausedTime / 1000 + 's');
            Log.d(TAG, "IS_PAUSED:" + isPaused);
            Log.d(TAG, "EXTRA_TIME:" + mSharedPreferences.getLong(EXTRA_TIME, 0));
        }

        editor.apply();
    }

    private static void removeTimeLockTimer() {
        timeLockTimer.removeCallbacksAndMessages(null);
    }

    public static long checkLeftPausedTime(TimeSlotSetting timeSlotSetting, long pausedTime) {

        long sessionResetTime = timeSlotSetting.getRestTime() * MINUTES_IN_MILLIS;

        if (sessionResetTime > pausedTime) {
            return sessionResetTime - pausedTime;
        }
        return 0;
    }

    public static long checkLeftSessionTime(TimeSlotSetting timeSlotSetting, long playedTime) {

        long playSessionTimeLimit = timeSlotSetting.getSessionTime() * MINUTES_IN_MILLIS;

        if (playSessionTimeLimit > playedTime) {
            return playSessionTimeLimit - playedTime;
        }
        return 0;
    }

    public static long checkLeftPlayTime(TimeSlotSetting timeSlotSetting, long playedTime) {

        long playTimeLimit = timeSlotSetting.getPlayTime() * MINUTES_IN_MILLIS;

        if (playTimeLimit > playedTime) {
            return playTimeLimit - playedTime;
        }
        return 0;
    }

    public static long checkTimeSlotLeftTime(TimeSlotSetting timeSlotSetting, Calendar now) {

        ArrayList<String[]> timeSlots = timeSlotSetting.getTimeSlot();
        int nowTimeInMinute = now.get(Calendar.MINUTE) + (now.get(Calendar.HOUR_OF_DAY) * 60);

        for (String[] slot : timeSlots) {
            String[] startHourAndMinute = slot[0].split(":");
            String[] endHourAndMinute = slot[1].split(":");
            int limitedStartTimeInMinute;
            int limitedEndTimeInMinute;

            if (startHourAndMinute.length > 1) {
                limitedStartTimeInMinute = (Integer.parseInt(startHourAndMinute[0]) * 60)
                        + Integer.parseInt(startHourAndMinute[1]);
            } else {
                limitedStartTimeInMinute = Integer.parseInt(startHourAndMinute[0]) * 60;
            }

            if (endHourAndMinute.length > 1) {
                limitedEndTimeInMinute = (Integer.parseInt(endHourAndMinute[0]) * 60)
                        + Integer.parseInt(endHourAndMinute[1]);
            } else {
                limitedEndTimeInMinute = Integer.parseInt(endHourAndMinute[0]) * 60;
            }

            if (nowTimeInMinute > limitedStartTimeInMinute
                    && nowTimeInMinute < limitedEndTimeInMinute) {
                return (limitedEndTimeInMinute - nowTimeInMinute) * MINUTES_IN_MILLIS;
            }
        }
        return 0;
    }

    private static void createTimeWarning(final long leftTimeSlot, final long leftPlayTime,
                                          final long leftSessionTime, final long leftResetTime) {
        mTimeWarningIntent = new Intent(mContext, TimeWarningService.class);
        mTimeWarningIntent.putExtra("SESSION_LEFT", leftSessionTime);
        mTimeWarningIntent.putExtra("PLAYTIME_LEFT", leftPlayTime);
        mTimeWarningIntent.putExtra("RESET_LEFT", leftResetTime);
        mTimeWarningIntent.putExtra("TIMESLOT_LEFT", leftTimeSlot);
        mTimeWarningIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startService(mTimeWarningIntent);
    }

    private static void createTimeLock() {
        if (mTimeLockIntent == null) {
            Log.d(TAG, "CREATE TIME LOCK");
            mTimeLockIntent = new Intent(mContext, TimeLockService.class);
            mTimeLockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startService(mTimeLockIntent);
        }
    }

    private static void dismissTimeLock() {
        if (mTimeLockIntent != null) {
            Log.d(TAG, "DISMISS TIME LOCK");
            mContext.stopService(mTimeLockIntent);
            mTimeLockIntent = null;
        }
    }

    private static void dismissTimeWarning() {
        if (mTimeWarningIntent != null) {
            mContext.stopService(mTimeWarningIntent);
            mTimeWarningIntent = null;
        }
    }

    private static void dissmissAll() {
        dismissTimeLock();
        dismissTimeWarning();
    }

    public synchronized void startRecording() {

        Calendar now = Calendar.getInstance();
        long nowInMillis = now.getTimeInMillis();

        int lastDayOfWeek = mSharedPreferences.getInt(CURRENT_DAY_OF_WEEK, 0);
        long lastChecked = mSharedPreferences.getLong(LAST_CHECKED_TIME, 0);
        long playedTime = mSharedPreferences.getLong(PLAYED_TIME, 0);
        long sessionPlayed = mSharedPreferences.getLong(PLAYED_SESSION_TIME, 0);
        long pausedTime = mSharedPreferences.getLong(PAUSED_TIME, 0);
        long extraTime = mSharedPreferences.getLong(EXTRA_TIME, 0);
        boolean isPaused = mSharedPreferences.getBoolean(IS_PAUSED, false);

        TimeSlotSetting timeSlotSetting =
                new TimeSlotSettingsDataAccess(mContext).getTimeSlotSettingsForUser(
                        mContext.getUserId(), now.get(Calendar.DAY_OF_WEEK));

        TimeSlotSetting timeSlotSettingAllDays =
                new TimeSlotSettingsDataAccess(mContext).getTimeSlotSettingsForUser(
                        mContext.getUserId(), 0);

        if (timeSlotSetting == null || timeSlotSettingAllDays == null) {
            Log.e(TAG, "timeslotDB is not correctly setup");
            return;
        }

        ComponentName frontPackageName = getFrontActivity();

        if (timeSlotSettingAllDays.isEnabled() &&
                !(ChildInfoCore.appsNeedGoogleAccount(mContext, frontPackageName.getPackageName()) && !ChildInfoCore.isGoogleAccountEnabled(mContext, mContext.getUserId()))) {
            // Use Allday setting if the day setting is not enabled
            if (!timeSlotSetting.isEnabled()) {
                timeSlotSetting = timeSlotSettingAllDays;
            }

            boolean pause = needToPause(frontPackageName, timeSlotSettingAllDays.isEducational());

            if (pause) {
                Log.d(TAG, "Start Recording for pause");
                dissmissAll();
            } else if (mTimeLockIntent != null) {
                pause = true;
                Log.d(TAG, "Start Recording for pause as it's locked");
            } else {
                Log.d(TAG, "Start Recording for resume");
            }

            if (lastChecked <= nowInMillis && lastDayOfWeek == now.get(Calendar.DAY_OF_WEEK)) {
                if (isPaused) {
                    // Check paused time
                    Log.d(TAG, "Pause -> Pause or Resume");
                    pausedTime = pausedTime + nowInMillis - lastChecked;
                } else {
                    // Check played time
                    Log.d(TAG, "Resume -> Pause or Resume");
                    playedTime = playedTime + nowInMillis - lastChecked;
                    sessionPlayed = sessionPlayed + nowInMillis - lastChecked;
                }
                lastChecked = nowInMillis;
            } else {
                Log.d(TAG, "Reset locks for day change or time rewinded");
                lastDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
                lastChecked = nowInMillis;
                playedTime = 0;
                sessionPlayed = 0;
                pausedTime = 0;
            }

            long leftTimeSlot = checkTimeSlotLeftTime(timeSlotSetting, now);
            long leftPlayTime = checkLeftPlayTime(timeSlotSetting, playedTime);
            long leftSessionTime = checkLeftSessionTime(timeSlotSetting, sessionPlayed);
            long leftResetTime = checkLeftPausedTime(timeSlotSetting, pausedTime);
            long leftExtraTime = extraTime - nowInMillis;

            if (!pause) {
                if (leftSessionTime == 0) {
                    Log.d(TAG, "Session locked. resetLeft:" + leftResetTime);
                    if (leftResetTime == 0) {
                        // Time to unlock Session lock
                        Log.d(TAG, "Session just unlocked");
                        sessionPlayed = 0;
                        pausedTime = 0;
                    }
                } else {
                    // Give play session time as it has been paused.
                    Log.d(TAG, "Give more session time");
                    if (pausedTime < sessionPlayed) {
                        sessionPlayed -= pausedTime;
                    } else {
                        sessionPlayed = 0;
                    }
                    pausedTime = 0;
                }
                leftSessionTime = checkLeftSessionTime(timeSlotSetting, sessionPlayed);
                leftResetTime = checkLeftPausedTime(timeSlotSetting, pausedTime);

                setTimeLock(leftTimeSlot, leftPlayTime, leftSessionTime, leftResetTime, leftExtraTime);
            } else {
                removeTimeLockTimer();
            }
            savePreference(lastDayOfWeek, lastChecked, playedTime,
                    sessionPlayed, pausedTime, pause);
        } else {
            Log.d(TAG, "timecontrol disabled");
            dissmissAll();
        }
    }

    private void setTimeLock(final long leftTimeSlot
            , final long leftPlayTime
            , final long leftSessionTime
            , final long leftResetTime
            , final long leftExtraTime) {

        long leftTime;

        if (leftExtraTime > 0) {
            leftTime = leftExtraTime;
        } else if (leftTimeSlot <= leftPlayTime && leftTimeSlot <= leftSessionTime) {
            leftTime = leftTimeSlot;
        } else if (leftPlayTime <= leftTimeSlot && leftPlayTime <= leftSessionTime) {
            leftTime = leftPlayTime;
        } else {
            leftTime = leftSessionTime;
        }

        timeLockTimer.removeCallbacksAndMessages(null);

        if (leftTime == 0) {
            Log.d(TAG, "Locked!");
            dismissTimeWarning();
            createTimeLock();
        } else if (leftTime <= 5 * MINUTES_IN_MILLIS) {
            // Display warnning and update it every 1 min
            Log.d(TAG, leftTime / 1000 + "s left to be locked");
            long postDelayTime = MINUTES_IN_MILLIS;

            if (leftPlayTime < MINUTES_IN_MILLIS) {
                postDelayTime = leftTime;
            }
            dismissTimeWarning();
            createTimeWarning(leftTimeSlot, leftPlayTime, leftSessionTime, leftResetTime);

            timeLockTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecording();
                }
            }, postDelayTime);
        } else {
            // Wait until five minute before locks
            dissmissAll();
            timeLockTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startRecording();
                }
            }, leftTime - (5 * MINUTES_IN_MILLIS));
        }
    }
}
