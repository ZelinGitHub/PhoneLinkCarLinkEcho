package com.incall.apps.hicar.servicesdk;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.huawei.hicarsdk.CarConfig;
import com.huawei.hicarsdk.HardwareInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * adapter for generating CarConfig
 *
 * @author zouhongtao
 * @since 2019-08-23
 */
public class HiCarAppAdapter {
    private static final String TAG = "WTPhoneLink/HiCarAppAdapter";

    private volatile static HiCarAppAdapter mHiCarAppAdapter = null;

    private static final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final String PROPERTIRES_PATH = "/system/etc/hicar.properties";

    private static final String MODEL_ID = "ModelId";

    private static final String PHYSICS_WIDTH = "PhysicsWidth";

    private static final String PHYSICS_HEIGHT = "PhysicsHeight";

    private static final String SCREEN_WIDTH = "ScreenWidth";

    private static final String SCREEN_HEIGHT = "ScreenHeight";

    private static final String SCREEN_SIZE = "ScreenSize";

    private static final String DEFAULT_BITRATE = "DefaultBitrate";

    private static final String MAX_BITRATE = "MaxBitrate";

    private static final String MIN_BITRATE = "MinBitrate";


    private static String mBluetoothMac;

    private static String mModuleId;

    private int mPhysicsWidth;

    private int mPhysicsHeight;

    private int mScreenWidth;

    private int mScreenHeight;

    private float mScreenSize;

    private int mDefaultBitrate;

    private int mMaxBitrate;

    private int mMinBitrate;

    private HiCarAppAdapter() {
        getBlueToothMac();
        readConfigFile();
    }

    /**
     * Singleton mode, get instance
     *
     * @return result HiCarAppAdapter
     */
    public static synchronized HiCarAppAdapter getInstance() {
        if (mHiCarAppAdapter == null) {
            mHiCarAppAdapter = new HiCarAppAdapter();
        }
        return mHiCarAppAdapter;
    }

    /**
     * create CarConfig with Bluetooth Mac and ModelId
     *
     * @return object CarConfig
     */
    public CarConfig createBasicCarConfig() {
        Log.i(TAG, "createBasicCarConfig()");
        if (mBluetoothMac == null || mBluetoothMac.isEmpty()) {
            Log.i(TAG, "createBasicCarConfig() call getBlueToothMac()");
            getBlueToothMac();
            Log.i(TAG, "createBasicCarConfig() call readConfigFile()");
            readConfigFile();
        }
        CarConfig.Builder builder = new CarConfig.Builder();
        if ((mBluetoothMac != null) && !mBluetoothMac.isEmpty()) {
            builder.withBrMac(mBluetoothMac);
        }
        if ((mModuleId != null) && !mModuleId.isEmpty()) {
            builder.withModeId(mModuleId);

        }
        if (mPhysicsWidth != 0) {
            builder.withPhysicsWidth(mPhysicsWidth);
        }
        if (mPhysicsHeight != 0) {
            builder.withPhysicsHeight(mPhysicsHeight);
        }
        if (mScreenWidth != 0) {
            builder.withScreenWidth(mScreenWidth);
        }
        // TODO: 2023/9/24  屏幕高度
        if (mScreenHeight != 0) {
            if (mScreenHeight != 848) {
                //dock栏高度：120
                //状态栏高度：112
                builder.withScreenHeight(mScreenHeight - 120 - 112);
            } else {
                builder.withScreenHeight(mScreenHeight);
            }
        }
        if (mScreenSize > 0.0f) {
            builder.withScreenSize(mScreenSize);
        }

        if (mDefaultBitrate != 0) {
            builder.withDefaultBitrate(mDefaultBitrate);
        } else {
            builder.withDefaultBitrate(2 * 1000 * 1000);
        }

        if (mMaxBitrate != 0) {
            builder.withMaxBitrate(mMaxBitrate);
        } else {
            builder.withMaxBitrate(20 * 1000 * 1000);
        }

        if (mMinBitrate != 0) {
            builder.withMinBitrate(mMinBitrate);
        } else {
            builder.withMinBitrate(500 * 1000);
        }
        builder.withSupportWireless(true);//支持无线连接
        builder.withSupportUsb(true);//支持USB连接
        builder.withSupportReconnect(true);//启用自动连接功能

        HardwareInfo hardwareInfo = new HardwareInfo("changan", "QCA1023", "QCA1023");
        builder.withHardwareInfo(hardwareInfo);
        Map<String, String> config = new HashMap<String, String>();
        config.put("DRIVING_POSITION", "left");//left right
        config.put("CAR_BRAND", "长安主页");
        config.put("DAY_NIGHT_MODE", "day");//night day notsupport tobeconfirmed

        //设置广播信号强度 广播信号强度的取值范围为-128 ~ 127 之间,数值越大，信号越强。
        int advPower = -112; //手机靠近车机约35cm 可以发现车机
        builder.withAdvPower(advPower);
        builder.withInitialConfig(config);
        builder.withIgnoreAndroidCamera(true);
        return builder.build();
    }

    @SuppressLint("HardwareIds")
    private void getBlueToothMac() {
        Log.i(TAG, "getBlueToothMac() ");
        if (mBluetoothMac != null) {
            return;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothMac = mBluetoothAdapter.getAddress();
        } else {
            Log.e(TAG, "getBlueToothMac()mBluetoothAdapter is null! ");
        }
        Log.i(TAG, "getBlueToothMac() mBluetoothMac: " + mBluetoothMac);
    }

    //从配置文件拿到宽度和高度
    //设置hicar投屏分辨率
    private void readConfigFile() {
        Log.i(TAG, "readConfigFile()");
        File file = new File(PROPERTIRES_PATH);
        if (!file.exists()) {
            Log.e(TAG, "readConfigFile() File of Hicar is not exists!! ");
            return;
        }
        InputStream is = null;
        try {
            try {
                is = Files.newInputStream(file.toPath());
                Properties prop = new Properties();
                prop.load(is);
                mModuleId = prop.getProperty(MODEL_ID);
                if ((mModuleId == null) || mModuleId.isEmpty()) {
                    Log.e(TAG, "readConfigFile() modelId is null! ");
                } else {
                    Log.i(TAG, "readConfigFile() moduleId: " + mModuleId);
                }

                String physicsWidth = prop.getProperty(PHYSICS_WIDTH);
                if ((physicsWidth == null) || physicsWidth.isEmpty()) {
                    Log.e(TAG, "readConfigFile() physicsWidth is invalid! ");
                } else {
                    mPhysicsWidth = Integer.parseInt(physicsWidth);
                    Log.i(TAG, "readConfigFile() physicsWidth: " + physicsWidth);
                }

                String physicsHeight = prop.getProperty(PHYSICS_HEIGHT);
                if ((physicsHeight == null) || physicsHeight.isEmpty()) {
                    Log.e(TAG, "readConfigFile() physicsHeight is invalid! ");
                } else {
                    mPhysicsHeight = Integer.parseInt(physicsHeight);
                    Log.i(TAG, "readConfigFile() physicsHeight: " + physicsHeight);
                }

                String screenWidth = prop.getProperty(SCREEN_WIDTH);
                if ((screenWidth == null) || screenWidth.isEmpty()) {
                    Log.e(TAG, "readConfigFile() screenWidth is invalid! ");
                } else {
                    mScreenWidth = Integer.parseInt(screenWidth);
                    Log.i(TAG, "readConfigFile() screenWidth: " + screenWidth);
                }

                String screenHeight = prop.getProperty(SCREEN_HEIGHT);
                if ((screenHeight == null) || screenHeight.isEmpty()) {
                    Log.e(TAG, "readConfigFile() screenHeight is invalid! ");
                } else {
                    mScreenHeight = Integer.parseInt(screenHeight);
                    Log.i(TAG, "readConfigFile() screenHeight: " + screenHeight);
                }

                String screenSize = prop.getProperty(SCREEN_SIZE);
                if ((screenSize == null) || screenSize.isEmpty()) {
                    Log.e(TAG, "readConfigFile() screenSize is invalid! ");
                } else {
                    mScreenSize = Float.parseFloat(screenSize);
                    Log.i(TAG, "readConfigFile() screenSize: " + screenSize);
                }

                String defaultBitrate = prop.getProperty(DEFAULT_BITRATE);
                if ((defaultBitrate == null) || defaultBitrate.isEmpty()) {
                    Log.e(TAG, "readConfigFile() defaultBitrate is invalid! ");
                } else {
                    mDefaultBitrate = Integer.parseInt(defaultBitrate);
                    Log.i(TAG, "readConfigFile() defaultBitrate: " + defaultBitrate);
                }

                String maxBitrate = prop.getProperty(MAX_BITRATE);
                if ((maxBitrate == null) || maxBitrate.isEmpty()) {
                    Log.e(TAG, "readConfigFile() maxBitrate is invalid! ");
                } else {
                    mMaxBitrate = Integer.parseInt(maxBitrate);
                    Log.i(TAG, "readConfigFile() maxBitrate: " + maxBitrate);
                }

                String minBitrate = prop.getProperty(MIN_BITRATE);
                if ((minBitrate == null) || minBitrate.isEmpty()) {
                    Log.e(TAG, "readConfigFile() minBitrate is invalid! ");
                } else {
                    mMinBitrate = Integer.parseInt(minBitrate);
                    Log.i(TAG, "readConfigFile() minBitrate: " + minBitrate);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "readConfigFile() " + e);
        }
    }
}
