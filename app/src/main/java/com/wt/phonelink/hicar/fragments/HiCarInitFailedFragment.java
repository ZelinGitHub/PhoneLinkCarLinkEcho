package com.wt.phonelink.hicar.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.incall.apps.hicar.iview.IInitFailedView;
import com.incall.apps.hicar.presenter.InitFailedPresenter;
import com.openos.skin.WTSkinManager;
import com.openos.skin.info.SkinInfo;
import com.wt.phonelink.R;

import wtcl.lib.theme.WTThemeManager;
import wtcl.lib.widget.WTButton;

//连接初始化失败fragment
public class HiCarInitFailedFragment extends BaseFragment<InitFailedPresenter> implements IInitFailedView {
    private static final String TAG = "WTWLink/InitFailedFragment";
    private WTButton btn_hicar_init_failed_cancel;
    private WTButton btn_hicar_init_failed_reset;

    WTSkinManager.SkinChangedListener skinChangedListener = new WTSkinManager.SkinChangedListener() {
        @Override
        public void onSkinChanged(@NonNull SkinInfo newInfo, @NonNull SkinInfo previousInfo) {
            Log.i(TAG, "onSkinChanged() ");
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
            }
            updateSkin();
        }
    };

    @Override
    protected int getlayout() {
        return R.layout.layout_init_failed;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WTSkinManager.get().addSkinChangedListener(skinChangedListener);
    }

    @Override
    protected void initViewRefs(View view) {
        btn_hicar_init_failed_cancel = view.findViewById(R.id.btn_hicar_init_failed_cancel);
        btn_hicar_init_failed_reset = view.findViewById(R.id.btn_hicar_init_failed_reset);
    }

    @Override
    protected void initUI() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        }
        btn_hicar_init_failed_cancel.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click hicarInitFailedCancel");
            FragmentActivity activity2 = getActivity();
            if (activity2 != null) {
                activity2.onBackPressed();
            }
        });
        btn_hicar_init_failed_reset.setOnClickListener(v -> {
            Log.i(TAG, "onClick() click hicarInitFailedReset");
            restartApp();
        });
        updateSkin();
    }

    private void updateSkin() {
        String packageName = WTSkinManager.get().getCurrentSkinInfo().getSkinPackageName();
        Log.d(TAG, "updateSkin() packageName: " + packageName);
        WTThemeManager.setSkinPkgName(packageName);
        WTThemeManager.setResources(WTSkinManager.get().getProxyResources());
        btn_hicar_init_failed_cancel.applyTheme();
        btn_hicar_init_failed_reset.applyTheme();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WTSkinManager.get().removeSkinChangedListener(skinChangedListener);//防止内存泄漏
    }

    @Override
    public void onHome() {
        Log.i(TAG, "onHome()");
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    @Override
    protected InitFailedPresenter initPresenter() {
        return new InitFailedPresenter();
    }


    /**
     * 初始化的方法失效，暂时使用重启app来进行
     */
    private void restartApp() {
        Log.i(TAG, "restartApp()");
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Intent intent = activity.getPackageManager().
                    getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("REBOOT", "reboot");
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent restartIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
