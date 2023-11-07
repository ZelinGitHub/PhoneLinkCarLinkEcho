package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;

import com.incall.apps.hicar.servicemanager.service.ICACallback;
import com.incall.apps.hicar.servicemanager.service.ICAService;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/20 16:22
 * @Description: 音频管理接口类
 */
public interface IAudioService extends ICAService {
    /**
     * 初始化
     * @param context
     */
    public void init(Context context,ICACallback callback);
}
