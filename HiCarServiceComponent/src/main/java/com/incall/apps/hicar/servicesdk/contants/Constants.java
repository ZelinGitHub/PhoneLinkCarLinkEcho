package com.incall.apps.hicar.servicesdk.contants;

public class Constants {
    public static int HICAR_START_MODE = 2;
    public static String HICAR_MAC = "hicar_phone_mac";
    public static final String KEY_PROPERTY_IPO_STATUS = "sys.hu.status";
    public static final String PROPERTY_IPO_SHUTDOWN = "shutdown";
    public static final String PROPERTY_IPO_BOOT = "boot";

    /**
     * carlink是否连接
     */
    public static String SP_IS_CARLINK_CONNECT = "SP_IS_CARLINK_CONNECT";
    public static String SP_IS_WTBOX_CONNECT = "SP_IS_WTBOX_CONNECT";
    public static String SYS_IS_CARLINK_CONNECT = "system.carlink.connect.state";
    public static String SYS_IS_HICAR_CONNECT = "system.hicar.connect.state";

    /**
     * hicar是否连接
     */
    public static String SP_IS_HICAR_CONNECT = "SP_IS_HICAR_CONNECT";
    /**
     * 手机型号
     */
    public static String SP_PHONE_MODEL = "SP_PHONE_MODEL";
    /**
     * 手机品牌:huawei、oppo、vivo、mi
     */
    public static String SP_PHONE_BRAND = "SP_PHONE_BRAND";

    //连接首页曝光
    public static final String SHOW_HOME_PAGE = "phonelink_show_homepage";
    //手机互联连接成功
    public static final String SERVER_LINK = "phonelink_server_link";
    //手机互联连接失败
    public static final String SERVER_BREAKLINK = "phonelink_server_breaklink";


    public class Services {
        public static final String PIN_SERVICE = "pin_service";
        public static final String MAIN_SERVICE = "main_service";
        public static final String HICAR_SERVICE = "hicar_service";
    }

    public class Event {
        public static final String PIN_CODE_CHANGE = "pin_code_change";
        public static final String DEVICE_CONNECT = "device_connect";
        public static final String DEVICE_DISCONNECT = "device_disconnect";
        public static final String DEVICE_PROJECT_CONNECT = "device_project_connect";
        public static final String DEVICE_PROJECT_DISCONNECT = "device_project_disconnect";
        public static final String DEVICE_SERVICE_PAUSE = "device_service_pause";
        public static final String DEVICE_SERVICE_RESUME = "device_service_resume";
        public static final String DEVICE_SERVICE_START = "device_service_start";
        public static final String DEVICE_SERVICE_STOP = "device_service_stop";
        public static final String DEVICE_DISPLAY_SERVICE_PLAYING = "device_display_service_playing";
        public static final String DEVICE_DISPLAY_SERVICE_PLAY_FAILED = "device_display_service_play_failed";
        public static final String BRAND_ICON_DATA_CHANGE = "brand_icon_data_change";
        public static final String OPEN_APP = "open_app";
        public static final String HICAR_BINDER_DIED = "hicar_binder_died";
        public static final String BT_CONNECTED = "bt_connected";
        public static final String PIN_CODE_FAILED = "pin_code_failed";
        public static final String ACC_OFF = "acc_off";
        public static final String ON_HOME = "on_home";
        public static final String REQUEST_SHARE_NET = "request_share_net";
        //方控长按唤醒语音
        public static final String SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE = "square_control_long_press_wakeup_voice";
    }

    public class Method {
        public static final String GET_PINCODE = "get_pincode";
        public static final String UPDATE_CARCONFIG = "updateCarConfig";
        public static final String START_PROJECTION = "startProjection";
        public static final String PAUSE_PROJECTION = "pauseProjection";
        public static final String STOP_PROJECTION = "stopProjection";
        public static final String START_HICAR_ADV = "starthicaradv";
        public static final String DIS_CONNECTED = "disConnected";
        public static final String IS_CONNECTED_DEVICE = "isConnectedDevice";
        public static final String STOP_HICAR_ADV = "stophicaradv";
        public static final String SET_ICON = "set_icon";
        public static final String GET_PHONE_NAME = "get_phone_name";
        public static final String IS_PLAY_ANIM = "isPlayAnim";
        public static final String IS_BT_CONNECTED = "is_bt_connected";
        public static final String OPEN_BT = "open_bt";
        public static final String HICAR_TY = "hicar_ty";
        public static final String DISCONNECT_BT = "disconnect_bt";
    }


    public class HiCarCons {
        public static final int EVENT_DEVICE_CONNECT = 101;//设备已连接
        public static final int EVENT_DEVICE_DISCONNECT = 102;//设备已断连
        public static final int EVENT_DEVICE_PROJECT_CONNECT = 104;//投屏通道已连接
        public static final int EVENT_DEVICE_PROJECT_DISCONNECT = 105;//投屏通道断开连接
        public static final int EVENT_DEVICE_ADV_START = 106;//开始发现广播通知
        public static final int EVENT_DEVICE_ADV_STOP = 107;//停止发现广播通知
        public static final int EVENT_DEVICE_MIC_REQUEST = 109;//请求使用车机Mic
        public static final int EVENT_DEVICE_MIC_RELEASE = 110;//车机Mic 释放通知

        public static final int EVENT_DEVICE_SERVICE_PAUSE = 202; //服务暂停
        public static final int EVENT_DEVICE_SERVICE_RESUME = 203; //服务恢复
        public static final int EVENT_DEVICE_SERVICE_START = 204; //服务启动
        public static final int EVENT_DEVICE_SERVICE_STOP = 205; //服务停止
        public static final int EVENT_DEVICE_DISPLAY_SERVICE_PLAYING = 207;//开始投屏
        public static final int EVENT_DEVICE_DISPLAY_SERVICE_PLAY_FAILED = 208;//投屏失败
        public static final int  EVENT_DEVICE_SERVICE_VIRMODEM_CALLING = 209;//Modem 通话接听中
        public static final int EVENT_DEVICE_SERVICE_VIRMODEM_HANG_UP = 210;//Modem 通话挂断


        public static final int DISCONNECT_TYPE_ABNORMAL = 1;//物理链路异常断开连接
        public static final int DISCONNECT_TYPE_MANUAL = 2;//用户手动断开连接
        public static final int DISCONNECT_TYPE_EXCEPTION = 3;//运行异常断开连接
        public static final int CONNECTION_TYPE_WIFI = 4;//连接方式为Wi-Fi
        public static final int CONNECTION_TYPE_USB = 5;//连接方式为USB

        public static final int SUCCESS = 0; //成功
        public static final int ERROR_CODE_FAILED = -1; //失败
        public static final int ERROR_CODE_INVALID_ARGUMENT = -2; //不是效参数
        public static final int ERROR_CODE_REMOTE_EXCEPTION = -3; //远程调用异常
        public static final int ERROR_CODE_NOT_REGISTER = -4; //事件监听未注册
        public static final int ERROR_CODE_BUSY = -5; //设备被占用
        public static final int ERROR_CODE_ALREADY_REGISTER = -6; //事件监听已注册
        public static final int ERROR_CODE_NOT_IMPLEMENT = -7; //接口功能未实现
        public static final int ERROR_CODE_NOT_CREATED = -8; //变量未创建
        public static final int ERROR_CODE_MISMATCH_CONNECTION_STATE = -50;//连接状态不匹配
        public static final int ERROR_CODE_MISMATCH_RECONNECT_STATE = -51;//回连状态不匹配
        public static final int ERROR_CODE_MISMATCH_ADV_STATE = -52; //广播状态不匹配
        public static final int ERROR_CODE_MISMATCH_PROJECTION_STATE = -53;//投屏状态不匹配
        public static final int ERROR_CODE_NOT_SUPPORT_MODE = -54; //连接模式不支持


        public static final int DATA_TYPE_HOTWORD = 2;//车机侧唤醒HiCar语音交互
        public static final int DATA_TYPE_VEHICLE_CONTROL = 500;//车辆空调 车窗 天窗控制模式
        public static final int DATA_TYPE_DAY_NIGHT_MODE = 501;//白天黑夜模式
        public static final int DATA_TYPE_BRAND_ICON_DATA = 502;//汽车品牌Icon 图标
        public static final int DATA_TYPE_NAV_FOCUS = 503;//导航焦点
        public static final int DATA_TYPE_CALL_STATE_FOCUS = 504;//电话状态和焦点
        public static final int DATA_TYPE_VOICE_STATE = 505;//语音状态
        public static final int DATA_TYPE_DRIVING_MODE = 506;//驾驶模式
        public static final int DATA_TYPE_CAR_STATE = 507;//车身数据
        public static final int DATA_TYPE_SERVICE_CHANNEL = 508;//车服务通道
        public static final int DATA_TYPE_KEYCODE = 509;//车机物理快捷键
        public static final int DATA_TYPE_SHARE_NET = 511;//共享上网
        public static final int DATA_TYPE_META_MEDIA = 515;//音频元数据
        public static final int DATA_TYPE_REQUEST_APP = 529;//打开APP
        public static final int DATA_TYPE_META_SCENE = 530;//场景元数据


        public static final int DATA_TYPE_DISCONNECT_BY_USER = 518;//主动断开车机连接通知0
    }

    public static boolean IS_HICAR_FRONT = false;//hicar 是否在前台运行
    public static boolean IS_HICAR_BACKGROUND_CONNECT = false;//hicar 是否在后台被连接后拉起

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
