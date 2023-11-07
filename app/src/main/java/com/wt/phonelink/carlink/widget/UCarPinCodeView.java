package com.wt.phonelink.carlink.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.wt.phonelink.R;


public class UCarPinCodeView extends LinearLayout {
    private static final int CODE_LENGTH = 6;
    private static final float DEFAULT_PERCENT = .45f;
    private final TextView[] mTextViews = new TextView[CODE_LENGTH];
    private String mPinCode;
    private final int mWidth;
    private final int mSize;

    public UCarPinCodeView(@NonNull Context context) {
        this(context, null);
    }

    public UCarPinCodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public UCarPinCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.UCarPinCodeView, defStyleAttr, 0);
        float widthPercent = DEFAULT_PERCENT;
        if (arr != null) {
            try {
                widthPercent = arr.getFloat(R.styleable.UCarPinCodeView_widthPercent, DEFAULT_PERCENT);
            } finally {
                arr.recycle();
            }
        }
        float widthPercent1 = Math.min(widthPercent, 1f);
        mWidth = (int) (displayWidth * widthPercent1)+50;
        mSize = (int) ((mWidth / (float) (CODE_LENGTH + 2)));
        Rect rect = new Rect();
        int textSize = -1;
        for (int i = 0; i < CODE_LENGTH; i++) {
            TextView textView = new TextView(context, null, 0, R.style.UCarPinCode_Style);
            if (textSize == -1) {
                final TextPaint textPaint = textView.getPaint();
                textPaint.getTextBounds("0", 0, 1, rect);
                float height = rect.height() * 2f;
                textSize = (int) (mSize / height * textPaint.getTextSize());
            }
            //之前是100
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 60);
            textView.setGravity(Gravity.CENTER);//@drawable/shape_12_blue
            textView.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_12_blue));
            textView.setTextColor(ContextCompat.getColor(context,R.color.wt_text_color_black_high_emphasis));
            MarginLayoutParams lp = new MarginLayoutParams(mSize, mSize);
            if (i == 0) {
                lp.rightMargin = 23;
            } else if (i == CODE_LENGTH - 1) {
                lp.leftMargin = 23;
            } else {
                lp.leftMargin = 23;
                lp.rightMargin = 23;
            }
            addView(textView, lp);
            mTextViews[i] = textView;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int count = getChildCount();
        if (count != CODE_LENGTH) {
            throw new RuntimeException("pin code must be length " + CODE_LENGTH);
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setPinCode(@NonNull String pinCode) {
        if (pinCode.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("pin code must be length " + CODE_LENGTH);
        }
        mPinCode = pinCode;
        for (int i = 0; i < CODE_LENGTH; i++) {
            mTextViews[i].setText(pinCode.substring(i, i + 1));
        }
    }

    public String getPinCode() {
        return mPinCode;
    }
}
