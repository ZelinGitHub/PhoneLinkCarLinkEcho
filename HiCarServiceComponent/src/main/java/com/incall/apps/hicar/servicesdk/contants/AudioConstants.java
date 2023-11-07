package com.incall.apps.hicar.servicesdk.contants;

public class AudioConstants {

    public static class Services {
        public static final String AUDIO_SERVICE = "audio_service";
    }

    public static class Event {
        public static final String AUDIO_FOCUS_CHANGE = "audio_focus_change";
    }

    public static class Method {
        public static final String INIT = "init";
        public static final String REGISTER_AUDIO_LISTENER = "register_audio_listener";
        public static final String REQUEST_AUDIO_FOCUS = "request_audio_focus";
    }

    public static class Params {
        public static final String STR_CONTEXT = "context";
    }

    /**
     * 参考HiCar手机互联系统逻辑设计v1.2文档，3.4.2.3
     */
    public static class SourceUsage {
        /**
         * 媒体
         * 多媒体音频（比如音乐播放器、电影音轨）
         */
        public static final int AUDIO_USAGE_MEDIA = 1;
        //来电/呼叫/通话
        /**
         * 电话
         * 语音通话（比如打电话、VoIP 通话）
         */
        public static final int AUDIO_USAGE_VOICE_COMMUNICATION = 2;
        /**
         * 铃声
         * 正在通话中提示音（比如线路忙音、DTMF 音）
         */
        public static final int AUDIO_USAGE_VOICE_COMMUNICATION_SIGNALLING = 3;
        //音乐多媒体
        /**
         * 提示音
         * 警示音（比如起床闹钟）
         */
        public static final int AUDIO_USAGE_ALARM = 4;
        /**
         * 提示音
         * 提示音
         */
        public static final int AUDIO_USAGE_NOTIFICATION = 5;

        /**
         * 铃声
         * 通话铃声（比如打电话、VoIP 通话）
         */
        public static final int AUDIO_USAGE_NOTIFICATION_TELEPHONY_RINGTONE = 6;
        /**
         * 提示音
         * 当请求加入或结束 VoIP、视频会议这样的会话时的提示音
         */
        public static final int AUDIO_USAGE_NOTIFICATION_COMMUNICATION_REQUEST = 7;
        /**
         * 提示音
         * 即时通讯提示音（比如即时聊天软件收到消息提示音、收到短信提示音）
         */
        public static final int AUDIO_USAGE_NOTIFICATION_COMMUNICATION_INSTANT = 8;
        /**
         * 提示音
         * 非即时通讯提示音（比如收到 E-Mail 时的提示音）
         */
        public static final int AUDIO_USAGE_NOTIFICATION_COMMUNICATION_DELAYED = 9;
        /**
         * 提示音
         * 当希望吸引用户注意时的提示音（比如日程提醒、低电量告警）
         */
        public static final int AUDIO_USAGE_NOTIFICATION_EVENT = 10;
        //语音交互
        /**
         * 易用性辅助语音（比如屏幕文字朗读器）
         */
        public static final int AUDIO_USAGE_ASSISTANCE_ACCESSIBILITY = 11;
        //导航播报
        /**
         * 导航
         * 导航语音（三方导航应用传入的 Usage是媒体，按媒体处理）AUDIO_USAGE_MEDIA（1）
         */
        public static final int AUDIO_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE = 12;

        /**
         * 发声辅助语音（比如使用 UI 界面时听到的声音）（语音播报音）
         */
        public static final int AUDIO_USAGE_ASSISTANCE_SONIFICATION = 13;

        /**
         * 媒体
         * 游戏音乐
         */
        public static final int AUDIO_USAGE_GAME = 14;
        /**
         * 媒体
         * 虚拟数据源
         */
        public static final int AUDIO_USAGE_VIRTUAL_SOURCE = 15;
        /**
         * 媒体
         * 辅助音
         */
        public static final int AUDIO_USAGE_ASSISTANT = 16;
        /**
         * 文本语音
         */
        public static final int AUDIO_USAGE_TTS = 17;
    }
}
