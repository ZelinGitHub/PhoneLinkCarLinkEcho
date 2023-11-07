package com.incall.apps.hicar.servicemanager.service;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import com.incall.apps.hicar.servicemanager.LogUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 17:23
 * @Description:
 */
public class CAServiceCenterTest implements ICACallback {
    private Context context;
    private CAServiceCenter caServiceCenter;

    @Before
    public void getContext() {
        context = InstrumentationRegistry.getContext();
        caServiceCenter = new CAServiceCenter();
    }

    @Test
    public void callService() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "1");
        caServiceCenter.callService("service", "method", map, this);
    }

    @Test
    public void hasService() {
        caServiceCenter.hasService("serviceName");
    }

    @Test
    public void callServiceSync() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "1");
        caServiceCenter.callServiceSync("serviceName", "methodname", map);
    }

    @Test
    public void registerService() {
        caServiceCenter.registerService("name", (ICAService) this);
    }

    @Test
    public void unregisterService() {
        caServiceCenter.unregisterService("name");
    }

    @Override
    public void onSuccess(Object o) {
        LogUtil.d("CAServiceCenterTest onSuccess");
    }

    @Override
    public void onFailed(CAServiceException exception) {
        LogUtil.d("CAServiceCenterTest onFailed");
    }
}
