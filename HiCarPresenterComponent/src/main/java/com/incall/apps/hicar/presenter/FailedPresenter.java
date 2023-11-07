package com.incall.apps.hicar.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.iview.IConnectFailedView;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;
import com.incall.apps.hicar.servicesdk.contants.Contants;

import java.util.HashMap;
//ConnectFailedFragment的presenter
public class FailedPresenter extends BasePresenter<IConnectFailedView> {

    private final String TAG = FailedPresenter.class.getSimpleName();

    private IConnectFailedView connectFailedView;

    private final int MSG_HOME = 1;

    public FailedPresenter() {
        serviceManager.addEventListener(Contants.Event.ON_HOME, eventListener);
        LogUtil.d(TAG, "FailedPresenter=");
    }

    @Override
    public void register(IConnectFailedView view) {
        super.register(view);
        connectFailedView = iView.get();
        LogUtil.d(TAG, "register=");
    }

    @Override
    public void unRegister() {
        super.unRegister();
        LogUtil.d(TAG, "unRegister=" + connectFailedView + "----iView=" + iView);
        connectFailedView = null;
    }

    private ICAEventListener eventListener = new ICAEventListener() {
        @Override
        public void onEvent(String eventName, Object message) {
            LogUtil.d(TAG, "eventName=" + eventName + "---message=" + message);
            switch (eventName) {
                case Contants.Event.ON_HOME:
                    connectFailedHandler.sendMessage(connectFailedHandler.obtainMessage(MSG_HOME, message));
                    break;
                default:break;
            }
        }
    };

    Handler connectFailedHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.d(TAG, " handleMessage---connectFailedView=" + connectFailedView);
            if (connectFailedView == null) {
                return;
            }
            LogUtil.d(TAG, " handleMessage---what=" + msg.what);
            switch (msg.what) {
                case MSG_HOME:
                    connectFailedView.onHome();
                    break;
                default:break;
            }
        }
    };

    public String getPhoneName() {
        HashMap<String, Object> params = new HashMap<>();
        return serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method.GET_PHONE_NAME, params).toString();
    }

    public void disConnected() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Contants.Services.MAIN_SERVICE, Contants.Method.DIS_CONNECTED, params);
    }
}