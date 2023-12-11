/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.incall.apps.hicar.servicesdk;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.incall.apps.hicar.servicesdk.aidl.HiCarManagerImpl;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.manager.RxIntervalManager;
import com.incall.serversdk.hicar.HiCarProxy;
import com.incall.serversdk.server.SvrMngProxy;

//HiCar的service，Hicar需要一个service，carLink不需要service
//这个是用来和其他东西跨进程交互的，比如和北斗方控交互
//在这个service里面，初始化HiCarServiceManager
public class HiCarService extends Service {
    private static final String TAG = "WTWLink/HiCarService";

    //BBinder对象 HiCarManagerImpl
    private HiCarManagerImpl hiCarManager;
    //HiCarServiceManager HiCar服务管理器
    private HiCarServiceManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind invoke");
        //返回BBinder对象
        return hiCarManager;
    }

    //在HiCarMainActivity的onCreate方法调用startService方法启动这个Service
    //第三方组件绑定这个service，也会启动这个service
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "HiCarService onCreate");

        //HiCarManagerImpl
        if (hiCarManager == null) {
            //创建BBinder对象 创建HiCarManagerImpl对象
            hiCarManager = new HiCarManagerImpl(getApplication());
        }
        // 修改 这个service 的包名还是使用 SDK中相同的，这样保持讯飞那边的使用 KongJing 2021.12.23
        SvrMngProxy.getInstance().addServer(HiCarProxy.getInstance().getServerName(), hiCarManager);
        //HandlerThread
        HandlerThread msgHandler = new HandlerThread(HiCarService.class.getSimpleName());
        //启动一个线程
        msgHandler.start();
        Looper looper = msgHandler.getLooper();
        if (looper == null) {
            Log.d(TAG, "getLooper failed");
            return;
        }
        //HiCar服务管理器
        //HiCarServiceManager相当于是一个单例工具类，没有继承或实现其他类
        manager = HiCarServiceManager.getInstance();
        Application app = getApplication();
        if (app != null) {
            //初始化HiCar服务管理器
            //主要任务就在这个init方法里面
            //这个init方法将调用HiCarServiceManager的loadHiCarSdk方法
            //loadHiCarSdk方法中将注册华为HiCar监听
            manager.init(app, looper);
        }
        //恢复免唤醒 KongJing 2021.10.14
        if (hiCarManager != null) {
            hiCarManager.voiceStatusChange(true);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = null;
            mChannel = new NotificationChannel("phonelink_channel", "手机互联", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), "phonelink_channel").build();
            //设置当前service为前台服务
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        manager.unloadHiCarSdk(this);
        //释放回连重连的轮询管理 KongJing 2021.12.29
        RxIntervalManager.getInstance().release();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //（1）当内存不足导致Service被系统强制杀死的一段时间后，系统会再次启动这个Service。
        //（2）再次启动时，Service对象的onStartCommand方法的Intent参数为null。
        //（3）粘性是Service默认的启动类型。
        return START_STICKY;
    }
}
