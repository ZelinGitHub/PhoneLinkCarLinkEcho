package com.incall.apps.hicar.servicesdk.manager;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.incall.apps.commoninterface.behavior.DataCollectManager;
import com.incall.apps.commoninterface.behavior.DataCollectType;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 埋点管理
 */
public class HiCarDataCollectManager {
    private static final String TAG = "WTPhoneLink/HiCarDataCollectManager";

    private static final String BT_DISCONNECT_EID = "2703";
    private static final String DEVICE_CONNECTED_EID = "4101";
    private static final String DEVICE_DISCONNECT_EID = "4102";
    private static final String HICAR_START_EID = "4103";
    private static final String VOICE_START_EID = "4104";
    private static final String DT_RECOMMEND = "hicar";
    private static final String HICAR_EM = "em";
    private static final String HICAR_TY = "ty";
    private static final String HICAR_FUN = "fun";
    private static final String HICAR_FUN_TYPE = "3";
    private final DataCollectManager dataCollectManager;
    //用来判断当前的埋点服务是否初始化了
    private final AtomicBoolean initFlag = new AtomicBoolean(true);

    private static class SingletonHolder {
        private static final HiCarDataCollectManager instance = new HiCarDataCollectManager();
    }

    public static HiCarDataCollectManager getInstance() {
        return SingletonHolder.instance;
    }

    private HiCarDataCollectManager() {
        dataCollectManager = DataCollectManager.getInstance();
    }

    public void init(Context mContext) {
        dataCollectManager.init(mContext);
        initFlag.set(true);
    }

    public void BTDisconnected() {
        if (!initFlag.get()) {
            Log.i(TAG, "BTDisconnected() noInit");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(DataCollectType.EID, BT_DISCONNECT_EID);
        json.addProperty(DataCollectType.TS, getUploadTime());
        json.addProperty(DataCollectType.DT, DT_RECOMMEND);
        json.addProperty(DataCollectType.DTN, DataCollectType.DTN_CA);
        //TODO:
//        json.addProperty(DataCollectType.VN, BuildConfig.VERSION_NAME);
        json.addProperty(HICAR_FUN, HICAR_FUN_TYPE);
        String data = json.toString();
        Log.i(TAG, "BTDisconnected() data: " + data);
        boolean ret = DataCollectManager.getInstance().uploaData(DT_RECOMMEND, data);
        Log.i(TAG, "BTDisconnected() ret: " + ret);
    }

    public void deviceConneted(String name) {
        if (!initFlag.get()) {
            Log.i(TAG, "deviceConneted() noInit");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(DataCollectType.EID, DEVICE_CONNECTED_EID);
        json.addProperty(DataCollectType.TS, getUploadTime());
        json.addProperty(DataCollectType.DT, DT_RECOMMEND);
        json.addProperty(DataCollectType.DTN, DataCollectType.DTN_CA);
//        json.addProperty(DataCollectType.VN, BuildConfig.VERSION_NAME);
        json.addProperty(HICAR_EM, name);
        String data = json.toString();
        Log.i(TAG, "deviceConneted() data: " + data);
        boolean ret = DataCollectManager.getInstance().uploaData(DT_RECOMMEND, data);
        Log.i(TAG, "deviceConneted() ret: " + ret);
    }

    public void deviceDisconnet(String name) {
        if (!initFlag.get()) {
            Log.i(TAG, "deviceDisconnet() noInit");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(DataCollectType.EID, DEVICE_DISCONNECT_EID);
        json.addProperty(DataCollectType.TS, getUploadTime());
        json.addProperty(DataCollectType.DT, DT_RECOMMEND);
        json.addProperty(DataCollectType.DTN, DataCollectType.DTN_CA);
//        json.addProperty(DataCollectType.VN, BuildConfig.VERSION_NAME);
        json.addProperty(HICAR_EM, name);
        String data = json.toString();
        Log.i(TAG, "deviceDisconnet() data: " + data);
        boolean ret = DataCollectManager.getInstance().uploaData(DT_RECOMMEND, data);
        Log.i(TAG, "deviceDisconnet() ret: " + ret);
    }


    public void startVoice() {
        if (!initFlag.get()) {
            Log.i(TAG, "startVoice() noInit");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(DataCollectType.EID, VOICE_START_EID);
        json.addProperty(DataCollectType.TS, getUploadTime());
        json.addProperty(DataCollectType.DT, DT_RECOMMEND);
        json.addProperty(DataCollectType.DTN, DataCollectType.DTN_CA);
//        json.addProperty(DataCollectType.VN, BuildConfig.VERSION_NAME);

        String data = json.toString();
        Log.i(TAG, "startVoice data: " + data);
        boolean ret = DataCollectManager.getInstance().uploaData(DT_RECOMMEND, data);
        Log.i(TAG, "startVoice ret: " + ret);
    }

    public void startHiCar(int i) {
        if (!initFlag.get()) {
            Log.i(TAG, "startHiCar() noInit");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(DataCollectType.EID, HICAR_START_EID);
        json.addProperty(DataCollectType.TS, getUploadTime());
        json.addProperty(DataCollectType.DT, DT_RECOMMEND);
        json.addProperty(DataCollectType.DTN, DataCollectType.DTN_CA);
//        json.addProperty(DataCollectType.VN, BuildConfig.VERSION_NAME);
        json.addProperty(HICAR_TY, i);
        String data = json.toString();
        Log.i(TAG, "startHiCar() data: " + data);
        boolean ret = DataCollectManager.getInstance().uploaData(DT_RECOMMEND, data);
        Log.i(TAG, "startHiCar() ret: " + ret);
    }

    private String getUploadTime() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 判断当前的服务初始化了
     */
    public boolean isInit() {
        return initFlag.get();
    }

}
