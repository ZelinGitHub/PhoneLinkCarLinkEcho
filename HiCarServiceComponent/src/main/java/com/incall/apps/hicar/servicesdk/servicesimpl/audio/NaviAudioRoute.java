package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/18 11:58
 * @Description: 导航类型焦点管控
 */
public class NaviAudioRoute extends BaseAudioRoute {
    private final static String TAG = "NaviAudioRoute";
    private static NaviAudioRoute S_NaviAudioRoute;
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

    NaviAudioRoute(Context context) {
        super(context);
    }

    @Override
    int requestAudioFocus() {
        return 0;
    }

    @Override
    void releaseAudioFocus() {

    }

    public static synchronized NaviAudioRoute getInstance(Context context) {
        if (S_NaviAudioRoute == null) {
            S_NaviAudioRoute = new NaviAudioRoute(context);
        }
        return S_NaviAudioRoute;
    }
}
