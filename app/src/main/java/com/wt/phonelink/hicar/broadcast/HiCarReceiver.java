package com.wt.phonelink.hicar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.wt.phonelink.hicar.HiCarMainActivity;

public class HiCarReceiver extends BroadcastReceiver {
    private static final String TAG = "WTWLink/HiCarReceiver";
    private static final String ACTION_START_MAINACTIVITY = "com.incall.apps.hicar.ACTION_START_MAINACTIVITY";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "HiCarReceiver onReceive");
        if (intent == null) {
            Log.d(TAG, "intent is null.");
            return;
        }
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_START_MAINACTIVITY) && !Constants.IS_HICAR_FRONT) {
            Log.d(TAG, "activityIntent: "+intent.getBooleanExtra("isConnect",false));
            Intent activityIntent = new Intent(context, HiCarMainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Constants.IS_HICAR_BACKGROUND_CONNECT = intent.getBooleanExtra("isConnect",false);
            context.startActivity(activityIntent);
        }
    }
}
