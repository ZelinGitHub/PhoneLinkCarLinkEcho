package com.tinnove.hicarclient;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.tinnove.hicarclient.beans.WTAppInfoBean;

import java.util.ArrayList;
import java.util.List;

public class HiCarManager {
    private static final String TAG = "HiCarManager";
    private Context mContext;
    //BpBinder
    private IHiCarInterface mHiCarInterface;
    //HiCar提供的应用程序数据信息监听
    private final List<AppInfoListener> mAppInfoListeners = new ArrayList<>();
    //HiCar提供的接收数据和设备连接监听
    private final List<HiCarServiceListener> mHiCarServiceListeners = new ArrayList<>();

    private final List<PhoneLinkConnectListener> mPhoneLinkConnectListeners = new ArrayList<>();


    private static class Holder {
        //饿汉式单例
        private static HiCarManager sInstance = new HiCarManager();
    }

    private HiCarManager() {
    }

    public static HiCarManager getInstance() {
        return Holder.sInstance;
    }

    public void init(Context context) {
        mContext = context;
    }


    public void registerPhoneLinkConnectListener(PhoneLinkConnectListener phoneLinkConnectListener) {
        if (!mPhoneLinkConnectListeners.contains(phoneLinkConnectListener)) {
            mPhoneLinkConnectListeners.add(phoneLinkConnectListener);
        }
    }

    //注册应用程序数据监听
    public void registerAppListener(AppInfoListener appInfoListener) {
        if (!mAppInfoListeners.contains(appInfoListener)) {
            mAppInfoListeners.add(appInfoListener);
        }
    }

    //注册数据接收和设备连接监听
    public void registerHiCarListener(HiCarServiceListener hiCarListener) {
        if (!mHiCarServiceListeners.contains(hiCarListener)) {
            mHiCarServiceListeners.add(hiCarListener);
        }
    }

    //客户端进程实现的BBinder（此时客户端进程成为响应端进程）
    private final AppInfoChangeListener.Stub mAppInfoChangeListener = new AppInfoChangeListener.Stub() {
        @Override
        public void onLoadAllAppInfo(String deviceId, List<WTAppInfoBean> list) {
            for (AppInfoListener appInfoListener : mAppInfoListeners) {
                appInfoListener.onLoadAllAppInfo(deviceId, list);
            }
        }

        @Override
        public void onAppInfoAdd(String deviceId, List<WTAppInfoBean> list) {
            for (AppInfoListener appInfoListener : mAppInfoListeners) {
                appInfoListener.onAppInfoAdd(deviceId, list);
            }
        }

        @Override
        public void onAppInfoRemove(String deviceId, List<WTAppInfoBean> list) {
            for (AppInfoListener appInfoListener : mAppInfoListeners) {
                appInfoListener.onAppInfoRemove(deviceId, list);
            }
        }

        @Override
        public void onAppInfoUpdate(String deviceId, List<WTAppInfoBean> list) throws RemoteException {
            for (AppInfoListener appInfoListener : mAppInfoListeners) {
                appInfoListener.onAppInfoUpdate(deviceId, list);
            }
        }
    };

    //客户端进程实现的BBinder（此时客户端进程成为响应端进程）
    HiCarListener.Stub mHiCarListener = new HiCarListener.Stub() {

        @Override
        public void onDataReceive(String key, int dataType, byte[] data) {
            for (HiCarServiceListener hiCarServiceListener : mHiCarServiceListeners) {
                hiCarServiceListener.onDataReceive(key, dataType, data);
            }
        }

        @Override
        public void onDeviceChange(String key, int event, int errorcode) {
            for (HiCarServiceListener hiCarServiceListener : mHiCarServiceListeners) {
                hiCarServiceListener.onDeviceChange(key, event, errorcode);
            }
        }
    };

    //连接服务端进程连接监听
    private final ServiceConnection mConnection = new ServiceConnection() {

        //服务连接成功
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected.");
            //BpBinder对象，对应的BBinder对象在服务端进程创建
            mHiCarInterface = IHiCarInterface.Stub.asInterface(service);
            if (mHiCarInterface == null) {
                Log.e(TAG, "mHiCarInterface is null, connect error.");
                return;
            }
            try {
                //注册监听器（传递客户端进程创建的BBinder对象，服务端进程将根据handle创建对应的BpBinder对象）
                mHiCarInterface.registerAppInfoChangeListener(mAppInfoChangeListener);
                //注册监听器（传递客户端进程创建的BBinder对象，服务端进程将根据handle创建对应的BpBinder对象）
                mHiCarInterface.registerHiCarListener(mHiCarListener);
                service.linkToDeath(mDeathRecipient, 0);
                for (PhoneLinkConnectListener phoneLinkConnectListener : mPhoneLinkConnectListeners) {
                    phoneLinkConnectListener.onServiceConnected();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected.");
            try {
                //清空BpBinder对象
                mHiCarInterface = null;
                //重新绑定service
                bindHiCarService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG, "onBindingDied.");
            try {
                //清空BpBinder对象
                mHiCarInterface = null;
                //重新绑定service
                bindHiCarService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public int sendCarData(int dataType, byte[] data) throws RemoteException {
        if (mHiCarInterface == null || !mHiCarInterface.asBinder().isBinderAlive()) {
            Log.e(TAG, "mHiCarInterface is null, connect error.");
            return -1;
        }
        //调用BpBinder的方法（跨进程调用，）
        return mHiCarInterface.sendCarData(dataType, data);
    }

    public int sendKeyEvent(int keyCode, int action) throws RemoteException {
        if (mHiCarInterface == null || !mHiCarInterface.asBinder().isBinderAlive()) {
            Log.e(TAG, "mHiCarInterface is null, connect error.");
            return -1;
        }
        //调用BpBinder的方法（跨进程调用，）
        return mHiCarInterface.sendKeyEvent(keyCode, action);
    }

    public String getPhoneName() throws RemoteException {
        if (mHiCarInterface == null || !mHiCarInterface.asBinder().isBinderAlive()) {
            Log.e(TAG, "mHiCarInterface is null, connect error.");
            return "";
        }
        //调用BpBinder的方法（跨进程调用，）
        return mHiCarInterface.getPhoneName();
    }

    public boolean isConnected() throws RemoteException {
        if (mHiCarInterface == null || !mHiCarInterface.asBinder().isBinderAlive()) {
            Log.e(TAG, "mHiCarInterface is null, connect error.");
            return false;
        }
        //调用BpBinder的方法（跨进程调用，）
        return mHiCarInterface.isConnected();
    }

    //绑定服务端进程
    public void bindHiCarService() throws Exception {
        if (mContext == null) {
            throw new Exception("context is null, please init() first.");
        }
        Log.e(TAG, "bindHiCarService");
        if (mHiCarInterface == null) {
            Intent intent = new Intent();
            intent.setClassName("com.wt.phonelink", "com.incall.apps.hicar.servicesdk.HiCarManagerService");
            mContext.bindService(intent, mConnection, BIND_AUTO_CREATE);
        } else {
            Log.e(TAG, "bindHiCarService is binded.");
        }
    }

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            // 当绑定的service异常断开连接后，自动执行此方法
            Log.e(TAG, "binderDied ");
            if (mHiCarInterface != null) {
                // 当前绑定由于异常断开时，将当前死亡代理进行解绑
                mHiCarInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
                //  重新绑定服务端的service
                Intent intent = new Intent();
                intent.setClassName("com.wt.phonelink", "com.incall.apps.hicar.servicesdk.HiCarManagerService");
                mContext.bindService(intent, mConnection, BIND_AUTO_CREATE);
            }
        }
    };

}
