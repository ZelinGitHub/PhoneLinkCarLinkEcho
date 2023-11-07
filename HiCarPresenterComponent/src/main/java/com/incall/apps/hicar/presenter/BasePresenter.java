package com.incall.apps.hicar.presenter;

import com.incall.apps.hicar.servicesdk.ServiceManager;

import java.lang.ref.WeakReference;
//所有presenter的基类
public abstract class BasePresenter<V> implements IPresenter<V> {
    protected WeakReference<V> iView;
    protected ServiceManager serviceManager;
    public BasePresenter(){
        serviceManager = ServiceManager.getInstance();
    }
    @Override
    public void register(V view) {
        iView = new WeakReference(view);
    }

    @Override
    public void unRegister() {
        if(iView != null){
            iView.clear();
            iView = null;
        }
    }
}
