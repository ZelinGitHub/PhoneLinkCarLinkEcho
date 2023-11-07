package com.omosoft.gesture;
import com.omosoft.gesture.IGestureCallback;

interface IGestureService {
    /**
     * IMS是否可用.
     */
    boolean isIMSEnable();
    /**
     * 用来控制开始/停止识别手势事件。只有SOURCE_APP_STUDY可以使用。
     * 调用后，IMSService会清空帧数据，使用后续数据帧来识别手势。
     *
     * @param recognition true:识别和反馈手势，false:不反馈手势。
     */
    void setGestureRecognition(boolean recognition);
    /**
     * 注册回调，并指明具体的数据源（app身份）。
     * 利用此方法，通知GestureService特殊app（SOURCE_APP_STUDY，SOURCE_APP_GAME，SOURCE_APP_TAKEPHOTO）在前台。
     * 特殊app在前台时，独享所有的手势事件，不再发送给其他app。
     * 而SOURCE_APP_COMMON不独占手势事件，所以GestureService不关心其在前台还是后台，都可以使用感兴趣的手势事件。
     *
     * 此方法调用后，GestureService默认开始发送手势事件。
     *
     * 如果同一个app需要多次调用registerCallback()，建议先调用unregisterCallback()，即保持同一时刻只有一个IGestureCallback起作用。
     *
     * 重要：registerCallback()和unregisterCallback()的配合使用，表达了SOURCE_APP_STUDY，SOURCE_APP_GAME，SOURCE_APP_TAKEPHOTO是否在前台。
     *      这会影响GestureService的手势分发逻辑，请正确使用。具体可参考配套的demo示例，或邮件咨询。
     *
     * @param cb IGestureCallback回调。
     * @param sourceApp app身份辨别。SOURCE_APP_STUDY、SOURCE_APP_GAME、SOURCE_APP_TAKEPHOTO或者SOURCE_APP_COMMON。
     */
    void registerCallback(IGestureCallback cb, int sourceApp);
    /**
     * 注销回调。同时表明特殊app转入后台。
     *
     * 重要：特殊app转入后台时，必须调用此方法，否则会影响手势事件分发逻辑。
     *
     * @param cb IGestureCallback回调。
     */
    void unregisterCallback(IGestureCallback cb);
}