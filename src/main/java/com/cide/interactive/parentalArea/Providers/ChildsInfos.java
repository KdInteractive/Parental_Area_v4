package com.cide.interactive.parentalArea.Providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;

import com.cide.interactive.parentalArea.Library.ChildInfoCore;

import java.util.List;

/**
 * Created by alexandre on 19/07/13.
 * used by kurio store
 */
public class ChildsInfos extends ContentProvider {

    public static int GENDER_UNKNOW = 0;
    public static int GENDER_BOY = 1;
    public static int GENDER_GIRL = 2;

    public static int KEY_IS_CHILD = 0;
    public static int KEY_UID = 1;
    public static int KEY_NAME = 2;
    public static int KEY_BIRTH = 3;
    public static int KEY_GENDER = 4;
    public static int FILEDS = 5;

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        Context context = getContext();
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);

        List<UserInfo> usersInfo = userManager.getUsers();
        String[] strUserInfo = new String[usersInfo.size() * 5];

        int usersInfoSize = usersInfo.size();
        for (int i = 0; i < usersInfoSize; i++) {

            UserInfo ui = usersInfo.get(i);
            boolean isChild = ChildInfoCore.isChild(getContext(), ui.id);

            strUserInfo[(i * FILEDS) + KEY_IS_CHILD] = String.valueOf(isChild);
            strUserInfo[(i * FILEDS) + KEY_UID] = String.valueOf(ui.id);
            strUserInfo[(i * FILEDS) + KEY_NAME] = ui.name;
            if (isChild) {
                ChildInfoCore childInfo = new ChildInfoCore(getContext(), ui.id);
                if (childInfo.getBirth() != null) {
                    strUserInfo[(i * FILEDS) + KEY_BIRTH] = childInfo.getBirth();
                } else {
                    strUserInfo[(i * FILEDS) + KEY_BIRTH] = "";
                }
                if (childInfo.isBoy()) {
                    strUserInfo[(i * FILEDS) + KEY_GENDER] = String.valueOf(GENDER_BOY);
                } else {
                    strUserInfo[(i * FILEDS) + KEY_GENDER] = String.valueOf(GENDER_GIRL);
                }
            } else {
                strUserInfo[(i * FILEDS) + KEY_BIRTH] = "";
                strUserInfo[(i * FILEDS) + KEY_GENDER] = String.valueOf(GENDER_UNKNOW);
            }
        }

        bundle.putStringArray("infos_compte", strUserInfo);

        return bundle;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
