package com.incall.apps.hicar.servicemanager.service;

import java.util.HashMap;
import java.util.Map;

/**
 *服务中心类，管理服务，事件注册和事件通知
 */
public class CAServiceCenter {
    //包含所有注册的Service容器
    private HashMap<String, ICAService> serviceMap = new HashMap();

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param service   服务
     */
    public synchronized void registerService(String serviceName, ICAService service) {
        serviceMap.put(serviceName,service);
    }

    /**
     * 服务取消注册
     * @param serviceName 服务名称
     */
    public synchronized void unregisterService(String serviceName) {
        serviceMap.remove(serviceName);
    }

    /**
     * 是否有某个服务
     * @param serviceName 服务名称
     * @return
     */
    public boolean hasService(String serviceName){
        return serviceMap.get(serviceName) != null;
    }


    /**
     * 调用服务
     * @param serviceName 服务名称
     * @param methodName 方法名
     * @param params 参数
     * @param callback  回调函数
     * @throws CAServiceException
     */
    public void callService(String serviceName ,
                             String methodName,
                             Map params,
                             ICACallback callback) throws CAServiceException{
        ICAService service = serviceMap.get(serviceName);
        if (service != null) {
            service.callMethod(methodName,params,callback);
        } else {
            throw new CAServiceException(CAServiceErrorCode.SERVICE_NOT_FOUND);
        }
    }

    /**
     * 调用服务
     * @param serviceName 服务名称
     * @param methodName 方法名
     * @param params 参数
     * @throws CAServiceException
     */
    public Object callServiceSync(String serviceName ,
                             String methodName,
                             Map params) throws CAServiceException{
        ICAService service = serviceMap.get(serviceName);
        if (service != null) {
            //callServiceSync调用callMethodSync
            //例如MainServiceImpl
            return service.callMethodSync(methodName,params);
        } else {
            throw new CAServiceException(CAServiceErrorCode.SERVICE_NOT_FOUND);
        }
    }
}
