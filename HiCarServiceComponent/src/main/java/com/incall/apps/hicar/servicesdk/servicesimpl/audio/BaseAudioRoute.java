package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;
import android.media.AudioManager;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/18 14:28
 * @Description: 基础音源类
 */
public abstract class BaseAudioRoute {
    private BaseAudioRoute mAudioRoute;
    protected AudioManager mAudioManager;
    protected int mAudioFocus;

    /**
     * 请求音频焦点
     * @return 焦点请求结果
     */
    abstract int requestAudioFocus();

    /**
     * 释放音频焦点
     */
    abstract void releaseAudioFocus();

    BaseAudioRoute(Context context) {
        mAudioManager = (AudioManager)
                context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }
}
