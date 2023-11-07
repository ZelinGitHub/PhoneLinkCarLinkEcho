package com.incall.apps.hicar.servicesdk.contants;

/**
 * @Author: LuoXia
 * @Date: 2023/5/13 15:19
 * @Description:
 */
public class HiCarCallStatusChangeEvent {
    private int callState;

    public HiCarCallStatusChangeEvent(int callState) {
        this.callState = callState;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }
}
