package com.wt.phonelink.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.util.Log;

import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.wt.phonelink.MyApplication;


public class VoiceUtils {

    private static final String TAG = "WTPhoneLink/VoiceUtils";
    private static VoiceUtils voiceUtils;
    //启动车机的全局免唤醒语音助手
    public static final String START_VOICE_ASSISTANT = "resumevr";
    //屏蔽车机的全局免唤醒语音助手
    public static final String STOP_VOICE_ASSISTANT = "stopvr";
    //当前讯飞语音是否可用
    public boolean IS_IFLYTEK_ENABLE = false;

    public static VoiceUtils getInstance() {
        if (voiceUtils == null) {
            synchronized (VoiceUtils.class) {
                voiceUtils = new VoiceUtils();
            }
        }
        return voiceUtils;
    }

    /**
     * 唤醒carlink语音助手
     *
     * @param keyword 手机语音助手唤醒词：小布小布、小V小V、小爱同学
     * @return
     */
    public boolean awakenCarLinkVoiceAssistant(String keyword) {
        UCarCommon.AudioFormat audioFormat = new UCarCommon.AudioFormat("audio/pcm", AudioFormat.ENCODING_PCM_16BIT,
                16000, 16);
        boolean result = UCarAdapter.getInstance().awakenVoiceAssistant(new byte[]{}, audioFormat, keyword);
        Log.d(TAG, " onStartCommand() result: " + result);
        return result;
    }

    /**
     * 播报讯飞tts
     *
     * @param text 播报内容
     */
    public void playTts(String text) {
        //voicecoreservice
        Intent intent = new Intent("com.iflytek.autofly.TtsService").setPackage("com.iflytek.autofly.voicecoreservice");
        intent.putExtra("text", text);
        intent.putExtra("operation", "PLAY");
        intent.putExtra("package", MyApplication.getContext().getPackageName());

        MyApplication.getContext().startService(intent);
    }

    /**
     * @param open 打开或者关闭 ：true or false
     */
    public void stopOrResumeVr(boolean open) {
        //SpeechClientService
        Intent intent = new Intent("com.iflytek.autofly.voicecoreservice.SpeechClientService").setPackage("com.iflytek.autofly.voicecoreservice");
        intent.putExtra("carlink", "");
        intent.putExtra("open", open);
        intent.putExtra("package", "com.wt.phonelink");
        Log.e(TAG, open ? "打开讯飞！！！" : "禁用讯飞！！！");
        IS_IFLYTEK_ENABLE = open;
        Context context=MyApplication.getContext();
        if (context != null) {
            context.startService(intent);
        }
    }
}
