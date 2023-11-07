package com.wt.phonelink.carlink.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.wt.phonelink.R;


public class UCarAutoScaleTextView extends AppCompatTextView {
    private static final String CALCULATION_STRING = "正";
    private float mWidthPercent;
    private int mWidthCalcBySize;
    private CharSequence mLastText;
    private int mLengthByChar;
    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            calcTextSize();
        }
    };

    public UCarAutoScaleTextView(Context context) {
        this(context, null);
    }

    public UCarAutoScaleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UCarAutoScaleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.UCarAutoScaleTextView, defStyleAttr, 0);
        float widthPercent = 1f;
        int lengthByChar = 0;
        if (arr != null) {
            try {
                widthPercent = arr.getFloat(R.styleable.UCarAutoScaleTextView_widthPercent, 1f);
                lengthByChar = arr.getInt(R.styleable.UCarAutoScaleTextView_lengthByChar, 0);
            } finally {
                arr.recycle();
            }
        }
        mLengthByChar = lengthByChar;
        mWidthPercent = Math.min(widthPercent, 1f);
        mWidthCalcBySize = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        calcTextSize();
        addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            final int width = (int) (mWidthCalcBySize * mWidthPercent + getPaddingLeft() + getPaddingRight());
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void calcTextSize() {
        CharSequence text = getText();
        if (text == null || TextUtils.isEmpty(text)) {
            return;
        }
        if (TextUtils.equals(mLastText, text)) {
            return;
        }
        mLastText = text;
        final int width = (int) (mWidthCalcBySize * mWidthPercent);
        final int lengthByChar = mLengthByChar;
        TextPaint textPaint = getPaint();
        String[] array = text.toString().split("\n");
        int maxLength = 0;
        for (String s : array) {
            maxLength = Math.max(maxLength, s.length());
        }
        float maxWidth;
        if (lengthByChar > maxLength) {
            maxWidth = measureText(textPaint, lengthByChar);
        } else {
            maxWidth = measureText(textPaint, array);
        }
        final float size = textPaint.getTextSize();
        textPaint.setTextSize(width / maxWidth * size);
        if (lengthByChar > maxLength) {
            maxWidth = measureText(textPaint, lengthByChar);
        } else {
            maxWidth = measureText(textPaint, array);
        }
        while (maxWidth > width) {
            float textSize = textPaint.getTextSize();
            textSize -= 1f;
            textPaint.setTextSize(textSize);
            if (lengthByChar > maxLength) {
                maxWidth = measureText(textPaint, lengthByChar);
            } else {
                maxWidth = measureText(textPaint, array);
            }
        }
    }

    private float measureText(TextPaint paint, String[] array) {
        float maxWidth = 0;
        for (String s : array) {
            float width = paint.measureText(s);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    private float measureText(TextPaint paint, int length) {
        float width = paint.measureText(CALCULATION_STRING);
        return width * length;
    }
}
