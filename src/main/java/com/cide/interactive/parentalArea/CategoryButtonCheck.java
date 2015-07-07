package com.cide.interactive.parentalArea;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by dorianchazalon on 26/03/15.
 */
public class CategoryButtonCheck extends RelativeLayout {
    private Context mContext;
    private ImageView mIVBackground;
    private TextView mTVName;
    private boolean mIsChecked;
    private String mIconName;
    private String mKey;

    public CategoryButtonCheck(Context context, String key) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.category_button_check, this);

        mContext = context;
        mIVBackground = (ImageView) findViewById(R.id.ivBackground);
        mTVName = (TextView) findViewById(R.id.tvIconName);
        mKey = key;
        int resId = getResources().getIdentifier(key + "_white", "drawable", context.getApplicationContext().getPackageName());
        mIVBackground.setImageDrawable(getResources().getDrawable(resId));
        mIsChecked = false;
    }

    public void setCheck(boolean checked) {
        mIsChecked = checked;
        int resId;
        if (mIsChecked) {
            resId = getResources().getIdentifier(mKey + "_green", "drawable", mContext.getApplicationContext().getPackageName());
        } else {
            resId = getResources().getIdentifier(mKey + "_white", "drawable", mContext.getApplicationContext().getPackageName());
        }
        mIVBackground.setImageResource(resId);
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setIconName(String nameToDisplay) {
        mTVName.setText(nameToDisplay);
        mIconName = nameToDisplay;
    }

    public String getIconName() {
        return mIconName;
    }
}
