package com.incall.apps.hicar.servicesdk.servicesimpl;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;


import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.servicesimpl.key.KeyManager;
import com.incall.apps.hicar.servicesdk.utils.AppStatusUtil;

/**
 * android辅助功能监听按键
 */
public class CommonKeyService extends AccessibilityService {

    private final String TAG = "WTPhoneLink/CommonKeyService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onCreate() {
        LogUtil.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 汽车方向盘按钮监听,长按语音会调用hicar的语音
     * @param event
     * @return
     */
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        LogUtil.i(TAG, "onKeyEvent,keycode:" + event.getKeyCode());
        KeyManager.getInstance().onKeyEvent(event);
        //add by lqs 2022.3.23 解决 DTM20220322000332 DTM20220214000099 方控跳两次和长按连跳 车机还有其他音乐播放时就会复现
        //super.onKeyEvent(event)一直返回false，导致与HiSightSurfaceView.onKeyDown()方法重复发送KeyEvent 在前台时返回true
        if (AppStatusUtil.isForeground(getApplication())){
            super.onKeyEvent(event);
            LogUtil.i(TAG, "HiCar is Foreground , onKeyEvent return true , other app can't response KeyEvent");
            return true;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
