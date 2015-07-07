package com.cide.interactive.parentalArea;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.ServiceManager;

import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.parentalArea.Providers.ParentalDataBaseProvider;
import com.cide.interactive.parentalArea.Receivers.UserInfoReceiver;
import com.nullwire.trace.ExceptionHandler;

/**
 * Created by leehack on 3/5/14.
 */
public class ParentalAreaApplication extends Application {

    private static final String TAG = "ParentalAreaApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.enableFileLogger(getApplicationContext().getObbDir().getAbsolutePath());
        ExceptionHandler.register(getApplicationContext(), Kurio.URL_HTTP_KDTABLET_COM
                + "/kuriotrace/trace.php", null);

        if (getUserId() != 0) {
            enableParentalComponentForChild(getUserId(), getApplicationContext());
        }
    }

    public static void enableParentalComponentForChild(int userId, Context context) {
        IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        try {
            ComponentName componentName = new ComponentName(context, BlockSettingActivity.class);
            pmForUser.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0, userId);
        } catch (Exception e) {
            Log.w(TAG, "GLOBAL SETTINGS", e);
        }
    }

    public static void disableParentalComponent(int userId, Context context) {
        IPackageManager pmForUser = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));

        try {
            ComponentName componentName = new ComponentName(context, ParentalDataBaseProvider.class);
            pmForUser.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP, userId);

//            componentName = new ComponentName(context, ChildsInfos.class);
//            pmForUser.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, userId);

            componentName = new ComponentName(context, UserInfoReceiver.class);
            pmForUser.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP, userId);

//            componentName = new ComponentName(context, UserLogInReceiver.class);
//            pmForUser.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, userId);
            enableParentalComponentForChild(userId, context);
        } catch (Exception e) {
            Log.w(TAG, "GLOBAL SETTINGS", e);
        }

    }
}