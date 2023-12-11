package com.wt.phonelink.hicar.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.incall.apps.commoninterface.util.LogUtil;
import com.incall.apps.hicar.iview.IAPConnetcView;
import com.incall.apps.hicar.presenter.ApConnectPresenter;
import com.incall.apps.hicar.servicesdk.manager.RxIntervalManager;
import com.openos.skin.WTSkinManager;
import com.wt.phonelink.MainActivity;
import com.wt.phonelink.R;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import wtcl.lib.theme.WTThemeManager;
import wtcl.lib.widget.WTButton;

//输入连接码的fragment
public class HiCarAPConnectFragment extends BaseFragment<ApConnectPresenter> implements IAPConnetcView {
    private static final String TAG = "WTWLink/APConnectFragment";
    private static final long FRESH_PIN_INTERVAL = 60 * 1000;
    private PinHandler mPinHandler;
    //presenter
    private ApConnectPresenter mApConnectPresenter;
    //连接码第1位数字
    private Button mHicarCodeValueText1Btn;
    //连接码第2位数字
    private Button mHicarCodeValueText2Btn;
    //连接码第3位数字
    private Button mHicarCodeValueText3Btn;
    //连接码第4位数字
    private Button mHicarCodeValueText4Btn;
    //连接码第5位数字
    private Button mHicarCodeValueText5Btn;
    //连接码第6位数字
    private Button mHicarCodeValueText6Btn;
    //取消连接
    private WTButton mHicarBtnCancelBtn;

    private View v_background;

    WTSkinManager.SkinChangedListener mSkinChangedListener = (newInfo, previousInfo) -> {
        Log.i(TAG, "onSkinChanged() ");
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        }
        updateSkin();
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach() context: " + context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected int getlayout() {
        return R.layout.layout_hicar_connect;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApConnectPresenter = basePresenter;
        //发送广播之前，断开蓝牙设备    如果有设备已经连接上了的话。
        mApConnectPresenter.disconnectBlueTooth();
        //暂停回连轮训 KongJing 2021.12.29
        RxIntervalManager.getInstance().pause();
        WTSkinManager.get().addSkinChangedListener(mSkinChangedListener);
        if (mPinHandler == null) {
            mPinHandler = new PinHandler(this, Looper.getMainLooper());
        }
        if (mPinHandler.hasMessages(1)) {
            mPinHandler.removeMessages(1);
        }
    }

    @Override
    protected void initViewRefs(View view) {
        mHicarCodeValueText1Btn = view.findViewById(R.id.hicar_code_value_text1);
        mHicarCodeValueText2Btn = view.findViewById(R.id.hicar_code_value_text2);
        mHicarCodeValueText3Btn = view.findViewById(R.id.hicar_code_value_text3);
        mHicarCodeValueText4Btn = view.findViewById(R.id.hicar_code_value_text4);
        mHicarCodeValueText5Btn = view.findViewById(R.id.hicar_code_value_text5);
        mHicarCodeValueText6Btn = view.findViewById(R.id.hicar_code_value_text6);
        v_background = view.findViewById(R.id.v_background);
        mHicarBtnCancelBtn = view.findViewById(R.id.hicar_btn_cancel);
    }
    @Override
    protected void initUI() {
        mHicarBtnCancelBtn.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click btnCancel");
            mApConnectPresenter.stopHiCarAdv();
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.onBackPressed();//直接finish掉activity了--Ondestory
            }
        });
        //连接码是从MainServiceImpl对象中得到的
        //调用MainServiceImpl的callMethodSync方法
        String pinCode = mApConnectPresenter.getPinCode();
        setPinCode(pinCode);
        updateSkin();
        mPinHandler.sendEmptyMessageDelayed(1, FRESH_PIN_INTERVAL);
    }

    private void updateSkin() {
        String packageName = WTSkinManager.get().getCurrentSkinInfo().getSkinPackageName();
        LogUtil.d(TAG, "updateSkin() packageName: " + packageName);
        WTThemeManager.setSkinPkgName(packageName);
        WTThemeManager.setResources(WTSkinManager.get().getProxyResources());
        mHicarBtnCancelBtn.applyTheme();
        mHicarBtnCancelBtn.setTextColor(WTSkinManager.get().getColor(R.color.app_name_color));
        v_background.setBackgroundColor(WTSkinManager.get().getColor(R.color.background));
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged() hidden:" + hidden);
        if (mPinHandler.hasMessages(1)) {
            mPinHandler.removeMessages(1);
        }
        if (!hidden) {
            mPinHandler.sendEmptyMessageDelayed(1, FRESH_PIN_INTERVAL);
        }
    }


    @Override
    protected ApConnectPresenter initPresenter() {
        return new ApConnectPresenter();
    }


    //设置连接码
    @SuppressLint("SetTextI18n")
    private void setPinCode(String pinCode) {
        if (pinCode.length() == 6) {
            mHicarCodeValueText1Btn.setText(pinCode.charAt(0) + "");
            mHicarCodeValueText2Btn.setText(pinCode.charAt(1) + "");
            mHicarCodeValueText3Btn.setText(pinCode.charAt(2) + "");
            mHicarCodeValueText4Btn.setText(pinCode.charAt(3) + "");
            mHicarCodeValueText5Btn.setText(pinCode.charAt(4) + "");
            mHicarCodeValueText6Btn.setText(pinCode.charAt(5) + "");
        }
    }

    //MVP，连接码改变
    @Override
    public void onPinCodeChange(String code) {
        Log.i(TAG, "onPinCodeChange() code: " + code);
        setPinCode(code);
        if (mPinHandler.hasMessages(1)) {
            mPinHandler.removeMessages(1);
        }
        mPinHandler.sendEmptyMessageDelayed(1, FRESH_PIN_INTERVAL);
        //发送广播之前，断开蓝牙设备    如果有设备已经连接上了的话。
        mApConnectPresenter.disconnectBlueTooth();
    }

    /**
     * 发送ble 蓝牙广播
     */
    private void startAdv() {
        //发送广播之前，断开蓝牙设备。如果有设备已经连接上了的话。
        mApConnectPresenter.disconnectBlueTooth();
        //启动蓝牙设备扫描
        mApConnectPresenter.startHicarAdv();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WTSkinManager.get().removeSkinChangedListener(mSkinChangedListener);//防止内存泄漏
        if (mPinHandler != null) {
            mPinHandler.removeCallbacksAndMessages(null);
            mPinHandler = null;
        }
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    static class PinHandler extends Handler {
        private final WeakReference<HiCarAPConnectFragment> mAPConnectFragment;

        private PinHandler(HiCarAPConnectFragment fragment, Looper looper) {
            super(looper);
            this.mAPConnectFragment = new WeakReference<>(fragment);
        }

        //定时改变连接码数字
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HiCarAPConnectFragment apConnectFragment = mAPConnectFragment.get();
            if (apConnectFragment == null) {
                return;
            }
            Log.i(TAG, "handleMessage() " +
                    "fragment.isResumed: " + apConnectFragment.isResumed()
                    + ", fragment.isVisible: " + apConnectFragment.isVisible()
            );
            //如果fragment可见，并且resume
            if (apConnectFragment.isVisible()
                    && apConnectFragment.isResumed()
            ) {
                if (msg.what == 1) {
                    //开始低功耗蓝牙的广播。核心代码就这一句。
                    apConnectFragment.startAdv();
                    PinHandler pinHandler = apConnectFragment.mPinHandler;
                    if(pinHandler==null){
                        return;
                    }
                    if (pinHandler.hasMessages(1)) {
                        pinHandler.removeMessages(1);
                    }
                    pinHandler.sendEmptyMessageDelayed(1, FRESH_PIN_INTERVAL);
                }
            }

        }
    }


}
