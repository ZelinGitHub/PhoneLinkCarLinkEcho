package com.incall.apps.hicar.servicesdk.servicesimpl.key;

import android.view.KeyEvent;

/**
 * 按键管理接口
 *
 */
public interface IKeyManager {

    /**
     * 注册监听
     *
     * @param listener 按键监听
     */
    void registerListener(IKeyListener listener);

    /**
     * 接注册监听
     *
     * @param listener 按键监听
     */
    void unregisterListener(IKeyListener listener);

    /**
     * 按键事件回调
     *
     * @param event 按键事件
     */
    void onKeyEvent(KeyEvent event);
}
