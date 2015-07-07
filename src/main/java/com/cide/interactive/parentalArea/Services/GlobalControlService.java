package com.cide.interactive.parentalArea.Services;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;

import com.android.internal.widget.LockPatternUtils;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Analytics.RegisterServerHandler;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by leehack on 11/22/13.
 */
public class GlobalControlService extends Service {

    private static final String TAG = "GlobalControlService";

    private static boolean mServiceIsRunning = false;
    private static UsbManager mUsbManager = null;

    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {

            final String action = intent.getAction();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (action != null) {
                        if (action.equals(Intent.ACTION_USER_SWITCHED)
                                || action.equals(UsbManager.ACTION_USB_STATE)) {
                            if (intent.hasExtra(Intent.EXTRA_USER_HANDLE)) {
                                int userHandle = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
                                if (userHandle == 0) {
                                    //to know the last log of parent
                                    ParentPreferencesDataAccess parentPreferencesDataAccess = new ParentPreferencesDataAccess(context);
                                    if (parentPreferencesDataAccess.isParentAlreadyRegistered()) {
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                                        String date = df.format(new Date());
                                        parentPreferencesDataAccess.setParentPreferences(Kurio.ANALYTICS_LAST_PARENT_LOG, date);
                                    }
                                }
                            }
                            usbControl(context);
                        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                            if (isWifiConnected(context)) {
                                Log.e("------------------------- DATA", "SENDED");
                                checkPasswordReset(context);
                                RegisterServerHandler.readAndSendInformation(context);
                            }
                        }
                    }
                }
            }).start();

        }
    };

    private static void checkPasswordReset(Context context) {
        new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... contexts) {
                Context context = contexts[0];
                try {
                    URL url = new URL(Kurio.URL_HTTP_KDTABLET_COM
                            + "/password_reset.php?should_reset_password=1&serial=" + Utils.getSerialId());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    InputStream stream = conn.getInputStream();
                    byte data[] = new byte[1024];
                    int count;
                    String response = "";
                    while ((count = stream.read(data, 0, 1024)) != -1) {
                        response += new String(Arrays.copyOfRange(data, 0, count));
                    }
                    if (response.equals("1")) {
                        LockPatternUtils lockPatternUtils = new LockPatternUtils(context);
                        lockPatternUtils.setCurrentUser(0);
                        lockPatternUtils.clearLock(false);

                    }
                    conn.disconnect();
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
                return null;
            }
        }.execute(context);
    }

    private static void usbControl(Context context) {

        if (mUsbManager == null)
            mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        try {
            if (ChildInfoCore.isChild(context, ActivityManagerNative.getDefault().getCurrentUser().id)) {

                ChildInfoCore childInfo = new ChildInfoCore(context, ActivityManagerNative.getDefault().getCurrentUser().id);

                if (!childInfo.isAllowUsbConnection()) {
                    mUsbManager.setCurrentFunction("none", false);
                } else {
                    mUsbManager.setCurrentFunction(null, true);
                }
            } else {
                mUsbManager.setCurrentFunction(null, true);
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }


    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), 0, new Intent("com.cide.interactive.parentalArea.NEED_TO_START_SERVICE"), 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mServiceIsRunning) {
            mServiceIsRunning = true;
            //Global Receiver
            checkPasswordReset(getApplicationContext());
            registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_USER_SWITCHED));
            registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            registerReceiver(mReceiver, new IntentFilter(UsbManager.ACTION_USB_STATE));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mServiceIsRunning = false;
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
