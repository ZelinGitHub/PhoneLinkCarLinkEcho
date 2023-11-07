package com.incall.apps.hicar.presenter;

public interface IPresenter<V> {
    void register(V view);
    void unRegister();
}
