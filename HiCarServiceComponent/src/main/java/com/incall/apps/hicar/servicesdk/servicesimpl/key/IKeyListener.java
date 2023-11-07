package com.incall.apps.hicar.servicesdk.servicesimpl.key;

import android.view.KeyEvent;

/**
 * 按键监听
 *
 */
public interface IKeyListener {
    /**
     * 按键回调
     *
     * @param event 按键
     */
    void onKeyEvent(KeyEvent event);
}
