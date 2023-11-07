package com.wt.phonelink;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.hicar.HiCarMainActivity;

/**
 * @Author: LuoXia
 * @Date: 2022/11/14 17:08
 * @Description: 手机互联桌面卡片
 */
public class PhoneLinkWidgetProvider extends AppWidgetProvider {

    private String TAG = "PhoneLinkWidgetProvider";
    private final Context mContext = MyApplication.getContext();
    private final String ACTION = "com.wt.phonelick.action.ACTION_CLICK_APP_WIDGET";
    private final SharedPreferencesUtil sp = SharedPreferencesUtil.getInstance(mContext);

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent == null) {
            LogUtil.d(TAG, "onReceive intent == null");
            return;
        }

        String action = intent.getAction();
        LogUtil.d(TAG, "onReceive action = " + action);
        if (ACTION.equals(action)) {
            if (sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT, false)) {
                Intent intent1 = new Intent(mContext, CarLinkMainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent1);
            } else if (sp.getBoolean(Contants.SP_IS_HICAR_CONNECT, false)) {
                Intent intent2 = new Intent(mContext, HiCarMainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent2);
            } else {
                Intent intent3 = new Intent(mContext, MainActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent3);
            }
        }
    }

    //小部件被添加时或者每次小部件更新时调用，更新周期取决于updatePeriodMillis
    @SuppressLint("RemoteViewLayout")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        LogUtil.d(TAG, "onUpdate SP_IS_CARLINK_CONNECT----" + (sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT)));
        LogUtil.d(TAG, "onUpdate SP_IS_HICAR_CONNECT----" + sp.getBoolean(Contants.SP_IS_HICAR_CONNECT));
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.layout_desk_card);
        if (sp.getBoolean(Contants.SP_IS_CARLINK_CONNECT) || sp.getBoolean(Contants.SP_IS_HICAR_CONNECT)) {
            remoteViews.setViewVisibility(R.id.tv_connect_phone, View.GONE);
            remoteViews.setViewVisibility(R.id.cl_phone_info, View.VISIBLE);
            remoteViews.setTextViewText(R.id.tv_phone_brand, sp.getString(Contants.SP_PHONE_BRAND, ""));
            remoteViews.setTextViewText(R.id.tv_phone_model, sp.getString(Contants.SP_PHONE_MODEL, ""));
        } else {
            remoteViews.setViewVisibility(R.id.cl_phone_info, View.GONE);
            remoteViews.setViewVisibility(R.id.tv_connect_phone, View.VISIBLE);
        }
        Intent intent = new Intent();
        intent.setAction(ACTION);
        if (Build.VERSION.SDK_INT >= 26) {
            intent.setComponent(new ComponentName(context, PhoneLinkWidgetProvider.class));
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.rl_phonelink_card, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, PhoneLinkWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }
}
