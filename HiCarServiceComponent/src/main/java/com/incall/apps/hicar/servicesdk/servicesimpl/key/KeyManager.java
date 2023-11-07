package com.incall.apps.hicar.servicesdk.servicesimpl.key;

import android.view.KeyEvent;


import com.incall.apps.hicar.servicemanager.LogUtil;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 按键管理
 *
 */
public class KeyManager implements IKeyManager {
    private static final String TAG = "KeyManager";
    private static KeyManager mManager;
    private CopyOnWriteArrayList<IKeyListener> mListeners = new CopyOnWriteArrayList<>();

    public static synchronized KeyManager getInstance() {
        if (null == mManager) {
            mManager = new KeyManager();
        }

        return mManager;
    }

    private KeyManager() {
    }

    @Override
    public void registerListener(IKeyListener listener) {
        LogUtil.i(TAG, "registerListener listener=" + listener);
        if (listener != null && !mListeners.contains(listener)) {
            LogUtil.i(TAG, "registerListener remove");
            mListeners.add(listener);
            LogUtil.i(TAG, "registerListener remove size=" + mListeners.size());
        }
    }

    @Override
    public void unregisterListener(IKeyListener listener) {
        LogUtil.i(TAG, "unregisterListener listener=" + listener);
        if (listener != null && mListeners.contains(listener)) {
            LogUtil.i(TAG, "unregisterListener remove");
            mListeners.remove(listener);
            LogUtil.i(TAG, "unregisterListener remove size=" + mListeners.size());
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        LogUtil.i(TAG, "onKeyEvent event=" + event);
        for (IKeyListener listener : mListeners) {
            listener.onKeyEvent(event);
        }
    }
}
