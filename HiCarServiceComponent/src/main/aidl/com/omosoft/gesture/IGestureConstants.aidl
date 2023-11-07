package com.omosoft.gesture;

interface IGestureConstants {
    /**
     * 向左挥动。动态。
     * 播放上一曲/上一个视频。
     */
    const int GESTURE_WAVE_LEFT=0;
    /**
     * 向右挥动。动态。
     * 播放下一曲/下一个视频。
     */
    const int GESTURE_WAVE_RIGHT=1;
    /**
     * 向上挥手。
     * 风量增加一档。
     */
    const int GESTURE_WAVE_UP=2;
    /**
     * 向下挥手。
     * 风量减小一档。
     */
    const int GESTURE_WAVE_DOWN=3;
    /**
     * 比心。静态。
     * 导航回家。
     */
    const int GESTURE_FINGER_HEART=4;
    /**
     * 点赞。静态。
     * 导航到公司。
     */
    const int GESTURE_THUMB_UP=5;
    /**
     * 胜利.静态。
     * 打开拍照APP自拍。
     */
    const int GESTURE_VECTORY=6;
    /**
     * 向后挥手。动态。
     * 打开天窗和遮阳帘。
     */
    const int GESTURE_BACKWARD=7;
    /**
     * 向后挥手。动态。
     * 关闭天窗和遮阳帘。
     */
    const int GESTURE_FORWARD=8;
    /**
     * 食指向前，顺时针旋转。动态，重置启动时间间隔1s。
     * HU音量增大。
     */
    const int GESTURE_CLOCKWISE=9;
    /**
     * 食指向前，逆时针旋转。动态，重置启动时间间隔1s。
     * HU音量减小。
     */
    const int GESTURE_COUNTCLOCKWISE=10;
    /**
     * 手掌向上比六。静态，停留1s。
     * 手势控制电话接听。
     */
    const int GESTURE_SIX_UP=11;
    /**
     * 手掌向下比六。静态，停留1s。
     * 手势控制电话挂断。
     */
    const int GESTURE_SIX_DOWN=12;
    /**
     * 注册IGestureCallback的源app：手势学习app。
     */
     const int SOURCE_APP_STUDY = 0;
    /**
     * 注册IGestureCallback的源app：游戏app。
     * 默认值。
     */
     const int SOURCE_APP_GAME = 1;
    /**
     * 注册IGestureCallback的源app：游戏app。
     * 默认值。
     */
     const int SOURCE_APP_TAKEPHOTO = 2;
    /**
     * 注册IGestureCallback的源app：普通app。
     * 默认值。
     */
     const int SOURCE_APP_COMMON = 3;
}