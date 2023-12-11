package com.incall.apps.hicar.presenter;

import com.incall.apps.hicar.servicesdk.contants.Constants;

import java.util.HashMap;
//蓝牙开启中fragment的presenter
public class BTPresenter extends BasePresenter {


    public BTPresenter() {
    }


    public void openBt() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.OPEN_BT, params);
    }

    public boolean isConnectedDevice() {
        HashMap<String, Object> params = new HashMap<>();
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.IS_CONNECTED_DEVICE, params);
    }

    public boolean isBtConnected() {
        HashMap<String, Object> params = new HashMap<>();
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.IS_BT_CONNECTED, params);
    }

}