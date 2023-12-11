package com.wt.phonelink;

import static Contants.IS_WTBOX_FRONT;
import static com.wt.phonelink.ScreenStatusMonitor.TYPE_ALL_HIDE;
import static com.wt.phonelink.ScreenStatusMonitor.TYPE_LEFT_SHOW;
import static com.wt.phonelink.ScreenStatusMonitor.TYPE_RIGHT_SHOW;

import android.annotation.SuppressLint;
import android.car.Car;
import android.car.hardware.power.CarPowerManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.incall.apps.hicar.servicesdk.PhoneLinkStateManager;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.openos.skin.WTSkinManager;
import com.openos.skin.info.SkinInfo;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.constant.Constants;
import com.wt.phonelink.decration.EntriesDecoration;
import com.wt.phonelink.hicar.HiCarMainActivity;
import com.wt.phonelink.utils.VoiceUtils;

import wtcl.lib.theme.WTThemeManager;
import wtcl.lib.widget.WTTitleBar;

/**
 * Author: LuoXia
 * Date: 2022/9/26 15:09
 * Description:
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WTPhoneLink/MainActivity";
    private MotionLayout motion_layout;
    private RecyclerView mEntriesRV;
    private WTTitleBar v_close;
    private SharedPreferencesUtil sp;
    private CarPowerManager carPowerManager;
    private long resumeTime = 0;

    private static final int TITLE_LEFT_MARGIN_BIG = 64;
    private static final int TITLE_LEFT_MARGIN_SMALL = 44;

    private final EntriesDecoration mEntriesDecoration = new EntriesDecoration();
    private final LinkEntryAdapter mAdapter = new LinkEntryAdapter();
    //屏幕状态监听器
    ScreenStatusMonitor.OnScreenStatusChangedListener mListener = new ScreenStatusMonitor.OnScreenStatusChangedListener() {
        ///全半屏切换监听
        @Override
        public void onChanged(int status) {
            Log.d(TAG, "onChanged: " + status);
            if (motion_layout == null) {
                return;
            }
            //左半屏弹出侧边栏，布局缩到右侧
            if (status == TYPE_LEFT_SHOW) {
                //motionLayout过渡
                motion_layout.transitionToState(R.id.right, 500);
            }
            //全屏
            else if (status == TYPE_ALL_HIDE) {
                //motionLayout过渡到全屏
                motion_layout.transitionToState(R.id.full, 500);
            }
            //右半屏弹出侧边栏，布局缩到左侧
            else if (status == TYPE_RIGHT_SHOW) {
                //motionLayout过渡
                motion_layout.transitionToState(R.id.left, 500);
            }
            changeUI(status);
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void changeUI(int status) {
        // TODO: 2023/10/23
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) v_close.getLayoutParams();
        if (status == 0) {
            mEntriesRV.removeItemDecoration(mEntriesDecoration);
            mEntriesDecoration.setMargin(EntriesDecoration.MARGIN_BIG);
            mEntriesRV.addItemDecoration(mEntriesDecoration);
            mAdapter.setItemWidth(LinkEntryAdapter.ITEM_WIDTH_BIG);
            mAdapter.notifyDataSetChanged();
            marginLayoutParams.leftMargin = TITLE_LEFT_MARGIN_BIG;
        } else {
            mEntriesRV.removeItemDecoration(mEntriesDecoration);
            mEntriesDecoration.setMargin(EntriesDecoration.MARGIN_SMALL);
            mEntriesRV.addItemDecoration(mEntriesDecoration);
            mAdapter.setItemWidth(LinkEntryAdapter.ITEM_WIDTH_SMALL);
            mAdapter.notifyDataSetChanged();
            marginLayoutParams.leftMargin = TITLE_LEFT_MARGIN_SMALL;
        }
        v_close.setLayoutParams(marginLayoutParams);
    }

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
            boolean isHiCarConnect = sp.getBoolean(Contants.SP_IS_HICAR_CONNECT);
            //carLink是否连接
            boolean isCarLinkConnect = sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT);
            Log.d(TAG, "onSkinChanged() isHiCarConnect: " + isHiCarConnect + "，isCarLinkConnect: " + isCarLinkConnect);
            //如果hiCar已经连接
            if (isHiCarConnect) {
                byte[] bytes = nightMode ? com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("night")
                        : com.incall.apps.hicar.servicesdk.utils.CommonUtil.getDayNightMode("day");
                HiCarServiceManager.getInstance().sendCarData(Contants.HiCarCons.DATA_TYPE_DAY_NIGHT_MODE, bytes);
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
        motion_layout.setBackgroundColor(WTSkinManager.get().getColor(R.color.background));
        String packageName = WTSkinManager.get().getCurrentSkinInfo().getSkinPackageName();
        Log.d(TAG, "updateSkin() packageName: " + packageName);
        WTThemeManager.setSkinPkgName(packageName);
        WTThemeManager.setResources(WTSkinManager.get().getProxyResources());
        v_close.applyTheme();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " onCreate() ");
        setContentView(R.layout.activity_main);
        sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        WTSkinManager.get().addSkinChangedListener(skinChangedListener);

        initViewsRefs();
        initUI();

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
    }


    private void initViewsRefs() {
        mEntriesRV = findViewById(R.id.rv_entries);
        v_close = findViewById(R.id.v_close);
        motion_layout = findViewById(R.id.motion_layout);
    }


    private void initUI() {
        getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        //adapter
        mAdapter.initMainItemBeanArrayList(getApplicationContext());
        int status = ScreenStatusMonitor.getInstance(this).getStatus();
        if (status == TYPE_ALL_HIDE) {
            mAdapter.setItemWidth(LinkEntryAdapter.ITEM_WIDTH_BIG);
        } else {
            mAdapter.setItemWidth(LinkEntryAdapter.ITEM_WIDTH_SMALL);
        }
        mAdapter.setOnItemClickListener((linkType, view) -> {
            if (System.currentTimeMillis() - resumeTime < 300) {
                Log.e(TAG, "此时可能界面还未加载完成  暂不支持点击");
                return;
            }
            switch (linkType) {
                // 手机盒子
                case Constants.LINK_TYPE_TINNOVE_BOX:
                    toTongBox();
                    break;
                // HiCar
                case Constants.LINK_TYPE_HICAR:
                    toHiCar();
                    break;
                // CarLink
                case Constants.LINK_TYPE_ICCOA:
                    toCarLink();
                    break;
                default:
                    break;
            }
        });
        mEntriesRV.setAdapter(mAdapter);
        //decoration
        if (status == TYPE_ALL_HIDE) {
            mEntriesDecoration.setMargin(EntriesDecoration.MARGIN_BIG);
        } else {
            mEntriesDecoration.setMargin(EntriesDecoration.MARGIN_SMALL);
        }
        mEntriesRV.addItemDecoration(mEntriesDecoration);
        //layoutManager
        mEntriesRV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        // 注册单独监听，这个回调会在activity启动之后再按注册顺序触发，同时请注意在适当的场景调用
        v_close.setOnClickListener(v -> {
            Log.i(TAG, "onClick click close btn");
            //进入后台
            moveTaskToBack(true);
        });
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) v_close.getLayoutParams();
        if (status == 0) {
            marginLayoutParams.leftMargin = TITLE_LEFT_MARGIN_BIG;
        } else {
            marginLayoutParams.leftMargin = TITLE_LEFT_MARGIN_SMALL;
        }
        v_close.setLayoutParams(marginLayoutParams);
        updateSkin();
    }

    private void toCarLink() {
        Intent intent = new Intent(MainActivity.this, CarLinkMainActivity.class);
        startActivity(intent);
    }

    private void toHiCar() {
        Intent intent = new Intent(MainActivity.this, HiCarMainActivity.class);
        startActivity(intent);
    }

    private void toTongBox() {
        //adb shell am start -n com.tinnove.link.client/.QrCodeActivity
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.tinnove.link.client", "com.tinnove.link.client.QrCodeActivity");
        intent.setComponent(componentName);
        intent.putExtra("pkg", "com.wt.phonelink");
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e("跳转手机盒子发生异常！！", e.toString());
            Toast.makeText(MainActivity.this, "没有安装手机盒子！", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        //初始化屏幕状态（左半屏、全屏还是右半屏）
        initMotionLayout();
        resumeTime = System.currentTimeMillis();
        Log.d(TAG, "onResume() resumeTime: " + resumeTime);
        initLayoutActivity();
        Contants.IS_PHONE_LINK_FRONT = true;
        boolean isWTBoxConnect = sp.getBoolean(Contants.SP_IS_WTBOX_CONNECT);
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
        //注册屏幕状态监听
        ScreenStatusMonitor.getInstance(this).addListener(mListener);
    }

    //初始化屏幕状态
    private void initMotionLayout() {
        //得到屏幕状态
        //主动获取Settings中保存的全半屏的状态，更新mStatus，通知所有监听器状态更新
        int status = ScreenStatusMonitor.getInstance(this).getStatus();
        //当前是左半屏弹出状态栏
        if (status == TYPE_LEFT_SHOW) {
            //motionLayout设置为右半屏
            motion_layout.jumpToState(R.id.right);
        }
        //当前是全屏
        else if (status == TYPE_ALL_HIDE) {
            //motionLayout设置为全屏
            motion_layout.jumpToState(R.id.full);
        }
        //当前是右半屏弹出状态栏
        else if (status == TYPE_RIGHT_SHOW) {
            //motionLayout设置为左半屏
            motion_layout.jumpToState(R.id.left);
        }
    }

    private void initLayoutActivity() {
        boolean isHiCarConnect = sp.getBoolean(Contants.SP_IS_HICAR_CONNECT);
        boolean isCarLinkConnect = sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT);
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
        Contants.IS_PHONE_LINK_FRONT = false;
        //注销屏幕状态监听
        ScreenStatusMonitor.getInstance(this).removeListener(mListener);
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
        Contants.IS_PHONE_LINK_FRONT = false;
        VoiceUtils.getInstance().stopOrResumeVr(true);
        carPowerManager.clearListener();
        WTSkinManager.get().removeSkinChangedListener(skinChangedListener);//防止内存泄漏
        ScreenStatusMonitor.getInstance(this).stopMonitor();
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
