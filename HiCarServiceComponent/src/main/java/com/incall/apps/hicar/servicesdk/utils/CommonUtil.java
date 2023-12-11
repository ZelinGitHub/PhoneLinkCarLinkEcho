package com.incall.apps.hicar.servicesdk.utils;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;


public class CommonUtil {

    private static final String TAG = "WTWLink/CommonUtil";

    public static int getIoCapability() {
        int capability = -1;
        try {
            Class clazz = null;
            clazz = Class.forName("android.bluetooth.BluetoothAdapter");
            Method getIoCapability = clazz.getMethod("getIoCapability");
            capability = (int) getIoCapability.invoke(BluetoothAdapter.getDefaultAdapter());
            Log.d(TAG, "getIoCapability  capability = " + capability);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return capability;
    }

    public static void setIoCapability(int value) {
        try {
            Class<?> class1 = null;
            class1 = Class.forName("android.bluetooth.BluetoothAdapter");
            Method setIoCapability = class1.getMethod("setIoCapability", int.class);
            setIoCapability.invoke(BluetoothAdapter.getDefaultAdapter(), value);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * img转成String类型
     *
     * @return
     */
    public static byte[] imgToBaseByte(Context context, int resId) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        Log.d(TAG, "imgToBase64String---" + bitmap);
        byte[] bytes = new byte[0];
        if (bitmap != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                bytes = baos.toByteArray();
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
                return bytes;
            } finally {
                baos.close();
            }
        }
        return bytes;
    }


    /**
     * 将图片文件转换成字节流
     */
    public static byte[] getPngBytes(String pictureFilePath) throws IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(pictureFilePath);
            Log.i(TAG, "fis=" + fis);
            baos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            Log.i(TAG, "get picture error");
            return null;
        } finally {
            fis.close();
            baos.close();
        }
    }

    public static void startHiCarActivity(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.incall.apps.hicar", "com.incall.apps.hicar.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void sendHiCarBroadcast(Context context, boolean isConnect) {
        Log.d(TAG, "sendHiCarBroadcast context = " + context + "，isConnect = " + isConnect);
        // 广播通知，如果HiCar在后台则拉起界面
        Intent intent = new Intent();
        intent.setAction("com.incall.apps.hicar.ACTION_START_MAINACTIVITY");
        intent.setComponent(new ComponentName("com.wt.phonelink", "com.wt.phonelink.hicar.broadcast.HiCarReceiver"));
        intent.putExtra("isConnect", isConnect);
        context.sendBroadcast(intent);
    }

    /**
     * 车机向HiCar App上报深浅模式
     *
     * @param mode day：浅色模式
     *             night：深色模式
     * @return
     */
    public static byte[] getDayNightMode(String mode) {
        String dayNightMode = "{\n" +
                " \"dayNightMode\": \"" + mode + "\"\n" +
                " }\n";
        return dayNightMode.getBytes();
    }

    /**
     * @param mode -1：不支持上报车辆行驶状态
     *             0：行车状态
     *             1：停车状态
     * @return
     */
    public static byte[] getDrivingMode(int mode) {
        String drivingMode = "{\n" +
                " \"drivingMode\": \"" + mode + "\"\n" +
                " }\n";
        return drivingMode.getBytes();
    }

    /**
     * 请求/结束  共享上网
     *
     * @param command
     * @param enable
     * @return
     */
    public static byte[] requestShareNet(int command, int enable) {
        String requestShareNet = "{\n" +
                "    \"service\":\"net\",\n" +
                "    \"subService\":\"internetShare\",\n" +
                "    \"command\":" + command + ",\n" +
                "    \"data\":{\n" +
                "        \"enable\":" + enable + "\n" +
                "    }\n" +
                "}";
        Log.i(TAG, "requestShareNetStr = " + requestShareNet);
        return requestShareNet.getBytes();
    }

    /**
     * 手机侧主动结束共享上网场景
     */
    public static byte[] stopShareNetByUser() {
        String stopShareNetByUser = "{\n" +
                "    \"service\":\"net\",\n" +
                "    \"subService\":\"internetShare\",\n" +
                "    \"command\":1,\n" +
                "    \"errorCode\":100000\n" +
                "}";
        Log.i(TAG, "stopShareNetByUser = " + stopShareNetByUser);
        return stopShareNetByUser.getBytes();
    }

    public static byte[] isUserDisconnect(byte[] bytes) {
        int result = -1;
        String message = new String(bytes);
        Log.d(TAG, "isUserDisconnect: " + message);

        try {
            JSONObject jsonObject = new JSONObject(message);
            int isUserDisconnect = jsonObject.getInt("isUserDisconnect");
            result = isUserDisconnect;
            Log.d(TAG, "isUserDisconnect: isUserDisconnect = " + isUserDisconnect);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String str = "{\"isUserDisconnect\":" + result + "} ";
        return str.getBytes();
    }

    public static void deleteRouteAndDnsInfo(ContentResolver resolver) {
        boolean clearDateWayResult = Settings.System.putString(resolver, Settings.System.WIFI_STATIC_GATEWAY, "");
        boolean clearDnsResult = Settings.System.putString(resolver, Settings.System.WIFI_STATIC_DNS1, "");
        Log.i(TAG, "deleteRouteAndDnsInfo = " + clearDateWayResult + "--" + clearDnsResult);
    }

    /**
     * 是否在前台
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isActivityRunning(Context context, String packageName) {
        if (("").equals(packageName) || packageName == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningAppProcessInfo> runningAppInfos =
                (ArrayList<ActivityManager.RunningAppProcessInfo>) activityManager.getRunningAppProcesses();
        for (int i = 0; i < runningAppInfos.size(); i++) {
            if (TextUtils.equals(runningAppInfos.get(i).processName, packageName) &&
                    runningAppInfos.get(i).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Log.i(TAG, "isActivityRunning true and start to MSG_START_ADV");
                return true;
            }
        }
        return false;
    }

    /**
     * 设置wifi开关状态
     *
     * @param context
     * @param isEnable  WIFI_STATE_DISABLED = 1,WIFI_STATE_ENABLED = 3;
     */
    public static void setWifiEnabled(Context context, boolean isEnable) {
        if (context == null) {
            Log.i(TAG, "setWifiEnabled context == null ");
            return;
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifi.getWifiState();
        Log.i(TAG, "current wifiState = " + wifiState + " and it will be changed to " + isEnable);
        wifi.setWifiEnabled(isEnable);
    }
}
