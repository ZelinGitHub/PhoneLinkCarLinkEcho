package com.incall.apps.hicar.servicemanager;

import android.util.Log;

import com.incall.apps.hicar.servicemanager.event.CAEventCenter;
import com.incall.apps.hicar.servicemanager.event.ICAEventListener;
import com.incall.apps.hicar.servicemanager.service.CAServiceCenter;
import com.incall.apps.hicar.servicemanager.service.CAServiceException;
import com.incall.apps.hicar.servicemanager.service.ICACallback;
import com.incall.apps.hicar.servicemanager.service.ICAService;

import java.util.Map;

/**
 * 服务管理
 * @author SongWeiMin
 */
public abstract class BaseCaServiceManager {
    //Service管理对象
    private final CAServiceCenter serviceCenter = new CAServiceCenter();
    //Event管理对象
    private final CAEventCenter eventCenter = new CAEventCenter();


    public CAServiceCenter getServiceCenter() {
        return serviceCenter;
    }
    public CAEventCenter getEventCenter() {
        return eventCenter;
    }

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param service     服务
     */
    public void registerService(String serviceName, ICAService service){
        serviceCenter.registerService(serviceName, service);
    }


    /**
     * 取消注册服务
     * @param serviceName 服务名称
     */
    public void unregisterService(String serviceName){
        serviceCenter.unregisterService(serviceName);
    }

    /**
     * 对外暴露的方法（异步），上层通过该接口调用服务提供的能力
     * @param serviceName   服务名称
     * @param methodName    方法名称
     * @param params        参数
     * @param callback      回调方法
     * @throws CAServiceException
     */
    public final void callService(String serviceName , String methodName, Map params, ICACallback callback)  throws CAServiceException{
        if(serviceCenter.hasService(serviceName)){
            //存在ServiceImpl，直接调用
            serviceCenter.callService(serviceName, methodName, params, callback);
        } else {
            //不存在,获取一次
            ICAService service = delayRegisterService(serviceName);
            if (service != null) {
                //获取到了，注册
                serviceCenter.registerService(serviceName, service);
            }
            serviceCenter.callService(serviceName, methodName, params, callback);
        }

    }

    /**
     * 对外暴露的方法（同步），上层通过该接口调用服务提供的能力
     * @param serviceName   服务名称
     * @param methodName    方法名称
     * @param params        参数
     * @return
     * @throws CAServiceException
     */
    public final Object callServiceSync(String serviceName , String methodName, Map params) throws CAServiceException{
        if(serviceCenter.hasService(serviceName)){
            //存在ServiceImpl，直接调用
            //调用callServiceSync
            return serviceCenter.callServiceSync(serviceName, methodName, params);
        } else {
            //不存在,获取一次
            ICAService service = delayRegisterService(serviceName);
            if (service != null) {
                //获取到了，注册
                serviceCenter.registerService(serviceName, service);
            }
            return serviceCenter.callServiceSync(serviceName, methodName, params);
        }
    }

    /**
     * 添加事件监听
     * @param eventName 事件名称
     * @param listener  监听器
     */
    public void addEventListener(String eventName, ICAEventListener listener){
        eventCenter.addEventListener(eventName,listener);
    }


    /**
     * 移除事件监听
     * @param eventName 事件名称
     * @param listener  监听器
     */
    public void removeEventListener(String eventName, ICAEventListener listener){
        eventCenter.removeEventListener(eventName,listener);
    }

    /**
     * 发布事件
     * @param eventName 事件名称
     * @param message   消息
     */
    public void postEvent(String eventName, Object message) {
        eventCenter.postEvent(eventName,message);
    }


    /**
     * 获取需要延迟注册的服务（调用服务时如果发现没有相应的服务，提供一个延迟注册的机会）
     * @param serviceName 服务名称
     * @return 返回参数中名称对应的服务，用于延迟注册(如果不注册，返回null)
     */
    public abstract ICAService delayRegisterService(String serviceName);
}
