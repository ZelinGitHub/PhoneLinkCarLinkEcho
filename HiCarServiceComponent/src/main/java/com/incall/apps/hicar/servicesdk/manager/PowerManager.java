package com.incall.apps.hicar.servicesdk.manager;

import android.content.Context;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.ServiceManager;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.openos.virtualcar.VirtualCar;
import com.openos.virtualcar.VirtualCarPropertyCallBack;
import com.openos.virtualcar.VirtualCarPropertyManager;
import com.openos.virtualcar.VirtualServiceReadyListener;
import com.openos.virtualcar.entity.VirtualCarValue;
import com.openos.virtualcar.exception.VirtualCarException;
import com.openos.virtualcar.property.VirtualCarSensorManager;
import com.openos.virtualcar.values.VirtualCarSensorFunction;
//电源管理器
public class PowerManager {
    private final String TAG = PowerManager.class.getSimpleName();

    private VirtualCar mVirtualCar;
    private VirtualCarPropertyManager propertyManager;

    private static class SingletonHolder {
        private static PowerManager instance = new PowerManager();
    }

    public static PowerManager getInstance() {
        return SingletonHolder.instance;
    }


    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mVirtualCar = VirtualCar.createVirtualCar(context, new VirtualServiceReadyListener() {
            @Override
            public void serviceReadySuccess() {
                LogUtil.d(TAG, "init serviceReadySuccess");
                registerVirtualCarListener();
            }

            @Override
            public void serviceReadyFailed() {
                LogUtil.d(TAG, "init serviceReadyFailed");
                unRegisterVirtualCarListener();
            }
        }, null);
        propertyManager = mVirtualCar.getVirtualCarManager(VirtualCar.PROPERTY_SERVICE);
        LogUtil.d(TAG, "init propertyManager = " + propertyManager);
    }

    private int ids[] = {VirtualCarSensorManager.SENSOR_TYPE_IGNITION_STATE};

    private void registerVirtualCarListener() {
        try {
            if (propertyManager != null) {
                propertyManager.register(ids, new VirtualCarPropertyCallBack() {
                    @Override
                    public void onCallBack(VirtualCarValue value) {
                        LogUtil.d(TAG, "registerVirtualCarListener id =  " + value.getFuncId());
                        switch (value.getFuncId()) {
                            case VirtualCarSensorManager.SENSOR_TYPE_IGNITION_STATE:
                                int status = (Integer) value.getValue();
                                /**
                                 * IGNITION_STATE_OFF ：2  off
                                 * IGNITION_STATE_ACC : 3 acc
                                 * IGNITION_STATE_ON :4 on
                                 * IGNITION_STATE_START:5 start
                                 */
                                LogUtil.d(TAG, "registerVirtualCarListener 电源状态  " + status);
                                if (status == VirtualCarSensorFunction.IgnitionStatus.IGNITION_STATE_OFF) {
                                    ServiceManager.getInstance().postEvent(Constants.Event.ACC_OFF, null);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
            }

        } catch (VirtualCarException e) {
            e.printStackTrace();
        }
    }

    private void unRegisterVirtualCarListener() {
        if (propertyManager != null) {
            try {
                propertyManager.unRegister(ids, new VirtualCarPropertyCallBack() {
                    @Override
                    public void onCallBack(VirtualCarValue value) {

                    }
                });
            } catch (VirtualCarException e) {
                e.printStackTrace();
            }
        }
    }

}
