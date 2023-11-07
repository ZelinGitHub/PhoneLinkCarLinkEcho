package com.incall.apps.hicar.servicesdk.contants;

/**
 * @Author: LuoXia
 * @Date: 2023/1/11 10:04
 * @Description:
 */
public class PlayStatusEvent {
    private int status;

    public PlayStatusEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
