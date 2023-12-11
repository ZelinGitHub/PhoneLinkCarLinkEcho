package com.incall.apps.hicar.servicesdk.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothProfile;
import android.util.Log;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

//用到framework.jar的地方
//蓝牙管理器
//HiCar和CarLink都会使用这个蓝牙管理器
public class BTManager {
    private static final String TAG = "WTWLink/BTManager";
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadsetClient mBluetoothHeadsetClient;

    private static class SingletonHolder {
        private static final BTManager instance = new BTManager();
    }

    public static BTManager getInstance() {
        return SingletonHolder.instance;
    }

    private BTManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isConnected() {
        int state = BluetoothAdapter.STATE_OFF;
        if (bluetoothAdapter != null) {
            state = bluetoothAdapter.getState();
        } else {
            Log.d(TAG, "isConnected bluetoothAdapter == null");
        }
        return state == BluetoothAdapter.STATE_ON;
    }

    /**
     * 关闭蓝牙
     */
    public void bluetoothOff() {
        if (bluetoothAdapter != null) {
            int state = bluetoothAdapter.getState();
            Log.i(TAG, "bluetoothOFF----------" + state);
            if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                //关闭蓝牙
                bluetoothAdapter.disable();
            }
        } else {
            Log.d(TAG, "isConnected bluetoothAdapter == null");
        }
    }

    /**
     * 打开蓝牙
     */
    public void bluetoothOn() {
        if (bluetoothAdapter != null) {
            int state = bluetoothAdapter.getState();
            Log.i(TAG, "bluetoothON----------" + state);
            if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                bluetoothAdapter.enable();
            }
        } else {
            Log.d(TAG, "isConnected bluetoothAdapter == null");
        }
    }


    //    BluetoothHeadsetClient连接设备
    //获取BluetoothHeadsetClient连接状态
    public int getConnectionState() {
        if (mBluetoothHeadsetClient != null) {
            List<BluetoothDevice> deviceList = mBluetoothHeadsetClient.getConnectedDevices();
            if (deviceList.isEmpty()) {
                return BluetoothProfile.STATE_DISCONNECTED;
            } else {
                return mBluetoothHeadsetClient.getConnectionState(deviceList.remove(0));
            }

        }
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    //连接远程设备
    public boolean connect(BluetoothDevice device) {
        if (null != mBluetoothHeadsetClient) {
            return mBluetoothHeadsetClient.connect(device);
        }
        Log.i(TAG, "connect mHeadsetClient == " + mBluetoothHeadsetClient);
        return false;
    }

    //断开BluetoothHeadsetClient连接
    public void disconnect(BluetoothDevice device) {
        if (null != mBluetoothHeadsetClient) {
            mBluetoothHeadsetClient.disconnect(device);
            return;
        }
        Log.i(TAG, "disconnect mHeadsetClient == " + mBluetoothHeadsetClient);
    }

    /**
     * 获得hfp 连接状态
     */
    public int getHfpConnectStatus() {
        int ret = -1;
        if (mBluetoothHeadsetClient != null) {
            ret = mBluetoothHeadsetClient.getConnectionState(getBluetoothDevice());
        } else {
            Log.i(TAG, "getHfpConnectStatus: mBluetoothHeadsetClient == null ");
        }
        Log.i(TAG, "getHfpConnectStatus: " + ret);
        return ret;
    }

    /**
     * hfp协议是否连接
     */
    public boolean isHfpConnected() {
        Log.i(TAG, "isHfpConnected");
        return getHfpConnectStatus() == BluetoothProfile.STATE_CONNECTED;
    }

    /**
     * BluetoothAdapter下面的隐藏方法disconnectAllEnabledProfiles
     */
    public void disconnectAllEnabledProfiles() {
        BluetoothDevice bluetoothDevice = getConnectedBluetoothDevice();
        boolean result = false;
        if (bluetoothDevice != null) {
            try {
                Class clazz = Class.forName("android.bluetooth.BluetoothAdapter");
                Method disconnectAllEnabledProfiles = clazz.getMethod("disconnectAllEnabledProfiles", BluetoothDevice.class);
                result = (boolean) disconnectAllEnabledProfiles.invoke(BluetoothAdapter.getDefaultAdapter(), bluetoothDevice);
                Log.d(TAG, "disconnectAllEnabledProfiles  result = " + result);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private BluetoothDevice getConnectedBluetoothDevice() {
        Set<BluetoothDevice> bondedBluetoothDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedBluetoothDevices != null && bondedBluetoothDevices.size() > 0) {
            for (BluetoothDevice device : bondedBluetoothDevices) {
                if (isDeviceBtConnected(device)) {
                    return device;
                }
            }
        }
        return null;
    }

    public boolean isDeviceBtConnected(BluetoothDevice device) {
        boolean result = false;
        try {
            Class clazz = Class.forName("android.bluetooth.BluetoothDevice");
            Method isConnected = clazz.getMethod("isConnected");
            result = (boolean) isConnected.invoke(device);
            Log.d(TAG, "isBtConnected  result = " + result);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取当前连接的蓝牙设备
     */
    public BluetoothDevice getBluetoothDevice() {
        if (mBluetoothHeadsetClient == null) {
            Log.i(TAG, "getBluetoothDevicefun mBluetoothHeadsetClient == null");
            return null;
        }

        List<BluetoothDevice> list = mBluetoothHeadsetClient.getConnectedDevices();
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public void setBluetoothHeadsetClient(BluetoothHeadsetClient bluetoothHeadsetClient) {
        mBluetoothHeadsetClient = bluetoothHeadsetClient;
    }
}
