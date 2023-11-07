package com.incall.apps.hicar.servicemanager.event;

public interface ICAEventListener {

    /**
     * @param eventName 事件名称
     * @param message   内容
     */
    public void onEvent(String eventName, Object message);
}
