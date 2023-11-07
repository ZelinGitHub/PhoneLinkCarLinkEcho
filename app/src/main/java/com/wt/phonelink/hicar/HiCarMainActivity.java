package com.wt.phonelink.hicar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.incall.apps.hicar.iview.IMainView;
import com.incall.apps.hicar.presenter.MainPresenter;
import com.incall.apps.hicar.servicesdk.HiCarService;
import com.incall.apps.hicar.servicesdk.contants.HicarCallStatusFocusEvent;
import com.incall.apps.hicar.servicesdk.contants.LaunchPhoneLinkEvent;
import com.incall.apps.hicar.servicesdk.manager.BTManager;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.incall.apps.hicar.servicesdk.utils.SystemProperties;
import com.openos.skin.WTSkinManager;
import com.wt.phonelink.Contants;
import com.wt.phonelink.MyApplication;
import com.wt.phonelink.R;
import com.wt.phonelink.VoiceManager;
import com.wt.phonelink.hicar.fragments.HiCarAPConnectFragment;
import com.wt.phonelink.hicar.fragments.HiCarBtFragment;
import com.wt.phonelink.hicar.fragments.HiCarConnectFailedFragment;
import com.wt.phonelink.hicar.fragments.HiCarFragment;
import com.wt.phonelink.hicar.fragments.HiCarInitFailedFragment;
import com.wt.phonelink.utils.CommonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

//华为hicar的连接界面
//这是一个activity，还有很多子界面是fragment实现的
public class HiCarMainActivity extends AppCompatActivity implements IMainView {
    //presenter
    private MainPresenter mainPresenter;
    private final String TAG = "WTPhoneLink/HiCarMainActivity";
    private HiCarFragment hiCarFragment;
    private HiCarAPConnectFragment apConnectFragment;
    private HiCarConnectFailedFragment connectFailedFragment;
    private HiCarBtFragment btFragment;
    private HiCarInitFailedFragment initFailedFragment;
    private SharedPreferencesUtil sharedPreferencesUtil;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        setContentView(R.layout.activity_hicar_main);
        Log.d(TAG, "onCreate()");
        mainPresenter = new MainPresenter();
        mainPresenter.register(this);
        //初始化hiCarService。启动Activity之后，会启动一个service
        initService();
        initView();
        initFragment();
        initData();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void initService() {
        Log.d(TAG, "initService()");
        //判断service是否启动，如果没有启动，那么就启动
        if (CommonUtil.isServiceRunning(this, "com.incall.apps.hicar.servicesdk.HiCarService")) {
            Log.e(TAG, "initService() hicar service is running");
            return;
        }
        //启动service HiCarService
        Intent intent = new Intent(this, HiCarService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //适配8.0机制
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    //创建所有的fragment对象
    private void initFragment() {
        Log.d(TAG, "initFragment() hiCarFragment: " + hiCarFragment);
        //华为投影fragment
        hiCarFragment = new HiCarFragment();
        //蓝牙连接fragment
        apConnectFragment = new HiCarAPConnectFragment();
        //连接失败fragment
        connectFailedFragment = new HiCarConnectFailedFragment();
        //蓝牙开启中fragment
        btFragment = new HiCarBtFragment();
        //初始化失败fragment
        initFailedFragment = new HiCarInitFailedFragment();
        Log.d(TAG, "initFragment() hiCarFragment: " + hiCarFragment);
    }


    private void initView() {
    }

    private void initData() {
        sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);
        addSvrMngServerToSmgr();
        //设置蓝牙
        getProfileProxy();
        EventBus.getDefault().register(this);

        SystemProperties.set("persist.sys.hicar.reuse.ap", true);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //HiCarServiceManager的onDeviceChange调用MainServiceImpl的onDeviceChange方法
    //MainServiceImpl的onDeviceChange方法发送Constants.Event.DEVICE_CONNECT设备已连接的事件
    //MainPresenter的MSG_DEVICE_CONNECT消息处理后调用
    //设备连接
    @Override
    public void onDeviceConnect() {
        Log.i(TAG, "onDeviceConnect()");
        //设备连接后，切换到华为hicar投影Fragment
        //连接之后，才开始投屏。投屏是在HiCarFragment的surface监听中调用的。
        switchFragment(Contants.FragmentCon.TAG_HICAR_SCREEN);
        //注册场景内语音
        VoiceManager.getInstance().registerHiCarWakeUp();
    }

    @Override
    public void onDeviceDisconnect() {
        Log.i(TAG, "onDeviceDisconnect()");
        finish();
        VoiceManager.getInstance().unRegisterWakeUp();
    }

    @Override
    public void onDeviceProjectConnect() {

    }

    @Override
    public void onDeviceProjectDisconnect() {
        Log.i(TAG, "onDeviceProjectDisconnect()");
    }

    @Override
    public void onDeviceServicePause() {
        Log.i(TAG, "onDeviceServicePause()");
    }

    //203
    @Override
    public void onDeviceServiceResume() {
        Log.i(TAG, "onDeviceServiceResume()");
    }

    @Override
    public void onDeviceServiceStart() {
        Log.i(TAG, "204 onDeviceServiceStart()");
    }

    @Override
    public void onDeviceServiceStop() {
        Log.i(TAG, "onDeviceServiceStop()");
    }

    @Override
    public void onDeviceDisplayServicePlaying() {
        Log.i(TAG, "onDeviceDisplayServicePlaying()");
    }

    @Override
    public void onDeviceDisplayServicePlayFailed() {
        Log.i(TAG, "onDeviceDisplayServicePlayFailed()");
        switchFragment(Contants.FragmentCon.TAG_HICAR_FAILED);
    }

    //蓝牙连接完成
    @Override
    public void onBtConnected() {
        Log.i(TAG, "onBtConnected()");
        mainPresenter.startHicarAdv();
    }

    //二维码改变
    @Override
    public void onPinCodeChange() {
        Log.i(TAG, "onPinCodeChange()");
        switchFragment(Contants.FragmentCon.TAG_AP_CONNECT);
    }

    @Override
    public void onBrandIconDataChange() {
        Log.i(TAG, "onBrandIconDataChange()");
        mainPresenter.setIcon(CommonUtil.imgToBytes(getApplicationContext(), R.mipmap.hicar_icon));
    }

    @Override
    public void onBinderDied() {
        Log.i(TAG, "onBinderDied()");
        toLauncher();
    }

    @Override
    public void onPinCodeFailed() {
        Log.i(TAG, "onPinCodeFailed()");
        switchFragment(Contants.FragmentCon.TAG_HICAR_INIT_FAILED);
    }

    @Override
    public void onAccOff() {
        Log.i(TAG, "onAccOff");
        toLauncher();
    }

    @Override
    public void onOpenApp(byte[] data) {
        //content: {"RequestAppResp":{"RespCode":100000,"AppPackage":"com.huawei.hicar.travel","Description":"success"}}
        Log.i(TAG, "onOpenApp() data: " + new String(data));
        try {
            String content = new String(data);
            org.json.JSONObject jsonObject = new org.json.JSONObject(content);
            String requestAppRespStr = jsonObject.getString("RequestAppResp");
            Log.i(TAG, "onOpenApp() requestAppRespStr: " + requestAppRespStr);
            //jsonObject2
            org.json.JSONObject jsonObject2 = new org.json.JSONObject(requestAppRespStr);
            int RespCode = jsonObject2.getInt("RespCode");
            String Description = jsonObject2.getString("Description");
            //MediaData
            switch (RespCode) {
                case 100000:
                    Log.i(TAG, Description);
                    Intent intent = new Intent(this, HiCarMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                case 100001:
                case 100002:
                    Log.e(TAG, Description);
                    Toast.makeText(this, Description, Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /** @noinspection deprecation*/
    @Override
    public void onQuestShareNet(byte[] data) {
        Log.i(TAG, "onQuestShareNet() data: " + Arrays.toString(data));

        if (data != null) {
            String message = new String(data);
            Log.d(TAG, "onQuestShareNet() data: " + message);

            try {
                JSONObject jsonObject = new JSONObject(message);
                int command = jsonObject.getInt("command");
                int errorCode = jsonObject.getInt("errorCode");
                Log.d(TAG, "onQuestShareNet() command: " + command);

                //车机侧主动操作请求/结束共享上网
                if (command == 0) {
                    if (errorCode == 100000) {
                        Log.d(TAG, "onQuestShareNet() 请求开始/结束共享上网成功");
                        JSONObject dataJsonObj = jsonObject.getJSONObject("data");
                        String gateway = dataJsonObj.getString("gateway");
                        String dns = dataJsonObj.getString("dns");
                        Log.d(TAG, "onQuestShareNet() command: " + command + "，errorCode: "
                                + errorCode + "，gateway: " + gateway + "，dns: " + dns);

                        boolean putDateWayResult = Settings.System.putString(getContentResolver(), Settings.System.WIFI_STATIC_GATEWAY, gateway);
                        boolean putDnsResult = Settings.System.putString(getContentResolver(), Settings.System.WIFI_STATIC_DNS1, dns);
                        Log.d(TAG, putDateWayResult + "--" + putDnsResult);
                    } else if (errorCode == 100002) {
                        //用户未授权通过
                        Log.d(TAG, "onQuestShareNet() 用户未授权通过");
                    }
                } else if (command == 1) {
                    //手机侧主动结束共享上网
                    JSONObject dataJsonObj = jsonObject.getJSONObject("data");
                    int enable = dataJsonObj.getInt("enable");
                    if (enable == 0) {
                        //HiCar App通过onDataReceive回调方法发送停止共享上网信息
                        com.incall.apps.hicar.servicesdk.utils.CommonUtil.stopShareNetByUser();
                        com.incall.apps.hicar.servicesdk.utils.CommonUtil.deleteRouteAndDnsInfo(getContentResolver());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //启动手机互联
    @Override
    public void launchPhoneLink() {
        //判断Activity是否在前台
        boolean isForeground = CommonUtil.isActivityRunning(MyApplication.getContext(), "com.wt.phonelink");
        Log.d(TAG, "launchPhoneLink() isActivityRunning: " + isForeground);
        if (!isForeground) {
            return;
        }
        //hiCar是否连接
        boolean isHicarConnected = sharedPreferencesUtil.getBoolean(com.incall.apps.hicar.servicesdk.contants.Contants.SP_IS_HICAR_CONNECT);
        Log.d(TAG, "launchPhoneLink() isHicarConnected: " + isHicarConnected);
        //如果hiCar已经连接
        if (isHicarConnected) {
            //唤醒华为
            VoiceManager.getInstance().wakeUpHuawei(null, null);
            return;
        }
        //carLink是否连接
        boolean isCarLinkConnected = sharedPreferencesUtil.getBoolean(com.incall.apps.hicar.servicesdk.contants.Contants.SP_IS_CARLINK_CONNECT);
        Log.d(TAG, "launchPhoneLink() isCarLinkConnected: " + isCarLinkConnected);
        //如果carLink已经连接
        if (isCarLinkConnected) {
            //得到手机商标
            String phoneBrand = sharedPreferencesUtil.getString(com.incall.apps.hicar.servicesdk.contants.Contants.SP_PHONE_BRAND).toUpperCase();
            Log.d(TAG, "launchPhoneLink() phoneBrand: " + phoneBrand);
            //判断手机商标
            switch (phoneBrand) {
                case "OPPO":
                    VoiceManager.getInstance().wakeUpOPPO(null, null);
                    break;
                case "VIVO":
                    VoiceManager.getInstance().wakeUpVivo(null, null);
                    break;
                case "MI":
                    VoiceManager.getInstance().wakeUpXiaoMi(null, null);
                    break;
                default:
                    break;

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        if (!mainPresenter.isConnectedDevice()) {
            int originCapabilityValue = com.incall.apps.hicar.servicesdk.utils.CommonUtil.getIoCapability();
            sharedPreferencesUtil.putInt("capability", originCapabilityValue);
            Log.i(TAG, "onResume() old ioCapability: " + originCapabilityValue);
            //连接前和连接中   保持值为3
            com.incall.apps.hicar.servicesdk.utils.CommonUtil.setIoCapability(3);
            Log.d(TAG, "onResume()  new ioCapability: " + (com.incall.apps.hicar.servicesdk.utils.CommonUtil.getIoCapability()));
        }

        Contants.IS_FRONT = true;
        if (!mainPresenter.isConnectedDevice()) {
            Log.d(TAG, "onResume() isBtConnected: " + mainPresenter.isBtConnected());
            if (mainPresenter.isBtConnected()) {
                mainPresenter.startHicarAdv();
            } else {
                switchFragment(Contants.FragmentCon.TAG_BT_TIPS);
            }
        } else {
            Log.i(TAG, " onResume() hiCar IS_BACKGROUND: " + Contants.IS_BACKGROUND);
            Log.i(TAG, " onResume() hiCar isActivityRunning: " + (CommonUtil.isActivityRunning(MyApplication.getContext(), "com.wt.phoneLink")));
            //后台连接后，被拉起
            if (Contants.IS_BACKGROUND) {
                Contants.IS_BACKGROUND = false;
                switchFragment(Contants.FragmentCon.TAG_HICAR_SCREEN);
            }
        }
        Log.e(TAG, "onResume(), threadId: " + Thread.currentThread().getId());
        startHiCarDataCollect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHicarCallStatusEvent(HicarCallStatusFocusEvent statusEvent) {
        Log.d(TAG, "onHicarCallStatusEvent() statusEvent: " + statusEvent);
        Intent intent = new Intent(this, HiCarMainActivity.class);
        startActivity(intent);
    }

    private void startHiCarDataCollect() {
        mainPresenter.startHiCarDataCollect(com.incall.apps.hicar.servicesdk.contants.Contants.HICAR_START_MODE);
        if (com.incall.apps.hicar.servicesdk.contants.Contants.HICAR_START_MODE != 2) {
            com.incall.apps.hicar.servicesdk.contants.Contants.HICAR_START_MODE = 2;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        if (!mainPresenter.isConnectedDevice()) {
            //设置为保存的值
            int capability = sharedPreferencesUtil.getInt("capability", -1);
            Log.d(TAG, "onPause()  capability: " + capability);
            com.incall.apps.hicar.servicesdk.utils.CommonUtil.setIoCapability(capability);
            Log.d(TAG, "onPause() get capability: " + (com.incall.apps.hicar.servicesdk.utils.CommonUtil.getIoCapability()));
        }
        Contants.IS_FRONT = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Contants.IS_FRONT = false;
        Log.i(TAG, "onStop()");
        //没有连接成功，就直接关闭
        if (!HiCarServiceManager.getInstance().isConnectedDevice()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        //hicar已经退出情况下收到的hicar是否通话中还是1，导致车机这边一直抢不到焦点。需增加异常处理，在未收到华为回调的情况下，在一些时机重置通话中的状态为0。 KongJing 20210609
        SystemProperties.set("system.ca.hicar.tel.state", "0");
        EventBus.getDefault().unregister(this);
        mainPresenter.unRegister();
    }

    /**
     * 返回 AI 主页的 操作
     * 之前返回的是 appList的界面
     */
    public void toLauncher() {
        Log.d(TAG, "toLauncher()");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tinnove.launcher", "com.tinnove.applist.AppListActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * 必须配置  app配置或者系统配置： TODO：setCountryCode这个方法北斗系统里面没有，不知道会不会影响
     */
    private void addSvrMngServerToSmgr() {
        Log.i(TAG, "start initNeedBandServer");
        try {
            Method addService;
            Class<?> clazz = Class.forName("android.net.wifi.WifiManager");
            addService = clazz.getMethod("setCountryCode", String.class);
            addService.invoke(getApplicationContext().getSystemService(WIFI_SERVICE), "CN");
            Log.i(TAG, "addServiceToServiceManager success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "addServiceToServiceManager error: " + e.getMessage());
        }
    }

    private Fragment currentFragment;

    public synchronized void switchFragment(String tag) {
        Log.i(TAG, "switchFragment() tag: " + tag);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        Log.i(TAG, "switchFragment() fragment: " + fragment);
        Log.i(TAG, "switchFragment() currentFragment: " + currentFragment);
        if (fragment != null && currentFragment == fragment) {
            Log.i(TAG, "switchFragment() replace: " + fragment);
            transaction.replace(R.id.fragment_container, fragment, tag);
        }
        if (fragment == null) {
            fragment = getFragment(tag);
            //add by calijia 2021/11/18 DTM20211028000112 休眠唤醒后会异常地显示正在连接的动画
            transaction.replace(R.id.fragment_container, fragment, tag);
            Log.i(TAG, "switchFragment() transaction.add fragment: " + fragment);
            transaction.show(fragment);
        } else {
            Log.i(TAG, "switchFragment() show fragment: " + fragment);
            transaction.show(fragment);
        }
        //需要判断前后界面是否相同，不要隐藏了已经显示唯一界面的。 KongJing 2021.11.1
        if (null != currentFragment && currentFragment != fragment) {
            Log.i(TAG, "switchFragment() hide fragment: " + currentFragment);
            transaction.hide(currentFragment);
        }
        currentFragment = fragment;
        transaction.commitAllowingStateLoss();

    }



    private Fragment getFragment(String tag) {
        Fragment fragment = null;
        switch (tag) {
            //这个TAG表示HiCar投影的fragment
            case Contants.FragmentCon.TAG_HICAR_SCREEN:
                fragment = hiCarFragment;
                break;
            case Contants.FragmentCon.TAG_AP_CONNECT:
                fragment = apConnectFragment;
                break;
            case Contants.FragmentCon.TAG_HICAR_FAILED:
                fragment = connectFailedFragment;
                break;
            //蓝牙fragment
            case Contants.FragmentCon.TAG_BT_TIPS:
                fragment = btFragment;
                break;
            case Contants.FragmentCon.TAG_HICAR_INIT_FAILED:
                //初始化失败fragment
                fragment = initFailedFragment;
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Log.i(TAG, "onWindowFocusChanged() HiCar get the windows focus , hide navigation bar");
//            hideStatusNavigationBar();
        }
    }


    /**
     * 连接服务
     */
    private void getProfileProxy() {
        //设置蓝牙
        ProxyServiceListener listener = new ProxyServiceListener();
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(MyApplication.getContext(), listener, BluetoothProfile.HEADSET);
    }

    //Android系统的蓝牙监听
    public final static class ProxyServiceListener implements BluetoothProfile.ServiceListener {

        private static final String TAG = "PhoneLink/ProxyServiceListener";
        private BluetoothHeadsetClient mBluetoothHeadsetClient;

        //蓝牙已连接
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG, "onServiceConnected() Bluetooth service connected profile:" + profile);
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadsetClient = (BluetoothHeadsetClient) proxy;
                BTManager.getInstance().setBluetoothHeadsetClient(mBluetoothHeadsetClient);
            }
        }

        //蓝牙断开连接
        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadsetClient = null;
                BTManager.getInstance().setBluetoothHeadsetClient(null);
            }
        }
    }

    //启动手机互联
    //消息的发送是在HiCarManagerImpl的launchPhoneLink方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaunchPhoneLinkEvent(LaunchPhoneLinkEvent statusEvent) {
        Log.d(TAG, "onLaunchPhoneLinkEvent() statusEvent: " + statusEvent);
        //启动手机互联
        launchPhoneLink();
    }

}
