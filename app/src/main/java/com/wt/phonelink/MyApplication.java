package com.wt.phonelink;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.HiCarManagerService;
import com.incall.apps.hicar.servicesdk.HiCarService;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.openos.common.WTConfigure;
import com.openos.skin.WTSkinManager;
import com.wt.phonelink.hicar.broadcast.BtReceiver;
import com.wt.phonelink.utils.CommonUtil;
import com.wt.phonelink.utils.VoiceUtils;

public class MyApplication extends Application {

    private static final String TAG = "WTPhoneLink/MyApplication";
    public static Context mContext;
    private SharedPreferencesUtil sp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate() Start PhoneLink");
        LinkStateReceiver.register(this);
        VoiceTestReceiver.register(this);
        mContext = this;
        WTConfigure.init(this, "fbb6ff7c43782cc6b3c6fa727eae060e", null);
        sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        //进入程序的时候初始化sp的值
        sp.putBoolean(Contants.SP_IS_WTBOX_CONNECT, false);
        sp.putBoolean(Contants.SP_IS_HICAR_CONNECT, false);
        CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_HICAR_CONNECT, 0);
        sp.putBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
        CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 0);
        //初始化语音管理器
        VoiceManager.getInstance().init(this);
        WTSkinManager.init(this);
        BtReceiver.register(this);
        //在application里面
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }
    }


    public static Context getContext() {
        if (mContext != null) {
            return mContext;
        }
        Log.d(TAG, "getContext() context: " + mContext);
        return null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.d("onTerminate()");
        sp.putBoolean(Contants.SP_IS_WTBOX_CONNECT, false);
        sp.putBoolean(Contants.SP_IS_HICAR_CONNECT, false);
        CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_HICAR_CONNECT, 0);
        sp.putBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
        CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 0);
        com.wt.phonelink.Contants.IS_PHONE_LINK_FRONT = false;
        VoiceUtils.getInstance().stopOrResumeVr(true);
        LinkStateReceiver.unregister(this);
        VoiceTestReceiver.unregister(this);
        BtReceiver.unregister(this);
    }


    //严苛模式 设置方法
    private void enableStrictMode() {
        // 监测当前线程（UI线程）上的网络、磁盘读写等耗时操作
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads() // 监测读磁盘
                        .detectDiskWrites() // 监测写磁盘
                        .detectNetwork() // 监测网络操作
                        .detectCustomSlowCalls() // 监测哪些方法执行慢
                        .detectResourceMismatches() // 监测资源不匹配
                        .penaltyLog() // 打印日志，也可设置为弹窗提示penaltyDialog()或者直接使进程死亡penaltyDeath()
                        .penaltyDropBox() //监测到将信息存到Dropbox文件夹 data/system/dropbox
                        .build()
        );

        // 监测VM虚拟机进程级别的Activity泄漏或者其它资源泄漏
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectActivityLeaks() // 监测内存泄露情况
                        .detectLeakedSqlLiteObjects() // SqlLite资源未关闭，如cursor
                        .detectLeakedClosableObjects() // Closable资源未关闭，如文件流
                        .detectCleartextNetwork() // 监测明文网络
                        .detectLeakedRegistrationObjects() // 监测广播或者ServiceConnection是否有解注册
                        .penaltyLog()
                        .build()
        );
    }

}
