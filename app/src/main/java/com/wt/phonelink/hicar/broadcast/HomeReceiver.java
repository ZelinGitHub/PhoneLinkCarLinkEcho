package com.wt.phonelink.hicar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incall.apps.hicar.servicemanager.LogUtil;


public class HomeReceiver extends BroadcastReceiver {
    private final String TAG = HomeReceiver.class.getSimpleName();
    private final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private final String HOME_KEY = "homekey";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(TAG, "HomeReceiver onReceive");
        if (intent == null) {
            LogUtil.d(TAG, "intent is null.");
            return;
        }
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (HOME_KEY.equals(reason)) {
                LogUtil.d(TAG, "onHome");
            }
        }
    }
}
