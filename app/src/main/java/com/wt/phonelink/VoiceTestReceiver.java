package com.wt.phonelink;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class VoiceTestReceiver extends BroadcastReceiver {
    private static final String TAG = "WTWLink/VoiceTestReceiver";
    public static final String ACTION_WT_VOICE_TEST = "com.wt.phonelink.action.VoiceTestReceiver";
    public static final String PERMISSION_WT_VOICE_TEST = "com.wt.phonelink.permission.VoiceTestReceiver";

    private static final VoiceTestReceiver RECEIVER = new VoiceTestReceiver();


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive() action: " + action);
        //收到手机盒子的广播
        if (ACTION_WT_VOICE_TEST.equalsIgnoreCase(action)) {
            int state = intent.getIntExtra("state", -1);
            Log.i(TAG, "onReceive() state: " + state);
            if (state == 1) {
                Log.i(TAG, "onReceive() state: " + 1);
//                VoiceManager.getInstance().wakeUpHuawei(null, null);
            }
            if (state == 2) {
                Log.i(TAG, "onReceive() state: " + 2);
                VoiceManager.getInstance().launchOam("小爱同学");
            }
            if (state == 3) {
                Log.i(TAG, "onReceive() state: " + 3);
//                VoiceManager.getInstance().onLaunchPhoneLinkEvent(null);
            }
        }
    }


    public static void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WT_VOICE_TEST);
        context.registerReceiver(RECEIVER, filter, PERMISSION_WT_VOICE_TEST, null);
    }

    public static void unregister(Context context) {
        context.unregisterReceiver(RECEIVER);
    }

}
