package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.util.Log;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/18 11:58
 * @Description: 媒体Media类型焦点管控（多媒体类型，包含音乐，在线电台，其它媒体播放器,此焦点类型不需要主动释放焦点）
 */
public class MediaAudioRoute extends BaseAudioRoute {
    private final static String TAG = "MediaAudioRoute";
    private static MediaAudioRoute S_MediaAudioRoute;
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

    MediaAudioRoute(Context context) {
        super(context);
    }

    @Override
    int requestAudioFocus() {
        AudioAttributes playbackAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        //
        AudioFocusRequest mGainFocusReq = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttr)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener(mAudioFocusListener)
                .build();

        int focusRequestStatus = mAudioManager.requestAudioFocus(mGainFocusReq);
        // If the request is granted begin streaming immediately and play sound.
        if (focusRequestStatus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
            //媒体类需要在申请成功的时候切源
//            sourceMngProxy.requestSource();
        }
        return focusRequestStatus;
    }

    @Override
    void releaseAudioFocus() {

    }

    public static synchronized MediaAudioRoute getInstance(Context context) {
        if (S_MediaAudioRoute == null) {
            S_MediaAudioRoute = new MediaAudioRoute(context);
        }
        return S_MediaAudioRoute;
    }

}
