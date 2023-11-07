package com.wt.phonelink;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.hicar.HiCarMainActivity;

import java.lang.ref.SoftReference;

/**
 * Created by zelin on 2020/12/17.
 */
public class LaunchFloatingService extends Service {
    private static final String TAG = LaunchFloatingService.class.getCanonicalName();
    private WindowManager mWindowManager;
    private SoftReference<ImageView> mImageViewSoftReference;
    private int mLastTouchX, mLastTouchY;
    private int mFirstTouchX, mFirstTouchY;
    private static final int TOUCH_SLOP = 10;
    private boolean mIsMoving;

    @SuppressLint({"RtlHardcoded", "ObsoleteSdkInt"})
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        String CHANNEL_ONE_ID = "com.wt.phonelink";
        String CHANNEL_ONE_NAME = "Channel One";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        //设置为前台service
        startForeground(1, notification);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //系统下拉栏的窗口type是2008，开机引导的窗口type是2024，蓝牙电话弹出框的窗口type是2010，状态栏的窗口type是2000
            //应用窗口的层级范围是1~99
            //子窗口的层级范围是1000~1999
            //系统窗口的层级范围是2000~2999
            //mLayoutParams.type = 2001;
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        layoutParams.width = (int) (240/1.5);
        layoutParams.height = (int) (240/1.5);
        //坐标系正方向会随窗口的重心改变。
        // 当窗口的重心是Gravity.RIGHT | Gravity.TOP时，右上角为原点，向左向下为正方向。
        layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;

        if (Settings.canDrawOverlays(this)) {
            ImageView imageView = new androidx.appcompat.widget.AppCompatImageView(getApplicationContext()) {
                //重写performClick，避免performClick警告
                @Override
                public boolean performClick() {
                    return super.performClick();
                }
            };
            imageView.setOnClickListener(v -> {
                Log.i(TAG, "onClick() click imageView");
                toPhoneLink();
//                toHicarOrCarlink();
            });
            imageView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIsMoving = false;
                        // 屏幕移动获取移动初始点
                        mLastTouchX = (int) (event.getRawX());
                        mLastTouchY = (int) (event.getRawY());
                        mFirstTouchX = mLastTouchX;
                        mFirstTouchY = mLastTouchY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //当前触点的坐标，减去起始触点的坐标，得到手指划动过的距离
                        int firstDistanceX = (int) (event.getRawX()) - mFirstTouchX;
                        int firstDistanceY = (int) (event.getRawY()) - mFirstTouchY;
                        if (Math.abs(firstDistanceX) > TOUCH_SLOP
                                ||
                                Math.abs(firstDistanceY) > TOUCH_SLOP) {
                            //当前触点的坐标，减去起始触点的坐标，得到手指划动过的距离
                            int distanceX = (int) (event.getRawX()) - mLastTouchX;
                            int distanceY = (int) (event.getRawY()) - mLastTouchY;
                            moveView(distanceX, distanceY);
                            //随着手指移动，更新手指坐标
                            mLastTouchX = (int) (event.getRawX());
                            mLastTouchY = (int) (event.getRawY());
                            mIsMoving = true;
                        } else {
                            Log.e(TAG, "not moving!! ");
                            mIsMoving = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!mIsMoving) {
                            imageView.performClick();
                        }
                        mIsMoving = false;
                        break;
                }
                return true;
            });
            imageView.setImageResource(R.drawable.icon_app_smallwindow);
            mImageViewSoftReference = new SoftReference<>(imageView);
            //直接判断是否允许draw over lays
            if (Settings.canDrawOverlays(this)) {
                mWindowManager.addView(mImageViewSoftReference.get(), layoutParams);
            } else {
                Log.e(TAG, "canDrawOverlays false!");
            }
        } else {
            Log.e(TAG, "canDrawOverlays false!");
        }
    }


    private void toPhoneLink() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toHicarOrCarlink(){
        SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        //hiCar是否连接
        boolean isHiCarConnect = sp.getBoolean(com.incall.apps.hicar.servicesdk.contants.Contants.SP_IS_HICAR_CONNECT);
        //carLink是否连接
        boolean isCarLinkConnect = sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT);
        if (isHiCarConnect) {
            Intent intent = new Intent(this, HiCarMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        if (isCarLinkConnect) {
            Intent intent = new Intent(this, CarLinkMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        //返回START_NOT_STICKY 被异常杀死的Service不会尝试重新启动
        //Service默认返回START_STIKY 被异常杀死的Service会尝试重新启动
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        dismissFloatingWindow();
        destructFloatingWindow();
    }

    private void dismissFloatingWindow() {
        mImageViewSoftReference.get().setVisibility(View.GONE);
        try {
            mWindowManager.removeView(mImageViewSoftReference.get());
        } catch (Exception exception) {
            Log.e(TAG, "dismissFloatingWindow() " + exception.getMessage());
        }
    }

    private void destructFloatingWindow() {
        stopForeground(true);
        mImageViewSoftReference.clear();
        mImageViewSoftReference = null;
    }

    protected void moveView(int distanceX, int distanceY) {
        View view = mImageViewSoftReference.get();
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) view.getLayoutParams();
        int x = layoutParams.x;
        int y = layoutParams.y;
        //坐标系正方向会随窗口的重心改变。
        //当窗口的重心是Gravity.RIGHT | Gravity.TOP时，右上角为原点，向左向下为正方向。
        x -= distanceX;
        y += distanceY;
        layoutParams.x = x;
        layoutParams.y = y;
        mWindowManager.updateViewLayout(view, layoutParams);
    }
}
