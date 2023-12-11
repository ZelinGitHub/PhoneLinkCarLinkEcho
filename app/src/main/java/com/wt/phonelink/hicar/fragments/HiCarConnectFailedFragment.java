package com.wt.phonelink.hicar.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.incall.apps.hicar.iview.IConnectFailedView;
import com.incall.apps.hicar.presenter.FailedPresenter;
import com.wt.phonelink.R;

//连接失败的fragment
public class HiCarConnectFailedFragment extends BaseFragment<FailedPresenter> implements IConnectFailedView {
    private static final String TAG = "WTWLink/ConnectFailedFragment";
    private Button hicarUsbConnectCancel;
    private Button hicarUsbConnectReset;
    private Button hicarUsbConnectCancelUsb;

    private TextView hicarPhoneName;
    private FailedPresenter failedPresenter;


    @Override
    protected int getlayout() {
        return R.layout.layout_usb_connect_failed;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        failedPresenter = basePresenter;
    }

    @Override
    protected void initViewRefs(View view) {
        hicarUsbConnectCancel = view.findViewById(R.id.hicar_usb_connect_cancel);
        hicarUsbConnectReset = view.findViewById(R.id.hicar_usb_connect_reset);
        hicarUsbConnectCancelUsb = view.findViewById(R.id.hicar_usb_connect_cancel_usb);
        hicarPhoneName = view.findViewById(R.id.hicar_phone_name);
    }

    @Override
    protected void initUI() {
        hicarUsbConnectCancel.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click hicarUsbConnectCancel");
            cancelConnect();
        });
        hicarUsbConnectCancelUsb.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click hicarUsbConnectCancelUsb");
            cancelConnect();
        });
        hicarUsbConnectReset.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click hicarUsbConnectReset");
            resetConnect();
        });
        setPhoneNameText();
    }

    private void resetConnect() {
        Log.d(TAG, "resetConnect()");
    }

    @Override
    public void onHome() {
        Log.i(TAG, "onHome()");
        cancelConnect();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setPhoneNameText();
        }
    }

    private void setPhoneNameText() {
        hicarPhoneName.setText(getString(R.string.huawei_hicar_connect_failed_title, failedPresenter.getPhoneName()));
    }

    @Override
    protected FailedPresenter initPresenter() {
        return new FailedPresenter();
    }

    private void cancelConnect() {
        failedPresenter.disConnected();
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

}
