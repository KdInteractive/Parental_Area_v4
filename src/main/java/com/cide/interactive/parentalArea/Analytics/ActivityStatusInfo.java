package com.cide.interactive.parentalArea.Analytics;

/**
 * Created by lionel on 17/07/14.
 */


//used to send activity info in json format to server
public class ActivityStatusInfo {

    private String analytics_uid;
    private String serial;
    private long launch_count, time_spent;
    private String enabled;
    private String package_name, activity_name;

    public String getAnalyticsUID() {
        return analytics_uid;
    }

    public void setAnalyticsUID(String analyticsUID) {
        this.analytics_uid = analyticsUID;
    }

    public long getLaunchCount() {
        return launch_count;
    }

    public void setLaunchCount(long mLaunchCount) {
        this.launch_count = mLaunchCount;
    }

    public long getTimeSpent() {
        return time_spent;
    }

    public void setTimeSpent(long mTimeSpent) {
        this.time_spent = mTimeSpent;
    }

    public String isEnabled() {
        return enabled;
    }

    public void setEnabled(String mEnabled) {
        this.enabled = mEnabled;
    }

    public String getPackageName() {
        return package_name;
    }

    public void setPackageName(String mPackageName) {
        this.package_name = mPackageName;
    }

    public String getActivityName() {
        return activity_name;
    }

    public void setActivityName(String mActivityName) {
        this.activity_name = mActivityName;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }
}
