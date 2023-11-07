package com.tinnove.hicarclient;

//HiCar提供的接收数据和设备连接监听
public interface HiCarServiceListener {
    //接收数据
    void onDataReceive(String key, int dataType, byte[] data);

    //设备接入或退出
    /**
     * @param key
     * @param event EVENT_DEVICE_CONNECT --- connected ;EVENT_DEVICE_DISCONNECT --- disconnect
     * @param errorcode
     */
    void onDeviceChange(String key, int event, int errorcode);
}
