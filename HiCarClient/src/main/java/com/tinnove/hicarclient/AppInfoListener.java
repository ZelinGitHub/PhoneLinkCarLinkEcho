package com.tinnove.hicarclient;

import com.tinnove.hicarclient.beans.WTAppInfoBean;
import java.util.List;
//HiCar提供的应用程序数据信息监听
public interface AppInfoListener {
    void onLoadAllAppInfo(String deviceId, List<WTAppInfoBean> list);

    void onAppInfoAdd(String deviceId, List<WTAppInfoBean> list);

    void onAppInfoRemove(String deviceId, List<WTAppInfoBean> list);

    void onAppInfoUpdate(String deviceId, List<WTAppInfoBean> list);
}
