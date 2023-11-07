package com.incall.apps.hicar.servicemanager.service;

import java.util.Map;

public interface ICAService {

    /**
     * 调用方法
     * @param methodName 方法名
     * @param params    参数
     * @param callback
     * @throws CAServiceException
     */
    public void callMethod(String methodName, Map params, ICACallback callback) throws CAServiceException;

    /**
     * 同步调用方法
     * @param methodName 方法名
     * @param params    参数
     * @throws CAServiceException
     */
    public Object callMethodSync(String methodName, Map params) throws CAServiceException;


}
