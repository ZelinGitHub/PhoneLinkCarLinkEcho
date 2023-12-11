package com.incall.apps.hicar.servicesdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.incall.apps.hicar.servicesdk.interfaces.BaseHiCarListener;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.tinnove.hicarclient.AppInfoChangeListener;
import com.tinnove.hicarclient.HiCarListener;
import com.tinnove.hicarclient.IHiCarInterface;
import com.tinnove.hicarclient.beans.WTAppInfoBean;

import java.util.ArrayList;
import java.util.List;

public class HiCarManagerService extends Service {
    private static final String TAG = "WTWLink/HiCarManagerService";
    private HiCarService mHiCarService;
    //BpBinder对象列表
    private final List<AppInfoChangeListener> mAppInfoListeners = new ArrayList<>();
    //BpBinder对象列表
    private final List<HiCarListener> mHiCarListeners = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        HiCarServiceManager.getInstance().registerServiceListener(new BaseHiCarListener() {
            @Override
            public void onDataReceive(String key, int dataType, byte[] data) {
                Log.d(TAG, "onDataReceive() i: " + dataType + ", content: " + new String(data));
                //遍历BpBinder对象列表
                for (HiCarListener mListener : mHiCarListeners) {
                    try {
                        //跨进程调用
                        mListener.onDataReceive(key, dataType, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDeviceChange(String key, int event, int errorcode) {
                Log.e(TAG, "onDeviceChange() event: " + event + ",  errorCode: " + errorcode);
                //遍历BpBinder对象列表
                for (HiCarListener mListener : mHiCarListeners) {
                    try {
                        //跨进程调用
                        mListener.onDeviceChange(key, event, errorcode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onLoadAllAppInfo(String deviceId, List<WTAppInfoBean> list) {
                Log.e(TAG, "onLoadAllAppInfo() deviceId: " + deviceId + ", list: " + list);
                //遍历BpBinder对象列表
                for (AppInfoChangeListener mListener : mAppInfoListeners) {
                    try {
                        //跨进程调用
                        mListener.onLoadAllAppInfo(deviceId, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAppInfoAdd(String deviceId, List<WTAppInfoBean> list) {
                Log.e(TAG, "onAppInfoAdd() deviceId: " + deviceId + ", list: " + list);
                //遍历BpBinder对象列表
                for (AppInfoChangeListener mListener : mAppInfoListeners) {
                    try {
                        //跨进程调用
                        mListener.onAppInfoAdd(deviceId, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAppInfoRemove(String deviceId, List<WTAppInfoBean> list) {
                Log.e(TAG, "onAppInfoRemove() deviceId: " + deviceId + ", list: " + list);
                //遍历BpBinder对象列表
                for (AppInfoChangeListener mListener : mAppInfoListeners) {
                    try {
                        //跨进程调用
                        mListener.onAppInfoRemove(deviceId, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAppInfoUpdate(String deviceId, List<WTAppInfoBean> list) {
                Log.e(TAG, "onAppInfoUpdate() deviceId: " + deviceId + ", list: " + list);
                //遍历BpBinder对象列表
                for (AppInfoChangeListener mListener : mAppInfoListeners) {
                    try {
                        mListener.onAppInfoUpdate(deviceId, list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //创建BBinder对象
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mHiCarService == null) {
            mHiCarService = new HiCarService();
        }
        return mHiCarService;
    }

    //BBinder对象
    private class HiCarService extends IHiCarInterface.Stub {

        //跨进程注册
        @Override
        public void registerAppInfoChangeListener(AppInfoChangeListener appInfoChangeListener) throws RemoteException {
            if (!mAppInfoListeners.contains(appInfoChangeListener)) {
                mAppInfoListeners.add(appInfoChangeListener);
            }
        }

        //跨进程注册
        @Override
        public void registerHiCarListener(HiCarListener hiCarListener) throws RemoteException {
            if (!mHiCarListeners.contains(hiCarListener)) {
                mHiCarListeners.add(hiCarListener);
            }
        }

        @Override
        public void unRegisterListener() throws RemoteException {
            mHiCarListeners.clear();
            mAppInfoListeners.clear();
        }


        //查询是否连接
        @Override
        public boolean isConnected() throws RemoteException {
            HiCarServiceManager instance = HiCarServiceManager.getInstance();
            if (instance != null) {
                return instance.isConnectedDevice();
            } else {
                Log.e(TAG, "isConnected() instance is null! ");
                return false;
            }
        }

        //查询手机名字
        @Override
        public String getPhoneName() throws RemoteException {
            HiCarServiceManager instance = HiCarServiceManager.getInstance();
            if (instance != null) {
                return instance.getPhoneName();
            } else {
                Log.e(TAG, "getPhoneName() instance is null! ");
                return "";
            }
        }

        //发送数据
        @Override
        public int sendCarData(int dataType, byte[] data) throws RemoteException {
            HiCarServiceManager instance = HiCarServiceManager.getInstance();
            if (instance != null) {
                return instance.sendCarData(dataType, data);
            } else {
                Log.e(TAG, "sendCarData() instance is null! ");
                return -1;
            }
        }

        //发送事件
        @Override
        public int sendKeyEvent(int keyCode, int action) throws RemoteException {
            HiCarServiceManager instance = HiCarServiceManager.getInstance();
            if (instance != null) {
                return instance.sendKeyData(keyCode, action);
            } else {
                Log.e(TAG, "sendKeyEvent() instance is null! ");
                return -1;
            }
        }
    }
}
