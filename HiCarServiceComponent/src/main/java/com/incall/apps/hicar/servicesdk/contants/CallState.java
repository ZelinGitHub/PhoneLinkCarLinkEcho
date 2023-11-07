package com.incall.apps.hicar.servicesdk.contants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * hicar 通话状态 字典
 * @author KongJing
 */
@IntDef({
    CallState.IDLE, CallState.RINGING, CallState.CALLING
})
@Retention(RetentionPolicy.SOURCE)
public @interface CallState {
    /**空闲*/
    int IDLE = 0;
    /**来电*/
    int RINGING = 1;
    /**接听中*/
    int CALLING = 2;
}
