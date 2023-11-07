package com.tinnove.hicarclient;
import java.util.List;
import com.tinnove.hicarclient.beans.WTAppInfoBean;
//监听器，是客户端进程作为响应端进程的BBinder
interface AppInfoChangeListener {
    void onLoadAllAppInfo(String deviceId, inout List<WTAppInfoBean> list);

    void onAppInfoAdd(String deviceId, inout List<WTAppInfoBean> list);

    void onAppInfoRemove(String deviceId, inout List<WTAppInfoBean> list);

    void onAppInfoUpdate(String deviceId, inout List<WTAppInfoBean> list);
}