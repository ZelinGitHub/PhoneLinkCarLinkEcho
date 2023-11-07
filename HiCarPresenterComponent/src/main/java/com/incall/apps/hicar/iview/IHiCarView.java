package com.incall.apps.hicar.iview;


public interface IHiCarView {
    void onDeviceConnect();
    void onDeviceDisconnect();
    void onDeviceProjectConnect();
    void onDeviceProjectDisconnect();

    void onDeviceServicePause();
    void onDeviceServiceResume();
    void onDeviceServiceStart();
    void onDeviceServiceStop();
    void onDeviceDisplayServicePlaying();

}
