package com.wt.phonelink;

public class Contants {
    public static boolean IS_FRONT = false;//hicar 是否在前台运行
    public static boolean IS_BACKGROUND = false;//hicar 是否在后台被连接后拉起

    public static boolean IS_CARLINK_FRONT = false;//carlink 是否在前台运行
    public static boolean IS_WTBOX_FRONT = false;//wt盒子 是否在前台运行
//    public static boolean IS_CARLINK_BACKGROUND = false;//carlink

    public static boolean IS_PHONE_LINK_FRONT = false;//手机互联是否在前台

    public static class FragmentCon {
        public static final String TAG_HICAR_SCREEN = "hicar_screen";
        public static final String TAG_AP_CONNECT = "hicar_ap_connect";
        public static final String TAG_BT_TIPS = "tag_bt_tips";
        public static final String TAG_HICAR_CONNECTING = "tag_hicar_connecting";
        public static final String TAG_HICAR_FAILED = "tag_hicar_failed";
        public static final String TAG_HICAR_INIT_FAILED = "tag_hicar_init_failed";
        public static final String TAG_HICAR_HELP = "tag_hicar_help";
    }

    //手机盒子投屏状态
    public static final int STATE_WINDOW_FLOAT = 5;//浮窗
    public static final int STATE_WINDOW_MAXIMIZE = 10; //最大化（全屏）
    public static final int STATE_WINDOW_MINIMIZE = 11;  //最小化（悬浮按钮）

    public static final String ACTION_WT_LINK_CONTROL = "com.tinnove.link.action.WT_LINK_CONTROL";
    public static final String PERMISSION_WT_LINK_CONTROL = "com.tinnove.link.permission.WT_LINK_CONTROL";
}
