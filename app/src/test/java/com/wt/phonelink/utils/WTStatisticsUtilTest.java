package com.wt.phonelink.utils;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 16:45
 * @Description:
 */
public class WTStatisticsUtilTest {
    @Mock
    Context context;

    @Test
    public void showHomePage() {
        WTStatisticsUtil.showHomePage(0);
    }

    @Test
    public void connectStatus() {
        WTStatisticsUtil.connectStatus("status", 0, "brand", "model");
    }

    @Test
    public void onEventId() {
        WTStatisticsUtil.onEvent(context, "id");
    }

    @Test
    public void onEventLabel() {
        WTStatisticsUtil.onEvent(context, "id", "label");
    }

    @Test
    public void onEventMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 1);
        WTStatisticsUtil.onEvent(context, "id", map);
    }

    @Test
    public void onEventMapLabel() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 1);
        WTStatisticsUtil.onEvent(context, "id", "label", map);
    }
}
