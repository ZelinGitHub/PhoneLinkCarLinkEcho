package com.incall.apps.hicar.presenter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.iview.IInitFailedView;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;
import com.incall.apps.hicar.servicesdk.contants.Constants;

import java.util.HashMap;
//InitFailedFragment的presenter
public class InitFailedPresenter extends BasePresenter<IInitFailedView> {

    private final String TAG = InitFailedPresenter.class.getSimpleName();
    private IInitFailedView iInitFailedView;
    private final int MSG_HOME = 1;

    public InitFailedPresenter() {
        serviceManager.addEventListener(Constants.Event.ON_HOME, eventListener);
        LogUtil.d(TAG, "InitFailedPresenter=");
    }

    @Override
    public void register(IInitFailedView view) {
        super.register(view);
        iInitFailedView =  iView.get();
        LogUtil.d(TAG, "register=");
    }

    @Override
    public void unRegister() {
        super.unRegister();
        LogUtil.d(TAG, "unRegister="+iInitFailedView+"----iView="+iView);
        iInitFailedView = null;
    }

    private ICAEventListener eventListener = new ICAEventListener() {
        @Override
        public void onEvent(String eventName, Object message) {
            LogUtil.d(TAG, "eventName=" + eventName + "---message=" + message);
            switch (eventName) {
                case Constants.Event.ON_HOME:
                    initFailedHandler.sendMessage(initFailedHandler.obtainMessage(MSG_HOME, message));
                    break;
                default:break;
            }
        }
    };

    Handler initFailedHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.d(TAG, " handleMessage---iInitFailedView=" + iInitFailedView);
            if (iInitFailedView == null) {
                return;
            }
            LogUtil.d(TAG, " handleMessage---what=" + msg.what);
            switch (msg.what) {
                case MSG_HOME:
                    iInitFailedView.onHome();
                    break;
                default:break;
            }
        }
    };

    public void startHicarAdv() {
        HashMap<String, Object> params = new HashMap<>();
        serviceManager.callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.START_HICAR_ADV, params);
    }
}