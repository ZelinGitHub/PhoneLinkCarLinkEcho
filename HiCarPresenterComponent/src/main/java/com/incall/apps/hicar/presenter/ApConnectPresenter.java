package com.incall.apps.hicar.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.incall.apps.hicar.iview.IAPConnetcView;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;

import java.util.HashMap;
//输入连接码的fragment APConnectFragment的presenter
public class ApConnectPresenter extends BasePresenter<IAPConnetcView> {

    private static final String TAG = "WTPhoneLink/ApConnectPresenter";
    private IAPConnetcView apConnetcView;
    private static final int MSG_PIN_CODE_CHANGE = 1;

    public ApConnectPresenter() {
        //添加连接码改变监听
        //事件接收器
        //这个和MainPresenter的eventListener一样，都是接收MainServiceImpl发送的事件
        //发送连接码改变的消息  消息内容是连接码字符串
        ICAEventListener eventListener = new ICAEventListener() {
            @Override
            public void onEvent(String eventName, Object message) {
                Log.d(TAG, "eventName: " + eventName + ", message: " + message);
                if (eventName.equals(Contants.Event.PIN_CODE_CHANGE)) {//发送连接码改变的消息  消息内容是连接码字符串
                    apConnectHandler.sendMessage(apConnectHandler.obtainMessage(MSG_PIN_CODE_CHANGE, message));
                }
            }
        };
        serviceManager.addEventListener(Contants.Event.PIN_CODE_CHANGE, eventListener);
        Log.d(TAG, "ApConnectPresenter()");
    }

    @Override
    public void register(IAPConnetcView view) {
        super.register(view);
        apConnetcView =  iView.get();
        Log.d(TAG, "register()");
    }

    @Override
    public void unRegister() {
        super.unRegister();
        Log.d(TAG, "unRegister="+apConnetcView+"----iView="+iView);
        apConnetcView = null;
    }

    Handler apConnectHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, " handleMessage---apConnetcView=" + apConnetcView);
            if (apConnetcView == null) {
                return;
            }
            Log.d(TAG, " handleMessage---what=" + msg.what);
            if (msg.what == MSG_PIN_CODE_CHANGE) {//连接码改变
                apConnetcView.onPinCodeChange(msg.obj.toString());
            }
        }
    };

    //启动蓝牙设备扫描
    public void startHicarAdv() {
        Log.d(TAG, "startHicarAdv=" );
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method.START_HICAR_ADV, params);
    }

    public void stopHiCarAdv() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method.STOP_HICAR_ADV, params);
    }

    //得到连接码
    public String getPinCode(){
        HashMap<String, Object> params = new HashMap<>();
        //启动服务，获取连接码
        //注意看服务的名字是Contants.Services.MAIN_SERVICE
        //会调用MainServiceImpl的callMethodSync方法
        return serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method.GET_PINCODE, params).toString();
    }

    /**
     * 断开蓝牙连接
     */
    public void disconnectBlueTooth() {
        Log.i(TAG, "disconnectBlueTooth ");
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method. DISCONNECT_BT, params);
    }
}
