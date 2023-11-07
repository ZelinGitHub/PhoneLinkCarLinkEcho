package com.incall.apps.hicar.servicesdk.interfaces;
import com.tinnove.hicarclient.AppInfoListener;
/**
 * Interface for HiCarService
 * 另外一个监听
 * 实现类有BaseHiCarListener、MainServiceImpl和HcAudioManager
 * @author zouhongtao
 * @since 2019-08-23
 */
public interface HiCarServiceListener extends AppInfoListener{

    void onDeviceChange(String s, int i, int i1);

    void onDeviceServiceChange(String s, int i);

    void onDataReceive(String s, int i, byte[] bytes);

    void onPinCode(String s);
    void onBinderDied();
    void onPinCodeFailed();
}