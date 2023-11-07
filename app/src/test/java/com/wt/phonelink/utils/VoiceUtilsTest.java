package com.wt.phonelink.utils;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 16:31
 * @Description:
 */
public class VoiceUtilsTest {
    private VoiceUtils voiceUtils;

    @Before
    public void getContext() {
        voiceUtils = VoiceUtils.getInstance();
    }

    @Test
    public void getInstance() {
        VoiceUtils.getInstance();
    }

    @Test
    public void awakenCarLinkVoiceAssistant() {
        voiceUtils.awakenCarLinkVoiceAssistant("key");
    }
//
//    @Test
//    public void playTts() {
//        voiceUtils.playTts("tts");
//    }
//
//    @Test
//    public void stopOrResumeVr() {
//        voiceUtils.stopOrResumeVr(true);
//    }

}
