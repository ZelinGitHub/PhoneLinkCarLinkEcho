package com.wt.phonelink.utils;

import android.content.Context;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.openos.statistics.WTClickAgent;
import com.wt.phonelink.MyApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: LuoXia
 * Date: 2022/9/19 15:04
 * Description: 梧桐埋点工具类
 */
public class WTStatisticsUtil {

    private static final String TAG = "WTWLink/WTStatisticsUtil";

    /**
     * @param method 打开方式 1=applist；2=语音指令打开、3=桌面卡片
     */
    public static void showHomePage(int method) {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("method", method);
        WTStatisticsUtil.onEvent(MyApplication.getContext(), Constants.SHOW_HOME_PAGE, hashMap);
    }

    /**
     * @param connectStatus 连接状态：连接、断开连接
     * @param sdkType       sdk类型：1=hicar、2=carlink
     * @param brand         手机品牌：huawei、oppo、vivo、mi
     * @param phoneModel    手机型号：以具体指为准
     */
    public static void connectStatus(String connectStatus, int sdkType, String brand, String phoneModel) {
        HashMap<String, Object> disConnectHashMap = new HashMap();
        disConnectHashMap.put("sdk_type", sdkType);
        disConnectHashMap.put("brand", brand);
        disConnectHashMap.put("model", phoneModel);
        WTStatisticsUtil.onEvent(MyApplication.getContext(), connectStatus, disConnectHashMap);
    }

    public static void onEvent(Context cxt, String eventId) {
        Log.d(TAG, "onEvent eventId:" + eventId);
        WTClickAgent.onEvent(cxt, eventId);
    }

    public static void onEvent(Context cxt, String eventId, String eventLabel) {
        Log.d(TAG, "onEvent eventId:" + eventId + ", eventLabel:" + eventLabel);
        WTClickAgent.onEvent(cxt, eventId, eventLabel);
    }

    public static void onEvent(Context cxt, String eventId, Map<String, Object> mapEvent) {
        Log.d(TAG, "onEvent eventId:" + eventId + ", mapEvent:" + mapEvent);
        WTClickAgent.onEvent(cxt, eventId);
    }

    public static void onEvent(Context cxt, String eventId, String eventLabel, Map<String, Object> mapEvent) {
        Log.d(TAG, "onEvent eventId:" + eventId + ", eventLabel:" + eventLabel + "，mapEvent：" + mapEvent);
        WTClickAgent.onEvent(cxt, eventId);
    }
}
