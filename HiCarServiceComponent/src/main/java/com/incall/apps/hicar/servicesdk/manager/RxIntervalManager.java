package com.incall.apps.hicar.servicesdk.manager;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * rxjava 的轮训管理，主要用于回连的轮训。
 * 远距离回连效果很差
 *
 * @author KongJing
 * 2021.12.29
 */
public class RxIntervalManager {
    private static final String TAG = "RxIntervalManager";
    /**
     * 注册管理对象
     */
    private static Disposable mDisposable;

    /**
     * 轮训管理
     */
    private static RxIntervalManager instance;

    /**
     * rxjava的事件管理生成对象
     */
    private static PublishSubject mSubject;

    /**
     * 时间间隔
     */
    private static long mPeriod = 10L;

    private RxIntervalManager() {
    }

    public static synchronized RxIntervalManager getInstance() {
        if (instance == null) {
            instance = new RxIntervalManager();
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void start() {
        //避免重复开启
        if (mDisposable != null && !mDisposable.isDisposed()) {
            Log.i(TAG, "start return : mDisposable = " + mDisposable + ", dispose = " + mDisposable.isDisposed());
            return;
        }
        Log.i(TAG, "start: create");
        //启用一个定时器，第一次是20秒执行，执行成功是20分钟执行一次，失败则20秒再执行。进行一次token的数据请求获取
        mSubject = PublishSubject.create();
        mDisposable = mSubject.switchMap(new Function<Long, ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> apply(Long peroidTime) throws Exception {
                Log.i(TAG, "apply: 下次执行回连间隔 " + peroidTime);
                return Observable.interval(peroidTime, TimeUnit.SECONDS);
            }
        }).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long l) {
                Log.i(TAG, "accept: deviceConnect = " + HiCarServiceManager.getInstance().isConnectedDevice() + ",bt=  "
                        + BTManager.getInstance().isHfpConnected() + ", tid " + Thread.currentThread().getId());
                //进行回连
                if (!HiCarServiceManager.getInstance().isConnectedDevice() && BTManager.getInstance().isHfpConnected()) {
                    //没有连接设备，蓝牙已经连接上
                    String mac = HiCarServiceManager.getInstance().getMac();
                    HiCarServiceManager.getInstance().startReconnect(mac);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe();
        mSubject.onNext(mPeriod);
    }

    public void pause() {
        Log.i(TAG, "pause: ");
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    /**
     * 释放
     */
    public void release() {
        if (mSubject != null) {
            mSubject.onComplete();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        instance = null;
    }
}
