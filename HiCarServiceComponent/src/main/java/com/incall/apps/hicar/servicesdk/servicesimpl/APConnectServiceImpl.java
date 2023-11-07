package com.incall.apps.hicar.servicesdk.servicesimpl;

import com.incall.apps.hicar.servicemanager.service.CAServiceException;
import com.incall.apps.hicar.servicemanager.service.ICACallback;

import java.util.Map;

/**
 * @author changan
 * 蓝牙设备扫描连接服务实现
 * 目前什么都没有
 */
public class APConnectServiceImpl implements IAPConnectService {

    @Override
    public void callMethod(String methodName, Map params, ICACallback callback) throws CAServiceException {

    }

    @Override
    public Object callMethodSync(String methodName, Map params) throws CAServiceException {
        return null;
    }
}
