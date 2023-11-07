/***********************************************************
 ** Copyright (C), 2008-2016, OPPO Mobile Comm Corp., Ltd.
 ** VENDOR_EDIT
 ** File: - UCarProgressBar.java
 ** Description: 
 ** Version: 1.0
 ** Date : 2021/05/20	
 ** Author: zhoupeng12@oppo.com
 **
 ** ---------------------Revision History: ---------------------
 **  <author>	        <data> 	  <version >	   <desc>
 **  zhoupeng12@oppo.com     2021/05/20     1.0     build this module
 ****************************************************************/
package com.wt.phonelink.carlink.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wt.phonelink.R;


public class UCarProgressBar {
    private ImageView mImageView;
    private Animation mAnimation;
    private TextView mProgress;
    private TextView mPhoneModel;
    private View mRootView;
    private FrameLayout mParentLayout;
    private boolean mIsShowing = false;

    public UCarProgressBar(Context context, FrameLayout parent) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.dialog_progess, null);
        mImageView = mRootView.findViewById(R.id.img);
        mProgress = mRootView.findViewById(R.id.tv_progress);
        mPhoneModel = mRootView.findViewById(R.id.tv_model);
        mAnimation = AnimationUtils.loadAnimation(context, R.anim.progress);
        mParentLayout = parent;
    }

    /**
     * 刷新进度条，总进度100
     * @param percent  当前进度， >= 0 and <= 100
     */
    public void updateProgress(String model, int percent) {
        mPhoneModel.setText(model);
        mProgress.setText(percent + "%");
    }

    public void show(){
        if (!mIsShowing) {
            mParentLayout.addView(mRootView,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER));
            mIsShowing = true;
        }
        mImageView.startAnimation(mAnimation);
    }

    public void hide(){
        if (mIsShowing) {
            mParentLayout.removeView(mRootView);
            mIsShowing = false;
        }
        mImageView.clearAnimation();
    }
}
