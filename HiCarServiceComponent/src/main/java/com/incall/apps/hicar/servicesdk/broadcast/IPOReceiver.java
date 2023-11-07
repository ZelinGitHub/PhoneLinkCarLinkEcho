package com.incall.apps.hicar.servicesdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;

/**
 * BroadcastReceiver for IPO event
 */
public class IPOReceiver extends BroadcastReceiver {
    private static final String TAG = IPOReceiver.class.getSimpleName();
    public static final String ACTION_SHUTDOWN_HU = "android.intent.action.ACTION_SHUTDOWN_HU";//IPO 关机广播
    public static final String ACTION_BOOT_HU = "android.intent.action.ACTION_BOOT_HU";//IPO开机广播

    public IPOReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(TAG, "IPOReceiver onReceive");
        if (intent == null) {
            LogUtil.d(TAG, "intent is null.");
            return;
        }
        String action = intent.getAction();
        LogUtil.i(TAG, "IPOReceiver onReceive action=" + action);
        if (action != null && action.equals(ACTION_SHUTDOWN_HU)) {
            HiCarServiceManager.getInstance().startIpo();
        } else if (action != null && action.equals(ACTION_BOOT_HU)) {
            HiCarServiceManager.getInstance().startReconnectByStopIpo();
        }
    }
}
