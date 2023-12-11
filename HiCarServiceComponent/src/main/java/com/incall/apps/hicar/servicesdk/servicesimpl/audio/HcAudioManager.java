package com.incall.apps.hicar.servicesdk.servicesimpl.audio;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.huawei.dmsdpsdk.CustomizedAudioAttributes;
import com.huawei.hicarsdk.CarAudioListener;
import com.incall.apps.hicar.servicesdk.ServiceManager;
import com.incall.apps.hicar.servicesdk.contants.AudioConstants;
import com.incall.apps.hicar.servicesdk.contants.CallState;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.contants.HiCarCallStatusChangeEvent;
import com.incall.apps.hicar.servicesdk.contants.HicarCallStatusFocusEvent;
import com.incall.apps.hicar.servicesdk.contants.PlayStatusEvent;
import com.incall.apps.hicar.servicesdk.interfaces.BaseHiCarListener;
import com.incall.apps.hicar.servicesdk.manager.HiCarDataCollectManager;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.servicesimpl.CommonKeyService;
import com.incall.apps.hicar.servicesdk.servicesimpl.key.IKeyListener;
import com.incall.apps.hicar.servicesdk.servicesimpl.key.KeyManager;
import com.incall.apps.hicar.servicesdk.servicesimpl.key.MyKeyCode;
import com.incall.apps.hicar.servicesdk.utils.AppStatusUtil;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.tinnove.skill.api.template.IResponseObserver;
import com.tinnove.skill.sdk.NaviSkillSDK;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Map;

/**
 * Author: ZengJie
 * CreateDate: 20/11/17 17:11
 * Description: hi Car音频管理类
 */
public class HcAudioManager extends BaseHiCarListener implements IKeyListener {
    private final static String TAG = "WTPhoneLink/HcAudioManager";
    /**
     * 接收导航控制action字段
     */
    //高德地图广播接收器
    private final static String NAVI_MAP_ACTION_RECEIVER = "AUTONAVI_STANDARD_BROADCAST_SEND";
    //高德地图包名
    private final static String NAVI_MAP_PACKAGE_NAME = "com.autonavi.amapauto";
    //腾讯地图广播接收器
    private final static String WECARNAVI_MAP_ACTION_SEND = "WECARNAVIAUTO_STANDARD_BROADCAST_SEND";
    //腾讯地图包名
    private final static String WECARNAVI_MAP_PACKAGE_NAME = "com.tinnove.wecarnavi";
    /**
     * 发给导航控制action字段
     */
    private final static String NAVI_MAP_ACTION_SEND = "AUTONAVI_STANDARD_BROADCAST_RECV";
    private final static String NAVI_MAP_EXTRA_TYPE = "KEY_TYPE";
    private final static String NAVI_MAP_EXTRA_STATUS = "EXTRA_STATE";
    private final static String NAVI_MAP_EXTRA_OPERA = "EXTRA_OPERA";
    /**
     * 导航控制action指令：退出
     */
    private final static int NAVI_MAP_EXTRA_VAL_EXIT = 10021;
    /**
     * 导航控制action指令：退出当前一次导航
     */
    private final static int NAVI_MAP_EXTRA_VAL_END = 10010;
    /**
     * 启动或退出腾讯导航app
     */
    private final static int WECAR_NAVI_MAP_EXTRA_VAL = 1002;
    /**
     * 获取地图的运行状态
     */
    private final static int NAVI_MAP_EXTRA_VAL_APP_STATUS = 10019;
    //=== 地图应用的运行状态 start
    private final static int NAVI_MAP_APP_STATUS_OPEN = 0;
    private final static int NAVI_MAP_APP_STATUS_INIT = 1;
    private final static int NAVI_MAP_APP_STATUS_EXIT = 2;
    private final static int NAVI_MAP_APP_STATUS_ONSTART = 3;

    private final static int NAVI_MAP_APP_STATUS_ONSTOP = 4;
    //=== 地图应用的运行状态 end
    //当前类的对象
    private static HcAudioManager S_HcAudioManager;
    private Context mContext;
    private boolean mNaviRegisterFlag;
    /**
     * 注册音频相关的功能. by zj. 20201127
     */
    private final HiCarAudioListener mHiCarAudioListener = new HiCarAudioListener();
    /**
     * 地图相关的广播类
     */
    private final NaviBroadCastReceiver naviBroadCastReceiver = new NaviBroadCastReceiver();
    //点击的首次点击时间
    private long mStartDownTime;
    //小艺语音助手的长按间隔时间
    private static final long VOICE_ASSIST_LONG_PRESSS_DOWN = 1500L;
    /**
     * 用于处理通话中，来电，空闲的情况下，同一个按钮，上一曲与下一曲的切换
     * 默认就0 空闲 1 来电 2 通话中
     * author KongJing
     * 2021.6.30
     */
    private int mCallState = 0;
    /**
     * 处理语音长按事件
     */
    private final static int MSG_VOICE_KEY = 1;
    private final MyHandler mHandler;

    public synchronized static HcAudioManager getInstance() {
        if (S_HcAudioManager == null) {
            S_HcAudioManager = new HcAudioManager();
        }
        return S_HcAudioManager;
    }

    //构造方法
    private HcAudioManager() {
        mHandler = new MyHandler();
    }

    //这个方法在HiCarServiceManager的loadHiCarSdk方法里面调用
    public void init(Context context) {
        mContext = context;
        KeyManager.getInstance().registerListener(this);
        //核心类HiCarServiceManager
        HiCarServiceManager.getInstance().registerServiceListener(this);
        initCommonKeyService();
        startCommonKeyService();
        registerNaviMapBroadcast(mContext);
    }

    /**
     * 注册广播监听地图导航
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void registerNaviMapBroadcast(Context context) {
        if (context == null || mNaviRegisterFlag) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NAVI_MAP_ACTION_RECEIVER);
        intentFilter.addAction(WECARNAVI_MAP_ACTION_SEND);
        context.registerReceiver(naviBroadCastReceiver, intentFilter);
        mNaviRegisterFlag = true;
        Log.i(TAG, "registerNaviMapBroadcast() receiver map status.");
    }

    /**
     * 解除注册广播监听地图导航
     */
    public void unRegisterNaviMapBroadcast(Context context) {
        if (context == null || !mNaviRegisterFlag) {
            return;
        }
        context.unregisterReceiver(naviBroadCastReceiver);
        mNaviRegisterFlag = false;
        Log.i(TAG, "unRegisterNaviMapBroadcast() remove map status.");
    }

    /**
     * 请求音频焦点
     * param type
     */
    public void requestAudioFocus(int type) {
        switch (type) {
            //多媒体
            case AudioAttributes.USAGE_MEDIA:
                MediaAudioRoute.getInstance(mContext).requestAudioFocus();
                break;
            //导航
            case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                NaviAudioRoute.getInstance(mContext).requestAudioFocus();
                break;
            //TTS语音
            case AudioAttributes.USAGE_ASSISTANT:
                VoiceAudioRoute.getInstance(mContext).requestAudioFocus();
                break;
            //电话
            case AudioAttributes.USAGE_VOICE_COMMUNICATION:
                PhoneCallAudioRoute.getInstance(mContext).requestAudioFocus();
                break;
            default:
                break;
        }
    }

    public HiCarAudioListener getHiCarAudioListener() {
        return mHiCarAudioListener;
    }

    /**
     * @param operate 0启动腾讯导航app；1退出腾讯导航app
     */
    public void operateWeCarNavi(int operate) {
        Intent intent = new Intent();
        intent.setAction("WECARNAVIAUTO_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 1002);
        intent.putExtra(NAVI_MAP_EXTRA_OPERA, operate);
        intent.setPackage(WECARNAVI_MAP_PACKAGE_NAME);
        mContext.sendBroadcast(intent);
        Log.i(TAG, "operateWeCarNavi() sendBroadcast success. operate : " + operate);
    }

    //停止导航
    public void stopNavi() {
        NaviSkillSDK naviSkillSDK = NaviSkillSDK.INSTANCE;
        //地图实现的方法
        naviSkillSDK.getNavigationSkill().stopNavigationForBackStage(new IResponseObserver() {
            @Override
            public void onComplete(int i) {
                Log.i(TAG, "stopNavi i " + i);
            }
        });
    }

    /**
     * 退出高德车机导航App
     */
    private void exitCarMapApp() {
        if (mContext == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(NAVI_MAP_ACTION_SEND);
        intent.putExtra(NAVI_MAP_EXTRA_TYPE, NAVI_MAP_EXTRA_VAL_END);
        intent.setPackage(NAVI_MAP_PACKAGE_NAME);
        mContext.sendBroadcast(intent);
        Log.i(TAG, "exitCarMapApp() sendBroadcast success.");
    }

    /**
     * 退出手机机导航App
     */
    private void exitPhoneMapApp() {
        //{"navFocus":"HICAR"}
        String nativeMap =
                "{\n" +
                        " \"navFocus\": \"NATIVE\"\n" +
                        " }\n";

        HiCarServiceManager.getInstance().sendCarData(
                Constants.HiCarCons.DATA_TYPE_NAV_FOCUS, nativeMap.getBytes());
        Log.i(TAG, "exitPhoneMapApp() sendCarData success. nativeMap: " + nativeMap);
    }

    /**
     * 退出或对象销毁调用
     */
    public void destroy() {
        KeyManager.getInstance().unregisterListener(this);
        HiCarServiceManager.getInstance().unregisterServiceListener(this);
        if (mContext != null) {
            unRegisterNaviMapBroadcast(mContext);
        }
        Log.i(TAG, "destroy() called.");
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent() Hicar Audio --- keycode: "
                + event.getKeyCode() + " action: " + event.getAction() + " EventTime: "
                + event.getEventTime() + ", DownTime: " + event.getDownTime());
        analyzeKeyEvent(event);
    }

    /**
     * 分析方向盘按钮的事件
     * param event
     */
    private void analyzeKeyEvent(KeyEvent event) {
        //需要过滤按键，上下曲，语音，主页按键 KongJing 2021.11.9
        if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
            //上一曲/挂断
            mediaPreviousKey(event);
        } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
            //下一曲/接听
            mediaNextKey(event);
        } else if (KeyEvent.KEYCODE_HOME == event.getKeyCode()) {
            //主页
            homeKey(event);
        } else if (KeyEvent.KEYCODE_VOICE_ASSIST == event.getKeyCode()) {
            voiceAssitKey(event);
        } else {
            Log.i(TAG, "filter keycode ");
        }
    }

    /**
     * param keyCode
     * param isLongPress
     * param longPressTime
     * param action        点击或者抬起按键  0 , 1
     */
    private void mediaButtonClick(int keyCode, boolean isLongPress, long longPressTime, int action) {
        Log.i(TAG, "mediaButtonClick: keyCode: " + keyCode
                + ", isLongPress: " + isLongPress + " , longPressTime: " + longPressTime + ", action: " + action);
        switch (keyCode) {
            //下一曲 87 /接起电话 5
            case MyKeyCode.PICKUP_PHONE:
                if (isLongPress) {
                    return;
                }
                //判断当前电话状态，如果是空闲的状态，那么就使用上下曲,如果是通话中或者来电，则使用挂断或者接听
                if (mCallState == CallState.IDLE) {
                    if (AppStatusUtil.isForeground(mContext)) {
                        //因为车机的音乐播放与hicar都可以进行切换，那么只有在hicar界面的时候，才去切换hicar的音乐 KongJing 2021.7.5
                        Log.i(TAG, "mediaButtonClick send data to sdk. KEYCODE_CALL 87.");
                        if (action == KeyEvent.ACTION_DOWN) {
                            HiCarServiceManager.getInstance().sendKeyData(87, 0);
                        } else if (action == KeyEvent.ACTION_UP) {
                            HiCarServiceManager.getInstance().sendKeyData(87, 1);
                        }
                    }
                } else {
                    Log.i(TAG, "mediaButtonClick send data to sdk. KEYCODE_CALL 5.");
                    /*
                     * KEYCODE_CALL 5 拨号键
                     */
                    if (action == KeyEvent.ACTION_DOWN) {
                        HiCarServiceManager.getInstance().sendKeyData(5, 0);
                    } else if (action == KeyEvent.ACTION_UP) {
                        HiCarServiceManager.getInstance().sendKeyData(5, 1);
                    }
                }
                break;
            //上一曲 88 /挂断电话 6
            case MyKeyCode.HANGUP_PHONE:
                if (isLongPress) {
                    return;
                }
                if (mCallState == CallState.IDLE) {
                    if (AppStatusUtil.isForeground(mContext)) {
                        //因为车机的音乐播放与hicar都可以进行切换，那么只有在hicar界面的时候，才去切换hicar的音乐 KongJing 2021.7.5
                        Log.i(TAG, "mediaButtonClick send data to sdk. KEYCODE_ENDCALL 88.");
                        if (action == KeyEvent.ACTION_DOWN) {
                            HiCarServiceManager.getInstance().sendKeyData(88, 0);
                        } else if (action == KeyEvent.ACTION_UP) {
                            HiCarServiceManager.getInstance().sendKeyData(88, 1);
                        }
                    }
                } else {
                    /*
                     * KEYCODE_ENDCALL 6 挂机键
                     */
                    Log.i(TAG, "mediaButtonClick send data to sdk. KEYCODE_ENDCALL 6.");
                    if (action == KeyEvent.ACTION_DOWN) {
                        HiCarServiceManager.getInstance().sendKeyData(6, 0);
                    } else if (action == KeyEvent.ACTION_UP) {
                        HiCarServiceManager.getInstance().sendKeyData(6, 1);
                    }
                }
                break;
            //语音唤醒
            case MyKeyCode.KEYCODE_VOICE_ASSIST:
                //长按1.5秒判断为唤起hicar语音。
                if (!isLongPress || longPressTime < 1500) {
                    return;
                }

                Log.i(TAG, "mediaButtonClick send data to sdk. will wake voice...");
                Constants.HICAR_START_MODE = 1;
                CommonUtil.startHiCarActivity(mContext);
                if (HiCarServiceManager.getInstance().isConnectedDevice()) {
                    wakeHiCarVoice();
                } else {
//                    CommonUtil.startHiCarActivity(mContext);
                }
                break;
            //Home 事件
            case MyKeyCode.KEY_HOME:
                Log.i(TAG, "KEY_HOME= 3");
                ServiceManager.getInstance().postEvent(Constants.Event.ON_HOME, null);
                break;
            default:
                break;
        }
    }

    /**
     * 物理按键唤醒hicar语音助手
     */
    private void wakeHiCarVoice() {
        String hotWordStr =
                "{\n" +
                        " \"frontEndInfo\": {\n" +
                        " \"car\": {\n" +
                        " \"micNumber\": 2,\n" +
                        " \"speakerNumber\": 4\n" +
                        " },\n" +
                        " \"centralControl\": {\n" +
                        " \"vendorName\": \"XX\",\n" +
                        " \"version\": \"10.0.0.12\"\n" +
                        " },\n" +
                        " \"intent\": \"startVoiceRecognize\",\n" +
                        " \"voice\": {\n" +
                        " \"infoToVendorCloud\": \"HicarManager\",\n" +
                        " \"vendorName\": \"10.0.0.12\",\n" +
                        " \"version\": \"12\"\n" +
                        " },\n" +
                        " \"wakeType\": \"buttonPress\"\n" +
                        " }\n" +
                        "}\n";

        Log.i(TAG, "wakeHiCarVoice() hotWordStr：" + hotWordStr);
        HiCarServiceManager.getInstance().sendHotWordData(hotWordStr);
    }

    /**
     * 物理按键关闭hicar语音助手
     */
    public void closeHiCarVoice() {
        String hotWordStr =
                "{\n" +
                        " \"frontEndInfo\": {\n" +
                        " \"car\": {\n" +
                        " \"micNumber\": 2,\n" +
                        " \"speakerNumber\": 4\n" +
                        " },\n" +
                        " \"centralControl\": {\n" +
                        " \"vendorName\": \"XX\",\n" +
                        " \"version\": \"10.0.0.12\"\n" +
                        " },\n" +
                        " \"intent\": \"stopVoiceRecognize\",\n" +
                        " \"voice\": {\n" +
                        " \"infoToVendorCloud\": \"HicarManager\",\n" +
                        " \"vendorName\": \"10.0.0.12\",\n" +
                        " \"version\": \"12\"\n" +
                        " },\n" +
                        " \"wakeType\": \"buttonPress\"\n" +
                        " }\n" +
                        "}\n";

        Log.i(TAG, "closeHiCarVoice() hotWordStr：" + hotWordStr);
        HiCarServiceManager.getInstance().sendHotWordData(hotWordStr);
    }

    /**
     * 通过车机语音唤醒hicar语音助手
     */
    public void wakeUpHiCarByVoice() {
        String hotWordStr = "{\n" +
                "\t\"frontEndInfo\": {\n" +
                "\t\t\"wakeType\": \"voice\",\n" +
                "\t\t\"intent\": \"startVoiceRecognize\",\n" +
                "\t\t\"voice\": {\n" +
                "\t\t\t\"wakeWord\": \"小艺小艺\",\n" +
                "\t\t\t\"vendorName\": \"xunfei\",\n" +
                "\t\t\t\"version\": \"1008\"\n" +
                "\t\t},\n" +
                "\t\t\"centralControl\": {\n" +
                "\t\t\t\"vendorName\": \"北斗\",\n" +
                "\t\t\t\"version\": \"A20230406\"\n" +
                "\t\t},\n" +
                "\t\t\"car\": {\n" +
                "\t\t\t\"micNumber\": 2,\n" +
                "\t\t\t\"speakerNumber\": 4\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        HiCarServiceManager.getInstance().sendHotWordData(hotWordStr);
    }

    /**
     * 通过车机语音关闭hicar语音助手
     */
    public void stopHiCarByVoice() {
        String hotWordStr = "{\n" +
                "\t\"frontEndInfo\": {\n" +
                "\t\t\"wakeType\": \"voice\",\n" +
                "\t\t\"intent\": \"stopVoiceRecognize\",\n" +
                "\t\t\"voice\": {\n" +
                "\t\t\t\"wakeWord\": \"小艺小艺\",\n" +
                "\t\t\t\"vendorName\": \"xunfei\",\n" +
                "\t\t\t\"version\": \"1008\"\n" +
                "\t\t},\n" +
                "\t\t\"centralControl\": {\n" +
                "\t\t\t\"vendorName\": \"北斗\",\n" +
                "\t\t\t\"version\": \"A20230406\"\n" +
                "\t\t},\n" +
                "\t\t\"car\": {\n" +
                "\t\t\t\"micNumber\": 2,\n" +
                "\t\t\t\"speakerNumber\": 4\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        HiCarServiceManager.getInstance().sendHotWordData(hotWordStr);
        ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_SERVICE_PAUSE, null);
    }

    private void initCommonKeyService() {
        Log.i(TAG, "initCommonKeyService()");
        //无障碍服务
        ComponentName toggledService = ComponentName.unflattenFromString("com.incall.apps.hicar/com.incall.apps.hicar.servicesdk.servicesimpl.CommonKeyService");
        AccessibilityUtils.setAccessibilityServiceState(mContext, toggledService, false);
        AccessibilityUtils.setAccessibilityServiceState(mContext, toggledService, true);
    }

    private void startCommonKeyService() {
        Log.i(TAG, "startCommonKeyService()");
        //CommonKeyService
        Intent intent = new Intent(mContext, CommonKeyService.class);
        mContext.startService(intent);
    }

    //HiCarServiceListener start
    @Override
    public void onDeviceChange(String s, int i, int i1) {

    }

    @Override
    public void onDeviceServiceChange(String s, int i) {

    }

    //HiCarServiceManager的onDataReceive方法会调用这个方法
    @Override
    public void onDataReceive(String s, int type, byte[] bytes) {
        Log.d(TAG, "onDataReceive() s: " + s + ", type: " + type + ", byte[]: " + Arrays.toString(bytes));
        switch (type) {
            //导航焦点通知
            case Constants.HiCarCons.DATA_TYPE_NAV_FOCUS:
                if (bytes == null) {
                    return;
                }
                //导航内容
                String navigationStr = new String(bytes);
                Log.d(TAG, "onDataReceive() DATA_TYPE_NAV_FOCUS will exit car. navigationStr: " + navigationStr);

                if (navigationStr.contains("\"HICAR\"")) {
                    //operateWeCarNavi(1);
                    Log.i(TAG, "onDataReceive() okin----");
                    stopNavi();
                }
                break;
            case Constants.HiCarCons.DATA_TYPE_VOICE_STATE:
                if (bytes == null) {
                    return;
                }
                String voiceStr = new String(bytes);
                Log.d(TAG, "onDataReceive() DATA_TYPE_VOICE_STATE . voiceStr: " + voiceStr);
                if (voiceStr.contains("1")) {
                    Constants.HICAR_START_MODE = 3;
                    HiCarDataCollectManager.getInstance().startVoice();
                    CommonUtil.sendHiCarBroadcast(mContext, false);
                    //语音唤醒，切换降噪库模式为 VR 模式 KongJing 2021.7.29
                    VoiceAudioRoute.getInstance(mContext).setVoiceRouteMode();
                }

                break;
            case Constants.HiCarCons.DATA_TYPE_CALL_STATE_FOCUS:
                //电话状态和焦点
                String call = new String(bytes);
                Log.d(TAG, "onDataReceive() s: " + s + " type: DATA_TYPE_CALL_STATE_FOCUS" + ", data: " + call);
                if (call.contains("1")) {
                    //来电状态
                    mCallState = CallState.RINGING;
                } else if (call.contains("2")) {
                    //通话中
                    mCallState = CallState.CALLING;
                    //通话中，切换降噪库模式 KongJing 2021.7.29
                    VoiceAudioRoute.getInstance(mContext).setPhoneMode();
                } else {
                    //空闲中
                    mCallState = CallState.IDLE;
                    //空闲情况，切换降噪库模式 KongJing 2021.7.29
                    VoiceAudioRoute.getInstance(mContext).setVoiceRouteMode();
                }
                handleCallStateWithVoice(mCallState);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPinCode(String s) {
    }

    @Override
    public void onBinderDied() {
    }

    @Override
    public void onPinCodeFailed() {
    }

    //HiCarServiceListener end

    /**
     * usage: 手机侧发送的音频数据的usage，多音源混音场景存在多个有效Usage，
     * 将音频数据的所有有效Usage都上报给厂商，由车机厂商决定优先级和转换策略
     * contentType: 类似usage的策略，将音频数据的所有contentType上报给厂商
     * focusGain: 焦点类型，手机侧申请的焦点类型
     * info: 预留的扩展属性，用于手机侧将音频类信息上报给车机厂商
     *
     * @author zengjie 20201127.
     */
    private class HiCarAudioListener extends CarAudioListener {

        //定义一个标志用来区分是否曾经拉起过hicar，如果拉起过，就不再拉起了
        public boolean hasBeenLaunched = false;

        @Override
        public int getCarAudioRecordingStatus() {
            return 2;
        }

        //得到个性化的音频属性
        @Override
        public CustomizedAudioAttributes getCustomizedAudioAttributes(int[] usages, int[] contentTypes,
                                                                      int focusGain, Map<String, String> info) {
            Log.i(TAG, "getCustomizedAudioAttributes() HiCar Audio "
                    + "usages: " + Arrays.toString(usages)
                    + ", contentTypes: " + Arrays.toString(contentTypes)
                    + ", focusGain: " + focusGain + ", info: " + info
            );
            EventBus.getDefault().post(new PlayStatusEvent(focusGain));
            CustomizedAudioAttributes customizedAudioAttributes;
            int streamType = usageToStreamType(usages, focusGain);
            if (contentTypes != null) {
                for (int i = 0; i < contentTypes.length; i++) {
                    Log.i(TAG, "getCustomizedAudioAttributes() contentTypes i: " + i + ", val: " + contentTypes[i]);
                }
            }
            int contentType = streamType2ContentType(streamType);
            if (contentType == -1) {
                return null;
            }
            Log.i(TAG, "getCustomizedAudioAttributes()  streamType: " + streamType + ", contentType: " + contentType);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(streamType)
                    .setContentType(contentType)
                    .build();
            customizedAudioAttributes = new CustomizedAudioAttributes(audioAttributes, focusGain, info);
            Log.i(TAG, "getCustomizedAudioAttributes() customAudioAttributes: " + customizedAudioAttributes);
            return customizedAudioAttributes;
        }

        //usage转换为流类型
        private int usageToStreamType(int[] usages, int focusGain) {
            //如果 usages 有多个值，则根据多个 usage 判断，根据判断之后的 usage 转换 streamType
            if (usages == null) {
                Log.i(TAG, "usageToStreamType() StreamType is null.ret! ");
                return -1;
            }
            int focusOne = -1;
            int focusTwo = -1;
            int focusCount = 0;
            Log.i(TAG, "usageToStreamType() StreamType start");
            for (int i = 0; i < usages.length; i++) {
                if (usages[i] == 0) {
                    continue;
                }

                if (i == 0) {
                    focusOne = usages[i];
                } else {
                    focusTwo = usages[i];
                }
                focusCount++;
                Log.i(TAG, "usageToStreamType() HiCar Audio Custom i: " + i + ", phoneUsages: " + usages[i]);
            }
            Log.i(TAG, "usageToStreamType() HiCar Audio Custom StreamType end. "
                    + "phone focusOne: " + focusOne + ", phone focusTwo: " + focusTwo + ", focusCount: " + focusCount);
            int convertUsage = -1;
            if (focusCount == 1) {
                convertUsage = convertUsageByCar(focusOne, focusGain);
            } else if (focusCount > 1) {
                //播放HiCar音乐的时候，点击拨号键，会造成卡顿音，作出的特殊处理
                if ((focusOne == 3 && focusTwo == 1) || (focusOne == 1 && focusTwo == 3)) {
                    focusOne = convertUsageByCar(focusOne, focusGain);
                    focusTwo = convertUsageByCar(focusTwo, focusGain);
                    convertUsage = AudioAttributes.USAGE_MEDIA;
                } else {
                    focusOne = convertUsageByCar(focusOne, focusGain);
                    focusTwo = convertUsageByCar(focusTwo, focusGain);
                    convertUsage = mixAudioRule(focusOne, focusTwo);
                }

            }
            Log.i(TAG, "usageToStreamType() HiCar Audio Custom StreamType convert result CarUsage: " + convertUsage);
            return convertUsage;
        }

        /**
         * 把手机的usage转换成车机系统的usage
         */
        private int convertUsageByCar(int phoneUsage, int focusGain) {
            int carUsage = -1;
            switch (phoneUsage) {
                /*
                 * 拨号
                 */
                //add by calijia 2021/8/18 for 没有播放音乐时按键音不正常的问题
                case AudioConstants.SourceUsage.AUDIO_USAGE_VOICE_COMMUNICATION_SIGNALLING:
                    carUsage = AudioAttributes.USAGE_ASSISTANCE_SONIFICATION;
                    Log.i(TAG, "HiCar Audio ConvertUsage  AUDIO_USAGE_VOICE_COMMUNICATION_SIGNALLING " + carUsage);
                    break;
                /*
                 * 来电/呼叫/通话 大类
                 */
                case AudioConstants.SourceUsage.AUDIO_USAGE_VOICE_COMMUNICATION:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION_TELEPHONY_RINGTONE:
                    carUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                    Log.i(TAG, "HiCar Audio ConvertUsage  USAGE_VOICE_COMMUNICATION " + carUsage);
                    break;
                /*
                 * 语音交互 大类
                 */
                case AudioConstants.SourceUsage.AUDIO_USAGE_ASSISTANCE_ACCESSIBILITY:
                case AudioConstants.SourceUsage.AUDIO_USAGE_ASSISTANCE_SONIFICATION:
                case AudioConstants.SourceUsage.AUDIO_USAGE_TTS:
                    carUsage = AudioAttributes.USAGE_ASSISTANT;
                    Log.i(TAG, "HiCar Audio ConvertUsage  USAGE_ASSISTANT " + carUsage);
                    break;
                /*
                 * 导航播报 大类
                 */
                //导航语音（三方导航应用传入的 Usage是媒体，按媒体处理）AUDIO_USAGE_MEDIA（1）,所以一般不会有这个状态
                case AudioConstants.SourceUsage.AUDIO_USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                    carUsage = AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;
                    HcAudioManager.getInstance().exitCarMapApp();
                    Log.i(TAG, "HiCar Audio ConvertUsageByCar --- called exitNAVIApp.");
                    break;
                /*
                 * 音乐多媒体 大类
                 */
                case AudioConstants.SourceUsage.AUDIO_USAGE_ALARM:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION_COMMUNICATION_REQUEST:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION_COMMUNICATION_INSTANT:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION_COMMUNICATION_DELAYED:
                case AudioConstants.SourceUsage.AUDIO_USAGE_NOTIFICATION_EVENT:
                case AudioConstants.SourceUsage.AUDIO_USAGE_GAME:
                case AudioConstants.SourceUsage.AUDIO_USAGE_VIRTUAL_SOURCE:
                case AudioConstants.SourceUsage.AUDIO_USAGE_ASSISTANT:
                    carUsage = AudioAttributes.USAGE_MEDIA;
                    Log.i(TAG, "HiCar Audio ConvertUsage  USAGE_MEDIA " + carUsage);
                    break;
                /*
                 * 导航语音（三方导航应用传入的 Usage是媒体，focusGain 为duck的时候是导航）AUDIO_USAGE_MEDIA（1）
                 * todo 查看这里高德导航压低声音不恢复的情况，这里 carUsage 是多少？DTM20210409000242 如果前面一次是 3 那么后面跟着是 0 ，其他算 media,目前華為的分析，不需要转，因为手机已经降音了
                 * 导航申请的焦点类型已经转换成功，但后续音频流（focusgain=0） 未做自定义转换，所以音频流还是送的媒体通道。请在导航焦点转换后，再做一次音频流的转换。
                 */
                case AudioConstants.SourceUsage.AUDIO_USAGE_MEDIA:
                    //华为不建议转义，直接使用 media ，因为高德的本身也会降音的, 其他车厂也是这样操作的。 KongJing 2021.11.1
                    carUsage = AudioAttributes.USAGE_MEDIA;
                    Log.i(TAG, "HiCar Audio ConvertUsage  ConvertUsageByCar  carUsage: " + carUsage);
                    break;
                default:
                    Log.i(TAG, "HiCar Audio ConvertUsage default! " + phoneUsage);
                    break;
            }
            return carUsage;
        }

        /**
         * 混音规则
         * 来电/呼叫/通话   AudioAttributes.USAGE_VOICE_COMMUNICATION
         * 语音交互        AudioAttributes.USAGE_ASSISTANT
         * 导航播报       AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
         * 音乐多媒体     AudioAttributes.USAGE_MEDIA
         */
        private int mixAudioRule(int focusOne, int focusTwo) {
            if (focusOne == AudioAttributes.USAGE_VOICE_COMMUNICATION
                    || focusTwo == AudioAttributes.USAGE_VOICE_COMMUNICATION) {
                return AudioAttributes.USAGE_VOICE_COMMUNICATION;
            }
            switch (focusOne) {
                //语音交互
                case AudioAttributes.USAGE_ASSISTANT:
                    switch (focusTwo) {
                        case AudioAttributes.USAGE_ASSISTANT:
                        case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                            return AudioAttributes.USAGE_ASSISTANT;
                        case AudioAttributes.USAGE_MEDIA:
                            return AudioAttributes.USAGE_MEDIA;
                        default:
                            Log.i(TAG, "HiCar Audio mixAudioRule USAGE_ASSISTANT " +
                                    "!default focusTwo: " + focusTwo);
                            break;
                    }
                    break;
                //导航播报
                case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                    switch (focusTwo) {
                        case AudioAttributes.USAGE_ASSISTANT:
                            return AudioAttributes.USAGE_ASSISTANT;
                        case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                            return AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;
                        case AudioAttributes.USAGE_MEDIA:
                            return AudioAttributes.USAGE_MEDIA;
                        default:
                            Log.i(TAG, "HiCar Audio mixAudioRule USAGE_NAVIGATION " +
                                    "!default focusTwo: " + focusTwo);
                            break;
                    }
                    break;
                //音乐多媒体
                case AudioAttributes.USAGE_MEDIA:
                    switch (focusTwo) {
                        case AudioAttributes.USAGE_ASSISTANT:
                        case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                        case AudioAttributes.USAGE_MEDIA:
                            return AudioAttributes.USAGE_MEDIA;
                        default:
                            Log.i(TAG, "HiCar Audio mixAudioRule USAGE_MEDIA " +
                                    "!default focusTwo: " + focusTwo);
                            break;
                    }
                    break;

                default:
                    Log.i(TAG, "HiCar Audio mixAudioRule !default focusOne: " + focusOne);
                    break;
            }

            return -1;
        }

        /**
         * 根据车机的usage返回对应的contentType
         *
         * @param carUsage 车机usage
         */
        private int streamType2ContentType(int carUsage) {
            int contentType = -1;
            switch (carUsage) {
                /*
                 * 来电/呼叫/通话
                 * BTCALL普通蓝牙电话
                 */
                case AudioAttributes.USAGE_VOICE_COMMUNICATION:
                    /*
                     * 导航播报 大类
                     */
                case AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE:
                    /*
                     * 语音交互 大类
                     * TTS
                     */
                case AudioAttributes.USAGE_ASSISTANT:
                    contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
                    Log.i(TAG, "streamType2ContentType() " +
                            "carUsage: " + carUsage
                            + ", CONTENT_TYPE_SPEECH: " + contentType
                    );
                    boolean isActivityRunning = CommonUtil.isActivityRunning(getInstance().mContext, "com.wt.phonelink");
                    Log.d(TAG, "streamType2ContentType() isActivityRunning: " + isActivityRunning);
                    Log.i(TAG, "streamType2ContentType() hasBeenLaunched: " + hasBeenLaunched);
                    Log.i(TAG, "streamType2ContentType() mCallState: " + mCallState);
                    if (!isActivityRunning && !hasBeenLaunched && mCallState == 1) {
                        hasBeenLaunched = true;
                        Log.i(TAG, "streamType2ContentType() post HicarCallStatusFocusEvent");
                        EventBus.getDefault().post(new HicarCallStatusFocusEvent());
                    }
                    if (mCallState == 0) {
                        hasBeenLaunched = false;
                    }
                    break;
                /*
                 * 音乐多媒体 大类
                 * Media （多媒体类型，包含音乐，在线电台，其它媒体播放器,此焦点类型不需要主动释放焦点）
                 */
                case AudioAttributes.USAGE_MEDIA:
                    contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
                    Log.i(TAG, "HiCar Audio streamType2ContentType carUsage: " + carUsage
                            + " CONTENT_TYPE_MUSIC " + contentType);
                    break;

                default:
                    Log.i(TAG, "HiCar Audio streamType2ContentType default! carUsage: " + carUsage);
                    break;
            }

            return contentType;
        }

    }

    /**
     * 导航状态的监听
     */
    private static class NaviBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "NaviBroadCastReceiver onReceive() Action: " + action);

            if (NAVI_MAP_ACTION_RECEIVER.equals(action)) {//高德地图
                int extraStatus = intent.getIntExtra(NAVI_MAP_EXTRA_STATUS, -1);
                Log.i(TAG, "NaviBroadCastReceiver onReceive() extraStatus: " + extraStatus);
                if (extraStatus == -1) {
                    return;
                }
                resolveMapAppStatus(extraStatus);

            } else if (WECARNAVI_MAP_ACTION_SEND.equals(action)) {//腾讯地图
                int type = intent.getIntExtra("TYPE", -1);
                Log.i(TAG, "NaviBroadCastReceiver onReceive()KEY_TYPE：" + type);
                if (type == -1) {
                    Log.i(TAG, "KEY_TYPE get val is -1. ret!");
                    return;
                }

                //6是开始，7是结束
                if (type == 6) {
                    SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(context);
                    boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
                    boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
                    if (isCarLinkConnect) {
                        UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_DOWN,
                                UCarCommon.KeyCodeType.KEY_CODE_NAVI_QUIT, 0);
                        UCarAdapter.getInstance().sendKeyEvent(UCarCommon.KeyEventActionType.KEY_EVENT_ACTION_UP,
                                UCarCommon.KeyCodeType.KEY_CODE_NAVI_QUIT, 0);
                    } else if (isHiCarConnect) {
                        HcAudioManager.getInstance().exitPhoneMapApp();
                    }

                }
            }
        }

        /**
         * 参考《长安项目平台化接口协议文档V6.6-20201012.pdf》--- 1.3
         * Application启动即为开始运⾏ 0; 初始化完成，每次创建地图完成通知 1; 运⾏结束，退出程序 2
         * 进⼊前台，OnStart函数中调⽤ 3; 进⼊后台，OnStop函数中调⽤ 4;
         * 开始导航 8; 结束导航 9; ...
         */
        private void resolveMapAppStatus(int status) {
            switch (status) {
                case NAVI_MAP_APP_STATUS_OPEN:
                    Log.i(TAG, "resolveMapAppStatus() open. status: " + status);
                    HcAudioManager.getInstance().exitPhoneMapApp();
                    break;
                case NAVI_MAP_APP_STATUS_INIT:
                    Log.i(TAG, "resolveMapAppStatus() init. status: " + status);
                    HcAudioManager.getInstance().exitPhoneMapApp();
                    break;
                case NAVI_MAP_APP_STATUS_EXIT:
                    Log.i(TAG, "resolveMapAppStatus() exit. status: " + status);
                    break;
                case NAVI_MAP_APP_STATUS_ONSTART:
                    Log.i(TAG, "resolveMapAppStatus() onstart. status: " + status);
                    HcAudioManager.getInstance().exitPhoneMapApp();
                    break;
                case NAVI_MAP_APP_STATUS_ONSTOP:
                    Log.i(TAG, "resolveMapAppStatus() onstop. status: " + status);
                    break;
                default:
                    break;
            }

        }
    }

    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_VOICE_KEY) {
                HcAudioManager.getInstance().mediaButtonClick(
                        MyKeyCode.KEYCODE_VOICE_ASSIST, true, 1600, KeyEvent.ACTION_DOWN);
            }
        }
    }

    /**
     * 上一曲 88 /挂断电话 6
     *
     * @author KongJing
     * 2021.11.11
     */
    private void mediaPreviousKey(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            mStartDownTime = System.currentTimeMillis();
            mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_DOWN);
        } else if (action == KeyEvent.ACTION_UP) {
            //间隔时间
            long interval = System.currentTimeMillis() - mStartDownTime;
            if (interval < VOICE_ASSIST_LONG_PRESSS_DOWN) {
                //短按的情况
                mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_UP);
            }
        }
    }

    /**
     * 下一曲 87 /接起电话 5
     *
     * @author KongJing
     * 2021.11.11
     */
    private void mediaNextKey(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            mStartDownTime = System.currentTimeMillis();
            mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_DOWN);
        } else if (action == KeyEvent.ACTION_UP) {
            //间隔时间
            long interval = System.currentTimeMillis() - mStartDownTime;
            if (interval < VOICE_ASSIST_LONG_PRESSS_DOWN) {
                //短按的情况
                mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_UP);
            }
        }
    }

    /**
     * 主页
     *
     * @author KongJing
     * 2021.11.12
     */
    private void homeKey(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            mStartDownTime = System.currentTimeMillis();
            mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_DOWN);
        } else if (action == KeyEvent.ACTION_UP) {
            //间隔时间
            long interval = System.currentTimeMillis() - mStartDownTime;
            if (interval < VOICE_ASSIST_LONG_PRESSS_DOWN) {
                //短按的情况
                mediaButtonClick(keyEvent.getKeyCode(), false, 0, KeyEvent.ACTION_UP);
            }
        }
    }

    /**
     * 语音助手，小艺，长按起效果
     */
    private void voiceAssitKey(KeyEvent keyEvent) {
        int action = keyEvent.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            mStartDownTime = System.currentTimeMillis();
            //移除上一次的长按
            //判断语音按键的延时
            mHandler.removeMessages(MSG_VOICE_KEY);
            mHandler.sendEmptyMessageDelayed(MSG_VOICE_KEY, 1500);
        } else if (action == KeyEvent.ACTION_UP) {
            //移除上一次的长按
            //判断语音按键的延时
            mHandler.removeMessages(MSG_VOICE_KEY);
            //间隔时间
            long interval = System.currentTimeMillis() - mStartDownTime;
            if (interval > VOICE_ASSIST_LONG_PRESSS_DOWN) {
                mHandler.removeMessages(MSG_VOICE_KEY);
            }
        }
    }

    /**
     * 来/去电过程中  禁止讯飞语音识别，结束后释放
     *
     * @param callState 当前电话状态
     */
    private void handleCallStateWithVoice(int callState) {
        Log.i(TAG, "handleCallStateWithVoice callState: " + callState);
        EventBus.getDefault().post(new HiCarCallStatusChangeEvent(callState));
    }

}
