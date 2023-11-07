package com.incall.apps.hicar.servicesdk.utils;

import android.annotation.SuppressLint;
import java.lang.reflect.Method;

/**
 * 获取系统属性值
 *
 * @author xiejunlin
 */
public class SystemProperties {
    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultValue) {

        return (String) invoke("get", key, defaultValue);
    }

    public static String set(String key, String defaultValue) {
        return (String) invoke("set", key, defaultValue);
    }

    public static int set(String key, int defaultValue) {
        return (int) invoke("set", key, defaultValue);
    }

    public static boolean set(String key, boolean defaultValue) {
        return (boolean) invoke("set", key, defaultValue);
    }

    /**
     * 封装通过反射获取 SystemProperties 属性值
     *
     * @param name         SystemProperties中对应的方法名字
     * @param key          name对应的方法参数 key
     * @param defaultValue name 对应的方法参数 默认值
     */

    private static Object invoke(String name, String key, Object defaultValue) {
        try {
            //通过传递的值 得到类型
            Class<?> parameterTypes = null;
            if (defaultValue instanceof Integer) {
                parameterTypes = Integer.class;
            } else if (defaultValue instanceof String) {
                parameterTypes = String.class;
            } else if (defaultValue instanceof Boolean) {
                parameterTypes = Boolean.class;
            }
            if (parameterTypes == null) {
                return defaultValue;
            }
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod(name, String.class, parameterTypes);
            return method.invoke(c, key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
