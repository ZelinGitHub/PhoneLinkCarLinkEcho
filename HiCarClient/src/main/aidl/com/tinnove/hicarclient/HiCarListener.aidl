package com.tinnove.hicarclient;
//监听器，是客户端进程作为响应端进程的BBinder
interface HiCarListener {
    void onDataReceive(String key, int dataType, inout byte[] data);
    void onDeviceChange(String key, int event, int errorcode);
}
