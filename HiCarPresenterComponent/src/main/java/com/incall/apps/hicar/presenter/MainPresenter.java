package com.incall.apps.hicar.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.incall.apps.hicar.iview.IMainView;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;

import java.util.HashMap;

//对应的界面是HiCarMainActivity
//接收MainServiceImpl发送的事件
public class MainPresenter extends BasePresenter<IMainView> {
    private static final String TAG = "WTWLink/MainPresenter";
    private IMainView mainView;
    private final int MSG_PIN_CODE_CHANGE = 0;
    private final int MSG_DEVICE_CONNECT = 1;
    private final int MSG_DEVICE_DISCONNECT = 2;
    private final int MSG_DEVICE_PROJECT_CONNECT = 3;
    private final int MSG_DEVICE_PROJECT_DISCONNECT = 4;
    private final int MSG_DEVICE_SERVICE_PAUSE = 5;
    private final int MSG_DEVICE_SERVICE_RESUME = 6;
    private final int MSG_DEVICE_SERVICE_START = 7;
    private final int MSG_DEVICE_SERVICE_STOP = 8;
    private final int MSG_DEVICE_DISPLAY_SERVICE_PLAYING = 9;
    private final int MSG_DEVICE_DISPLAY_SERVICE_PLAY_FAILED = 10;
    private final int MSG_BRAND_ICON_DATA_CHANGE = 11;
    private final int MSG_HICAR_BINDER_DIED = 12;
    private final int MSG_BT_CONNECTED = 13;
    private final int MSG_PIN_CODE_FAILED = 14;
    private final int MSG_ACC_OFF = 15;
    private final int MSG_REQUEST_SHARE_NET = 16;
    private final int MSG_OPEN_APP = 18;
    private final int MSG_SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE = 17;

    public MainPresenter() {
        serviceManager.addEventListener(Constants.Event.PIN_CODE_CHANGE, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_CONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_DISCONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_PROJECT_CONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_PROJECT_DISCONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_PAUSE, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_PAUSE, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_RESUME, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_START, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_STOP, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_DISPLAY_SERVICE_PLAYING, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_DISPLAY_SERVICE_PLAY_FAILED, eventListener);
        serviceManager.addEventListener(Constants.Event.BRAND_ICON_DATA_CHANGE, eventListener);
        serviceManager.addEventListener(Constants.Event.HICAR_BINDER_DIED, eventListener);
        serviceManager.addEventListener(Constants.Event.BT_CONNECTED, eventListener);
        serviceManager.addEventListener(Constants.Event.PIN_CODE_FAILED, eventListener);
        serviceManager.addEventListener(Constants.Event.ACC_OFF, eventListener);
        serviceManager.addEventListener(Constants.Event.OPEN_APP, eventListener);
        //添加事件监听器
        serviceManager.addEventListener(Constants.Event.SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE, eventListener);
        Log.d(TAG, "MainPresenter()");
    }

    @Override
    public void register(IMainView view) {
        super.register(view);
        mainView = iView.get();
        Log.d(TAG, "register()");
    }

    @Override
    public void unRegister() {
        super.unRegister();
        Log.d(TAG, "unRegister() mainView: " + mainView);
        mainView = null;
    }

    Handler mainHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage() msg.what: " + msg.what);
            if (mainView == null) {
                Log.e(TAG, "handleMessage() mainView is null! ");
                return;
            }
            switch (msg.what) {
                //连接码改变
                case MSG_PIN_CODE_CHANGE:
                    mainView.onPinCodeChange();
                    break;
                //设备连接
                case MSG_DEVICE_CONNECT:
                    mainView.onDeviceConnect();
                    break;
                //断开连接的消息
                case MSG_DEVICE_DISCONNECT:
                    //断开连接
                    mainView.onDeviceDisconnect();
                    break;
                case MSG_DEVICE_PROJECT_CONNECT:
                    mainView.onDeviceProjectConnect();
                    break;
                case MSG_DEVICE_PROJECT_DISCONNECT:
                    mainView.onDeviceProjectDisconnect();
                    break;
                case MSG_DEVICE_SERVICE_PAUSE:
                    mainView.onDeviceServicePause();
                    break;
                //203
                case MSG_DEVICE_SERVICE_RESUME:
                    mainView.onDeviceServiceResume();
                    break;
                //204
                //HiCarConst.EVENT_DEVICE_SERVICE_START  在HiCarFragment里面响应这个回调
                case MSG_DEVICE_SERVICE_START:
                    Log.d(TAG, "204 handleMessage() MSG_DEVICE_SERVICE_START call mainView.onDeviceServiceStart()");
                    mainView.onDeviceServiceStart();
                    break;
                case MSG_DEVICE_SERVICE_STOP:
                    mainView.onDeviceServiceStop();
                    break;
                case MSG_DEVICE_DISPLAY_SERVICE_PLAYING:
                    mainView.onDeviceDisplayServicePlaying();
                    break;
                case MSG_DEVICE_DISPLAY_SERVICE_PLAY_FAILED:
                    mainView.onDeviceDisplayServicePlayFailed();
                    break;
                case MSG_BRAND_ICON_DATA_CHANGE:
                    mainView.onBrandIconDataChange();
                    break;
                case MSG_HICAR_BINDER_DIED:
                    mainView.onBinderDied();
                    break;
                case MSG_BT_CONNECTED:
                    mainView.onBtConnected();
                    break;
                case MSG_PIN_CODE_FAILED:
                    mainView.onPinCodeFailed();
                    break;
                case MSG_ACC_OFF:
                    mainView.onAccOff();
                    break;
                case MSG_REQUEST_SHARE_NET:
                    mainView.onQuestShareNet((byte[]) msg.obj);
                case MSG_OPEN_APP:
                    Log.i(TAG, "handleMessage() what is MSG_OPEN_APP");
                    mainView.onOpenApp((byte[]) msg.obj);
                    break;
                //启动手机互联
                case MSG_SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE:
                    //跳转到HiCarMainActivity
                    mainView.launchPhoneLink();
                    break;
                default:
                    break;
            }
        }
    };

    //接收MainServiceImpl发送的事件
    private final ICAEventListener eventListener = new ICAEventListener() {
        @Override
        public void onEvent(String eventName, Object obj) {
            Log.i(TAG, "onEvent() eventName: " + eventName);
            if (mainHandler == null) {
                Log.e(TAG, "onEvent() mainHandler is null! ");
                return;
            }
            switch (eventName) {
                //连接码改变
                case Constants.Event.PIN_CODE_CHANGE:
                    //发送连接码改变的消息，可以查看本类中mainHandler的实现  只是一个空消息
                    mainHandler.sendEmptyMessage(MSG_PIN_CODE_CHANGE);
                    break;
                //设备已连接
                case Constants.Event.DEVICE_CONNECT:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_CONNECT);
                    break;
                //设备断开连接
                case Constants.Event.DEVICE_DISCONNECT:
                    //发送断开连接的消息
                    mainHandler.sendEmptyMessage(MSG_DEVICE_DISCONNECT);
                    break;
                case Constants.Event.DEVICE_PROJECT_CONNECT:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_PROJECT_CONNECT);
                    break;
                //设备投影断开连接
                case Constants.Event.DEVICE_PROJECT_DISCONNECT:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_PROJECT_DISCONNECT);
                    break;
                //202
                case Constants.Event.DEVICE_SERVICE_PAUSE:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_PAUSE);
                    break;
                //203
                case Constants.Event.DEVICE_SERVICE_RESUME:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_RESUME);
                    break;
                //204
                //HiCarConst.EVENT_DEVICE_SERVICE_START
                case Constants.Event.DEVICE_SERVICE_START:
                    Log.d(TAG, "204 onEvent() DEVICE_SERVICE_START send MSG_DEVICE_SERVICE_START");
                    mainHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_START);
                    break;
                case Constants.Event.DEVICE_SERVICE_STOP:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_STOP);
                    break;
                case Constants.Event.DEVICE_DISPLAY_SERVICE_PLAYING:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_DISPLAY_SERVICE_PLAYING);
                    break;
                case Constants.Event.DEVICE_DISPLAY_SERVICE_PLAY_FAILED:
                    mainHandler.sendEmptyMessage(MSG_DEVICE_DISPLAY_SERVICE_PLAY_FAILED);
                    break;
                case Constants.Event.BRAND_ICON_DATA_CHANGE:
                    mainHandler.sendEmptyMessage(MSG_BRAND_ICON_DATA_CHANGE);
                    break;
                case Constants.Event.HICAR_BINDER_DIED:
                    mainHandler.sendEmptyMessage(MSG_HICAR_BINDER_DIED);
                    break;
                case Constants.Event.BT_CONNECTED:
                    mainHandler.sendEmptyMessage(MSG_BT_CONNECTED);
                    break;
                case Constants.Event.PIN_CODE_FAILED:
                    mainHandler.sendEmptyMessage(MSG_PIN_CODE_FAILED);
                    break;
                case Constants.Event.ACC_OFF:
                    mainHandler.sendEmptyMessage(MSG_ACC_OFF);
                    break;
                case Constants.Event.REQUEST_SHARE_NET:
                    if (obj == null) {
                        Log.e(TAG, "onEvent() eventListener obj is null! ");
                        return;
                    }
                    Message msg = Message.obtain();
                    msg.what = MSG_REQUEST_SHARE_NET;
                    msg.obj = obj;
                    mainHandler.sendMessage(msg);
                    break;
                case Constants.Event.OPEN_APP:
                    Log.d(TAG, "onEvent() eventName is Contants.Event.OPEN_APP");
                    if (obj == null) {
                        Log.e(TAG, "onEvent() eventListener obj is null! ");
                        return;
                    }
                    Message msg2 = Message.obtain();
                    msg2.what = MSG_OPEN_APP;
                    msg2.obj = obj;
                    mainHandler.sendMessage(msg2);
                    break;
                //启动手机互联的事件
                //这个事件发送的代码在launchPhoneLink的launchPhoneLink方法，目前我看是被注释掉了
                case Constants.Event.SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE:
                    //发送启动手机互联的消息
                    mainHandler.sendEmptyMessage(MSG_SQUARE_CONTROL_LONG_PRESS_WAKEUP_VOICE);
                    break;
                default:
                    break;
            }
        }
    };

    public void startHicarAdv() {
        Log.d(TAG, "startHicarAdv()");
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.START_HICAR_ADV, params);
    }

    public boolean isBtConnected() {
        HashMap<String, Object> params = new HashMap<>();
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.IS_BT_CONNECTED, params);
    }

    public boolean isConnectedDevice() {
        HashMap<String, Object> params = new HashMap<>();
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.IS_CONNECTED_DEVICE, params);
    }

    public void setIcon(byte[] b) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("bytes", b);
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.SET_ICON, params);
    }

    /**
     * 启动埋点服务
     */
    public void startHiCarDataCollect(int i) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("hicar_ty", i);
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.HICAR_TY, params);
    }
}
