package com.omosoft.gesture;
oneway interface IGestureCallback {
    /**
     * 反馈手势数据。
     * 手势类型参考IGestureConstants定义。
     * @param gestureEvent 手势事件。
     */
    void onGestureEvent(int gestureEvent);

    /**
     * 错误情况反馈。
     * @param errorEvent 错误事件，暂未详细定义。
     * @param errorString 错误描述字符串。
     */
    void onError(int errorEvent, String errorString);
}