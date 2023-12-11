package com.incall.apps.hicar.servicesdk.servicesimpl;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.Surface;

import com.huawei.hicarsdk.HiCarConst;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.ServiceManager;
import com.incall.apps.hicar.servicesdk.interfaces.BaseHiCarListener;
import com.incall.apps.hicar.servicesdk.manager.BTManager;
import com.incall.apps.hicar.servicesdk.manager.HiCarDataCollectManager;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicemanager.service.CAServiceException;
import com.incall.apps.hicar.servicemanager.service.ICACallback;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;

import java.util.Map;

public class MainServiceImpl extends BaseHiCarListener implements IMainService {
    //核心类HiCarServiceManager HiCar服务管理器
    private HiCarServiceManager hiCarManager;
    //蓝牙管理器
    private BTManager btManager;
    private static final String TAG = "WTWLink/MainServiceImpl";


    //调用init
    public MainServiceImpl() {
        init();
    }

    //调用hiCarManager注册自身对象
    private void init() {
        Log.d(TAG, "init()");
        hiCarManager = HiCarServiceManager.getInstance();
        //得到蓝牙管理器
        btManager = BTManager.getInstance();
        //注册自己到HiCarServiceManager的mListenerList
        hiCarManager.registerServiceListener(this);

    }

    @Override
    public void callMethod(String methodName, Map params, ICACallback callback) throws CAServiceException {

    }

    @Override
    public Object callMethodSync(String methodName, Map params) throws CAServiceException {
        Object object = new Object();
        switch (methodName) {
            //更新车配置
            case Constants.Method.UPDATE_CARCONFIG:
                object = hiCarManager.updateCarConfig((Surface) params.get("surface"),
                        Integer.parseInt(params.get("width").toString()), Integer.parseInt(params.get("height").toString()));
                break;
            //开始投屏
            case Constants.Method.START_PROJECTION:
                //开始投屏
                object = hiCarManager.startProjection();
                //请求共享上网
                HiCarServiceManager.getInstance().sendCarData(Constants.HiCarCons.DATA_TYPE_SHARE_NET, CommonUtil.requestShareNet(0, 1));
                break;
            case Constants.Method.IS_CONNECTED_DEVICE:
                object = hiCarManager.isConnectedDevice();

                break;
            //暂停投屏
            case Constants.Method.PAUSE_PROJECTION:
                hiCarManager.pauseProjection();
                break;
            case Constants.Method.STOP_PROJECTION:
                hiCarManager.stopProjection();
                //投屏结束，请求停止共享上网
                HiCarServiceManager.getInstance().sendCarData(Constants.HiCarCons.DATA_TYPE_SHARE_NET, CommonUtil.requestShareNet(0, 0));
                break;
            //启动蓝牙设备扫描
            case Constants.Method.START_HICAR_ADV:
                Log.d(TAG, "START_HICAR_ADV---");
                hiCarManager.sendMsgDelayed(HiCarServiceManager.MSG_START_ADV, 0);
                break;
            //断开设备连接
            case Constants.Method.DIS_CONNECTED:
                //断开设备连接
                hiCarManager.disconnectDevice();
                break;
            case Constants.Method.STOP_HICAR_ADV:
                hiCarManager.sendMsgDelayed(HiCarServiceManager.MSG_STOP_ADV, 0);
                break;
            case Constants.Method.SET_ICON:
                try {
                    byte[] b = (byte[]) params.get("bytes");
                    hiCarManager.sendCarData(Constants.HiCarCons.DATA_TYPE_BRAND_ICON_DATA, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Constants.Method.GET_PHONE_NAME:
                object = hiCarManager.getPhoneName();
                break;
            //得到连接码
            case Constants.Method.GET_PINCODE:
                //得到连接码
                object = hiCarManager.getPinCode();
                break;
            case Constants.Method.IS_PLAY_ANIM:
                object = hiCarManager.isPlayAnim();
                break;
            case Constants.Method.IS_BT_CONNECTED:
                object = btManager.isConnected();
                break;
            case Constants.Method.OPEN_BT:
                btManager.bluetoothOn();
                break;
            case Constants.Method.HICAR_TY:
                object = params.get("hicar_ty");
                if (!object.toString().isEmpty()) {
                    HiCarDataCollectManager.getInstance().startHiCar(Integer.parseInt(object.toString()));
                }
                break;
            case Constants.Method.DISCONNECT_BT:
                if (btManager == null) {
                    Log.e(TAG, "btManager is null! ");
                    break;
                }
                Log.i(TAG, "DISCONNECT_BT state: " + btManager.isHfpConnected());

                if (btManager.isHfpConnected()) {
                    // 2 已连接，如果当前有设备连接，那么就断开
                    BluetoothDevice device = btManager.getBluetoothDevice();
                    if (device != null) {
                        Log.i(TAG, "DISCONNECT_BT address: " + device);
                        btManager.disconnect(device);
                    }
                }

                btManager.disconnectAllEnabledProfiles();
                break;
            default:
                break;
        }
        return object;
    }

    //主要是设备连接的事件
    //这个方法被HiCarServiceManager的onDeviceChange方法调用
    @Override
    public void onDeviceChange(String s, int i, int i1) {
        Log.d(TAG, "onDeviceChange() mDeviceId: " + s + ", i: " + i);
        //发送事件
        switch (i) {
            //设备连接
            case HiCarConst.EVENT_DEVICE_CONNECT:
                //发送设备已连接的事件
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_CONNECT, i);
                break;
            //设备断开连接
            case HiCarConst.EVENT_DEVICE_DISCONNECT:
                //发送断开连接的事件
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_DISCONNECT, i);
                break;
            //设备投影连接
            case HiCarConst.EVENT_DEVICE_PROJECT_CONNECT:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_PROJECT_CONNECT, i);
                break;
            //设备投影断开连接
            //没有找到调用的地方
            case HiCarConst.EVENT_DEVICE_PROJECT_DISCONNECT:
                //发送事件
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_PROJECT_DISCONNECT, i);
                break;
            default:
        }
    }

    //主要是指设备投屏服务
    @Override
    public void onDeviceServiceChange(String s, int i) {
        Log.d(TAG, "onDeviceServiceChange() i: " + i);
        //发送事件
        switch (i) {
            //设备服务暂停  暂停投屏 202
            case HiCarConst.EVENT_DEVICE_SERVICE_PAUSE:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_SERVICE_PAUSE, i);
                break;
            //设备服务恢复 恢复投屏 203
            case HiCarConst.EVENT_DEVICE_SERVICE_RESUME:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_SERVICE_RESUME, i);
                break;
            //设备服务开始 启动投屏 204
            case HiCarConst.EVENT_DEVICE_SERVICE_START:
                Log.d(TAG, "204 onDeviceServiceChange() postEvent Contants.Event.DEVICE_SERVICE_START");
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_SERVICE_START, i);
                break;
            //设备服务停止
            case HiCarConst.EVENT_DEVICE_SERVICE_STOP:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_SERVICE_STOP, i);
                break;
            //播放中
            case HiCarConst.EVENT_DEVICE_DISPLAY_SERVICE_PLAYING:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_DISPLAY_SERVICE_PLAYING, i);
                break;
            //播放失败
            case HiCarConst.EVENT_DEVICE_DISPLAY_SERVICE_PLAY_FAILED:
                ServiceManager.getInstance().postEvent(Constants.Event.DEVICE_DISPLAY_SERVICE_PLAY_FAILED, i);
                break;
            default:
        }
    }

    @Override
    public void onDataReceive(String s, int i, byte[] bytes) {
        Log.d(TAG, "onDataReceive() i: " + i + ", content: " + new String(bytes));
        //商标图标数据
        if (i == Constants.HiCarCons.DATA_TYPE_BRAND_ICON_DATA) {
            //发送事件
            ServiceManager.getInstance().postEvent(Constants.Event.BRAND_ICON_DATA_CHANGE, s);
        }
        //共享上网
        else if (i == Constants.HiCarCons.DATA_TYPE_SHARE_NET) {
            ServiceManager.getInstance().postEvent(Constants.Event.REQUEST_SHARE_NET, bytes);
        }
        //主动断开车机连接通知0
        else if (i == Constants.HiCarCons.DATA_TYPE_DISCONNECT_BY_USER) {
            HiCarServiceManager.getInstance().sendCarData(Constants.HiCarCons.DATA_TYPE_DISCONNECT_BY_USER, CommonUtil.isUserDisconnect(bytes));
        }
        //打开app
        else if (i == Constants.HiCarCons.DATA_TYPE_REQUEST_APP) {
            Log.d(TAG, "onDataReceive() call postEvent(Contants.Event.OPEN_APP)");
            ServiceManager.getInstance().postEvent(Constants.Event.OPEN_APP, bytes);
        }

    }

    //这个方法的调用在HiCarServiceManager的public void onPinCode(String s)方法
    @Override
    public void onPinCode(String s) {
        Log.i(TAG, "onPinCode() pinCode: " + s);
        //发送连接码改变的事件，这个貌似是事件，不是消息
        //这个事件貌似会被MainPresenter和ApConnectPresenter都接收到，s是连接码字符串
        ServiceManager.getInstance().postEvent(Constants.Event.PIN_CODE_CHANGE, s);
    }

    @Override
    public void onBinderDied() {
        Log.i(TAG, "onBinderDied()");
        //发送事件
        ServiceManager.getInstance().postEvent(Constants.Event.HICAR_BINDER_DIED, null);
    }

    @Override
    public void onPinCodeFailed() {
        Log.i(TAG, "onPinCodeFailed()");
        //发送事件
        ServiceManager.getInstance().postEvent(Constants.Event.PIN_CODE_FAILED, null);
    }
}
