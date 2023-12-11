package com.incall.apps.hicar.servicesdk.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.alibaba.fastjson2.JSONObject;
import com.huawei.hicarsdk.CarConfig;
import com.huawei.hicarsdk.CarListener;
import com.huawei.hicarsdk.HiCarAdapter;
import com.huawei.hicarsdk.HiCarConst;
import com.huawei.hicarsdk.HiCarInitCallback;
import com.huawei.hicarsdk.TrustPhoneInfo;
import com.huawei.managementsdk.launcher.AppInfoBean;
import com.huawei.managementsdk.launcher.AppInfoChangeListener;
import com.huawei.managementsdk.launcher.AppTransManager;
import com.incall.apps.hicar.servicesdk.HiCarAppAdapter;
import com.incall.apps.hicar.servicesdk.ServiceManager;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.interfaces.HiCarServiceListener;
import com.incall.apps.hicar.servicesdk.servicesimpl.audio.HcAudioManager;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.incall.apps.hicar.servicesdk.utils.SystemProperties;
import com.tinnove.hicarclient.beans.WTAppInfoBean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static com.incall.apps.hicar.servicesdk.aidl.HiCarManagerImpl.setGlobalProp;
import static com.incall.apps.hicar.servicesdk.contants.Constants.IS_CARLINK_FRONT;

//管理类，可以看做工具类。是一个核心类。
//HiCar服务管理器
public class HiCarServiceManager {
    private static final String TAG = "WTPhoneLink/HiCarServiceManager";
    //华为hiCar sdk中的类 这个是核心类，主要就是通过这个类的对象来调用hiCar sdk的功能
    private HiCarAdapter mHiCarAdapter = null;
    //车配置
    private CarConfig mCarConfig;
    private static final int SUCCESS = 0;

    private static final int FAILED = -1;

    private static final int TIME_FOR_DELAY_BOOT = 0;

    private static final int TIME_FOR_DELAY_BOOT_FAIL = 500;

    private static final int TIME_FOR_ADV_RETRY = 500;

    private static final int TIME_FOR_ADV = 5 * 60 * 1000;
    //断开回连广播
    //蓝牙电话
    // BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
    private final String BT_HEADSETCLIENT_ACTION = "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED";
    //蓝牙音乐
    private final String BT_AVRCP_CONTROLLER_ACTION = "android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED";

    public final String ACTION_SHUTDOWN_HU = "android.intent.action.ACTION_SHUTDOWN_HU";//IPO 关机广播
    public final String ACTION_BOOT_HU = "android.intent.action.ACTION_BOOT_HU";//IPO开机广播
    public final String ACTION_AP_STATUS = "android.net.wifi.WIFI_AP_STATE_CHANGED";//热点开关变化

    private boolean isIpo = true;//退出IPO后启动车机，打开热点的时候启动回连

    private final int ADV_MAX_RETRY_TIMES = 5;
    private int mRetryTimes = 0;
    //已连接设备
    private String connectedDevice = "";
    //连接码
    private String pinCode = "";
    /**
     * public static final int DISCONNECT_TYPE_ABNORMAL = 1;
     * public static final int DISCONNECT_TYPE_MANUAL = 2;
     * public static final int DISCONNECT_TYPE_EXCEPTION = 3;
     */
    private int disconnectType = -1;
    private int eventDeviceService = -1;

    //用于判断当前的工程是否进行投屏
    private boolean mIsProject = false;
    //实现CarListener
    private HiCarListener mHiCarListener;
    //HiCarServiceListener的实现类有MainServiceImpl
    //HiCarServiceListener是wt自己定义的一个接口
    private ArrayList<HiCarServiceListener> mListenerList = new ArrayList<>();
    private final int MSG_LOAD_HICAR_SDK = 1;

    public static final int MSG_START_ADV = 2;

    public static final int MSG_STOP_ADV = 3;

    //handler
    private HiCarHandler mMsgHandler;
    //车管理器
    private CarManager carManager;
    //电源管理器
    private PowerManager powerManager;
    //埋点管理
    private HiCarDataCollectManager hiCarDataCollectManager;
    private SharedPreferencesUtil sp;
    //是否已经回连
    private boolean isIpoReconnect = false;
    private int mConnectDeviceType = -1;
    private String BT_ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";

    private Context mContext;

    private static class SingletonHolder {
        //使用默认空构造方法，构造方法什么都没有做
        private static HiCarServiceManager instance = new HiCarServiceManager();
    }

    public static HiCarServiceManager getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * 初始化数据
     */
    public void init(Context context, Looper looper) {
        Log.d(TAG, "init() looper: " + looper + ", thread: " + Thread.currentThread());
        mContext = context;
        //虚拟车管理器
        carManager = CarManager.getInstance();
        carManager.init(context);
        //电源管理器
        powerManager = PowerManager.getInstance();
        powerManager.init(context);
        //hiCar数据收集管理器
        hiCarDataCollectManager = HiCarDataCollectManager.getInstance();
        hiCarDataCollectManager.init(context);
        //sp
        sp = SharedPreferencesUtil.getInstance(context);
        //Android handler
        mMsgHandler = new HiCarHandler(context, looper);
        //发送一个消息 MSG_LOAD_HICAR_SDK 这个消息将调用 loadHiCarSdk方法
        sendMsgDelayed(MSG_LOAD_HICAR_SDK, TIME_FOR_DELAY_BOOT);

    }


    /**
     * register DMDSPListener
     *
     * @param listener HiCarServiceListener
     */
    public void registerServiceListener(HiCarServiceListener listener) {
        if (!mListenerList.contains(listener)) {
            mListenerList.add(listener);
        }
        Log.d(TAG, "registerServiceListener()");
    }

    /**
     * register DMDSPListener
     */
    public void unregisterServiceListener(HiCarServiceListener listener) {
        if (mListenerList.contains(listener)) {
            mListenerList.remove(listener);
        }
    }


    //加载hiCarSDK
    //在这个里面创建HiCarListener并注册
    private int loadHiCarSdk(final Context context) {
        if (mHiCarAdapter != null) {
            Log.d(TAG, "loadHiCarSdk() HiCar has inited, return");
            return FAILED;
        }
        //注册监听连接状态的广播接收者
        registerBroadcast(context);
        //注册监听蓝牙状态的广播接收者
        registerBtStatusChange(context);
        //创建基础车配置
        mCarConfig = HiCarAppAdapter.getInstance().createBasicCarConfig();
        if (mCarConfig == null) {
            Log.d(TAG, "loadHiCarSdk() carConfig is null");
            return FAILED;
        }
        Log.i(TAG, "loadHiCarSdk() carConfig build");
        //支持有线usb
        mCarConfig.updateSupportUsb(true);
        //只是无线
        mCarConfig.updateSupportWireless(true);
        //支持重连
        mCarConfig.updateSupportReconnect(true);
        Log.i(TAG, "loadHiCarSdk() carConfig getBrMac=" + mCarConfig.getBrMac() + "---isSupportWireless=" + mCarConfig.isSupportWireless() + "----mCarConfig=" + mCarConfig.getBrMac());
        //创建HiCarListener对象 华为SDK的HiCar监听器
        if (mHiCarListener == null) {
            //华为SDK的HiCar监听器
            mHiCarListener = new HiCarListener(context);
        }
        Log.i(TAG, "loadHiCarSdk() carConfig getBrMac=" + mCarConfig);
        Log.i(TAG, "loadHiCarSdk() carConfig getBrMac=" + Thread.currentThread());
        //初始化音频管理(上下文).by zj.20201210
        //在这个方法中，会将HcAudioManager对象注册到mListenerList
        //HcAudioManager实现了HiCarServiceListener
        HcAudioManager.getInstance().init(context);
        //初始化流程是一个异步的流程
        HiCarAdapter.init(context, mCarConfig, new HiCarInitCallback() {
            //初始化成功
            @Override
            public void onInitSuccess(HiCarAdapter hiCarAdapter) {
                Log.i(TAG, "onInitSuccess()");
                if (hiCarAdapter == null) {
                    Log.d(TAG, "onInitSuccess() hiCarAdapter is null");
                    return;
                }
                //得到hiCarAdapter
                mHiCarAdapter = hiCarAdapter;
                Log.i(TAG, "onInitSuccess() mHiCarListener: " + mHiCarListener + ", audioListener: " + HcAudioManager.getInstance().getHiCarAudioListener());
                //注册mHiCarListener 华为的HiCar监听器 之后可能会调用HiCar监听器的方法
                //调用HiCarAdapter的方法
                int retCode = mHiCarAdapter.registerCarListener(mHiCarListener);
                //注册HiCarAudioListener
                int carAudioCode = mHiCarAdapter.registerCarAudioListener(HcAudioManager.getInstance().getHiCarAudioListener());//onInitSuccess
                Log.i(TAG, "HiCar Audio reg listener " + (carAudioCode == HiCarConst.SUCCESS));
                //没有成功
                if (retCode != HiCarConst.SUCCESS) {
                    Log.d(TAG, "HiCar reg listener failed");
                    return;
                }
                //退出IPO的时候需要重连HiCar 车机重启的时候判断是否是退出IPO启动
                if (SystemProperties.get(Constants.KEY_PROPERTY_IPO_STATUS).contains(Constants.PROPERTY_IPO_BOOT)) {
                    startReconnectByStopIpo();
                }
                /*取消注释，因为kill本身hicar的时候，会发生闪退或者卡logo 2021.7.21 KongJing
                 * 增加一个判断是否app处于前台的方法 LiJia 2021.7.28*/
                if (CommonUtil.isActivityRunning(mContext, "com.wt.phonelink")) {
                    //防止连接CarLink时候，自动连接hicar
                    if (!IS_CARLINK_FRONT) {
                        Log.i(TAG, "onInitSuccess() call sendMsgDelayed() param: MSG_START_ADV");
                        //发送蓝牙设备扫描消息，开始蓝牙设备扫描 ADV是advertise的简写
                        sendMsgDelayed(MSG_START_ADV, 0);
                    } else {
                        Log.e(TAG, "onInitSuccess() CarLinkActivity is running");
                    }
                } else {
                    Log.e(TAG, "onInitSuccess() PhoneLink activities is not running");
                }
                Log.i(TAG, "HiCar reg listener success");
            }

            //初始化失败
            @Override
            public void onInitFail(int errCode) {
                //接口在初始化失败时（超时时间为 30s）
                Log.i(TAG, "HiCar init failed--error--code==" + errCode);
                sendMsgDelayed(MSG_LOAD_HICAR_SDK, TIME_FOR_DELAY_BOOT_FAIL);
                for (HiCarServiceListener mListener : mListenerList) {
                    mListener.onPinCodeFailed();
                }
            }

            /**
             * onBinderDied 接口用于服务发生异常时触发回调，例如服务异常退出，或被其他进
             * 程杀死触发该回调。上层应用根据业务场景需要，可以选择重新拉起服务或者清理
             * 释放资源(将HiCarAdapter 实例化对象清空)。
             */
            @Override
            public void onBinderDied() {
                Log.i(TAG, "HiCar service bind died " + getConnectionType());
                unloadHiCarSdk(context);
                for (HiCarServiceListener mListener : mListenerList) {
                    mListener.onBinderDied();
                }
                sendMsgDelayed(MSG_LOAD_HICAR_SDK, TIME_FOR_DELAY_BOOT);
                //新增一个开机判断，如果是才开机就发生了异常，则不打开蓝牙设备，目前发现可以先屏蔽这里，不影响使用。 KongJing 2021.10.19
                /*if (HiCarConst.CONNECTION_TYPE_USB != mConnectDeviceType) {
                    //如果是有线断开连接，就不使用蓝牙打开，不进行回连
                    BTManager.getInstance().bluetoothOn();
                }*/
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        });
        Log.d(TAG, "HiCar loadHiCarSdk, SUCCESS");
        return SUCCESS;
    }

    //卸载hiCar SDK
    public void unloadHiCarSdk(Context context) {
        Log.i(TAG, "HiCar unloadHiCarSdk mHiCarAdapter: " + mHiCarAdapter);
        if (mHiCarAdapter != null) {
            Log.i(TAG, "HiCar unloadHiCarSdk mHiCarListener: " + mHiCarListener + "------audioListener=" + HcAudioManager.getInstance().getHiCarAudioListener());
            //取消注册音频相关的功能. by zengjie. 20201127 start
            //注销CarAudioListener
            int carAudioRerCode = mHiCarAdapter.unRegisterCarAudioListener(
                    HcAudioManager.getInstance().getHiCarAudioListener());//unloadHiCarSdk

            if (carAudioRerCode != HiCarConst.SUCCESS) {
                Log.e(TAG, "HiCar Audio unreg listener failed!");
            }
            //调用HiCarAdapter注销HiCarListener
            int retCode = mHiCarAdapter.unRegisterCarListener(mHiCarListener);
            if (retCode != HiCarConst.SUCCESS) {
                Log.d(TAG, "HiCar unreg listener failed");
            }
            //取消注册音频相关的功能. by zengjie. 20201127 end
            mHiCarAdapter.deInit();
            mHiCarAdapter = null;
            //已连接设备置空
            connectedDevice = "";
        }
    }

    /**
     * 启动蓝牙设备扫描
     */
    private void handleStartAdv() {
        //如果已经连接上设备
        if (isConnectedDevice()) {
            Log.i(TAG, "device is connected");
            return;
        }
        Log.i(TAG, "handleStartAdv enter");
        if (mHiCarAdapter != null) {
            //启动蓝牙设备扫描
            int retCode = mHiCarAdapter.startAdv();
            Log.i(TAG, "handleStartAdv: try start adv retCode: " + retCode);
            if (retCode == HiCarConst.ERROR_CODE_MISMATCH_RECONNECT_STATE) {
                for (HiCarServiceListener mListener : mListenerList) {
                    mListener.onPinCodeFailed();
                }
            }
            //HiCarConst.SUCCESS
            if (retCode == HiCarConst.SUCCESS) {
                Log.i(TAG, "handleStartAdv: start adv success");
                mRetryTimes = 0;
                //解决无线首次连接失败PIN码错误问题，调用stopAdv将导致认证失败
                //sendMsgDelayed(MSG_STOP_ADV, TIME_FOR_ADV);
                return;
            }
        }
        //重试
        if (mRetryTimes < ADV_MAX_RETRY_TIMES) {
            mRetryTimes++;
            sendMsgDelayed(MSG_START_ADV, TIME_FOR_ADV_RETRY);
            Log.d(TAG, "startAdv retry " + mRetryTimes);
        } else {
            mRetryTimes = 0;
            Log.d(TAG, "startAdv failed");
        }
    }

    //处理停止设备扫描
    private void handleStopAdv() {
        Log.i(TAG, "handleStopAdv enter");
        if (mHiCarAdapter != null) {
            //调用HiCarAdapter停止设备扫描
            int retCode = mHiCarAdapter.stopAdv();
            if (retCode != HiCarConst.SUCCESS) {
                Log.d(TAG, "handleStopAdv failed");
                if (!isConnectedDevice()) {
                    sendMsgDelayed(MSG_STOP_ADV, TIME_FOR_ADV_RETRY);
                }
            } else {
                Log.d(TAG, "handleStopAdv success");
            }
        }
    }

    /**
     * whether the car has connected to a device
     * 是否连接设备
     *
     * @return result boolean
     */
    public boolean isConnectedDevice() {
        Log.d(TAG, "isConnectedDevice() connectedDevice: " + connectedDevice);
        if (connectedDevice.isEmpty() || mHiCarAdapter == null) {
            Log.d(TAG, "isConnectedDevice() false");
            return false;
        }
        Log.d(TAG, "isConnectedDevice() true");
        return true;
    }

    //启动投屏
    //从HiCarFragment的showHiCar方法调用而来
    public boolean startProjection() {
        if (mHiCarAdapter == null) {
            return false;
        }
        Log.i(TAG, "startProjection() mIsProject: " + mIsProject);
        if (isConnectedDevice() && !mIsProject) {
            Log.i(TAG, "startProjection()");
            //调用HicarAdapter启动投屏
            int retCode = mHiCarAdapter.startProjection();
            if (retCode == HiCarConst.SUCCESS) {
                Log.i(TAG, "startProjection() retCode: success");
                mIsProject = true;
            }
            return retCode == HiCarConst.SUCCESS;
        }
        return false;
    }

    /**
     * 暂停投屏
     */
    public void pauseProjection() {
        if (mHiCarAdapter == null) {
            return;
        }
        if (isConnectedDevice() && mIsProject) {
            Log.i(TAG, "pauseProjection() ");
            //调用HiCarAdapter暂停投屏
            int retCode = mHiCarAdapter.pauseProjection();
            if (retCode == HiCarConst.SUCCESS) {
                mIsProject = false;
            }
        }
    }

    //停止投屏目前也是调用的暂停投屏
    public void stopProjection() {
        if (mHiCarAdapter == null) {
            return;
        }
        if (isConnectedDevice() && mIsProject) {
            Log.i(TAG, "stopProjection() ");
            //暂停投屏
            int retCode = mHiCarAdapter.pauseProjection();
            if (retCode == HiCarConst.SUCCESS) {
                mIsProject = false;
            }
        }
    }

    //断开连接
    public int disconnectDevice() {
        int retCode = HiCarConst.ERROR_CODE_FAILED;
        if (mHiCarAdapter == null) {
            Log.e(TAG, "disconnectDevice() mHiCarAdapter is null! ");
            return retCode;
        }
        if (sp == null) {
            Log.e(TAG, "disconnectDevice() sp is null! ");
            return retCode;
        }
        //从sp取值。判断hiCar当前是否连接
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT, false);
        Log.i(TAG, "disconnectDevice() isHiCarConnect：" + isHiCarConnect);
        //判断当前的连接品牌是hicar还是carlink。当前连接的是hiCar
        if (isHiCarConnect) {
            Log.i(TAG, "disconnectDevice() mIsProject: " + mIsProject);
            if (isConnectedDevice()) {//&&mIsProject去掉后,HiCar在后台运行的时候可以断开
                Log.i(TAG, "disconnectDevice() disconnectDevice: " + connectedDevice);
                //断开hiCar的连接
                retCode = mHiCarAdapter.disconnectDevice(connectedDevice);
                Log.i(TAG, "disconnectDevice()  retCode: " + retCode);
                if (retCode == HiCarConst.SUCCESS) {
                    mIsProject = false;
                }
                return retCode;
            }
        }
        return 0;
    }

    /**
     * 更新车配置
     * update CarConfig
     * CarConfig的华为sdk中的类型
     *
     * @param surface Surface
     * @param width   int
     * @param height  int
     * @return result boolean
     */
    public boolean updateCarConfig(Surface surface, int width, int height) {
        Log.i(TAG, "updateCarInfo() width: " + width + ", height: " + height);
        if (mCarConfig == null) {
            Log.d(TAG, "updateCarInfo() mCarConfig is null");
            return false;
        }
        Log.d(TAG, mCarConfig.toString());
        //更新surface
        mCarConfig.updateSurface(surface);
        //更新视频宽度
        mCarConfig.updateVideoWidth(width);
        //更新视频高度
        mCarConfig.updateVideoHeight(height);
        //更新是否支持自动连接功能
        mCarConfig.updateSupportReconnect(true);
        //在运行时动态更新无线功能状态
        mCarConfig.updateSupportWireless(true);
        if (mHiCarAdapter == null) {
            Log.d(TAG, "updateCarInfo() mHiCarAdapter is null");
            return false;
        }
        //调用HiCarAdapter更新车配置
        return (mHiCarAdapter.updateCarConfig(mCarConfig) == HiCarConst.SUCCESS);
    }

    /**
     * 更新是否支持USB连接
     */
    public boolean updateCarConfigUsb(boolean b) {
        //更新是否支持usb
        mCarConfig.updateSupportUsb(b);
        if (mHiCarAdapter == null) {
            Log.e(TAG, "updateCarConfigUsb() mHiCarAdapter is null!! ");
            return false;
        }
        //更新车配置
        return (mHiCarAdapter.updateCarConfig(mCarConfig) == HiCarConst.SUCCESS);
    }

    /**
     * 更新是否支持无线连接
     */
    public boolean updateCarConfigWireless(boolean b) {
        //更新是否支持无线连接
        mCarConfig.updateSupportWireless(b);
        if (mHiCarAdapter == null) {
            Log.d(TAG, "updateCarConfigWireless() mHiCarAdapter is null");
            return false;
        }
        //更新车配置
        return (mHiCarAdapter.updateCarConfig(mCarConfig) == HiCarConst.SUCCESS);
    }


    /**
     * 获取HiCarSDK的版本信息接口
     * 目前这个方法还没有找到调用的地方
     *
     * @return
     */
    public String getVersion() {
        Log.d(TAG, "getVersion() mHiCarAdapter: " + mHiCarAdapter);
        if (mHiCarAdapter != null) {
            //通过HiCarAdapter得到hiCar sdk的版本信息
            return mHiCarAdapter.getVersion();
        }
        return null;
    }

    /**
     * 获取hicar的连接方式
     *
     * @return 4=HiCarConst.CONNECTION_TYPE_WIFI，5=HiCarConst.CONNECTION_TYPE_USB
     */
    public int getConnectionType() {
        if (mHiCarAdapter != null) {
            //通过HiCarAdapter得到连接的类型
            return mHiCarAdapter.getConnectionType();
        }
        return -1;
    }

    /**
     * 获取连接的手机型号
     *
     * @return
     */
    public String getPhoneName() {
        String name = "";
        if (mHiCarAdapter != null) {
            //得到受信任的设备列表
            List<TrustPhoneInfo> list = mHiCarAdapter.getTrustDeviceList();
            //遍历所有设备
            for (int j = 0; j < list.size(); j++) {
                Log.i(TAG, "HiCar name: " + list.get(j).getPhoneName() + ", id: " + list.get(j).getPhoneId() + ", mac: " + list.get(j).getPhoneBrMac() + ", time: " + list.get(j).getLastConnectTime());
                //得到当前连接的设备的名字
                if (connectedDevice.equals(list.get(j).getPhoneId())) {
                    //设备名字
                    name = list.get(j).getPhoneName();
                    break;
                }

            }
        }
        Log.i(TAG, "HiCar name:" + name);
        //设备名字不为空
        if (!TextUtils.isEmpty(name)) {
            //存入SP
            if (name.contains(" ")) {
                String brand = name.substring(0, name.indexOf(" "));
                String model = name.substring(name.indexOf(" ") + 1);
                sp.putString(Constants.SP_PHONE_BRAND, brand);
                sp.putString(Constants.SP_PHONE_MODEL, model);
            } else {
                setDefaultValue();
            }
        } else {
            setDefaultValue();
        }
        //返回设备名字
        return name;
    }

    private void setDefaultValue() {
        sp.putString(Constants.SP_PHONE_BRAND, "HUAWEI");
        sp.putString(Constants.SP_PHONE_MODEL, "");
    }

    private String mac = "";

    public void setLastPhoneMac() {
        if (mHiCarAdapter != null) {
            //调用CarAdapter，得到所有设备列表
            List<TrustPhoneInfo> list = mHiCarAdapter.getTrustDeviceList();
            //遍历设备列表
            for (int j = 0; j < list.size(); j++) {
                Log.i(TAG, "HiCar name: " + list.get(j).getPhoneName() + ", id: " + list.get(j).getPhoneId() + ", mac: " + list.get(j).getPhoneBrMac() + ", time: " + list.get(j).getLastConnectTime());
                //得到当前已连接设备的mac地址
                if (connectedDevice.equals(list.get(j).getPhoneId())) {
                    //得到mac地址
                    mac = list.get(j).getPhoneBrMac();
                    break;
                }

            }
        }

    }

    /**
     * @return event_device_service
     * 为202的时候说明是HiCar在后台运行
     * 为203的时候说明是HiCar在后台 通过小艺语音进入
     * 再次进入HiCar的时候不需要动画
     */

    public boolean isPlayAnim() {
        return !(eventDeviceService == Constants.HiCarCons.EVENT_DEVICE_SERVICE_PAUSE ||
                eventDeviceService == Constants.HiCarCons.EVENT_DEVICE_SERVICE_RESUME);
    }

    public String getMac() {
        return mac;
    }

    /**
     *
     */

    public String getPinCode() {
        return pinCode;
    }

    /**
     * 发送数据给hicar
     *
     * @param data
     * @return
     */
    public int sendCarData(int dataType, byte[] data) {
        Log.d(TAG, "sendCarData() mHiCarAdapter: " + mHiCarAdapter + ", data: " + new String(data));
        if (data == null) {
            return -1;
        }
        if (mHiCarAdapter != null) {
            //调用CarAdapter发送车数据
            return mHiCarAdapter.sendCarData(dataType, data);
        }
        return -1;
    }

    /**
     * 发送物理按键事件给手机
     *
     * @param keyCode
     * @param action
     * @return
     */
    public int sendKeyData(int keyCode, int action) {
        if (mHiCarAdapter != null) {
            return mHiCarAdapter.sendKeyEvent(keyCode, action);
        }
        return -1;
    }

    /**
     * 发送热词事件或唤醒语音给手机
     *
     * @return
     */
    public int sendHotWordData(String str) {
        if (mHiCarAdapter != null) {
            return mHiCarAdapter.sendHotWord(str);
        }
        return -1;
    }

    /**
     * 回连
     *
     * @param mac
     */
    public void startReconnect(String mac) {
        Log.i(TAG, "----" + mHiCarAdapter + "----mac=" + mac);
        //修复DTM20220225000090，当前正在回连中导致HiCar初始化失败，在进行回连时需要满足蓝牙已经连接上
        if (mHiCarAdapter != null && !isConnectedDevice() && BTManager.getInstance().isHfpConnected()) {
            isIpoReconnect = false;
            mHiCarAdapter.startReconnect(mac);
        }
    }

    /**
     * 车机进入IPO模式保存手机mac用于HiCar回连
     */
    public void startIpo() {
        // 因为熄火重连的时候，判断设备不准确，没有保存蓝牙mac地址成功，导致下次唤醒回连失败。 Lijia 2021.11.08
        int type = mConnectDeviceType;
        // 正常断开HiCar连接后，熄火不保存蓝牙Mac地址，不进行熄火重连。 Lijia 2022.1.26
        // DTM20220509000251 接收到IPO关机广播比华为回调断开连接早，disconnectType还是上次的
        // 多加一个判断是否连接了设备的条件，表示在连接过程中休眠唤醒的话，会进行重连，add by CAlqs 2022.5.10
        if (HiCarConst.CONNECTION_TYPE_WIFI == type && (disconnectType != HiCarConst.DISCONNECT_TYPE_MANUAL || !TextUtils.isEmpty(connectedDevice))) {
            Log.i(TAG, "startIpo()->savedMac:" + getMac());
            sp.putString(Constants.HICAR_MAC, getMac());
        }
//        sp.putString(Contants.HICAR_MAC, "1C:B7:96:B7:DF:17");//保时捷
//        sp.putString(Contants.HICAR_MAC, "E4:FD:A1:C6:00:1C");//mate 30 pro
    }

    /**
     * 车机退出IPO模式后HiCar回连
     */
    public void startReconnectByStopIpo() {
        String mac = sp.getString(Constants.HICAR_MAC, "-1");
        Log.d(TAG, "startReconnectByStopIPO---mac=" + mac);
        if (!"-1".equals(mac)) {
            isIpoReconnect = true;
            if (BTManager.getInstance().isConnected()) {
                Log.d(TAG, "startReconnectByStopIPO---1");
                startReconnect(mac);
            } else {
                BTManager.getInstance().bluetoothOn();
            }
        }

    }

    //注册蓝牙状态改变的广播接收者
    private void registerBtStatusChange(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BT_ACTION_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    //接收蓝牙状态改变的广播接收者
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //蓝牙状态改变
            if (BT_ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                Log.i(TAG, "broadcastReceiver onReceive state: " + state);
                switch (state) {
                    //蓝牙关闭
                    case BluetoothAdapter.STATE_OFF:
                        HiCarDataCollectManager.getInstance().BTDisconnected();
                        break;
                    //蓝牙打开
                    case BluetoothAdapter.STATE_ON:
                        ServiceManager.getInstance().postEvent(Constants.Event.BT_CONNECTED, null);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    //注册回连的广播接收者
    private void registerBroadcast(Context context) {
        Log.d(TAG, "registerBroadcast()");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BT_HEADSETCLIENT_ACTION);
        filter.addAction(BT_AVRCP_CONTROLLER_ACTION);
        filter.addAction(ACTION_SHUTDOWN_HU);
        filter.addAction(ACTION_BOOT_HU);
        filter.addAction(ACTION_AP_STATUS);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(reconnectRecevier, filter);
    }

    /**
     * 回连的广播接收者
     */
    BroadcastReceiver reconnectRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive() action: " + action + ", hiCarAdapter: " + mHiCarAdapter);
            //
            if (action != null && (action.equals(BT_HEADSETCLIENT_ACTION) || action.equals(BT_AVRCP_CONTROLLER_ACTION))) {
                //断开回连广播
                if (eventDeviceService == HiCarConst.EVENT_DEVICE_DISCONNECT &&
                        (disconnectType == HiCarConst.DISCONNECT_TYPE_ABNORMAL || disconnectType == HiCarConst.DISCONNECT_TYPE_EXCEPTION)) {
                    Log.i(TAG, "onReceive()" + eventDeviceService + "---" + disconnectType + "-----" + getMac());
                    if (HiCarConst.CONNECTION_TYPE_USB != mConnectDeviceType) {
                        //如果连接的设备是USB的设备，那么不进行回连
//                        startReconnect(getMac());
                        RxIntervalManager.getInstance().start();
                    }

                } else if (isIpoReconnect) {
                    Log.d(TAG, "onReceive() isIPOReconnect: " + isIpoReconnect + "-----" + getMac());
                    if (HiCarConst.CONNECTION_TYPE_USB != mConnectDeviceType) {
                        startReconnectByStopIpo();
                    }
                }
                Log.d(TAG, "onReceive() ---111");
            }
            //中控关闭action
            else if (action != null && action.equals(ACTION_SHUTDOWN_HU)) {
                //IPO 关机广播 启动回连
                startIpo();
            }
            //中控启动action
            else if (action != null && action.equals(ACTION_BOOT_HU)) {
                //开机广播，启动断开ipo后的回连
                startReconnectByStopIpo();
            }
            //热点状态action
            else if (action != null && action.equals(ACTION_AP_STATUS)) {
                int state = intent.getIntExtra("wifi_state", 0);
                // 10：热点正在关闭
                // 11：热点已关闭
                // 12：热点正在开启
                // 13：热点已开启
//                ActivityThread: Receiving broadcast android.net.wifi.WIFI_AP_STATE_CHANGED seq=-1 to com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager$3@d5a8a7c
//                HiCarServiceManager: onReceive() android.net.wifi.WIFI_AP_STATE_CHANGED, com.huawei.hicarsdk.HiCarAdapter@ba4593c
//                HiCarServiceManager: onReceive() state: 10, isIpo: true, isIPOBoot=
//                ActivityThread: Receiving broadcast android.net.wifi.WIFI_AP_STATE_CHANGED seq=-1 to com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager$3@d5a8a7c
//                HiCarServiceManager: onReceive() android.net.wifi.WIFI_AP_STATE_CHANGED, com.huawei.hicarsdk.HiCarAdapter@ba4593c
//                HiCarServiceManager: onReceive() state: 11, isIpo: true, isIPOBoot=
                Log.d(TAG, "onReceive() state: " + state + ", isIpo: " + isIpo + ", isIPOBoot: " + SystemProperties.get(Constants.KEY_PROPERTY_IPO_STATUS));
                if (isIpo && state == 13 && SystemProperties.get(Constants.KEY_PROPERTY_IPO_STATUS).contains(Constants.PROPERTY_IPO_BOOT)) {
                    isIpo = false;
                    startReconnectByStopIpo();
                }
            }
        }
    };


    /**
     * delay some time to send message
     *
     * @param msgType     int
     * @param delayMillis long
     */
    public void sendMsgDelayed(int msgType, long delayMillis) {
        Log.d(TAG, "sendMsgDelayed() mMsgHandler: " + mMsgHandler);
        if (mMsgHandler != null) {
            mMsgHandler.sendMessageDelayed(mMsgHandler.obtainMessage(msgType), delayMillis);
        }
    }

    /**
     * HiCar handler for handling message
     */
    private class HiCarHandler extends Handler {
        Context context;

        HiCarHandler(Context context, Looper looper) {
            super(looper);
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage() msg.what: " + msg.what);
            switch (msg.what) {
                //加载HiCar sdk
                case MSG_LOAD_HICAR_SDK:
                    //加载hiCarSdk
                    loadHiCarSdk(context);
                    break;
                //开始蓝牙设备扫描
                case MSG_START_ADV:
                    //处理启动蓝牙设备扫描
                    handleStartAdv();
                    break;
                //停止蓝牙设备扫描
                case MSG_STOP_ADV:
                    //处理停止蓝牙设备扫描
                    handleStopAdv();
                    break;
                default:
                    break;
            }
        }
    }

    //设备已连接处理
    private void deviceConnected(Context context) {
        Log.i(TAG, "deviceConnected()  EVENT_DEVICE_CONNECT: " + getConnectionType());
        CommonUtil.sendHiCarBroadcast(context, true);
        sp.putString(Constants.HICAR_MAC, "-1");

        //hicar 连接成功后关闭蓝牙 蓝牙只是用来配对，真实连接是WiFi或者是USB
        BTManager.getInstance().bluetoothOff();
        //有线连接后断开无线，无线连接后断开有线
        //得到连接的类型，USB（有线）还是WiFi（无线）
        int type = getConnectionType();
        //更新当前连接类型
        mConnectDeviceType = type;
        //更新usb配置为false
        //如果连接类型是无线（WiFi），设置USB有线连接是false
        if (HiCarConst.CONNECTION_TYPE_WIFI == type) {
            updateCarConfigUsb(false);
        }
        //有线连接后断开无线，无线连接后断开有线
        //如果连接类型是USB有线，设置WiFi无线连接是false
        else if (HiCarConst.CONNECTION_TYPE_USB == type) {
            updateCarConfigWireless(false);
        }
    }

    /**
     * HiCar listener
     * 华为的HiCar监听器
     * CarListener是华为HiCarSDK监听器
     *
     * @author caden.zouhongtao@huawei.com
     * @since 2019-08-23
     */
    public class HiCarListener implements CarListener {
        Context context;

        public HiCarListener(Context context) {
            this.context = context;
        }


        //主要是设备连接的事件
        @Override
        public void onDeviceChange(String s, int i, int i1) {
            //mDeviceId: null, 106, 0
            Log.i(TAG, "onDeviceChange() mDeviceId: " + s + ", i: " + i + ", i1: " + i1);
            eventDeviceService = i;
            //状态是已连接
            switch (i) {
                case HiCarConst.EVENT_DEVICE_CONNECT:
                    CommonUtil.setIoCapability(3);
                    Log.d(TAG, "onDeviceChange()  EVENT_DEVICE_CONNECT capability: " + (CommonUtil.getIoCapability()));
                    //暂停回连轮训 KongJing 2021.12.29
                    RxIntervalManager.getInstance().pause();
                    //得到已连接设备 todo
                    connectedDevice = s;
                    hiCarDataCollectManager.deviceConneted(connectedDevice);
                    setLastPhoneMac();
                    //已连接
                    deviceConnected(context);
                    CarManager.getInstance().sendGear();
                    AppTransManager.getInstance().registerAppInfoChangeListener(mAppInfoChangeListener);
                    Log.i(TAG, "onDeviceChange() postMetaDataRequest");
                    postMetaDataRequest();
                    Log.i(TAG, "onDeviceChange() postAppsRequest");
                    postAppsRequest();
                    sp.putBoolean(Constants.SP_IS_HICAR_CONNECT, true);
                    if (mContext != null) {
                        setGlobalProp(mContext, Constants.SYS_IS_HICAR_CONNECT, 1);
                    }
                    break;
                //状态是断开连接
                case HiCarConst.EVENT_DEVICE_DISCONNECT:
                    disconnectType = i1;
                    mIsProject = false;
                    //hiCar断开后打开无线有线两种连接方式
                    //设置usb连接方式
                    boolean isSupportUsb = updateCarConfigUsb(true);
                    Log.d(TAG, "onDeviceChange() isSupportUsb: " + isSupportUsb);
                    //设置无线连接方式
                    updateCarConfigWireless(true);
                    //hicar 断开连接后打开蓝牙
                    Log.d(TAG, "onDeviceChange() getConnectionType: " + getConnectionType());
                    if (HiCarConst.CONNECTION_TYPE_USB != mConnectDeviceType) {
                        BTManager.getInstance().bluetoothOn();
                    }
                    if (connectedDevice.equals(s)) {
                        hiCarDataCollectManager.deviceDisconnet(connectedDevice);
                        //清空已连接的设备
                        connectedDevice = "";
                    } else {
                        Log.i(TAG, "onDeviceChange()  disconnect diff device");
                    }
                    //断连接的时候，释放hicar拨打电话的焦点
                    SystemProperties.set("system.ca.hicar.tel.state", "0");
                    sp.putBoolean(Constants.SP_IS_HICAR_CONNECT, false);
                    if (mContext != null) {
                        setGlobalProp(mContext, Constants.SYS_IS_HICAR_CONNECT, 0);
                    }
                    //打开wifi
                    //CommonUtil.setWifiEnabled(context, true);
                    break;
                //状态是停止设备扫描
                case Constants.HiCarCons.EVENT_DEVICE_ADV_STOP:
                    sendMsgDelayed(MSG_STOP_ADV, 0);
                    break;
            }
            Log.d(TAG, "onDeviceChange() connectedDevice: " + connectedDevice);
            //通知监听
            if (!mListenerList.isEmpty()) {
                for (int j = 0; j < mListenerList.size(); j++) {
                    //包括MainServiceImpl的onDeviceChange方法
                    mListenerList.get(j).onDeviceChange(s, i, i1);
                }
            } else {
                Log.e(TAG, "onDeviceChange()  mListener is null! ");
            }
        }


        //设备投屏服务
        //被华为sdk内部调用
        @Override
        public void onDeviceServiceChange(String s, int i) {
            Log.d(TAG, "onDeviceServiceChange() i: " + i);
            eventDeviceService = i;
            Log.d(TAG, "onDeviceServiceChange() isConnectedDevice3: connectedDevice: " + connectedDevice);
            if (i == Constants.HiCarCons.EVENT_DEVICE_SERVICE_VIRMODEM_CALLING) {
                SystemProperties.set("system.ca.hicar.tel.state", "1");
            } else if (i == Constants.HiCarCons.EVENT_DEVICE_SERVICE_VIRMODEM_HANG_UP) {
                SystemProperties.set("system.ca.hicar.tel.state", "0");
            }
            Log.d(TAG, "onDeviceServiceChange() " + SystemProperties.get("system.ca.hicar.tel.state"));
            if (mListenerList.isEmpty()) {
                Log.d(TAG, "onDeviceServiceChange() mListener is null");
                return;
            }
            for (HiCarServiceListener mListener : mListenerList) {
                mListener.onDeviceServiceChange(s, i);
            }
        }

        //数据接收
        //被华为sdk内部调用
        @Override
        public void onDataReceive(String s, int i, byte[] bytes) {
            Log.d(TAG, "onDataReceive() i: " + i + ", content: " + new String(bytes));
            if (mListenerList.isEmpty()) {
                Log.d(TAG, "onDataReceive() mListener is null");
                return;
            }
            for (HiCarServiceListener mListener : mListenerList) {
                mListener.onDataReceive(s, i, bytes);
            }
        }

        //显示连接码
        //被华为sdk内部调用
        @Override
        public void onPinCode(String s) {
            Log.i(TAG, "onPinCode() s: " + s);
            //保存连接码
            pinCode = s;
            if (mListenerList.isEmpty()) {
                Log.d(TAG, "onPinCode() mListener is null");
                return;
            }
            //显示连接码
            for (HiCarServiceListener mListener : mListenerList) {
                //回调，通知连接码已改变
                //会调用MainServiceImpl的onPinCode方法
                //MainServiceImpl是ICAService的实现，并且实现了HiCarServiceListener
                mListener.onPinCode(s);
            }

        }

        //得到安全文件大小
        @Override
        public long getSecureFileSize(String fileName) {
            try (FileInputStream fis = context.openFileInput(fileName)) {
                return fis.available();
            } catch (IOException e) {
                Log.d(TAG, "read file IOException");
            }
            return FAILED;
        }

        //读安全文件
        @Override
        public byte[] readSecureFile(String fileName) {
            Log.i(TAG, "readSecureFile: fileName: " + fileName);
            byte[] readFileData = null;
            FileInputStream fis = null;
            boolean result = false;
            try {
                fis = context.openFileInput(fileName);
                byte[] temp = new byte[1024];
                int len = 0;
                // read file
                Log.d(TAG, "readSecureFile: openFileInput success");
                while ((len = fis.read(temp)) != FAILED) {
                    if ((readFileData != null) && (readFileData.length != 0)) {
                        readFileData = byteMerger(readFileData, temp, len);
                    } else {
                        readFileData = new byte[0];
                        readFileData = byteMerger(readFileData, temp, len);
                    }
                }
                result = true;
                Log.d(TAG, "read secure file");
            } catch (IOException e) {
                Log.d(TAG, "read file IOException");
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "close file IOException");
                }
            }

            if (!result) {
                readFileData = null;
            }

            return readFileData;
        }

        //写安全文件
        @Override
        public boolean writeSecureFile(String fileName, byte[] data) {
            Log.i(TAG, "write secure file:" + fileName + ", data: " + data.length);
            if (data.length == 0) {
                return false;
            }
            FileOutputStream fos = null;
            boolean retCode = false;
            try {
                fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(data);
                fos.flush();
                retCode = true;
            } catch (FileNotFoundException e) {
                Log.d(TAG, "write file FileNotFoundException");
            } catch (IOException e) {
                Log.d(TAG, "write file IOException");
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "close file IOException");
                }
            }
            return retCode;
        }

        //移除安全文件
        @Override
        public boolean removeSecureFile(String s) {
            return false;
        }

        //显示启动页
        @Override
        public void onShowStartPage() {
        }

        @Override
        public void syncPhoneTime(long l) {

        }


        //字节融合
        private byte[] byteMerger(byte[] byteA, byte[] byteB, int byteLength) {
            byte[] result = new byte[byteA.length + byteLength];
            System.arraycopy(byteA, 0, result, 0, byteA.length);
            System.arraycopy(byteB, 0, result, byteA.length, byteLength);
            return result;
        }
    }


    private void postAppsRequest() {
        Log.i(TAG, "postAppsRequest()");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RequestData", "all");
            jsonObject.put("RequestCaller", "00040900");
            String data = jsonObject.toJSONString();
            mHiCarAdapter.sendCarData(528, data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //在车机和手机已经完成 HiCar 连接后，车机向HiCar App请求音频元数据，Data Type值为514
    //HiCar音乐开始播放后，手机侧向车机侧发送音频元数据， Data Type值为515
    //在车机和手机已经完成 HiCar 连接后，车机向HiCar App请求场景类元数据，Data Type值为514。场景类元数据包括日历、天气、情景智能、IoT 控制。
    //HiCar 有情景智能元数据后，手机侧向车机侧发送情景智能元数据，Data Type值为530 。其中SceneData 中的Type 为 3 ，表示情景智能元数据
    public void postMetaDataRequest() {
        Log.i(TAG, "postMetaDataRequest()");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RequestAAData", 127);
            jsonObject.put("RequestCaller", "00040900");
            String data = jsonObject.toJSONString();
            mHiCarAdapter.sendCarData(514, data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postMediaRequest() {
        Log.i(TAG, "postMediaDataRequest()");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RequestAAData", 1);
            jsonObject.put("RequestCaller", "00040900");
            String data = jsonObject.toJSONString();
            mHiCarAdapter.sendCarData(514, data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void mediaPlayOrPause() {
        //入参包括按键值及按键操作
        //0：ACTION_DOWN。
        //1：ACTION_UP。
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PLAY_PAUSE, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PLAY_PAUSE, 1);
    }

    public void mediaStop() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_STOP, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_STOP, 1);
    }

    public void mediaToPrevious() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PREVIOUS, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PREVIOUS, 1);
    }

    public void mediaToNext() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_NEXT, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_NEXT, 1);
    }

    public void mediaFastForward() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_FAST_FORWARD, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_FAST_FORWARD, 1);
    }

    public void mediaFastBackward() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_REWIND, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_REWIND, 1);
    }

    public void mediaPlay() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PLAY, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PLAY, 1);
    }

    public void mediaPause() {
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PAUSE, 0);
        mHiCarAdapter.sendKeyEvent(KEYCODE_MEDIA_PAUSE, 1);
    }

    AppInfoChangeListener mAppInfoChangeListener = new AppInfoChangeListener() {
        @Override
        public void onLoadAllAppInfo(String deviceId, List<AppInfoBean> list) {
            if (list == null || list.isEmpty()) {
                Log.e(TAG, "onLoadAllAppInfo() list is null, return.");
                return;
            }
            Log.e(TAG, "onLoadAllAppInfo() deviceId: " + deviceId + ", list:  " + list);

            for (HiCarServiceListener hiCarServiceListener : mListenerList) {
                hiCarServiceListener.onLoadAllAppInfo(deviceId, convertAppInfoBean(list));
            }
        }

        @Override
        public void onAppInfoAdd(String deviceId, List<AppInfoBean> list) {
            Log.e(TAG, "onAppInfoAdd() deviceId:  " + deviceId + ", list:  " + list);
            for (HiCarServiceListener hiCarServiceListener : mListenerList) {
                hiCarServiceListener.onAppInfoAdd(deviceId, convertAppInfoBean(list));
            }
        }

        @Override
        public void onAppInfoRemove(String deviceId, List<AppInfoBean> list) {
            Log.e(TAG, "onAppInfoRemove() deviceId:  " + deviceId + ", list:  " + list);
            for (HiCarServiceListener hiCarServiceListener : mListenerList) {
                hiCarServiceListener.onAppInfoRemove(deviceId, convertAppInfoBean(list));
            }
        }

        @Override
        public void onAppInfoUpdate(String deviceId, List<AppInfoBean> list) {
            Log.e(TAG, "onAppInfoUpdate() deviceId: " + deviceId + ", list:  " + list);
            for (HiCarServiceListener hiCarServiceListener : mListenerList) {
                hiCarServiceListener.onAppInfoUpdate(deviceId, convertAppInfoBean(list));
            }
        }
    };

    public List<WTAppInfoBean> convertAppInfoBean(List<AppInfoBean> list) {
        List<WTAppInfoBean> wtAppInfoBeans = new ArrayList<>();
        for (AppInfoBean infoBean : list) {
            WTAppInfoBean wtAppInfoBean = new WTAppInfoBean(infoBean.getPkgName(), infoBean.getName(), infoBean.getIcon(), infoBean.getType());
            wtAppInfoBeans.add(wtAppInfoBean);
        }
        return wtAppInfoBeans;
    }
}
