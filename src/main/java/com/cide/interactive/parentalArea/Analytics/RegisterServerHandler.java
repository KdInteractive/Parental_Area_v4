package com.cide.interactive.parentalArea.Analytics;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserManager;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.kuriolib.TimeSlotSetting;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.DataBase.Accessor.ChildInfoDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.ParentPreferencesDataAccess;
import com.cide.interactive.parentalArea.DataBase.Accessor.TimeSlotSettingsDataAccess;
import com.cide.interactive.parentalArea.DataBase.Tables.AppAnalyticsTable;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.Services.GlobalControlService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by leehack on 7/22/13.
 */
public class RegisterServerHandler {

    private static final boolean DEBUG = true;

    private static final String TAG = "RegisterServerHandler";

    private static boolean mTaskRunning = false;

    public static long TIME_TO_CHECK_XML = (long) 24 * 60 * 60 * 1000; // 1 days
    //    public static long TIME_TO_CHECK_XML = (long) 60 * 1000; // 1 minute
    private static long mLastTimeChecked = 0;

    private static final String POST_DATA_SERIAL = "serial";
    private static final String POST_DATA_DISPLAY_ID = "ro_build_display_id";
    private static final String POST_DATA_VERSION_INCREMENTAL = "ro_build_version_incremental";
    private static final String POST_DATA_MODEL = "ro_product_model";
    private static final String POST_DATA_PROD_NAME = "ro_product_name";
    private static final String POST_DATA_DEVICE = "ro_product_device";
    private static final String POST_DATA_BOARD = "ro_product_board";
    private static final String POST_DATA_PRODUCT_LANGUAGE = "ro_product_locale_language";
    private static final String POST_DATA_PRODUCT_REGION = "ro_product_locale_region";
    private static final String POST_DATA_MAC = "MAC_address";
    private static final String POST_DATA_LAST_IP = "last_ip";
    private static final String POST_DATA_LANGUAGE = "system_language";
    //Do not need    private static final String POST_DATA_FIRST_ACTIVATION = "first_activation";
    private static final String POST_DATA_SETUP_DATE = "setup_date";
    private static final String POST_DATA_LAST_MODIF = "last_modification";
    private static final String POST_DATA_EMAIL = "parent_email";
    private static final String POST_DATA_LOCKSCREEN_CODE = "owner_lockscreen_code";
    private static final String POST_DATA_ACTIVE_PROFILES = "active_profiles";
    private static final String POST_DATA_CHILD_PROFILES = "children_profiles";
    private static final String POST_DATA_KURIO_SYSTEM_VERSION = "kurio_system_version";

    public static Bundle readData(Context context) {

        String displayId;
        String versionInc;
        String model;
        String name;
        String device;
        String board;
        String localeLang;
        String localeRegion;
        String macAddr;
        String ipAddr;
        String systemLanguage;
        String setupDate;
        String lastModifyDate;
        String email;

        Bundle b = new Bundle();

        displayId = SystemProperties.get("ro.build.display.id", "unknown");
        b.putString(POST_DATA_DISPLAY_ID, displayId);
        versionInc = SystemProperties.get("ro.build.version.incremental", "unknown");
        b.putString(POST_DATA_VERSION_INCREMENTAL, versionInc);
        model = SystemProperties.get("ro.product.model", "unknown");
        b.putString(POST_DATA_MODEL, model);
        name = SystemProperties.get("ro.product.name", "unknown");
        b.putString(POST_DATA_PROD_NAME, name);
        device = SystemProperties.get("ro.product.device", "unknown");
        b.putString(POST_DATA_DEVICE, device);
        board = SystemProperties.get("ro.product.board", "unknown");
        b.putString(POST_DATA_BOARD, board);
        localeLang = SystemProperties.get("ro.product.locale.language", "unknown");
        b.putString(POST_DATA_PRODUCT_LANGUAGE, localeLang);
        localeRegion = SystemProperties.get("ro.product.locale.region", "unknown");
        b.putString(POST_DATA_PRODUCT_REGION, localeRegion);

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        macAddr = wm.getConnectionInfo().getMacAddress();
        b.putString(POST_DATA_MAC, macAddr);
        ipAddr = String.valueOf(wm.getConnectionInfo().getIpAddress());
        b.putString(POST_DATA_LAST_IP, ipAddr);
        systemLanguage = Locale.getDefault().toString();
        b.putString(POST_DATA_LANGUAGE, systemLanguage);

        lastModifyDate = Utils.formatCalendar(Calendar.getInstance(), "yyyy-MM-dd");
        b.putString(POST_DATA_LAST_MODIF, lastModifyDate);

        ParentPreferencesDataAccess pref =
                new ParentPreferencesDataAccess(context);

        setupDate = pref.getParentPreferencesForKey(Kurio.SETUPDATE_KEY);
        email = pref.getParentPreferencesForKey(Kurio.EMAIL_KEY);

        b.putString(POST_DATA_EMAIL, email);
        b.putString(POST_DATA_SETUP_DATE, setupDate);

        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);

        ChildInfoDataAccess childInfoDataAccess = new ChildInfoDataAccess(context);
        Integer childCount = childInfoDataAccess.getChildCount();
        Integer activeUser = userManager.getUserCount();

        b.putString(POST_DATA_ACTIVE_PROFILES, activeUser.toString());
        b.putString(POST_DATA_CHILD_PROFILES, childCount.toString());
        b.putAll(AnalyticsManager.getInstance().getAnalyticsFromParentPreferences(context));

        try {
            b.putString(POST_DATA_KURIO_SYSTEM_VERSION,
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }

        return b;
    }

    public static boolean removeChildProfiles() {
        Bundle b = new Bundle();
        b.putString("deleteallchild", "true");
        return postData(b);
    }

    public static ArrayList<Bundle> readChildProfiles(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserInfo> userList = userManager.getUsers();

        ArrayList<Bundle> childInfoList = new ArrayList<>();
        for (UserInfo ui : userList) {
            if (ChildInfoCore.isChild(context, ui.getUserHandle().getIdentifier())) {
                try {
                    ChildInfoCore childInfo = new ChildInfoCore(context, ui.id);
                    if (childInfo != null) {
                        Bundle childBundle = new Bundle();
                        childBundle.putString("id_profile", Integer.toString(ui.id));
                        childBundle.putString(DBUriManager.CHILDINFO_ANALYTICS_UID, childInfo.getAnalyticsUID());
                        childBundle.putString("is_boy", Boolean.toString(childInfo.isBoy()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                        Date birth = sdf.parse(childInfo.getBirth());
                        childBundle.putString("birth", new SimpleDateFormat("yyyy-MM-dd").format(birth));
                        childBundle.putString("internet_access_mode",
                                Boolean.toString(childInfo.isWebAccessOn()));
                        TimeSlotSetting timeSlotSetting =
                                new TimeSlotSettingsDataAccess(context).getTimeSlotSettingsForUser(
                                        ui.getUserHandle().getIdentifier(), 0);
                        //TODO need to check if we need to check if enabled for all different daysadb reboot
                        childBundle.putString("time_control_status",
                                Boolean.toString(timeSlotSetting.isEnabled()));
                        childBundle.putString("allow_USB_status",
                                Boolean.toString(childInfo.isAllowUsbConnection()));
                        childBundle.putString("authorize_ads_status",
                                Boolean.toString(childInfo.isActivateAdsFilter()));
                        childBundle.putString("auto_authorise_status",
                                Boolean.toString(childInfo.isAutoAuthorizeApplication()));
                        childBundle.putString("locked_interface_status",
                                Boolean.toString(childInfo.isLockedInterface()));
                        childBundle.putString(DBUriManager.CHILDINFO_PROFILE_CHANGED,
                                Boolean.toString(childInfo.isProfileChanged()));
                        childBundle.putString("child_protected_by_pwd",
                                Boolean.toString(AnalyticsManager.getInstance().isChildProtectedByPassword(context, ui.id)));
                        childBundle.putString("is_web_list_activated",
                                Boolean.toString(childInfo.isWebListOn()));
                        childBundle.putAll(AnalyticsManager.getInstance().getChildTimeControlAnalytics(childInfo));
                        TimeSlotSettingsDataAccess timeSlotSettingsDataAccess = new TimeSlotSettingsDataAccess(context);
                        ArrayList<TimeSlotSetting> timeSlotSettings = timeSlotSettingsDataAccess.getTimeSlotSettingsForUser(ui.getUserHandle().getIdentifier());
                        LinkedHashMap<Integer, TimeSlotSetting> copySlotSettingsSorted = new LinkedHashMap<Integer, TimeSlotSetting>();
                        for (int i = 0; i < timeSlotSettings.size(); i++) {
                            copySlotSettingsSorted.put(timeSlotSettings.get(i).getDay(), timeSlotSettings.get(i));
                        }

                        boolean isTimeSlotEnabled = false;
                        String playTime = "";
                        String maxSession = "";
                        String restTime = "";
                        for (int i = 0 ; i < copySlotSettingsSorted.size() ; i++) {
                            TimeSlotSetting ts = copySlotSettingsSorted.get(i);
                            if (!playTime.isEmpty()) {
                                playTime = playTime.concat("@");
                            }
                            if (!maxSession.isEmpty()) {
                                maxSession = maxSession.concat("@");
                            }
                            if (!restTime.isEmpty()) {
                                restTime = restTime.concat("@");
                            }

                            if (ts.isEnabled()) {
                                isTimeSlotEnabled = true;
                                playTime = playTime.concat(String.valueOf(ts.getPlayTime()));
                                maxSession = maxSession.concat(String.valueOf(ts.getSessionTime()));
                                restTime = restTime.concat(String.valueOf(ts.getRestTime()));
                            } else {
                                playTime = playTime.concat(String.valueOf(copySlotSettingsSorted.get(0).getPlayTime()));
                                maxSession = maxSession.concat(String.valueOf(copySlotSettingsSorted.get(0).getSessionTime()));
                                restTime = restTime.concat(String.valueOf(copySlotSettingsSorted.get(0).getRestTime()));
                            }
                        }
                        childBundle.putString("is_time_slot_enabled",
                                Boolean.toString(isTimeSlotEnabled));
                        childBundle.putString("daily_play_time_week_days",
                                playTime);
                        childBundle.putString("max_session_duration_week_days",
                                maxSession);
                        childBundle.putString("rest_time_week_days",
                                restTime);
                        childInfoList.add(childBundle);
                    }
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }
        }
        return childInfoList;
    }

    private static HttpClient sslClient(HttpClient client) {
        try {
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception ex) {
            return null;
        }
    }

    private static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public static boolean postJsonData(String data, String tableToUse) {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient = sslClient(httpclient);
        String serial = Build.SERIAL;
        Bundle b = new Bundle();
        b.putString(POST_DATA_SERIAL, serial);
        b.putString("hidden_key", "register_kurio_2749");
        b.putString("table_to_use", tableToUse);
        b.putString("json_data", data);

        HttpPost httppost = new HttpPost(Kurio.URL_HTTPS_KDTABLET_COM
                + "/DB_Kurio/analytics_v4.php");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            for (String key : b.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, b.getString(key)));
            }

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            int status = response.getStatusLine().getStatusCode();
            if (DEBUG) {
                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String content = EntityUtils.toString(entity);
                    Log.e("Result : ", content);
                }
            }
        } catch (ClientProtocolException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean postData(Bundle b) {
        HttpClient httpclient = new DefaultHttpClient();
        httpclient = sslClient(httpclient);
        String serial = Utils.getSerialId();
        b.putString(POST_DATA_SERIAL, serial);
        b.putString("hidden_key", "register_kurio_2749");
        b.putString("table_to_use", "serial");
        HttpPost httppost = new HttpPost(Kurio.URL_HTTPS_KDTABLET_COM
                + "/DB_Kurio/analytics_v4.php");
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);

            String toto = Kurio.URL_HTTPS_KDTABLET_COM
                    + "/DB_Kurio/analytics_v4.php";

            for (String key : b.keySet()) {
                nameValuePairs.add(new BasicNameValuePair(key, b.getString(key)));
                toto = toto.concat("?"+key+"="+b.getString(key));
            }

            Log.e("URL ; ", toto);

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            int status = response.getStatusLine().getStatusCode();

            if (DEBUG) {

                    HttpEntity entity = response.getEntity();
                    String content = EntityUtils.toString(entity);
                    Log.e(TAG, "result : " +  content);

            }
        } catch (ClientProtocolException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void readAndSendInformation(Context context) {

        if (mTaskRunning || !isTimeToSendData(context)) {
            return;
        }

        AsyncTask<Context, Void, Void> readAndSendTask = new AsyncTask<Context, Void, Void>() {
            @Override
            protected Void doInBackground(Context... contexts) {
                mTaskRunning = true;
                Context context = contexts[0];
                boolean successed = false;

                // we always send the serial
                Bundle data = RegisterServerHandler.readData(context);
                RegisterServerHandler.postData(data);

                //for those analytics we check if we need to send data to server
                if (AnalyticsManager.getInstance().needToUpdateAnalytics(context, DBUriManager.CHILDINFO_TABLE)) {
                    ArrayList<Bundle> childProfiels = RegisterServerHandler.readChildProfiles(context);

                    for (Bundle b : childProfiels) {
                        successed = RegisterServerHandler.postData(b);
                        if (!successed) break;
                    }
                    if (successed) {
                        AnalyticsManager.getInstance().setNeedToSendAnalytics(context, false, DBUriManager.CHILDINFO_TABLE);
                        //send active profiles
                        String activeProfiles = AnalyticsManager.getInstance().getActiveProfileAnlyticsUid(context);
                        RegisterServerHandler.postJsonData(activeProfiles, Kurio.ANALYTICS_ACTIVE_PROFILES);
                    }

                }

                if (AnalyticsManager.getInstance().needToUpdateAnalytics(context, DBUriManager.ACTIVITY_STATUS_TABLE)) {
                    String activityStatusData = AnalyticsManager.getInstance().getActivityStatusAnalytics(context);
                    if (activityStatusData != null) {
                        successed = RegisterServerHandler.postJsonData(activityStatusData, "activity_status_by_romid");

                        if (successed) {
                            //reset table data
                            AnalyticsManager.getInstance().setNeedToSendAnalytics(context, false, DBUriManager.ACTIVITY_STATUS_TABLE);
                            AnalyticsManager.getInstance().dataSendForActivityStatus(context);
                        }
                    }
                }

                if (AnalyticsManager.getInstance().needToUpdateAnalytics(context, AppAnalyticsTable.APP_ANALYTICS_TABLE)) {
                    String appAnalyticsData = AnalyticsManager.getInstance().getAppAnalytics(context);
                    if (appAnalyticsData != null) {
                        successed = RegisterServerHandler.postJsonData(appAnalyticsData,
                                AppAnalyticsTable.APP_ANALYTICS_TABLE);

                        if (successed) {
                            AnalyticsManager.getInstance().dataSendForApps(context);
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mTaskRunning = false;
                super.onPostExecute(aVoid);
            }
        };
        readAndSendTask.execute(context);
    }

    private static boolean isTimeToSendData(Context context) {

        /*if (mLastTimeChecked == 0 || (mLastTimeChecked + TIME_TO_CHECK_XML) < SystemClock.uptimeMillis()) {
            if (GlobalControlService.isWifiConnected(context)) {
                mLastTimeChecked = SystemClock.uptimeMillis();
            }
            return true;
        }*/
        return true;
    }

}
