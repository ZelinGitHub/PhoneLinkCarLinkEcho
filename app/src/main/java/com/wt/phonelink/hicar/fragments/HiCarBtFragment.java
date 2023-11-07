package com.wt.phonelink.hicar.fragments;

import android.view.View;

import com.incall.apps.hicar.presenter.BTPresenter;
import com.wt.phonelink.R;
//蓝牙开启中fragment
public class HiCarBtFragment extends BaseFragment<BTPresenter> {
    private BTPresenter btPresenter;
    @Override
    protected int getlayout() {
        return R.layout.layout_bt_tips;
    }

    @Override
    protected void initViewRefs(View view) {
    }

    @Override
    protected void initUI() {
        btPresenter = basePresenter;
        openBt();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            openBt();
        }
    }


    private void openBt(){
        if (!btPresenter.isConnectedDevice() && !btPresenter.isBtConnected()) {
            btPresenter.openBt();
        }
    }

    @Override
    protected BTPresenter initPresenter() {
        return new BTPresenter();
    }


}
