package com.incall.apps.hicar.presenter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.incall.apps.hicar.iview.IHiCarView;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;

//HiCarFragment的presenter
public class HiCarPresenter extends BasePresenter<IHiCarView> {
    private static final String TAG = "WTWLink/HiCarPresenter";
    private IHiCarView hiCarView;
    private HiCarHandler hiCarHandler;
    private static final int MSG_DEVICE_CONNECT = 1;
    private static final int MSG_DEVICE_DISCONNECT = 2;
    private static final int MSG_DEVICE_PROJECT_CONNECT = 3;
    private static final int MSG_DEVICE_PROJECT_DISCONNECT = 4;
    private static final int MSG_DEVICE_SERVICE_PAUSE = 5;
    private static final int MSG_DEVICE_SERVICE_RESUME = 6;
    private static final int MSG_DEVICE_SERVICE_START = 7;
    private static final int MSG_DEVICE_SERVICE_STOP = 8;
    private static final int MSG_DEVICE_DISPLAY_SERVICE_PLAYING = 9;

    public HiCarPresenter() {
        serviceManager.addEventListener(Constants.Event.DEVICE_CONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_DISCONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_PROJECT_CONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_PROJECT_DISCONNECT, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_PAUSE, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_RESUME, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_START, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_SERVICE_STOP, eventListener);
        serviceManager.addEventListener(Constants.Event.DEVICE_DISPLAY_SERVICE_PLAYING, eventListener);
        Log.d(TAG, "HiCarPresenter()");
    }

    @Override
    public void register(IHiCarView view) {
        super.register(view);
        hiCarView = (IHiCarView) iView.get();
        if (hiCarHandler == null) {
            hiCarHandler = new HiCarHandler(hiCarView);
        }
        Log.d(TAG, "register()");
    }

    @Override
    public void unRegister() {
        super.unRegister();
        hiCarView = null;
        Log.d(TAG, "unRegister()");
    }

    class HiCarHandler extends Handler {
        WeakReference<IHiCarView> weakReference;

        public HiCarHandler(IHiCarView hiCarView) {
            weakReference = new WeakReference<IHiCarView>(hiCarView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            IHiCarView view = weakReference.get();
            if (view != null) {
                switch (msg.what) {
                    case MSG_DEVICE_CONNECT:
                        if (hiCarView != null) {
                            hiCarView.onDeviceConnect();
                        }
                        break;
                    case MSG_DEVICE_DISCONNECT:
                        if (hiCarView != null) {
                            hiCarView.onDeviceDisconnect();
                        }
                        break;
                    case MSG_DEVICE_PROJECT_CONNECT:
                        if (hiCarView != null) {
                            hiCarView.onDeviceProjectConnect();
                        }
                        break;
                    //设备投影断开连接
                    case MSG_DEVICE_PROJECT_DISCONNECT:
                        if (hiCarView != null) {
                            hiCarView.onDeviceProjectDisconnect();
                        }
                        break;
                    case MSG_DEVICE_SERVICE_PAUSE:
                        if (hiCarView != null) {
                            hiCarView.onDeviceServicePause();
                        }
                        break;
                        //203
                    case MSG_DEVICE_SERVICE_RESUME:
                        if (hiCarView != null) {
                            hiCarView.onDeviceServiceResume();
                        }
                        break;
                    case MSG_DEVICE_SERVICE_START:
                        Log.d(TAG, "204 handleMessage() MSG_DEVICE_SERVICE_START call hiCarView.onDeviceServiceStart()");
                        if (hiCarView != null) {
                            hiCarView.onDeviceServiceStart();
                        }
                        break;
                    case MSG_DEVICE_SERVICE_STOP:
                        if (hiCarView != null) {
                            hiCarView.onDeviceServiceStop();
                        }
                        break;
                    case MSG_DEVICE_DISPLAY_SERVICE_PLAYING:
                        if (hiCarView != null) {
                            hiCarView.onDeviceDisplayServicePlaying();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private ICAEventListener eventListener = new ICAEventListener() {
        @Override
        public void onEvent(String eventName, Object message) {
            Log.d(TAG, "onEvent() eventName: " + eventName + ", message: " + message);
            if (hiCarHandler == null) {
                Log.e(TAG, "onEvent() hiCarHandler is null!! ");
                return;
            }
            switch (eventName) {
                case Constants.Event.DEVICE_CONNECT:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_CONNECT);
                    break;
                case Constants.Event.DEVICE_DISCONNECT:
                    //发送断开连接的消息
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_DISCONNECT);
                    break;
                case Constants.Event.DEVICE_PROJECT_CONNECT:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_PROJECT_CONNECT);
                    break;
                //设备投影断开连接
                case Constants.Event.DEVICE_PROJECT_DISCONNECT:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_PROJECT_DISCONNECT);
                    break;
                //202
                case Constants.Event.DEVICE_SERVICE_PAUSE:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_PAUSE);
                    break;
                //203
                case Constants.Event.DEVICE_SERVICE_RESUME:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_RESUME);
                    break;
                //204
                //HiCarConst.EVENT_DEVICE_SERVICE_START
                case Constants.Event.DEVICE_SERVICE_START:
                    Log.d(TAG, "204 onEvent() send MSG_DEVICE_SERVICE_START");
                    Log.d(TAG, "204 onEvent() hiCarHandler: " + hiCarHandler);
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_START);
                    break;
                case Constants.Event.DEVICE_SERVICE_STOP:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_SERVICE_STOP);
                    break;
                case Constants.Event.DEVICE_DISPLAY_SERVICE_PLAYING:
                    hiCarHandler.sendEmptyMessage(MSG_DEVICE_DISPLAY_SERVICE_PLAYING);
                    break;
                default:
                    break;
            }

        }
    };


    //更新车配置
    public boolean updateCarConfig(Surface surface, int width, int height) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("width", width);
        params.put("height", height);
        params.put("surface", surface);
        //更新车配置
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.UPDATE_CARCONFIG, params);
    }

    public boolean isPlayAnim() {
        HashMap<String, Object> params = new HashMap<>();
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.IS_PLAY_ANIM, params);
    }

    //204
    //HiCarConst.EVENT_DEVICE_SERVICE_START
    //开始投屏
    public boolean startProjection() {
        Log.d(TAG, "startProjection()");
        HashMap<String, Object> params = new HashMap<>();
        //调用到MainServiceImpl的对Contants.Method.START_PROJECTION的处理
        //调用HiCarServiceManager的startProjection方法，最后调用mHiCarAdapter的startProjection方法
        return (Boolean) serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.START_PROJECTION, params);
    }

    //暂停投屏
    public void pauseProjection() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.PAUSE_PROJECTION, params);
    }

    public void stopProjection() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.STOP_PROJECTION, params);
    }

    //断开设备连接
    public void disConnected() {
        HashMap<String, Object> params = new HashMap<>();
        //将调用到HiCarServiceManager的disconnectDevice方法
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.DIS_CONNECTED, params);
    }

    public void stopHiCarAdv() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.STOP_HICAR_ADV, params);
    }

    public String getPhoneName() {
        HashMap<String, Object> params = new HashMap<>();
        return serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.GET_PHONE_NAME, params).toString();
    }
}
