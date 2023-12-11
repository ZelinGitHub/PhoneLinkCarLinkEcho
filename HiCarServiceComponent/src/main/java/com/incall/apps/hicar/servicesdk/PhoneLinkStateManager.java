package com.incall.apps.hicar.servicesdk;

import android.content.Context;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.ucar.vehiclesdk.UCarAdapter;

public class PhoneLinkStateManager {
    private static final String TAG = "WTWLink/PhoneLinkStateManager";

    //断开连接
    public static void disconnectDevice(Context context) {
        Log.i(TAG, "disconnectDevice()");
        //从sp取值。判断hiCar当前是否连接
        SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(context);
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT, false);
        //从sp取值。判断carLink当前是否连接。
        boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT, false);
        Log.i(TAG, "disconnectDevice() isHiCarConnect：" + isHiCarConnect + ", isCarLinkConnect: " + isCarLinkConnect);
        //判断当前的连接品牌是hicar还是carlink。当前连接的是hiCar
        if (isHiCarConnect) {
            Log.i(TAG, "disconnectDevice() hiCar");
            HiCarServiceManager.getInstance().disconnectDevice();
        }
        //当前连接的是carlink
        else if (isCarLinkConnect) {
            Log.i(TAG, "disconnectDevice() carLink");
            //断开CarLink的连接
            boolean disconnectCarLinkResult = UCarAdapter.getInstance().disconnect();
            Log.i(TAG, "disconnectDevice() disconnectCarLinkResult: " + disconnectCarLinkResult);
        }
    }
}
