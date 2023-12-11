package com.wt.phonelink.hicar.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.huawei.hicarsdk.HiSightSurfaceView;
import com.incall.apps.hicar.iview.IHiCarView;
import com.incall.apps.hicar.presenter.HiCarPresenter;
import com.incall.apps.hicar.servicesdk.manager.HiCarServiceManager;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;
import com.wt.phonelink.AppConfig;
import com.wt.phonelink.LaunchFloatingService;
import com.wt.phonelink.MainActivity;
import com.wt.phonelink.R;
import com.wt.phonelink.carlink.DragImageView;
import com.wt.phonelink.hicar.HiCarMainActivity;

//显示投影的fragment
public class HiCarFragment extends BaseFragment<HiCarPresenter> implements IHiCarView {
    private static final String TAG = "WTWLink/HiCarFragment";
    private HiCarPresenter mHiCarPresenter;
    //控件对象的引用
    private HiSightSurfaceView mHiSightSurfaceView;
    private RelativeLayout mLayoutConnectingView;
    private TextView mHicarConnectingPhoneTv;
    private ImageView mHicarConnectingImgIv;
    private ImageView mHicarTransImgIv;
    private DragImageView mCloseCastBtn;
    //状态
    private volatile boolean mIsCloseCastBtnMoving;
    private volatile boolean mIsSurfaceViewDestroy;

    private int mLastTouchX;
    private int mLastTouchY;

    private Animation mAnimation;
    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mIsSurfaceViewDestroy = false;
            Log.i(TAG, "surface view created");
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            Log.i(TAG, "surface view changed() width: " + width + ", height: " + height);
            if (surfaceHolder != null && surfaceHolder.getSurface() != null && surfaceHolder.getSurface().isValid()) {
                //更新车配置
                if (mHiCarPresenter.updateCarConfig(surfaceHolder.getSurface(), width, height)) {
                    Log.i(TAG, "surface view changed() startProjection  ");
                    //启动投屏
                    mHiCarPresenter.startProjection();
                }
            } else {
                Log.i(TAG, "surface holder or surface is null or invalid");
                //暂停投屏
                mHiCarPresenter.pauseProjection();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.i(TAG, "surface view destroyed");
            mIsSurfaceViewDestroy = true;
            mHiCarPresenter.pauseProjection();
            mHicarTransImgIv.setVisibility(View.GONE);
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Log.d(TAG, "onGlobalLayout()");
            mHiSightSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            mHiSightSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        }
    };


    @Override
    protected int getlayout() {
        return R.layout.layout_hicar;
    }

    @Override
    protected HiCarPresenter initPresenter() {
        return new HiCarPresenter();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHiCarPresenter = basePresenter;
    }

    @Override
    protected void initViewRefs(View view) {
        Log.d(TAG, "initViews()");
        mHiSightSurfaceView = view.findViewById(R.id.surface);
        mLayoutConnectingView = view.findViewById(R.id.layout_connecting);
        mHicarConnectingPhoneTv = view.findViewById(R.id.hicar_connecting_phone_text);
        mHicarConnectingImgIv = view.findViewById(R.id.hicar_connecting_img);
        mHicarTransImgIv = view.findViewById(R.id.hicar_trans_img);
        mCloseCastBtn = view.findViewById(R.id.iv_close);
    }

    @Override
    protected void initUI() {
        Log.d(TAG, "initDatas()");
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.hicar_connecting_rotate);
        if (mHiSightSurfaceView != null) {
            ViewTreeObserver viewTreeObserver = mHiSightSurfaceView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
        mCloseCastBtn.setOnClickListener(v -> {
            Log.i(TAG, "onClick() disConnected");
            //断开连接
            mHiCarPresenter.disConnected();
        });
        mCloseCastBtn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG, "onTouch() mCloseCastBtn ACTION_DOWN");
                    mIsCloseCastBtnMoving = false;
                    if (event.getPointerCount() > 1) {
                        return false;
                    }
                    // 屏幕移动获取移动初始点
                    mLastTouchX = (int) (event.getRawX());
                    mLastTouchY = (int) (event.getRawY());

                    break;
                case MotionEvent.ACTION_MOVE:
                    int distanceX = (int) (event.getRawX()) - mLastTouchX;
                    int distanceY = (int) (event.getRawY()) - mLastTouchY;
                    if (Math.abs(distanceX) > 5 || Math.abs(distanceY) > 5) {
                        mIsCloseCastBtnMoving = true;
                        moveView(distanceX, distanceY);
                        //随着手指移动，更新手指坐标
                        mLastTouchX = (int) (event.getRawX());
                        mLastTouchY = (int) (event.getRawY());
                    }
                    //如果没有移动，则不更新mLastTouchX、mLastTouchY坐标
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch() mCloseCastBtn ACTION_UP mIsCloseCastBtnMoving: " + mIsCloseCastBtnMoving);
                    if (mIsCloseCastBtnMoving) {
                        mIsCloseCastBtnMoving = false;
                    } else {
                        mCloseCastBtn.performClick();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mIsCloseCastBtnMoving = false;
                    Log.w(TAG, "onTouch() mCloseCastBtn ACTION_CANCEL");
            }
            return true;
        });
        showHiCar();
    }

    protected void moveView(int distanceX, int distanceY) {
        float x = mCloseCastBtn.getX();
        float y = mCloseCastBtn.getY();
        x += distanceX;
        y += distanceY;
        int left = getLeftBoundary();
        int right = getRightBoundary();
        int top = getTopBoundary();
        int bottom = getBottomBoundary();
        if (x < left) {
            x = left;
        }
        if (x > right) {
            x = right;
        }
        if (y < top) {
            y = top;
        }
        if (y > bottom) {
            y = bottom;
        }
        mCloseCastBtn.setX(x);
        mCloseCastBtn.setY(y);
    }

    private int getLeftBoundary() {
        return 0;
    }

    private int getTopBoundary() {
        return 0;
    }

    private int getRightBoundary() {
        return AppConfig.getExpectedDisplayWidth() - mCloseCastBtn.getWidth();
    }


    private int getBottomBoundary() {
        return AppConfig.getExpectedDisplayHeight() - AppConfig.getDockHeight() - mCloseCastBtn.getHeight();
    }

    //当使用add()+show()，hide()跳转新的Fragment时，旧的Fragment回调onHiddenChanged()，不会回调onStop()等生命周期方法
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged() hidden: " + hidden);
        if (hidden) {
            mHiCarPresenter.pauseProjection();
        } else {
            showHiCar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        stopLauncherFloater();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (HiCarServiceManager.getInstance().isConnectedDevice()) {
            startLauncherFloater();
        } else {
            Log.e(TAG, "onStop() HiCar connection is broken! ");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销，防止内存泄露
        try {
            mHiSightSurfaceView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            mHiSightSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
            stopLauncherFloater();
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void startLauncherFloater() {
        Log.i(TAG, "startLauncherFloater()");
        //启动悬浮窗服务
        FragmentActivity activity = getActivity();
        if (null != activity) {
            Intent intent = new Intent(activity, LaunchFloatingService.class);
            activity.startService(intent);
        }
    }

    private void stopLauncherFloater() {
        Log.i(TAG, "stopLauncherFloater()");
        //停止悬浮窗服务
        FragmentActivity activity = getActivity();
        if (null != activity) {
            Intent intent = new Intent(activity, LaunchFloatingService.class);
            activity.stopService(intent);
        }
    }

    private void showHiCar() {
        if (mHiCarPresenter.isPlayAnim()) {
            Log.d(TAG, "showHiCar() isPlayAnim");
            mHicarTransImgIv.setVisibility(View.GONE);
            mLayoutConnectingView.setVisibility(View.VISIBLE);
            mHicarConnectingPhoneTv.setText(getString(R.string.huawei_hicar_ap_connecting, mHiCarPresenter.getPhoneName()));
            mHicarConnectingImgIv.startAnimation(mAnimation);
        } else {
            Log.d(TAG, "showHiCar() isNotPlayAnim");
            mLayoutConnectingView.setVisibility(View.GONE);
            mHicarTransImgIv.setVisibility(View.GONE);
        }
        Log.d(TAG, "showHiCar() isSurfaceViewDestroy: " + mIsSurfaceViewDestroy);
        if (!mIsSurfaceViewDestroy) {
            //开始投屏
            mHiCarPresenter.startProjection();
        }
    }


    @Override
    public void onDeviceConnect() {
        Log.i(TAG, "onDeviceConnect()");

    }

    @Override
    public void onDeviceDisconnect() {
        Log.i(TAG, "onDeviceDisconnect()");
        stopLauncherFloater();
    }

    @Override
    public void onDeviceProjectConnect() {
        Log.i(TAG, "onDeviceProjectConnect()");
    }

    //设备投影断连
    //HiCarPersenter handle MSG_DEVICE_PROJECT_DISCONNECT
    //HiCarPersenter send MSG_DEVICE_PROJECT_DISCONNECT
    //MainPresenter send MSG_DEVICE_PROJECT_DISCONNECT
    @Override
    public void onDeviceProjectDisconnect() {
        Log.i(TAG, "onDeviceProjectDisconnect()");
        //断开连接
        mHiCarPresenter.disConnected();
    }

    //设备服务暂停  暂停投屏 202
    //暂停投屏，回到桌面
    @Override
    public void onDeviceServicePause() {
        Log.i(TAG, "onDeviceServicePause()");
        //BUG：我这里点击这个“长安主页”的图标之后，hiCar会返回一个202的消息，然后会暂停投屏、回到车机桌面，重新进入hicar之后投屏也没有恢复
//        hiCarPresenter.pauseProjection();  删除掉
        HiCarMainActivity activity = (HiCarMainActivity) getActivity();
        if (activity != null) {
            activity.toLauncher();
        }
    }

    //设备服务恢复 恢复投屏 203
    @Override
    public void onDeviceServiceResume() {
        Log.i(TAG, "203 onDeviceServiceResume()");
        //投屏 又以一个语音后台唤醒hicar导致的黑屏问题，所以这里先注释掉startProjection
//        hiCarPresenter.startProjection();
    }

    //204
    //HiCarConst.EVENT_DEVICE_SERVICE_START
    //开始投屏
    @Override
    public void onDeviceServiceStart() {
        Log.i(TAG, "204 onDeviceServiceStart()");
        mHiCarPresenter.startProjection();
    }

    @Override
    public void onDeviceServiceStop() {
        Log.i(TAG, "onDeviceServiceStop()");
        mHiCarPresenter.stopProjection();
        HiCarMainActivity activity = (HiCarMainActivity) getActivity();
        if (activity != null) {
            activity.toLauncher();
        }
        //投屏结束，删除路由和dns信息
        Context context = getContext();
        if (context != null) {
            CommonUtil.deleteRouteAndDnsInfo(context.getContentResolver());
        }
    }

    @Override
    public void onDeviceDisplayServicePlaying() {
        Log.i(TAG, "onDeviceDisplayServicePlaying()");
        mAnimation.cancel();
        mLayoutConnectingView.setVisibility(View.GONE);
        mHicarTransImgIv.setVisibility(View.GONE);
    }
}
