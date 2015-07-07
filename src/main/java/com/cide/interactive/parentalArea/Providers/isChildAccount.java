package com.cide.interactive.parentalArea.Providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.cide.interactive.parentalArea.Library.ChildInfoCore;

public class isChildAccount extends ContentProvider {

    @Override
    public Bundle call(String method, String arg, Bundle extras) {

        try {
            if (ChildInfoCore.isChild(getContext(), getContext().getUserId())) {
                ChildInfoCore childInfo = ChildInfoCore.getCurrentChildInfo(getContext(), false);
                Bundle b = new Bundle();
                b.putInt("age", childInfo.getAge());
                return b;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        throw new UnsupportedOperationException(
                "Not supported by this provider");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException(
                "Not supported by this provider");
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        throw new UnsupportedOperationException(
                "Not supported by this provider");
    }

    @Override
    public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
        throw new UnsupportedOperationException(
                "Not supported by this provider");
    }

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
                      String[] as) {
        throw new UnsupportedOperationException(
                "Not supported by this provider");
    }
}
