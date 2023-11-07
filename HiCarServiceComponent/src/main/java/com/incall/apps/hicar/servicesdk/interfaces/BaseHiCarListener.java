package com.incall.apps.hicar.servicesdk.interfaces;

import com.tinnove.hicarclient.beans.WTAppInfoBean;

import java.util.List;

//BaseHiCarListener实现了HiCarServiceListener
//这个类在HiCarManagerImpl这个BBinder类中有使用
public class BaseHiCarListener implements HiCarServiceListener {
    @Override
    public void onDeviceChange(String s, int i, int i1) {

    }

    @Override
    public void onDeviceServiceChange(String s, int i) {

    }

    @Override
    public void onDataReceive(String s, int i, byte[] bytes) {

    }

    @Override
    public void onPinCode(String s) {

    }

    @Override
    public void onBinderDied() {

    }

    @Override
    public void onPinCodeFailed() {

    }

    @Override
    public void onLoadAllAppInfo(String deviceId, List<WTAppInfoBean> list) {

    }

    @Override
    public void onAppInfoAdd(String deviceId, List<WTAppInfoBean> list) {

    }

    @Override
    public void onAppInfoRemove(String deviceId, List<WTAppInfoBean> list) {

    }

    @Override
    public void onAppInfoUpdate(String deviceId, List<WTAppInfoBean> list) {

    }
}
