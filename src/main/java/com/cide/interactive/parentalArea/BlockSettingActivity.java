package com.cide.interactive.parentalArea;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leehack on 3/10/14.
 */
public class BlockSettingActivity extends Activity {

    private static String mAction;
    private PackageManager mPackageManager;

    private List<ResolveInfo> findLaunchableActivitiesForPackage(String packageName, String action, int flags) {
        final PackageManager packageManager = getPackageManager();

        final Intent mainIntent = new Intent(action, null);
        mainIntent.setPackage(packageName);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(
                mainIntent, flags);

        return apps != null ? apps : new ArrayList<ResolveInfo>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            enableSettings();
            Intent intent = new Intent();
            intent.setAction(mAction);
            for (ResolveInfo resolveInfo : findLaunchableActivitiesForPackage("com.android.settings", mAction, PackageManager.GET_DISABLED_COMPONENTS)) {
                intent.setClassName("com.android.settings", resolveInfo.activityInfo.name);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent currentIntent = getIntent();
        final String action = currentIntent.getAction();

        mPackageManager = getPackageManager();

        ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(getApplicationContext(), false);

        if (action != null && childInfo.getAge() > 7 && (action.equals("android.settings.WIFI_SETTINGS") || action.equals("android.settings.BLUETOOTH_SETTINGS"))) {

            partiallyEnableSettingsWifi(action);
            Intent intent = new Intent();
            intent.setAction(action);
            //start all intent linked to this action in settings package.
            for (ResolveInfo resolveInfo : findLaunchableActivitiesForPackage("com.android.settings", action, PackageManager.GET_DISABLED_COMPONENTS)) {
                intent.setClassName("com.android.settings", resolveInfo.activityInfo.name);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            mAction = action;
            Intent intent = new Intent("com.cide.interactive.action.PARENT_CHECK");
            intent.putExtra(Kurio.TITLE_PASSWORD, getString(R.string.settings_title_description_password));
            startActivityForResult(intent, 1);
        }
    }

    private void partiallyEnableSettingsWifi(String action) {

        mPackageManager.setApplicationEnabledSetting("com.android.settings",
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

        mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                        "com.android.settings.Settings"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0
        );

        if (action.equals("android.settings.WIFI_SETTINGS")) {
            mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$WifiSettingsActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0
            );
            mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$BluetoothSettingsActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0
            );
        } else if (action.equals("android.settings.BLUETOOTH_SETTINGS")) {
            mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$BluetoothSettingsActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0
            );
            mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                            "com.android.settings.Settings$WifiSettingsActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0
            );
        }
    }

    private void enableSettings() {
//        mPackageManager.setComponentEnabledSetting(new ComponentName(this, this.getClass()),
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        mPackageManager.setApplicationEnabledSetting("com.android.settings",
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
        mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                        "com.android.settings.Settings"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0
        );
        mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                        "com.android.settings.Settings$BluetoothSettingsActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0
        );
        mPackageManager.setComponentEnabledSetting(new ComponentName("com.android.settings",
                        "com.android.settings.Settings$WifiSettingsActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0
        );
    }
}
