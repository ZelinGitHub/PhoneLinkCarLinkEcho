package com.wt.phonelink.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.wt.phonelink.MyApplication;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class CommonUtil {

    private static final String TAG = "WTWLink/CommonUtil";

    /**
     * img转成String类型
     */
    public static byte[] imgToBytes(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        Log.d(TAG, "imgToBytes() bitmap: " + bitmap);
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }
        return null;
    }

    /**
     * 判断服务是否运行 com.incall.apps.hicar.servicesdk.HiCarService
     * @param serviceName com.incall.apps.hicar.servicesdk.HiCarService
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (("").equals(serviceName) || serviceName == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningServiceInfos =
                (ArrayList<ActivityManager.RunningServiceInfo>) activityManager.getRunningServices(100);
        for (int i = 0; i < runningServiceInfos.size(); i++) {
            if (TextUtils.equals(runningServiceInfos.get(i).service.getClassName(), serviceName)) {
                Log.i(TAG, serviceName + "serviceName: " + serviceName + ", isServiceRunning: true");
                return true;
            }
        }
        return false;
    }

    /**
     * 是否在前台
     */
    public static boolean isActivityRunning(Context context, String packageName) {
        if (("").equals(packageName) || packageName == null) {
            return false;
        }
        //AMS
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningAppProcessInfo> runningAppInfos =
                (ArrayList<ActivityManager.RunningAppProcessInfo>) activityManager.getRunningAppProcesses();
        for (int i = 0; i < runningAppInfos.size(); i++) {
            if (TextUtils.equals(runningAppInfos.get(i).processName, packageName) &&
                    runningAppInfos.get(i).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Log.i(TAG, "true");
                return true;
            }
        }
        return false;
    }


    public static void setSystemProp(String key, int value) {
        Log.i(TAG, "setSystemProp() key: " + key + "，value: " + value);
        Context context = MyApplication.getContext();
        if (context != null) {
            Settings.System.putInt(context.getContentResolver(), key, value);
        }
    }

    /**
     * systemui是跑在user 0用户下，系统设置以及手机互联等应用都是跑在user 10用户下，如果使用setSystemProp去存，
     * 就是存在user 10下的，会导致systemui拿不到值，所以我们这里使用的Global去存储，来避免这个问题

     */
    public static void setGlobalProp(Context context, String key, int value) {
        Log.i(TAG, "setGlobalProp() key: " + key + "，value: " + value);
        Settings.Global.putInt(context.getContentResolver(), key, value);
    }

    /**
     * 这里通过反射解决异常：Handler sending message to a Handler on a dead thread
     */
    public void sendCancelConvertMsg(Handler myHandler) {
        Log.i(TAG, "sendCancelConvertMsg");
        Field messageQueueField = null;
        try {
            messageQueueField = Looper.class.getDeclaredField("mQueue");
            messageQueueField.setAccessible(true);
            Class<MessageQueue> messageQueueClass = (Class<MessageQueue>) Class.forName("android.os.MessageQueue");
            Constructor<MessageQueue>[] messageQueueConstructor = (Constructor<MessageQueue>[]) messageQueueClass.getDeclaredConstructors();
            for (Constructor<MessageQueue> constructor : messageQueueConstructor) {
                constructor.setAccessible(true);
                Class[] types = constructor.getParameterTypes();
                for (Class clazz : types) {
                    if (clazz.getName().equalsIgnoreCase("boolean")) {
                        messageQueueField.set(myHandler.getLooper(), constructor.newInstance(true));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "sendCancelConvertMsg myHandler " + myHandler);
        myHandler.post(() -> myHandler.sendEmptyMessageDelayed(1001, 6000));
    }
}
