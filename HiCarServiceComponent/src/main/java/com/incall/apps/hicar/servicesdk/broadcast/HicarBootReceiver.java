package com.incall.apps.hicar.servicesdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.HiCarService;

/**
 * BroadcastReceiver for boot event
 * @author zouhongtao
 * @since 2019-08-23
 */
public class HicarBootReceiver extends BroadcastReceiver {
    private static final String TAG = HicarBootReceiver.class.getSimpleName();
    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    public HicarBootReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i(TAG, "HicarBootReceiver onReceive");
        if (intent == null) {
            LogUtil.d(TAG, "intent is null.");
            return;
        }

        String action = intent.getAction();
        LogUtil.i(TAG, "HicarBootReceiver onReceive action="+action);
        if (action != null && action.equals(ACTION_BOOT)) {
            Intent service = new Intent(context, HiCarService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //适配8.0机制
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        }
    }
}
