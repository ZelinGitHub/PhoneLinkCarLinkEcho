package com.incall.apps.hicar.servicesdk.manager;

import android.content.Context;
import android.util.Log;

import com.incall.apps.hicar.servicemanager.LogUtil;
import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.utils.CommonUtil;
import com.openos.virtualcar.VirtualCar;
import com.openos.virtualcar.VirtualCarPropertyCallBack;
import com.openos.virtualcar.VirtualCarPropertyManager;
import com.openos.virtualcar.VirtualServiceReadyListener;
import com.openos.virtualcar.entity.VirtualCarValue;
import com.openos.virtualcar.exception.VirtualCarException;
import com.openos.virtualcar.property.VirtualCarSensorManager;
import com.openos.virtualcar.values.VirtualCarSensorFunction;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;

//车管理器
public class CarManager {
    private final String TAG = CarManager.class.getSimpleName();
    private VirtualCar mVirtualCar;
    private VirtualCarPropertyManager propertyManager;

    private static class SingletonHolder {
        private static CarManager instance = new CarManager();
    }

    public static CarManager getInstance() {
        return SingletonHolder.instance;
    }

    public void init(Context context) {
        LogUtil.d(TAG, "init");
        mVirtualCar = VirtualCar.createVirtualCar(context, new VirtualServiceReadyListener() {
            @Override
            public void serviceReadySuccess() {
                propertyManager = mVirtualCar.getVirtualCarManager(VirtualCar.PROPERTY_SERVICE);
                LogUtil.d(TAG, "init serviceReadySuccess propertyManager: " + propertyManager);
                registerVirtualCarListener();
            }

            @Override
            public void serviceReadyFailed() {
                LogUtil.d(TAG, "init serviceReadyFailed");
                unRegisterVirtualCarListener();
            }
        }, null);
    }

    private int ids[] = {VirtualCarSensorManager.SENSOR_TYPE_CURRENT_GEAR, VirtualCarSensorManager.SENSOR_TYPE_CAR_SPEED};

    private int carSpeed = 0;

    private void registerVirtualCarListener() {
        try {
            if (propertyManager != null) {
                propertyManager.register(ids, new VirtualCarPropertyCallBack() {
                    @Override
                    public void onCallBack(VirtualCarValue value) {
                        onVirtualCarValueCallback(value);

                    }
                });
            }

        } catch (VirtualCarException e) {
            e.printStackTrace();
        }
    }

    private void onVirtualCarValueCallback(final VirtualCarValue value) {
        LogUtil.d(TAG, "onVirtualCarValueCallback() id: " + value.getFuncId());
        try {
            switch (value.getFuncId()) {
                case VirtualCarSensorManager.SENSOR_TYPE_CURRENT_GEAR:
                    int gear = (Integer) value.getValue();
                    LogUtil.d(TAG, "onVirtualCarValueCallback()  挡位切换:  " + gear);
                    onGearChange(gear);
                    break;
                case VirtualCarSensorManager.SENSOR_TYPE_CAR_SPEED:
                    carSpeed = Integer.parseInt(value.getValue().toString());
                    LogUtil.d(TAG, "onVirtualCarValueCallback() speed: " + carSpeed);

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
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

    private void onGearChange(int gear) {
        LogUtil.i(TAG, "onGearChange gear=" + gear);
        int resultCode = 0;
        if (gear == VirtualCarSensorFunction.CurrentGear.GEAR_PARK) {
            //停车模式
            resultCode = HiCarServiceManager.getInstance().sendCarData(
                    Contants.HiCarCons.DATA_TYPE_DRIVING_MODE, CommonUtil.getDrivingMode(1));
        } else {
            //行车模式
            resultCode = HiCarServiceManager.getInstance().sendCarData(
                    Contants.HiCarCons.DATA_TYPE_DRIVING_MODE, CommonUtil.getDrivingMode(0));
        }

        //发送挡位数据给carlink
        boolean result = UCarAdapter.getInstance().sendGearStateInfo(new UCarCommon.GearStateInfo(gearTransform(gear), carSpeed));
        LogUtil.i(TAG, "onGearChange resultCode=" + resultCode + "--result: " + result);
    }

    /**
     * 设备连接成功后，主动上报一次当前的档位信息给华为
     * GEAR_NEUTRAL :1 N挡
     * GEAR_REVERSE : 2 R挡
     * GEAR_PARK : 4 P挡
     * GEAR_DRIVE : 8 D挡
     */
    protected void sendGear() {
        int gear = 0;
        try {
            if (propertyManager != null) {
                gear = (int) propertyManager.getValue(VirtualCarSensorManager.SENSOR_TYPE_CURRENT_GEAR, 0);
            }
        } catch (VirtualCarException e) {
            e.printStackTrace();
        }
        LogUtil.i(TAG, "send Gear " + gear);
        CarManager.getInstance().onGearChange(gear);
    }

    /**
     * @param wtGear 梧桐获取到的挡位数据
     * @return 转换后的carlink的挡位值 ：
     * GEAR_PARK 1 停车档
     * GEAR_DRIVE 2 前进挡
     * GEAR_REVERSE 3 倒车挡
     * GEAR_NEUTRAL 4 空挡
     * GEAR_UNKNOWN 0 未知档位
     */
    private UCarCommon.GearState gearState = UCarCommon.GearState.GEAR_UNKNOWN;

    private UCarCommon.GearState gearTransform(int wtGear) {
        switch (wtGear) {
            case VirtualCarSensorFunction.CurrentGear.GEAR_NEUTRAL:
                gearState = UCarCommon.GearState.GEAR_NEUTRAL;
                break;
            case VirtualCarSensorFunction.CurrentGear.GEAR_REVERSE:
                gearState = UCarCommon.GearState.GEAR_REVERSE;
                break;
            case VirtualCarSensorFunction.CurrentGear.GEAR_DRIVE:
                gearState = UCarCommon.GearState.GEAR_DRIVE;
                break;
            case VirtualCarSensorFunction.CurrentGear.GEAR_PARK:
                gearState = UCarCommon.GearState.GEAR_PARK;
                break;
            default:
                gearState = UCarCommon.GearState.GEAR_UNKNOWN;
                break;
        }
        LogUtil.i(TAG, "gearTransform carLinkGearValue: " + gearState);
        return gearState;
    }

    public UCarCommon.GearState getCurrentGear() {
        LogUtil.i(TAG, "getCurrentGear gearState: " + gearState);
        return gearState;
    }

}
