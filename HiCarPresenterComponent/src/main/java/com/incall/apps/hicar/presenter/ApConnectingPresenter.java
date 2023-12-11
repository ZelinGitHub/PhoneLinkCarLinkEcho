package com.incall.apps.hicar.presenter;

import com.incall.apps.hicar.iview.IAPConnetcingView;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.ServiceManager;

import java.util.HashMap;

//ConnectingFragment的presenter，ConnectingFragment这个类目前没有用到
public class ApConnectingPresenter extends BasePresenter<IAPConnetcingView> {

    private IAPConnetcingView apConnetcingView;
    private final int MSG_PIN_CODE_CHANGE = 1;

    public ApConnectingPresenter() {
    }

    @Override
    public void register(IAPConnetcingView view) {
        super.register(view);
        apConnetcingView = (IAPConnetcingView) iView.get();

    }

    public String getPhoneName() {
        HashMap<String, Object> params = new HashMap<>();
        return ServiceManager.getInstance().callServiceSync(Constants.Services.MAIN_SERVICE, Constants.Method.GET_PHONE_NAME, params).toString();
    }

}