package com.incall.apps.hicar.servicemanager.service;

public interface ICACallback<E> {
    public void onSuccess(E e);
    public void onFailed(CAServiceException exception);
}
