package com.wt.phonelink.carlink.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wt.phonelink.R;

public class UCarAlertView {
    private Activity mActivity;
    private View mView;
    private boolean mIsShown = false;

    public UCarAlertView(Context context) {
        if (!(context instanceof Activity)) {
            throw new IllegalArgumentException("context must be activity");
        }
        mActivity = (Activity) context;
    }

    public void setContentView(View view) {
        if (isShowing()) {
            dismiss();
            mView = view;
            show();
        } else {
            mView = view;
        }
    }

    public void show() {
        if (isShowing()) {
            dismiss();
        }
        FrameLayout decorView = mActivity.findViewById(R.id.content);
        if (decorView != null && mView != null) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            );
            decorView.addView(mView, layoutParams);
            mIsShown = true;
        }
    }

    public boolean isShowing() {
        return mIsShown;
    }

    public void dismiss() {
        if (mIsShown && mView != null) {
            FrameLayout decorView = mActivity.findViewById(android.R.id.content);
            if (decorView != null) {
                decorView.removeView(mView);
            }
            mIsShown = false;
        }
    }
}
