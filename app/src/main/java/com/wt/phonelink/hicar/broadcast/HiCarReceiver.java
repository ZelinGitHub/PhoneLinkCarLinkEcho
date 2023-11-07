package com.wt.phonelink.hicar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wt.phonelink.hicar.HiCarMainActivity;
import com.wt.phonelink.Contants;

public class HiCarReceiver extends BroadcastReceiver {
    private static final String TAG = "WTPhoneLink/HiCarReceiver";
    private static final String ACTION_START_MAINACTIVITY = "com.incall.apps.hicar.ACTION_START_MAINACTIVITY";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "HiCarReceiver onReceive");
        if (intent == null) {
            Log.d(TAG, "intent is null.");
            return;
        }
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_START_MAINACTIVITY) && !Contants.IS_FRONT) {
            Log.d(TAG, "activityIntent: "+intent.getBooleanExtra("isConnect",false));
            Intent activityIntent = new Intent(context, HiCarMainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Contants.IS_BACKGROUND = intent.getBooleanExtra("isConnect",false);
            context.startActivity(activityIntent);
        }
    }
}
