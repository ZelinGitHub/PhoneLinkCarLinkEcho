package com.incall.apps.hicar.servicesdk.aidl;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.huawei.hicarsdk.HiCarConst;
import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.ServiceManager;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.contants.LaunchPhoneLinkEvent;
import com.incall.apps.hicar.servicesdk.interfaces.BaseHiCarListener;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.servicesimpl.audio.HcAudioManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.incall.serversdk.hicar.IHiCarListener;
import com.incall.serversdk.hicar.IHiCarManager;
import com.incall.serversdk.hicar.IHiCarVoiceFreeListener;

import org.greenrobot.eventbus.EventBus;

//BBinder
//这个是用来跨进程交互的，比如和北斗方控交互
public class HiCarManagerImpl extends IHiCarManager.Stub {
    private static final String TAG = "WTPhoneLink/HiCarManagerImpl";
    private RemoteCallbackList<IHiCarListener> listListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IHiCarVoiceFreeListener> listVoiceListener = new RemoteCallbackList<>();
    private HiCarServiceManager manager;
    private HcAudioManager audioManager;
    private SharedPreferencesUtil sp;
    private final Context mContext;

    public HiCarManagerImpl(Context context) {
        mContext = context;
        manager = HiCarServiceManager.getInstance();
        audioManager = HcAudioManager.getInstance();
        //注册监听器hiCarServiceListener到HiCarServiceManager的mListenerList
        manager.registerServiceListener(hiCarServiceListener);
        sp = SharedPreferencesUtil.getInstance(context);
    }

    @Override
    public boolean isConnected() throws RemoteException {
        Log.i(TAG, "isConnected() manager: " + manager);
        return manager.isConnectedDevice();
    }


    //启动语音助手
    @Override
    public void launchPhoneLink() throws RemoteException {
        Log.i(TAG, "launchPhoneLink()");
        EventBus.getDefault().post(new LaunchPhoneLinkEvent());
    }

    @Override
    public int disConnectDevice() throws RemoteException {
        Log.i(TAG, "disConnectDevice() manager: " + manager);
        return manager.disconnectDevice();
    }

    @Override
    public String getPhoneName() throws RemoteException {
        Log.i(TAG, "getPhoneName() manager: " + manager);
        return manager.getPhoneName();
    }

    @Override
    public void registerHiCarListener(IHiCarListener iHiCarListener) throws RemoteException {
        Log.i(TAG, "registerHiCarListener()");
        listListener.register(iHiCarListener);
    }

    @Override
    public void unRegisterHiCarListener(IHiCarListener iHiCarListener) throws RemoteException {
        Log.i(TAG, "registerHiCarListener()");
        listListener.unregister(iHiCarListener);
    }

    @Override
    public void closeVoice() throws RemoteException {
        Log.i(TAG, "closeVoice()");
        audioManager.closeHiCarVoice();
        ServiceManager.getInstance().postEvent(Contants.Event.DEVICE_SERVICE_PAUSE, null);
    }

    @Override
    public void registerHiCarVoiceFreeListener(IHiCarVoiceFreeListener iHiCarVoiceFreeListener) throws RemoteException {
        listVoiceListener.register(iHiCarVoiceFreeListener);
    }

    @Override
    public void unRegisterHiCarVoiceFreeListener(IHiCarVoiceFreeListener iHiCarVoiceFreeListener) throws RemoteException {
        listVoiceListener.unregister(iHiCarVoiceFreeListener);
    }

    //BaseHiCarListener实现了HiCarServiceListener
    BaseHiCarListener hiCarServiceListener = new BaseHiCarListener() {
        @Override
        public void onDeviceChange(String s, int i, int i1) {
            switch (i) {
                //设备连接
                case HiCarConst.EVENT_DEVICE_CONNECT:
                    Log.i(TAG, "onDeviceChange() status:  " + i);
                    sp.putBoolean(Contants.SP_IS_HICAR_CONNECT, true);
                    if (mContext != null) {
                        setGlobalProp(mContext,Contants.SYS_IS_HICAR_CONNECT, 1);
                    }
                    hiCarStatusChange(true);
                    voiceStatusChange(false);//hicar连接成功关闭车机语音助手免唤醒
                    break;
                //设备断开连接
                case HiCarConst.EVENT_DEVICE_DISCONNECT:
                    Log.i(TAG, "onDeviceChange() status: " + i);
                    sp.putBoolean(Contants.SP_IS_HICAR_CONNECT, false);
                    if (mContext != null) {
                        setGlobalProp(mContext,Contants.SYS_IS_HICAR_CONNECT, 0);
                    }
                    hiCarStatusChange(false);
                    voiceStatusChange(true);//hicar断开连接打开车机语音助手免唤醒
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDeviceServiceChange(String s, int i) {
            if (i == HiCarConst.EVENT_DEVICE_DISPLAY_SERVICE_PLAYING) {
//                hiCarStatusChange(true);
                voiceStatusChange(false);//hicar在在前台的时候(hicar在后台再次进入的时候会回调)关闭车机语音助手免唤醒
            } else if (i == HiCarConst.EVENT_DEVICE_DISPLAY_SERVICE_PLAY_FAILED) {
                //投屏失败 增加业务逻辑 启动车机语音助手免唤醒
                voiceStatusChange(true);
            }
        }
    };


    private void hiCarStatusChange(boolean status) {
        try {
            int num = listListener.beginBroadcast();
            for (int i = 0; i < num; i++) {
                IHiCarListener listener1 = listListener.getBroadcastItem(i);
                listener1.onStatusChange(status);
            }
            listListener.finishBroadcast();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void voiceStatusChange(boolean status) {
        try {
            int num = listVoiceListener.beginBroadcast();
            for (int i = 0; i < num; i++) {
                IHiCarVoiceFreeListener listener1 = listVoiceListener.getBroadcastItem(i);
                listener1.onVoiceStatusChange(status);
            }
            listVoiceListener.finishBroadcast();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void setGlobalProp(Context context,String key, int value) {
        Log.i(TAG, "setGlobalProp() key: " + key + "，value: " + value);
        Settings.Global.putInt(context.getContentResolver(), key, value);
    }
}
