package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;
import android.media.AudioManager;

import com.incall.apps.hicar.servicesdk.contants.AudioConstants;
import com.incall.apps.hicar.servicemanager.service.CAServiceErrorCode;
import com.incall.apps.hicar.servicemanager.service.CAServiceException;
import com.incall.apps.hicar.servicemanager.service.ICACallback;

import java.util.Map;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/20 16:20
 * @Description: 音频焦点管理服务类
 */
public class AudioServiceImpl implements IAudioService {
    private AudioManager mAudioManager;
    private final static String TAG = "AudioService";

    @Override
    public void callMethod(String methodName, Map params, ICACallback callback) throws CAServiceException {
        switch (methodName) {
            case AudioConstants.Method.INIT:
                if (params.get(AudioConstants.Params.STR_CONTEXT) instanceof Context) {
                    init((Context) params.get(AudioConstants.Params.STR_CONTEXT), callback);
                } else {
                    throw new CAServiceException(CAServiceErrorCode.ILLEGAL_ARGUMENT);
                }
                break;
            case AudioConstants.Method.REGISTER_AUDIO_LISTENER:
                break;
            default:
                throw new CAServiceException(CAServiceErrorCode.METHOD_NOT_FOUND);
        }
    }

    @Override
    public Object callMethodSync(String methodName, Map params) throws CAServiceException {
        return null;
    }

    @Override
    public void init(Context context, ICACallback callback) {
        HcAudioManager.getInstance();
    }
}
