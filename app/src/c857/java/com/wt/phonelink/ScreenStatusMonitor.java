package com.wt.phonelink;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//测试全半屏：
//左侧边栏展开
//adb shell settings put global "key_screen_show" -1
//左右侧边栏收起
//adb shell settings put global "key_screen_show" 0
//右侧边栏展开
//adb shell settings put global "key_screen_show" 1

//进入左侧显示
//        Settings.Global.putInt(BaseApp.getApp().getContentResolver(), KEY_SCREEN_SHOW, TYPE_LEFT_SHOW);
//        右侧显示
//        Settings.Global.putInt(BaseApp.getApp().getContentResolver(), KEY_SCREEN_SHOW, TYPE_RIGHT_SHOW);
//        全屏显示
//        Settings.Global.putInt(BaseApp.getApp().getContentResolver(), KEY_SCREEN_SHOW, TYPE_ALL_HIDE);
public class ScreenStatusMonitor {
    public static final int TYPE_LEFT_SHOW = -1;  //左侧显示
    public static final int TYPE_RIGHT_SHOW = 1;  //右侧显示
    public static final int TYPE_ALL_HIDE = 0;    //全屏

    private static final String TAG = "WTPhoneLink/ScreenStatusMonitor";
    //在Settings中保存的全半屏状态的数据（value）对应的key
    private static final String KEY_SCREEN_SHOW = "key_screen_show";
    private static ScreenStatusMonitor mInstance;
    private final Context mContext;
    //当前的全半屏状态
    private int mStatus = 0;
    //全半屏状态监听器
    private final List<OnScreenStatusChangedListener> mListeners = new ArrayList<>();
    //内容观察者
    //为指定键值对数据注册内容观察者，参数是指定键值对的Uri和内容观察者
    //我们这里的键值对的key就是SCREEN_STATUS_KEY（FLAG_SCREEN_SHOW），value是全半屏的状态。
    //当键值对的value发生改变时，将通知内容观察者
    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        //当指定key的value发生变化时，调用onChange方法
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "onChange()");
            //主动获取Settings中保存的全半屏的状态，更新mStatus，通知所有监听器状态更新
            updateStatus();
        }
    };

    private ScreenStatusMonitor(Context context) {
        Log.d(TAG, "ScreenStatusMonitor()");
        mContext = context;
    }

    //当前对象
    public static ScreenStatusMonitor getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ScreenStatusMonitor(context);
        }
        Log.d(TAG, "getInstance()");
        return mInstance;
    }

    //注册内容观察者
    public void startMonitor() {
        Log.d(TAG, "startMonitor()");
        //Settings中指定数据（键值对）的Uri
        Uri uriFor = Settings.Global.getUriFor(KEY_SCREEN_SHOW);
        //为指定键值对数据注册内容观察者，参数是指定键值对的Uri和内容观察者
        //当键值对的value发生改变时，将通知内容观察者
        mContext.getContentResolver().registerContentObserver(uriFor, false, mObserver);
    }

    //注销内容观察者
    public void stopMonitor() {
        Log.d(TAG, "stopMonitor()");
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }

    //注册全半屏状态监听
    public void addListener(OnScreenStatusChangedListener listener) {
        Log.d(TAG, "addListener()");
        mListeners.add(listener);
    }

    //注销全半屏状态监听
    public void removeListener(OnScreenStatusChangedListener listener) {
        Log.d(TAG, "removeListener()");
        mListeners.remove(listener);
    }

    //得到当前的全半屏状态
    public int getStatus() {
        //主动获取Settings中保存的全半屏的状态，更新mStatus，通知所有监听器状态更新
        updateStatus();
        //返回这个状态
        return mStatus;
    }

    //通知所有监听器，全半屏状态更新
    private void notifyListener() {
        Log.d(TAG, "notifyListener()");
        mListeners.forEach(l -> l.onChanged(mStatus));
    }

    //更新状态
    private void updateStatus() {
        //得到Settings中保存的全半屏的状态
        //得到内容解析者
        ContentResolver contentResolver = mContext.getContentResolver();
        //从Settings中得到指定的Key保存的数据，需要传入内容解析者
        int newStatus = Settings.Global.getInt(contentResolver, KEY_SCREEN_SHOW, 0);
        Log.d(TAG, "updateStatus() old: " + mStatus + ", new: " + newStatus);
        if (newStatus != mStatus) {
            //更新保存的全半屏的状态
            mStatus = newStatus;
            //通知所有监听器，全半屏更新
            notifyListener();
        }
    }

    //全半屏状态监听器
    public interface OnScreenStatusChangedListener {
        //-1 左侧 0全屏 1 右侧
        void onChanged(int status);
    }
}
