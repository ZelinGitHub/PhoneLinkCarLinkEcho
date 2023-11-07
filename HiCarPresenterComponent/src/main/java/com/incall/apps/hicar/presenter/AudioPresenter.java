package com.incall.apps.hicar.presenter;

import android.content.Context;

import com.incall.apps.hicar.iview.IAudioView;

/**
 * @Author: ZengJie
 * @CreateDate: 20/11/20 16:53
 * @Description: xxx
 */
public class AudioPresenter extends BasePresenter<IAudioView> {

    private Context mAppContext;
    public AudioPresenter(Context context) {
        IAudioView audioView = (IAudioView)iView.get();
    }
}
