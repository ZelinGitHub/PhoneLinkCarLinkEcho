package com.wt.phonelink;

import static Contants.IS_WTBOX_FRONT;

import android.car.Car;
import android.car.hardware.power.CarPowerManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.openos.skin.WTSkinManager;
import com.openos.skin.info.SkinInfo;
import com.tinnove.hihonor.HiHonorActivity;
import com.tinnove.hihonor.link.HiHonorManager;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.wt.phonelink.adapter.LinkEntryAdapter;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.constant.Constants;
import com.wt.phonelink.decration.EntriesDecoration;
import com.wt.phonelink.hicar.HiCarMainActivity;
import com.wt.phonelink.utils.CommonUtil;
import com.wt.phonelink.utils.VoiceUtils;

/**
 * @Author: LuoXia
 * @Date: 2022/9/26 15:09
 * @Description:
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, LinkEntryAdapter.OnItemClickListener {

    private static final String TAG = "WTWLink/MainActivity";
    //    private ImageView /*ivWtBox,*/ ivHiCar, ivCarLink,ivHiHonor;
    private SharedPreferencesUtil sp;
    private ConstraintLayout clMainBg;
    private CarPowerManager carPowerManager;

    private RecyclerView mEntriesRV;

    //皮肤更改监听器（白天黑夜模式）
    WTSkinManager.SkinChangedListener skinChangedListener = new WTSkinManager.SkinChangedListener() {
        @Override
        public void onSkinChanged(SkinInfo skinInfo, @NonNull SkinInfo skinInfo1) {
            Log.i(TAG, "onThemeChanged() ");
            //资源需要如下方法重新从sdk获取刷新即可
//            ivWtBox.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_wt_box));
//            ivHiHonor.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_wt_box));
//            ivHiCar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_hicar));
//            ivCarLink.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.bg_carlink));
            clMainBg.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.background));

            //白天黑夜模式
            boolean nightMode = skinInfo.getSkinParams().getBoolean("NightMode", false);
            Log.i(TAG, "onThemeChanged nightMode: " + nightMode);

            //hiCar是否连接
            boolean isHiCarConnect = sp.getBoolean(Contants.SP_IS_HICAR_CONNECT);
            //carLink是否连接
            boolean isCarLinkConnect = sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT);
            Log.i(TAG, "onThemeChanged isHiCarConnect: " + isHiCarConnect + "，isCarLinkConnect: " + isCarLinkConnect);

            //如果hiCar已经连接
            if (isHiCarConnect) {
                byte[] bytes = nightMode ? com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("night") : com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("day");
                HiCarServiceManager.getInstance().sendCarData(Contants.HiCarCons.DATA_TYPE_DAY_NIGHT_MODE, bytes);
                return;
            }
            //如果carLink已经连接
            if (isCarLinkConnect) {
                if (nightMode) {
                    UCarAdapter.getInstance().notifySwitchDayOrNight(UCarCommon.DayNightMode.DAY_MODE);
                } else {
                    UCarAdapter.getInstance().notifySwitchDayOrNight(UCarCommon.DayNightMode.DAY_MODE);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, " onCreate() ");
        initView();
        initData();
        WTSkinManager.get().addSkinChangedListener(skinChangedListener);
    }


    private void initView() {
//        ivWtBox = findViewById(R.id.iv_wt_box);
//        ivHiHonor = findViewById(R.id.iv_hihonor);
//        ivHiCar = findViewById(R.id.iv_hicar);
//        ivCarLink = findViewById(R.id.iv_carlink);
        clMainBg = findViewById(R.id.cl_main_bg);
        mEntriesRV = findViewById(R.id.rv_entries);
        mEntriesRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        LinkEntryAdapter adapter = new LinkEntryAdapter();
        mEntriesRV.addItemDecoration(new EntriesDecoration());
        mEntriesRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

//        ivHiHonor.setOnClickListener(this);
//        ivWtBox.setOnClickListener(this);
//        ivHiCar.setOnClickListener(this);
//        ivCarLink.setOnClickListener(this);
// 注册单独监听，这个回调会在activity启动之后再按注册顺序触发，同时请注意在适当的场景调用
    }

    private void initData() {
        sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());

        Car car = Car.createCar(this);
        carPowerManager = (CarPowerManager) car.getCarManager(android.car.Car.POWER_SERVICE);
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "set carPowerManager Listener");
                carPowerManager.setListener(mCarPowerStateListener);
            }
        }.start();
    }

    //点击事件，点击进入hicar或carlink连接
    @Override
    public void onClick(View v) {
//        Log.d(TAG, "onClick() " + (System.currentTimeMillis() - resumeTime));
//        if (System.currentTimeMillis() - resumeTime < 700) {
//            Log.d(TAG, "此时可能界面还未加载完成  暂不支持点击");
//            return;
//        }
//        switch (v.getId()) {
//            case R.id.iv_wt_box:
////
////                adb shell am start -n com.tinnove.link.client/.QrCodeActivity
//                Intent intent = new Intent();
//                ComponentName componentName = new ComponentName("com.tinnove.link.client", "com.tinnove.link.client.QrCodeActivity");
//                intent.setComponent(componentName);
//                intent.putExtra("pkg", "com.wt.phonelink");
//                try {
//                    startActivity(intent);
//                } catch (Exception e) {
//                    Log.e("跳转手机盒子发生异常！！", e.toString());
//                    Toast.makeText(this, "没有安装手机盒子！", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.iv_hicar:
//                startActivity(new Intent(this, HiCarMainActivity.class));
//                HiHonorManager.getInstance().deInit();
//                break;
//            case R.id.iv_carlink:
//                startActivity(new Intent(this, CarLinkMainActivity.class));
//                HiHonorManager.getInstance().deInit();
//                break;
//            case R.id.iv_hihonor:
//                startActivity(new Intent(this, HiHonorActivity.class));
//                break;
//            default:
//                break;
//        }
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
        Contants.IS_PHONE_LINK_FRONT = true;

        boolean isWTBoxConnect = sp.getBoolean(Contants.SP_IS_WTBOX_CONNECT);
        Log.e(TAG, "onResume() isWTBoxConnect: " + isWTBoxConnect);
        if (isWTBoxConnect) {
            Log.e(TAG, "onResume() 手机盒子已经启动，关闭手机互联");
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        Contants.IS_PHONE_LINK_FRONT = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        Contants.IS_PHONE_LINK_FRONT = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        CommonUtil.setGlobalProp(Contants.SYS_IS_CARLINK_CONNECT, 0);
        Contants.IS_PHONE_LINK_FRONT = false;
        VoiceUtils.getInstance().stopOrResumeVr(true);
        carPowerManager.clearListener();
        WTSkinManager.get().removeSkinChangedListener(skinChangedListener);//防止内存泄漏
    }

    private void initLayoutActivity() {
        boolean isHiCarConnect = sp.getBoolean(Contants.SP_IS_HICAR_CONNECT);
        Log.d(TAG, "initLayoutActivity() isHiCarConnect: " + isHiCarConnect);
        if (isHiCarConnect) {
            startActivity(new Intent(this, HiCarMainActivity.class));
            return;
        }

        boolean isCarLinkConnect = sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT);
        Log.d(TAG, "initLayoutActivity() isCarLinkConnect: " + isCarLinkConnect);
        if (isCarLinkConnect) {
            startActivity(new Intent(this, CarLinkMainActivity.class));
            return;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(TAG, "onConfigurationChanged() ");
    }

    //车电源状态管理器
    private CarPowerManager.CarPowerStateListener mCarPowerStateListener = powerState -> {

        Log.i(TAG, "Power State Changed() powerState: " + powerState);

        switch (powerState) {
            //休眠后
            case CarPowerManager.CarPowerStateListener.SHUTDOWN_PREPARE:
                //从sp取值。判断hiCar当前是否连接
                boolean isHiCarConnect = SharedPreferencesUtil.getInstance(MyApplication.getContext()).getBoolean(Contants.SP_IS_HICAR_CONNECT, false);
                //从sp取值。判断carLink当前是否连接。
                boolean isCarLinkConnect = SharedPreferencesUtil.getInstance(MyApplication.getContext()).getBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
                Log.i(TAG, "Power State Changed() isHiCarConnect：" + isHiCarConnect + " ,isCarLinkConnect: " + isCarLinkConnect);
                //车机休眠后断开主动连接，避免引起系统深度休眠（STR）
                HiCarServiceManager.getInstance().disconnectDevice();
                break;
            //未休眠
            case CarPowerManager.CarPowerStateListener.ON:
                break;
            default:
                break;
        }
    };


    @Override
    public void onItemClick(String linkType, View view) {
        if (System.currentTimeMillis() - resumeTime < 700) {
            Log.d(TAG, "此时可能界面还未加载完成  暂不支持点击");
            return;
        }
        switch (linkType) {
            case Constants.LINK_TYPE_TINNOVE_BOX: // 手机盒子
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
            case Constants.LINK_TYPE_HICAR: // hicar
                startActivity(new Intent(this, HiCarMainActivity.class));
                HiHonorManager.getInstance().deInit();
                break;
            case Constants.LINK_TYPE_ICCOA: // carlink
                startActivity(new Intent(this, CarLinkMainActivity.class));
                HiHonorManager.getInstance().deInit();
                break;
            case Constants.LINK_TYPE_HIHONOR: // 荣耀
                startActivity(new Intent(this, HiHonorActivity.class));
                break;
            default:
                break;
        }
    }
}
