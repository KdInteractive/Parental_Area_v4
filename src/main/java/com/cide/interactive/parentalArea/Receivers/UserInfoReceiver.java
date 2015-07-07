package com.cide.interactive.parentalArea.Receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.UserManager;

import com.cide.interactive.kuriolib.DBUriManager;
import com.cide.interactive.kuriolib.Utils;
import com.cide.interactive.parentalArea.Library.ChildInfoCore;
import com.cide.interactive.parentalArea.ParentalAreaApplication;

import java.util.List;

/**
 * Created by lionel on 25/02/14.
 */
public class UserInfoReceiver extends BroadcastReceiver {

    private static final String TAG = "ChildInfoBroadcast";

    private static void checkNewUsers(Intent intent, Context context) {

        Bundle ext = intent.getExtras();

        if (ext == null) {
            return;
        }

        //for new users we disable the component of the parental
        int userId = ext.getInt(Intent.EXTRA_USER_HANDLE);
        ParentalAreaApplication.disableParentalComponent(userId, context);
    }

    public static void checkRemovedUsers(Context context) {

        ContentResolver contentResolver = Utils.getOwnerContext(context).getContentResolver();
        String[] projection = {DBUriManager.CHILDINFO_UID};

        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        List<UserInfo> userInfos = userManager.getUsers(true);

        String whereClause = DBUriManager.CHILDINFO_UID + " not in (-1";

        for (UserInfo userInfo : userInfos) {
            whereClause += ", " + userInfo.id;
        }

        whereClause += ")";

        Cursor cursor = contentResolver.query(DBUriManager.CONTENT_URI_CHILDINFOS, projection, whereClause, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int userId = cursor.getInt(0);
                    ChildInfoCore currentChild = new ChildInfoCore(context, userId);
                    if (currentChild != null) {
                        currentChild.deleteUser();
                    }
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_USER_ADDED)) {
                checkNewUsers(intent, context);
            } else if (action.equals(Intent.ACTION_USER_REMOVED)) {
                checkRemovedUsers(context);
            }
        }
    }
}
