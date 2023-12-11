package com.wt.phonelink;

import static com.incall.apps.hicar.servicesdk.contants.Constants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;


public class LinkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "WTPhoneLink/LinkStateReceiver";
    public static final String ACTION_WT_LINK_STATE = "com.tinnove.link.action.WT_LINK_STATE";
    public static final String PERMISSION_WT_LINK_STATE = "com.tinnove.link.permission.WT_LINK_STATE";

    private static final LinkStateReceiver mLinkStateReceiver = new LinkStateReceiver();


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive() action is " + action);
        //收到手机盒子的广播
        if (ACTION_WT_LINK_STATE.equalsIgnoreCase(action)) {
            int state = intent.getIntExtra("state", -1);
            Log.i(TAG, "onReceive() ACTION_WT_LINK_STATE state is " + state);
            SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
            switch (state) {
                case 0:
                default:
                    IS_WTBOX_FRONT = false;
                    sp.putBoolean(Constants.SP_IS_WTBOX_CONNECT, false);
                    break;
                case STATE_WINDOW_FLOAT:
                case STATE_WINDOW_MAXIMIZE:
                    IS_WTBOX_FRONT = true;
                    sp.putBoolean(Constants.SP_IS_WTBOX_CONNECT, true);
                    break;
                case STATE_WINDOW_MINIMIZE:
                    IS_WTBOX_FRONT = false;
                    sp.putBoolean(Constants.SP_IS_WTBOX_CONNECT, true);
                    break;
            }
        }
        SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        Log.e(TAG, "onReceive() isWTBoxConnect: " + isWTBoxConnect);
    }


    public static void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_WT_LINK_STATE);
        context.registerReceiver(mLinkStateReceiver, filter, PERMISSION_WT_LINK_STATE, null);
    }

    public static void unregister(Context context) {
        context.unregisterReceiver(mLinkStateReceiver);
    }

}
