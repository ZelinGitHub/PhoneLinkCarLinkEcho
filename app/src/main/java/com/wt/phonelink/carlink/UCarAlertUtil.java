package com.wt.phonelink.carlink;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.core.content.ContextCompat;

import com.wt.phonelink.R;
import com.wt.phonelink.carlink.widget.*;

public class UCarAlertUtil {
    public interface OnClickListener {
        boolean onClick();
    }

    public static UCarAlertView makeDialog(Activity activity,
                                           @LayoutRes int layoutResId,
                                           int titleDrawableResId,
                                           String title,
                                           CharSequence msg,
                                           String done,
                                           String cancel,
                                           OnClickListener doneClick,
                                           OnClickListener cancelClick) {
        final UCarAlertView dialog = new UCarAlertView(activity);
        final View view = LayoutInflater.from(activity).inflate(layoutResId, null, false);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_icon_title);
        if (titleDrawableResId != 0) {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(activity, titleDrawableResId), null, null);
        }
        tvTitle.setText(title);
        if (titleDrawableResId == 0 && TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
        }
        TextView tvMessage = view.findViewById(R.id.tv_dialog_message);
        tvMessage.setText(msg);
        TextView tvDone = view.findViewById(R.id.tv_dialog_done);
        tvDone.setText(done);
        tvDone.setOnClickListener(v -> {
            if (doneClick != null && doneClick.onClick()) {
                return;
            }
            dialog.dismiss();
        });
        TextView tvCancel = view.findViewById(R.id.tv_dialog_cancel);
        tvCancel.setText(cancel);
        tvCancel.setOnClickListener(v -> {
            if (cancelClick != null && cancelClick.onClick()) {
                return;
            }
            dialog.dismiss();
        });
        dialog.setContentView(view);
        return dialog;
    }

    public static void hideSystemUi(Window window) {
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
