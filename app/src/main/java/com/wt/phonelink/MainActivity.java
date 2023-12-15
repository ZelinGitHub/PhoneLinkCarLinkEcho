package com.wt.phonelink;

import static com.incall.apps.hicar.servicesdk.contants.Constants.IS_WTBOX_FRONT;

import android.car.Car;
import android.car.hardware.power.CarPowerManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.incall.apps.hicar.servicesdk.PhoneLinkStateManager;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.openos.skin.WTSkinManager;
import com.openos.skin.info.SkinInfo;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.hicar.HiCarMainActivity;
import com.wt.phonelink.utils.VoiceUtils;

import wtcl.lib.theme.WTThemeManager;
import wtcl.lib.widget.WTTitleBar;

/**
 * Author: LuoXia
 * Date: 2022/9/26 15:09
 * Description:
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "WTWLink/MainActivity";
    private ImageView ivWtBox, ivHiCar, ivCarLink;
    private WTTitleBar v_close;
    private SharedPreferencesUtil sp;
    private ConstraintLayout clMainBg;
    private CarPowerManager carPowerManager;

    //皮肤更改监听器（白天黑夜模式）
    WTSkinManager.SkinChangedListener skinChangedListener = new WTSkinManager.SkinChangedListener() {
        @Override
        public void onSkinChanged(SkinInfo newInfo, @NonNull SkinInfo previousInfo) {
            Log.i(TAG, "onSkinChanged() ");
            getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
            updateSkin();
            //白天黑夜模式
            boolean nightMode = newInfo.getSkinParams().getBoolean("NightMode", false);
            Log.i(TAG, "onSkinChanged() nightMode: " + nightMode);
            //hiCar是否连接
            boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
            //carLink是否连接
            boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
            Log.d(TAG, "onSkinChanged() isHiCarConnect: " + isHiCarConnect + "，isCarLinkConnect: " + isCarLinkConnect);
            //如果hiCar已经连接
            if (isHiCarConnect) {
                byte[] bytes = nightMode ? com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("night")
                        : com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("day");
                HiCarServiceManager.getInstance().sendCarData(Constants.HiCarCons.DATA_TYPE_DAY_NIGHT_MODE, bytes);
                return;
            }
            //如果carLink已经连接
            if (isCarLinkConnect) {
                UCarAdapter.getInstance().notifySwitchDayOrNight(UCarCommon.DayNightMode.DAY_MODE);
            }
        }
    };

    private void updateSkin() {
        //资源需要如下方法重新从sdk获取刷新即可
        ivWtBox.setBackground(WTSkinManager.get().getDrawable(R.drawable.bg_wt_box));
        ivHiCar.setBackground(WTSkinManager.get().getDrawable(R.drawable.bg_hicar));
        ivCarLink.setBackground(WTSkinManager.get().getDrawable(R.drawable.bg_carlink));
        clMainBg.setBackgroundColor(WTSkinManager.get().getColor(R.color.background));
        String packageName = WTSkinManager.get().getCurrentSkinInfo().getSkinPackageName();
        Log.d(TAG, "updateSkin() packageName: " + packageName);
        WTThemeManager.setSkinPkgName(packageName);
        WTThemeManager.setResources(WTSkinManager.get().getProxyResources());
        v_close.applyTheme();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, " onCreate() ");
        initView();
        initData();
    }


    private void initView() {
        v_close = findViewById(R.id.v_close);
        ivWtBox = findViewById(R.id.iv_wt_box);
        ivHiCar = findViewById(R.id.iv_hicar);
        ivCarLink = findViewById(R.id.iv_carlink);
        clMainBg = findViewById(R.id.cl_main_bg);
        ivWtBox.setOnClickListener(this);
        ivHiCar.setOnClickListener(this);
        ivCarLink.setOnClickListener(this);
// 注册单独监听，这个回调会在activity启动之后再按注册顺序触发，同时请注意在适当的场景调用
        v_close.setOnClickListener(v -> {
            Log.i(TAG, "onClick click close btn");
            //进入后台
            moveTaskToBack(true);
        });
    }

    /**
     * 返回 AI 主页的 操作
     */
    public void startAppListActivity() {
        Log.d(TAG, "startAppListActivity()");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tinnove.launcher", "com.tinnove.applist.AppListActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void initData() {
        WTSkinManager.get().addSkinChangedListener(skinChangedListener);
        getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        Car car = Car.createCar(this);
        carPowerManager = (CarPowerManager) car.getCarManager(Car.POWER_SERVICE);
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.i(TAG, "initData() 注册carPowerManager！");
                carPowerManager.setListener(mCarPowerStateListener);
            }
        }.start();
        updateSkin();
    }

    //点击事件，点击进入hicar或carlink连接
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick() " + (System.currentTimeMillis() - resumeTime));
        if (System.currentTimeMillis() - resumeTime < 300) {
            Log.d(TAG, "此时可能界面还未加载完成  暂不支持点击");
            return;
        }
        switch (v.getId()) {
            case R.id.iv_wt_box:
//                adb shell am start -n com.tinnove.link.client/.QrCodeActivity
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName("com.tinnove.link.client", "com.tinnove.link.client.QrCodeActivity");
                intent.setComponent(componentName);
                intent.putExtra("pkg", "com.wt.phonelink");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("跳转手机盒子发生异常！！", e.toString());
                    Toast.makeText(this, "没有安装手机盒子！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_hicar:
                startActivity(new Intent(this, HiCarMainActivity.class));
                break;
            case R.id.iv_carlink:
                startActivity(new Intent(this, CarLinkMainActivity.class));
                break;
            default:
                break;
        }
    }

    private long resumeTime = 0;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        resumeTime = System.currentTimeMillis();
        Log.d(TAG, "onResume() resumeTime: " + resumeTime);
        initLayoutActivity();
        Constants.IS_PHONE_LINK_FRONT = true;
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        Log.e(TAG, "onResume() isWTBoxConnect: " + isWTBoxConnect);
        if (isWTBoxConnect) {
            Log.e(TAG, "onResume() 手机盒子已经启动，关闭手机互联");
            //进入后台
            moveTaskToBack(true);
            if (!IS_WTBOX_FRONT) {
                //启动wtBox主activity
                // adb shell am start -n com.tinnove.link.client/.QrCodeActivity
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName("com.tinnove.link.client", "com.tinnove.link.client.QrCodeActivity");
                intent.setComponent(componentName);
                intent.putExtra("pkg", "com.wt.phonelink");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("跳转手机盒子发生异常！！", e.toString());
                    Toast.makeText(this, "没有安装手机盒子！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initLayoutActivity() {
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        Log.i(TAG, "initLayoutActivity() isHiCarConnect: " + isHiCarConnect);
        Log.i(TAG, "initLayoutActivity() isCarLinkConnect: " + isCarLinkConnect);
        if (isHiCarConnect) {
            startActivity(new Intent(this, HiCarMainActivity.class));
        } else if (isCarLinkConnect) {
            startActivity(new Intent(this, CarLinkMainActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        Constants.IS_PHONE_LINK_FRONT = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        Constants.IS_PHONE_LINK_FRONT = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Constants.IS_PHONE_LINK_FRONT = false;
        VoiceUtils.getInstance().stopOrResumeVr(true);
        carPowerManager.clearListener();
        WTSkinManager.get().removeSkinChangedListener(skinChangedListener);//防止内存泄漏
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged() newConfig: " + newConfig);
    }

    //车电源状态管理器
    private final CarPowerManager.CarPowerStateListener mCarPowerStateListener = powerState -> {
        Log.i(TAG, "Power state changed, powerState: " + powerState);
        switch (powerState) {
            //休眠后
            case CarPowerManager.CarPowerStateListener.SHUTDOWN_PREPARE:
                Log.i(TAG, "Power state changed, powerState is CarPowerManager.CarPowerStateListener.SHUTDOWN_PREPARE");
                Log.i(TAG, "Power state changed, 车机已经休眠");
                //车机休眠后断开主动连接，避免引起系统深度休眠（STR）
                PhoneLinkStateManager.disconnectDevice(getApplicationContext());
                break;
            //未休眠
            case CarPowerManager.CarPowerStateListener.ON:
                Log.i(TAG, "Power state changed, powerState is CarPowerManager.CarPowerStateListener.ON");
                Log.i(TAG, "Power state changed, 车机未休眠");
                break;
            default:
                break;
        }
    };


}
