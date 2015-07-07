package com.cide.interactive.parentalArea.TimeControl;

import android.app.Service;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Button;

import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.parentalArea.ParentCheckActivity;
import com.cide.interactive.parentalArea.R;

/**
 * Created by alexandre on 17/04/14.
 */
public class TimeLockService extends Service {

    private static final String TAG = "TimeLockService";
    private View mRootView;
    private StatusBarManager mStatusBarManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE CREATED");
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        initView();
    }

    @Override
    public void onDestroy() {
        removeView();
        mStatusBarManager.disable(0);
        Log.d(TAG, "SERVICE DESTROYED");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
                PixelFormat.TRANSLUCENT
        );
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

        mStatusBarManager.disable(View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_SEARCH | View.STATUS_BAR_DISABLE_RECENT
                | View.STATUS_BAR_DISABLE_EXPAND | View.STATUS_BAR_DISABLE_BACK);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.time_control_stop, null);

        Button mBtnCancelAdd = (Button) mRootView.findViewById(R.id.btnCancelAdd);
        Button mBtnAdd = (Button) mRootView.findViewById(R.id.btnAdd);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ParentCheckActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Kurio.TITLE_PASSWORD, getString(R.string.time_control_quick_setting_title_description_password));
                intent.putExtra(Kurio.FOR_TIME_LOCK, true);
                startActivity(intent);
            }
        });

        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                    TimeLockService.this.stopSelf();
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                }
            }
        };
        mBtnCancelAdd.setOnClickListener(cancelListener);

        mRootView.setSystemUiVisibility(View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_RECENT | View.STATUS_BAR_DISABLE_BACK | View.STATUS_BAR_DISABLE_SEARCH);
        wm.addView(mRootView, params);
    }

    private void removeView() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (mRootView != null) {
            wm.removeView(mRootView);
        }
    }
}
