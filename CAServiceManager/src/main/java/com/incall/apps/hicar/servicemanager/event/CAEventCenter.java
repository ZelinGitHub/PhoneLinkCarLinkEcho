package com.incall.apps.hicar.servicemanager.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CAEventCenter {
    /**
     * 线程池
     */
    ExecutorService executorService = Executors.newFixedThreadPool(5);

    //包含所有Event的容器
    private HashMap<String, List<ICAEventListener>> eventListenersMap = new HashMap();

    /**
     *
     * @param eventName event名称
     * @param listener  event监听回调
     */
    public void addEventListener(String eventName, ICAEventListener listener) {
        List<ICAEventListener> eventListeners = eventListenersMap.get(eventName);
        if (eventListeners==null){
            eventListeners = new ArrayList<ICAEventListener>();
            eventListenersMap.put(eventName,eventListeners);
        }
        eventListeners.add(listener);
    }

    /**
     * @param eventName event名称
     * @param listener  需要移除的event监听回调
     */
    public void removeEventListener(String eventName, ICAEventListener listener) {
        List<ICAEventListener> eventListeners = eventListenersMap.get(eventName);
        if (eventListeners==null) {
            return;
        }
        eventListeners.remove(listener);
    }

    private void notifyAllListener(final String eventName, final Object message){
        List<ICAEventListener> eventListeners = eventListenersMap.get(eventName);
        if (eventListeners==null) {
            return;
        }
        for (int i = 0; i < eventListeners.size(); i++) {
            final ICAEventListener eventListener = eventListeners.get(i);
            if (eventListener!=null){
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eventListener.onEvent(eventName,message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * 发送事件
     * @param eventName 事件名称
     * @param message 事件
     */
    public void postEvent(String eventName, Object message) {
        notifyAllListener(eventName,message);
    }
}
