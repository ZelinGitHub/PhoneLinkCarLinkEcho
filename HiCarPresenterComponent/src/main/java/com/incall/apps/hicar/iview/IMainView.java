package com.incall.apps.hicar.iview;


public interface IMainView {
    void onDeviceConnect();
    void onDeviceDisconnect();
    void onDeviceProjectConnect();
    void onDeviceProjectDisconnect();

    void onDeviceServicePause();
    void onDeviceServiceResume();
    void onDeviceServiceStart();
    void onDeviceServiceStop();
    void onDeviceDisplayServicePlaying();
    void onDeviceDisplayServicePlayFailed();

    void onBtConnected();
    void onPinCodeChange();
    void onBrandIconDataChange();
    void onBinderDied();
    void onPinCodeFailed();
    void onAccOff();
    void onQuestShareNet(byte[] data);
    void launchPhoneLink();

    void onOpenApp(byte[] data);
}
