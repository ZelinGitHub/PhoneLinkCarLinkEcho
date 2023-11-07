package com.incall.apps.hicar.servicemanager.event;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import com.incall.apps.hicar.servicemanager.LogUtil;

import org.junit.Before;
import org.junit.Test;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 17:08
 * @Description:
 */
public class CAEventCenterTest implements ICAEventListener {
    private Context context;
    private CAEventCenter caEventCenter;

    @Before
    public void getContext() {
        context = InstrumentationRegistry.getContext();
        caEventCenter = new CAEventCenter();
    }

    @Test
    public void postEvent() {
        caEventCenter.postEvent("eventName", 1);
    }

    @Test
    public void addEventListener() {
        caEventCenter.addEventListener("event", this);
    }

    @Test
    public void removeEventListener() {
        caEventCenter.removeEventListener("event", this);
    }

    @Override
    public void onEvent(String eventName, Object message) {
        LogUtil.d("onEvent eventName " + eventName);
    }
}
