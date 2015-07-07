package com.cide.interactive.parentalArea.Receivers;

import android.app.ActivityManagerNative;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;

import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.parentalArea.DataBase.Accessor.AppCategoryDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.DialogActivity;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.ParentCheckActivity;
import com.cide.interactive.parentalArea.R;
import com.cide.interactive.parentalArea.Services.GlobalControlService;
import com.cide.interactive.parentalArea.Services.ProcessMonitorService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by leehack on 6/18/13.
 */
public class StartReceiver extends BroadcastReceiver {
    private final static String TAG = "UserLogInReceiver";

    public void onReceive(final Context context, Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (context.getUserId() == 0) {

                    //to know the last log of parent when the device boot
                    ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
                    if (parentPreferencesDataAccess.isParentAlreadyRegistered()) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        String date = df.format(new Date());
                        parentPreferencesDataAccess.setParentPreferences(Kurio.ANALYTICS_LAST_PARENT_LOG, date);
                    }
                    context.startService(new Intent(context, GlobalControlService.class));

                } else if (ChildInfoCore.isChild(context, context.getUserId())) {

                    context.startService(new Intent(context, ProcessMonitorService.class));
                }

                //check if there are new apps to categorize and uncategorized apps
                AppCategoryDataAccess appCategoryDataAccess = new AppCategoryDataAccess(context);
                ArrayList<ResolveInfo> uncategorized = appCategoryDataAccess.getAppsToCategorize(context);

                clearNotification(context);
                if (!uncategorized.isEmpty()) {
                    displayNotification(context);
                }
            }
        }).start();

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

        if (context.getUserId() == 0) {
            intentDialog.setClass(context, DialogActivity.class);
        } else {
            intentDialog.setClass(context, ParentCheckActivity.class);
            intentDialog.putExtra("fromCategoryNotif", true);
            intentDialog.putExtra("CHILD_ID", context.getUserId());
        }

        intentDialog.setAction(Intent.ACTION_VIEW);
        intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, PackageInfoReceiver.NOTIF_ID, intentDialog, 0);

        int messageIcon = R.drawable.ic_app_default;

        Notification.Builder notifBuilder = new Notification.Builder(context)
                .setContentTitle(context.getResources().getString(R.string.app_category_dialog_title))
                .setContentText(context.getResources().getString(R.string.app_category_notifications_categorize_one_app))
                .setSmallIcon(messageIcon)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent);

        notificationManager.notify(PackageInfoReceiver.NOTIF_ID, notifBuilder.build());
    }

    private void clearNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PackageInfoReceiver.NOTIF_ID);
    }
}
