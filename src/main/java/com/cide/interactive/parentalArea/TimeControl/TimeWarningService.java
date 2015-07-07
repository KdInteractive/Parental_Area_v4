package com.cide.interactive.parentalArea.TimeControl;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cide.interactive.parentalArea.R;

public class TimeWarningService extends Service {
    private static final String TAG = "TimeWarningService";

    private RelativeLayout rlTimeWarning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        rlTimeWarning = (RelativeLayout) inflater.inflate(R.layout.dialog_time_warning, null);

        TextView tvStatus = (TextView) rlTimeWarning.findViewById(R.id.tvStatus);
        tvStatus.setVisibility(View.GONE);

        rlTimeWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(rlTimeWarning, params);
    }

    @Override
    public void onDestroy() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeView(rlTimeWarning);
        super.onDestroy();
    }
}