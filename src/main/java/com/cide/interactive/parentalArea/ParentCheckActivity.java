package com.cide.interactive.parentalArea;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManagerGlobal;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.LockPatternView;
import com.cide.interactive.kuriolib.Kurio;
import com.cide.interactive.kuriolib.KurioLockPatternUtil;
import com.cide.interactive.kuriolib.Log;
import com.cide.interactive.parentalArea.TimeControl.TimeController;
import com.cide.interactive.parentalArea.TimeControl.TimeLockService;

import java.util.Calendar;
import java.util.List;

/**
 * Created by leehack on 02/12/14.
 */


/**
 * WARNING This activity also manage the time control add time feature.
 */
public class ParentCheckActivity extends Activity {
    private static final String TAG = "ParentCheckActivity";
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;
    private static SharedPreferences mSharedPreferences;
    StatusBarManager mStatusBarManager;
    private EditText mEditText;
    private LockPatternView mLockPatternView;
    private KurioLockPatternUtil mLockPatternUtils;
    private boolean mForTimeLock = false;
    private boolean mFromCategoryNotif = false;
    private LinearLayout mLLTimeLock;
    private LinearLayout mRootView;
    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
            = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (mLockPatternUtils.checkPattern(pattern, 0)) {
                if (mForTimeLock) {
                    mRootView.removeAllViews();
                    mRootView.addView(mLLTimeLock);
                } else if (mFromCategoryNotif) {
                    Intent intent = new Intent("com.cide.interactive.action.APP_MANAGEMENT");
                    intent.putExtra("CHILD_ID", getUserId());
                    intent.putExtra("selectUncategorized", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            } else {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                Toast.makeText(getApplicationContext(), getString(R.string.parent_check_password_wrong_password), 1).show();
                postClearPatternRunnable();
            }
        }
    };

    // clear the wrong pattern unless they have started a new one
    // already
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.parental_pin_code_popup);

        Intent timeLockIntent = new Intent(getApplicationContext(), TimeLockService.class);
        stopService(timeLockIntent);

        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);

        mRootView = (LinearLayout) findViewById(R.id.rootView);
        mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        mEditText = (EditText) findViewById(R.id.et_pin_code);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        mLockPatternUtils = new KurioLockPatternUtil(this);
        TextView tvTitleDescription = (TextView) findViewById(R.id.tvTitleDescription);
        tvTitleDescription.setText(getIntent().getStringExtra(Kurio.TITLE_PASSWORD));
        mForTimeLock = getIntent().getBooleanExtra(Kurio.FOR_TIME_LOCK, false);
        if (mForTimeLock) {
            iniTimeLock();
        }
        mFromCategoryNotif = getIntent().getBooleanExtra("fromCategoryNotif", false);

        if (mLockPatternUtils.isLockParentPasswordEnabled()) {
            mEditText.setVisibility(View.VISIBLE);
            mLockPatternView.setVisibility(View.GONE);
            Log.e(TAG, "lock pattern util " + String.valueOf(mLockPatternUtils.getActivePasswordQuality()));
            if (!mLockPatternUtils.isLockParentPinEnabled()) {
                mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            tvTitle.setText(getString(R.string.parent_check_password_type_pin_or_text));
            mEditText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                            finish();
                        } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            String password = mEditText.getText().toString();
                            if (mLockPatternUtils.checkPassword(password, 0)) {
                                if (mForTimeLock) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                                    mRootView.removeAllViews();
                                    mRootView.addView(mLLTimeLock);
                                } else if (mFromCategoryNotif) {
                                    Intent intent = new Intent("com.cide.interactive.action.APP_MANAGEMENT");
                                    intent.putExtra("CHILD_ID", getUserId());
                                    intent.putExtra("selectUncategorized", true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    setResult(RESULT_OK, new Intent());
                                    finish();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.parent_check_password_wrong_password), 1).show();
                                mEditText.setText("");
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        } else if (mLockPatternUtils.isLockParentPatternEnabled()) {
            tvTitle.setText(getString(R.string.parent_check_password_type_pattern));
            mLockPatternView.setTactileFeedbackEnabled(true);
            mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
            mEditText.setVisibility(View.GONE);
            mLockPatternView.setVisibility(View.VISIBLE);
        } else {
            mLockPatternView.setTactileFeedbackEnabled(true);
            mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
            mEditText.setVisibility(View.GONE);
            mLockPatternView.setVisibility(View.VISIBLE);
            tvTitle.setText(getString(R.string.parent_check_password_no_password));
        }
    }

    private void iniTimeLock() {

        mLLTimeLock = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.time_control_stop_add_time, null);

        Button btnCancelTime;
        Button btn5;
        Button btn10;
        Button btn15;
        Button btn20;

        btnCancelTime = (Button) mLLTimeLock.findViewById(R.id.btnCancelTime);
        btn5 = (Button) mLLTimeLock.findViewById(R.id.btn5);
        btn10 = (Button) mLLTimeLock.findViewById(R.id.btn10);
        btn15 = (Button) mLLTimeLock.findViewById(R.id.btn15);
        btn20 = (Button) mLLTimeLock.findViewById(R.id.btn20);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentValueToAdd = Integer.valueOf(v.getTag().toString());
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                currentValueToAdd = currentValueToAdd * 60 * 1000;

                Calendar now = Calendar.getInstance();
                long nowInMillis = now.getTimeInMillis();
                editor.putLong(TimeController.EXTRA_TIME, currentValueToAdd + nowInMillis);
                editor.apply();
                finish();
            }
        };

        btn5.setOnClickListener(listener);
        btn10.setOnClickListener(listener);
        btn15.setOnClickListener(listener);
        btn20.setOnClickListener(listener);

        View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusBarManager.disable(0);
                finish();
                Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                } catch (RemoteException e) {
                    Log.w(TAG, e);
                }
            }
        };

        btnCancelTime.setOnClickListener(cancelListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusBarManager.disable(View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_SEARCH | View.STATUS_BAR_DISABLE_RECENT
                | View.STATUS_BAR_DISABLE_EXPAND);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStatusBarManager.disable(0);
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        mStatusBarManager.disable(0);
    }*/

    @Override
    public void onBackPressed() {
        if (mForTimeLock) {
            mStatusBarManager.disable(0);
            finish();
            Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            try {
                WindowManagerGlobal.getWindowManagerService().lockNow(null);

            } catch (RemoteException e) {
                Log.w(TAG, e);
            }
        } else {
            super.onBackPressed();
        }
    }
}
