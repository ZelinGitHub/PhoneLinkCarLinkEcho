package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/18 15:20
 * @Description: TTS类型焦点管控
 */
public class VoiceAudioRoute extends BaseAudioRoute {
    private final static String TAG = "VoiceAudioRoute";
    private static VoiceAudioRoute S_VoiceAudioRoute;
    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener
            = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.v(TAG, "onAudioFocusChange(" + focusChange + ")");
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:

                    break;
                case AudioManager.AUDIOFOCUS_LOSS:             //长失去焦点时
                    Log.i(TAG, "Unexpected audio focus loss");
                    releaseAudioFocus();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:  //暂时失去焦点

                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:  //当前音频降低音量,继续播放

                    break;
                default:
                    Log.w(TAG, "Unexpected audio focus state: " + focusChange);
            }
        }
    };

    VoiceAudioRoute(Context context) {
        super(context);
    }

    @Override
    int requestAudioFocus() {
        return 0;
    }

    @Override
    void releaseAudioFocus() {

    }

    public static synchronized VoiceAudioRoute getInstance(Context context) {
        if (S_VoiceAudioRoute == null) {
            S_VoiceAudioRoute = new VoiceAudioRoute(context);
        }
        return S_VoiceAudioRoute;
    }

    /**
     * 讯飞新版的降噪库，针对华为 hicar 处理 VR模式 切换到 电话模式
     * 电话模式 iflyAecMode = 1
     */
    public void setPhoneMode() {
        if (mAudioManager != null) {
            Log.i(TAG, "change phone mode: iflyAecMode=1 ");
            mAudioManager.setParameters("iflyAecMode=1");
        }
    }

    /**
     * 讯飞新版的降噪库，针对华为 hicar 处理 VR模式 切换到 电话模式
     * VR模式 iflyAecMode = 0
     */
    public void setVoiceRouteMode() {
        if (mAudioManager != null) {
            Log.i(TAG, "change voice mode: iflyAecMode=0 ");
            mAudioManager.setParameters("iflyAecMode=0");
        }
    }
}
