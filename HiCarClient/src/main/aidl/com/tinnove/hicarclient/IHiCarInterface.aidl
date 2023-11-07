package com.tinnove.hicarclient;

import com.tinnove.hicarclient.AppInfoChangeListener;
import com.tinnove.hicarclient.HiCarListener;
//API
interface IHiCarInterface {
    void registerAppInfoChangeListener(AppInfoChangeListener appInfoChangeListener);
    void registerHiCarListener(HiCarListener hiCarListener);
    void unRegisterListener();
    boolean isConnected();
    String getPhoneName();
    int sendCarData(int dataType, inout byte[] data);
    int sendKeyEvent(int keyCode, int action);
}