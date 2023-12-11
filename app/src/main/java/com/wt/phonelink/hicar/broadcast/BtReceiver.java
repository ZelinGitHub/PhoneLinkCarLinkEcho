package com.wt.phonelink.hicar.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.PhoneLinkStateManager;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;

/**
 * @Author: LuoXia
 * @Date: 2023/2/20 9:50
 * @Description: 监听蓝牙广播的广播接收者
 */
public class BtReceiver extends BroadcastReceiver {

    private static final String TAG = "WTPhoneLink/BtReceiver";

    private static final BtReceiver receiver = new BtReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive()");
        if (intent == null) {
            Log.e(TAG, "onReceive() intent is null! ");
            return;
        }
        Log.d(TAG, "onReceive intent: " + intent);
        //蓝牙状态改变的action
        String intentAction = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intentAction)) {
            /*
             * blueToothState 对应的值如下
             * BluetoothAdapter.STATE_TURNING_ON 14
             * BluetoothAdapter.STATE_ON 12
             * BluetoothAdapter.STATE_TURNING_OFF 13
             * BluetoothAdapter.STATE_OFF 10
             */
            int blueToothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            Log.i(TAG, "onReceive() blueToothState: " + blueToothState);
            if (blueToothState == BluetoothAdapter.STATE_ON) {
                //监听到蓝牙广播打开后再去设置值为之前保存的值
                //断开连接且打开蓝牙后设置为之前保存的值
                int value = SharedPreferencesUtil.getInstance(context).getInt("capability", -1);
                Log.i(TAG, "onReceive() onDeviceChange  sp IoCapability: " + value);
                if (value != -1) {
                    CommonUtil.setIoCapability(value);
                    Log.i(TAG, "onReceive() onDeviceChange EVENT_DEVICE_DISCONNECT  capability: " + (CommonUtil.getIoCapability()));
                }
                PhoneLinkStateManager.disconnectDevice(context.getApplicationContext());
            }
        }
    }

    /**
     * 注册
     */
    public static void register(Context context) {
        Log.i(TAG, "register() 注册BtReceiver");
        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(receiver, filter);
    }

    /**
     * 注销
     */
    public static void unregister(Context context) {
        Log.i(TAG, "unregister() 注销BtReceiver");
        context.unregisterReceiver(receiver);
    }

}
