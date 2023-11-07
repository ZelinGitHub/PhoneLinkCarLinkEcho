package com.incall.apps.hicar.servicesdk;

import com.incall.apps.hicar.servicemanager.BaseCaServiceManager;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.servicesimpl.APConnectServiceImpl;
import com.incall.apps.hicar.servicemanager.service.ICAService;
import com.incall.apps.hicar.servicesdk.servicesimpl.MainServiceImpl;
//服务管理器，这是一个单例类，主要是用来管理MainServiceImpl
public class ServiceManager extends BaseCaServiceManager {
    //两个接口对象，不是BBinder对象
    //BBinder对象只有一个HiCarManagerImpl
    private APConnectServiceImpl apConnectService;
    private MainServiceImpl mainService;

    private static class SingletonHolder {
        private static ServiceManager instance = new ServiceManager();
    }

    public static ServiceManager getInstance() {
        return SingletonHolder.instance;
    }

    private ServiceManager() {
        //初始化服务
        initService();
    }

    private void initService() {
        //先调用getService方法，然后再registerService
        registerService(Contants.Services.PIN_SERVICE, getService(Contants.Services.PIN_SERVICE));
        //先调用getService方法，然后再registerService
        registerService(Contants.Services.MAIN_SERVICE, getService(Contants.Services.MAIN_SERVICE));
    }


    @Override
    public ICAService delayRegisterService(String serviceName) {
        //懒加载，动态注册服务
        return getService(serviceName);
    }

    //创建两个服务
    private ICAService getService(String serviceName) {
        switch (serviceName) {
            //连接码服务，也叫蓝牙扫描服务
            case Contants.Services.PIN_SERVICE:
                if (apConnectService == null) {
                    //创建蓝牙设备扫描连接服务
                    //APConnectServiceImpl这个类里面目前什么都没有。
                    apConnectService = new APConnectServiceImpl();
                }
                return apConnectService;
            case Contants.Services.MAIN_SERVICE:
                if (mainService == null) {
                    //主服务
                    //在构造方法中，会注册MainServiceImpl到HiCarServiceManager的mListenerList
                    mainService = new MainServiceImpl();
                }
                return mainService;
            default:break;
        }
        return null;
    }
}
