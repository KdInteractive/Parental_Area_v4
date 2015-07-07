package com.cide.interactive.parentalArea;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cide.interactive.kuriolib.view.CircleImageView;

/**
 * Created by dorianchazalon on 26/03/15.
 */
public class ChildButtonCheck extends RelativeLayout {
    CircleImageView mIVBackground;
    ImageView mIVCheck;
    TextView mTVName;
    boolean mIsChecked;
    String mIconName;

    public ChildButtonCheck(Context context, Bitmap background) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.child_button_check, this);

        mIVBackground = (CircleImageView) findViewById(R.id.ivBackground);
        mIVCheck = (ImageView) findViewById(R.id.ivCheck);
        mTVName = (TextView) findViewById(R.id.tvIconName);
        mIVBackground.setImageBitmap(background);
        mIsChecked = false;
    }

    public void setCheck(boolean checked) {
        mIsChecked = checked;
        if (mIsChecked) {
            mIVCheck.setImageResource(R.drawable.ic_tick_yes);
        } else {
            mIVCheck.setImageResource(0);
        }
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
