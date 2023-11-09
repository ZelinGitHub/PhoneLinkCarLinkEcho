package com.wt.phonelink.carlink;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.incall.apps.hicar.servicesdk.contants.Contants;
import com.incall.apps.hicar.servicesdk.manager.BTManager;
import com.incall.apps.hicar.servicesdk.manager.CarManager;
import com.incall.apps.hicar.servicesdk.servicesimpl.audio.HcAudioManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.openos.skin.WTSkinManager;
import com.openos.skin.info.SkinInfo;
import com.ucar.sdk.BuildConfig;
import com.ucar.vehiclesdk.ICameraInfoListener;
import com.ucar.vehiclesdk.ICarAudioRecorderListener;
import com.ucar.vehiclesdk.ICarConnectListener;
import com.ucar.vehiclesdk.ICarInitCallback;
import com.ucar.vehiclesdk.IPhoneDataListener;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.ucar.vehiclesdk.UCarConfig;
import com.ucar.vehiclesdk.UCarSurfaceView;
import com.ucar.vehiclesdk.camera.AbstractCamera;
import com.wt.phonelink.AppConfig;
import com.wt.phonelink.LaunchFloatingService;
import com.wt.phonelink.MainActivity;
import com.wt.phonelink.MyApplication;
import com.wt.phonelink.R;
import com.wt.phonelink.VoiceManager;
import com.wt.phonelink.carlink.widget.UCarAlertView;
import com.wt.phonelink.carlink.widget.UCarPinCodeView;
import com.wt.phonelink.carlink.widget.UCarProgressBar;
import com.wt.phonelink.utils.CommonUtil;
import com.wt.phonelink.utils.VoiceUtils;
import com.wt.phonelink.utils.WTStatisticsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.ucar.vehiclesdk.UCarConnectState.ConnectType.CONNECT_TYPE_WIFI_P2P;
import static com.ucar.vehiclesdk.UCarConnectState.ConnectType.CONNECT_TYPE_WIFI_SOFT_AP;
import static com.ucar.vehiclesdk.UCarConnectState.DISCONNECT_BY_USER;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_CAST_CONNECTED;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_CAST_DISCONNECTED;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_CONNECTED;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_CONNECT_FAILED;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_DISCONNECT;
import static com.ucar.vehiclesdk.UCarConnectState.UCAR_DEVICE_GO_TO_HOME;
import static com.wt.phonelink.Contants.IS_CARLINK_FRONT;

import wtcl.lib.theme.WTThemeManager;
import wtcl.lib.widget.WTButton;

//carLink的连接界面
public class CarLinkMainActivity extends PermissionsReqActivity {
    private static final String TAG = "WTPhoneLink/CarLinkMainActivity";
    private ICarAudioRecorderListener mAudioRecorderListener;

    public static final int MSG_ON_CONNECT_STATE_CHANGED = 101;
    public static final int MSG_START_ADVERTISE_SCAN_DEVICE = 102;
    public static final int MSG_STOP_ADVERTISE_SCAN_DEVICE = 103;
    public static final int MSG_SHOW_PROGRESS = 104;
    public static final int MSG_CLOSE_PROGRESS = 105;
    public static final int MSG_SHOW_PIN_CODE = 106;

    //    public static final int DISPLAY_DENSITY_DPI = 320;
    public static final int DISPLAY_FPS = 30;
    public static final int START_CAST_DELAY = 100;

    //    private static final int ALIGNMENT = 16;
    private static final int CAR_ID_SIZE = 6;
    private static final int PROCESS_0 = 0;
    private static final int PROCESS_COMPLETE = 100;

    private static final String CAR_ID_PREFS = "car_id_prefs";
    private static final String CAR_ID_KEY = "car_id_key";
    private static final String CAMERA_ID = "1";
    private static final String CAMERA_NAME = "Car_Camera_1";

    private static boolean sIsServiceInitialized = false;
    private UCarConfig mUCarConfig;
    private UCarProgressBar mProgress;
    private boolean mIsProgressShown;
    //输入连接码的控件
    private UCarPinCodeView mPinCodeView;
    private TextView mDeviceName;

    private FrameLayout mRootLayout;
    private WTButton btn_exit;
    private View castManager;
    private View castLayout;

    private boolean mIsCastSucceed = false;
    private boolean mIsPendingCheckExitProcess = false;

    private int mFullDisplayWidth;
    private int mFullDisplayHeight;
    private int mExpectedDisplayWidth;
    private int mExpectedDisplayHeight;
    private int mDisplayDensityDpi;

    private ICarConnectListener mConnectCallback;
    private IPhoneDataListener mPhoneDataCallback;
    private ICameraInfoListener mCameraInfoCallback;
    private final UCarCommon.PhoneStateInfo mPhoneStateInfo = new UCarCommon.PhoneStateInfo();

    //carlink的surfaceView控件
    private UCarSurfaceView mSurfaceView;
    private SurfaceHolder.Callback mSurfaceHolderCallback;

    private UCarAlertView mDialog;
    private SharedPreferencesUtil mSharedPreferencesUtil;

    private DragImageView mCloseCastBtn;
    private boolean mIsCloseCastBtnMoving;
    private int mLastTouchX, mLastTouchY;
    private UCarCommon.CallInfo mCallInfo;
    private volatile boolean mIsClose = false;
    private TextView tv_sdk_version;
    WTSkinManager.SkinChangedListener mSkinChangedListener = (newInfo, previousInfo) -> {
        Log.i(TAG, "onSkinChanged() ");
        getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        updateSkin();
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //连接状态改变
                case MSG_ON_CONNECT_STATE_CHANGED:
                    handleConnectStateChangedMessage(msg);
                    break;
                case MSG_START_ADVERTISE_SCAN_DEVICE:
                    Log.d(TAG, "handleMessage() msg.what: MSG_START_ADVERTISE_SCAN_DEVICE");
                    //开始蓝牙设备扫描（用来查找设备）
                    UCarAdapter.getInstance().startAdvertise();
                    UCarAdapter.getInstance().enableUsbDeviceDetection(true);
                    break;
                case MSG_STOP_ADVERTISE_SCAN_DEVICE:
                    //停止蓝牙设备扫描
                    UCarAdapter.getInstance().stopAdvertise();
                    UCarAdapter.getInstance().enableUsbDeviceDetection(false);
                    break;
                //显示进度
                case MSG_SHOW_PROGRESS:
                    Bundle data = (Bundle) msg.obj;
                    String modelName = data.getString("NAME");
                    int progress = data.getInt("PROGRESS");

                    if (progress == PROCESS_0) {
                        mHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
                    } else if (progress == PROCESS_COMPLETE) {
                        mHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
                    } else {
                        mIsProgressShown = true;
                        mProgress.updateProgress(modelName, progress);
                        mProgress.show();
                        // 底层可能会尝试自动连接，如果有其它提示弹窗，需要取消显示
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    }
                    break;
                case MSG_CLOSE_PROGRESS:
                    mProgress.hide();
                    mIsProgressShown = false;
                    break;
                //显示连接码的消息
                case MSG_SHOW_PIN_CODE:
                    Bundle args = (Bundle) msg.obj;
                    String pinCode = args.getString("PIN_CODE");
                    String deviceName = args.getString("DEVICE_NAME");
                    //设置连接码到控件
                    mPinCodeView.setPinCode(pinCode);
                    mDeviceName.setText(deviceName);
                    break;
                default:
                    break;
            }
        }
    };

    private void updateSkin() {
        String packageName = WTSkinManager.get().getCurrentSkinInfo().getSkinPackageName();
        Log.d(TAG, "updateSkin() packageName: " + packageName);
        WTThemeManager.setSkinPkgName(packageName);
        WTThemeManager.setResources(WTSkinManager.get().getProxyResources());
        btn_exit.applyTheme();
        btn_exit.setTextColor(WTSkinManager.get().getColor(R.color.app_name_color));
        mRootLayout.setBackgroundColor(WTSkinManager.get().getColor(R.color.background));
    }


    @Override
    protected String[] getPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };
    }

    @Override
    protected void onPermissionsGranted() {
        setActions();
    }

    private void setActions() {
        btn_exit.setOnClickListener(view -> {
            mProgress.hide();
            finish();
            Log.i(TAG, "onClick() click btn_exit");
            IS_CARLINK_FRONT = false;
        });
        if (mCloseCastBtn != null) {
            mCloseCastBtn.setOnClickListener(v -> {
                mIsClose = true;
                castManager.setVisibility(View.GONE);
                //断开直播
                Log.i(TAG, "onClick() click closeCastBtn");
                //通知手机，车机端投屏需要暂停
                //调用这个方法直接就会断开连接，并退出当前activity了
                UCarAdapter.getInstance().disconnect();
                mProgress.hide();
                IS_CARLINK_FRONT = false;
            });
            mCloseCastBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.i(TAG, "onTouch() mCloseCastBtn ACTION_DOWN");
                            mIsCloseCastBtnMoving = false;
                            if (event.getPointerCount() > 1) {
                                return false;
                            }
                            // 屏幕移动获取移动初始点
                            mLastTouchX = (int) (event.getRawX());
                            mLastTouchY = (int) (event.getRawY());
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int distanceX = (int) (event.getRawX()) - mLastTouchX;
                            int distanceY = (int) (event.getRawY()) - mLastTouchY;
                            if (Math.abs(distanceX) > 5 || Math.abs(distanceY) > 5) {
                                mIsCloseCastBtnMoving = true;
                                moveView(mCloseCastBtn, distanceX, distanceY);
                                //随着手指移动，更新手指坐标
                                mLastTouchX = (int) (event.getRawX());
                                mLastTouchY = (int) (event.getRawY());
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.i(TAG, "onTouch() mCloseCastBtn ACTION_UP mIsCloseCastBtnMoving: " + mIsCloseCastBtnMoving);
                            if (mIsCloseCastBtnMoving) {
                                mIsCloseCastBtnMoving = false;
                            } else {
                                mCloseCastBtn.performClick();
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            mIsCloseCastBtnMoving = false;
                            Log.w(TAG, "onTouch() mCloseCastBtn ACTION_CANCEL");
                    }
                    return true;
                }
            });
        }
    }

    protected void moveView(View view, int distanceX, int distanceY) {
        float x = view.getX();
        float y = view.getY();
        x += distanceX;
        y += distanceY;
        int left = getLeftBoundary();
        int right = getRightBoundary();
        int top = getTopBoundary();
        int bottom = getBottomBoundary();
        if (x < left) {
            x = left;
        }
        if (x > right) {
            x = right;
        }
        if (y < top) {
            y = top;
        }
        if (y > bottom) {
            y = bottom;
        }
        view.setX(x);
        view.setY(y);
    }


    private int getLeftBoundary() {
        return 0;
    }

    private int getTopBoundary() {
        return 0;
    }

    private int getRightBoundary() {
        return castLayout.getWidth() - mCloseCastBtn.getWidth();
    }


    private int getBottomBoundary() {
        return AppConfig.getDisplayHeight() - AppConfig.getDockHeight() - mCloseCastBtn.getHeight();
    }


    @Override
    protected void onPermissionsDenied() {
        finish();
        IS_CARLINK_FRONT = false;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_ucar_cast);
        WTSkinManager.get().addSkinChangedListener(mSkinChangedListener);
        mSharedPreferencesUtil = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        initViewRefs();
        initUI();
    }

    private void initViewRefs() {
        mRootLayout = findViewById(R.id.fl_root);
        mProgress = new UCarProgressBar(this, mRootLayout);
        tv_sdk_version = findViewById(R.id.tv_sdk_version);
        //carlink的surfaceView控件
        mSurfaceView = this.findViewById(R.id.surface_view);
        mCloseCastBtn = this.findViewById(R.id.iv_close);
        btn_exit = findViewById(R.id.btn_exit);
        castManager = findViewById(R.id.cl_ucar_connect_container);
        castLayout = findViewById(R.id.rl_cast_player);
        mDeviceName = findViewById(R.id.tv_device_name);
        mPinCodeView = findViewById(R.id.upc_pin_code);
    }

    private void initUI() {
        getWindow().setStatusBarColor(WTSkinManager.get().getColor(R.color.background));
        WTStatisticsUtil.showHomePage(1);
        tv_sdk_version.setText(BuildConfig.SDK_VERSION);
        updateSkin();
    }


    private void registerCarEventListener() {
        //连接回调对象
        mConnectCallback = new ICarConnectListener() {
            //carlink sdk回调 返回连接码的回调
            @Override
            public void onPinCode(String pinCode, String carDeviceName) {
                Log.i(TAG, "onPinCode() pin code: " + pinCode + ", device name: " + carDeviceName);
                //发送显示连接码的消息
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_PIN_CODE;
                Bundle args = new Bundle();
                args.putString("PIN_CODE", pinCode);
                args.putString("DEVICE_NAME", carDeviceName);
                msg.obj = args;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onConnectingProgress(String deviceID, int progress) {
                Log.i(TAG, "onConnectingProgress() progress: " + progress + ", deviceID: " + deviceID);
                //根据ID获取连接的手机信息，参考MobileDevice。
                //返回值:
                //NULL: 无此ID指定的设备
                //UCarCommon.MobileDevice: 手机的详细信息
                //注：SDK V1.5.0 增加
                UCarCommon.MobileDevice mobileDevice = UCarAdapter.getInstance().getMobileDevice(deviceID);
                String phoneDeviceName = "";
                if (mobileDevice != null) {
                    phoneDeviceName = mobileDevice.getModel();
                }
                Bundle data = new Bundle();
                data.putString("ID", deviceID);
                data.putString("NAME", phoneDeviceName);
                data.putInt("PROGRESS", progress);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_PROGRESS;
                msg.obj = data;
                mHandler.sendMessage(msg);
                //phoneDeviceName:VIVO X60
                String[] phoneDeviceArrays = phoneDeviceName.split(" ");
                String phoneDevice = "";
                String phoneModelStr = "";
                if (phoneDeviceArrays.length >= 2) {
                    phoneDevice = phoneDeviceArrays[0];
                    phoneModelStr = phoneDeviceArrays[1];
                } else {
                    phoneDevice = phoneDeviceName;
                }
                mSharedPreferencesUtil.putString(Contants.SP_PHONE_BRAND, phoneDevice);
                mSharedPreferencesUtil.putString(Contants.SP_PHONE_MODEL, phoneModelStr);
            }


            //carlink sdk回调 连接状态改变
            //state可能是
            //UCAR_DEVICE_DISCONNECT
            //UCAR_DEVICE_CONNECTED
            //UCAR_DEVICE_CAST_CONNECTED
            //UCAR_DEVICE_CAST_DISCONNECTED
            @Override
            public void onConnectStateChanged(String deviceID, int state, int parameter) {
                //设备id
                Log.i(TAG, "onConnectStateChanged() deviceID: " + deviceID + ",state :" + state + ", parameter:" + parameter);
                Bundle data = new Bundle();
                data.putString("ID", deviceID);
                data.putInt("STATE", state);
                data.putInt("PARAMETER", parameter);

                Message msg = Message.obtain();
                //发送连接状态改变的消息
                msg.what = MSG_ON_CONNECT_STATE_CHANGED;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public UCarCommon.WorkMode onSelectWorkMode(String s, UCarCommon.WorkMode workMode) {
                return null;
            }
        };
        //注册连接对象
        UCarAdapter.getInstance().registerCarConnectListener(mConnectCallback);

        mPhoneDataCallback = new IPhoneDataListener() {
            @Override
            public void onPhoneStateInfoReceived(String deviceID, UCarCommon.PhoneStateInfo phoneStateInfo) {
                Log.i(TAG, "onPhoneStateInfoReceived() ");
                handlePhoneStateInfoReceived(phoneStateInfo);
            }

            //注：SDK V1.5.0 更改返回类型由原生AudioAttributes 变更为UCarCommon.AudioAttributes
            @Override
            public UCarCommon.AudioAttributes getAudioAttributesByType(UCarCommon.AudioType type) {
                //如果该方法有修改需要及时同步到OPPO研发以备他们后续分析使用
                // 车厂需要根据车机实际情况，基于传入的type类型，设置合适的Usage与ContentType，
                // UCar根据返回的AudioAttributes推定音频焦点申请策略，如果返回null，将默认使用
                // Usage为USAGE_MEDIA，ContentType为CONTENT_TYPE_MUSIC，推定音频焦点为AUDIOFOCUS_GAIN
                Log.d(TAG, "getAudioAttributesByType() type：" + type);
                int usage;
                int contentType;
                switch (type) {
                    case STREAM_IP_CALL:
                    case STREAM_MODEM_CALL:
                    case STREAM_RING:
                        usage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                        contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
                        break;
                    case STREAM_AI_ASSISTANT:
                    case STREAM_NOTIFICATION:
                    case STREAM_TTS:
                    case STREAM_SYSTEM:
                    case STREAM_CAST_MUSIC:
                    default:
                        usage = AudioAttributes.USAGE_MEDIA;
                        contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
                        break;
                }
                return new UCarCommon.AudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(usage)
                                .setContentType(contentType)
                                .build()
                        , usage, contentType
                );
            }

            @Override
            public void onMusicInfoReceived(String deviceID, UCarCommon.MusicInfo info) {
                // 车厂可以获取当前手机音乐播放详细信息，可以作为卡片显示在车机原生桌面
                // 请处理与本地音乐的互斥
                if (info.isPlaying()) {
                    Log.i(TAG, "onMusicInfoReceived() ucar music is playing");
                } else {
                    Log.i(TAG, "onMusicInfoReceived() ucar music is idle");
                }
            }

            @Override
            public void onNavigationInfoReceived(String deviceID, UCarCommon.NavigationInfo info) {
                // 车厂可以获取当前手机地图导航详细信息，可以作为卡片显示在车机原生桌面
                // 请处理与本地导航的互斥
                if (info.isNavigating()) {
                    HcAudioManager.getInstance().stopNavi();
                    Log.i(TAG, "onNavigationInfoReceived() ucar navigation is active");
                } else {
                    Log.i(TAG, "onNavigationInfoReceived() ucar navigation is idle");
                }
            }

            //当Carlink音频焦点发生变化时回调，车机如果有音源控制，需要将音源设置为Carlink
            //参数类型说明
            //type UCarCommon.AudioType 需要申请焦点的音频流类型
            //int focusChange 音频焦点发生变化时的状态，详见音频焦点状态定义
            //注：SDK V1.5.0 增加
            @Override
            public void onAudioFocusChanged(UCarCommon.AudioType audioType, int i) {
                Log.d(TAG, "onAudioFocusChanged()");
            }

            //当手机端可投屏的应用列表发生改变时
            //参数类型说明
            //deviceId String 手机ID
            //appIds List<Integer>
            //手机端应用的ID集，同一种状态变化的应用可以同时发送
            //state UCarCommon.AppListState 手机应用安装状态
            //注：SDK V1.5.0 增加
            @Override
            public void onAppListStateChanged(String s, List<Integer> list, UCarCommon.AppListState appListState) {
                Log.d(TAG, "onAppListStateChanged()");
            }

            //注：SDK V1.5.0 增加
            @Override
            public void onAppStateChanged(String s, int i, boolean b, int i1, int i2, UCarCommon.AppState appState) {
                Log.d(TAG, "onAppStateChanged()");
            }

            //注：SDK V1.5.0 增加
            @Override
            public void onCallInfoReceived(String s, UCarCommon.CallInfo callInfo) {
                Log.d(TAG, "onCallInfoReceived()");
                //保存当前的通话状态
                mCallInfo = callInfo;
            }

            //注：SDK V1.5.0 增加
            @Override
            public void onPOIReceived(String s, UCarCommon.POIAddress poiAddress) {
                Log.d(TAG, "onPOIReceived()");
            }
        };
        UCarAdapter.getInstance().registerPhoneDataListener(mPhoneDataCallback);

        if (mUCarConfig.isSupportCamera()) {
            mCameraInfoCallback = new ICameraInfoListener() {
                @Override
                public ArrayList<UCarCommon.CameraInfo> getAndroidCameraInfo() {
                    ArrayList<UCarCommon.CameraInfo> cameraInfoList = new ArrayList<>();
                    UCarCommon.CameraInfo cameraDriver =
                            new UCarCommon.CameraInfo(CAMERA_ID,
                                    CAMERA_NAME,
                                    UCarCommon.LensFacing.LENS_FACING_BACK);
                    cameraInfoList.add(cameraDriver);
                    return cameraInfoList;
                }

                @Override
                public ArrayList<AbstractCamera> getNativeCamera() {
                    // add user-define external camera, excluding android builtin camera
                    return null;
                }
            };
            UCarAdapter.getInstance().registerCameraInfoListener(mCameraInfoCallback);
        }
        if (mUCarConfig.isUseCustomAudioRecord()) {
            mAudioRecorderListener = new CarlinkCustomAudioRecord();
            UCarAdapter.getInstance().registerAudioRecorderListener(mAudioRecorderListener);
        }
    }


    //处理连接状态改变的消息
    private void handleConnectStateChangedMessage(Message msg) {
        Log.i(TAG, "handleConnectStateChangedMessage() msg: " + msg);
        Bundle args = (Bundle) msg.obj;
        //设备ID
        String deviceID = args.getString("ID");
        //连接状态
        int state = args.getInt("STATE");
        int parameter = args.getInt("PARAMETER");

        String phoneBrand = mSharedPreferencesUtil.getString(Contants.SP_PHONE_BRAND);
        String phoneModel = mSharedPreferencesUtil.getString(Contants.SP_PHONE_MODEL);

        Log.i(TAG, "handleConnectStateChangedMessage() state: " + state);

        switch (state) {
            //设备断开连接
            case UCAR_DEVICE_DISCONNECT:
                boolean isDisconnectByUser = (parameter == DISCONNECT_BY_USER);
                Log.i(TAG, "handleConnectStateChangedMessage() ucar device disconnected, is disconnect by user : " +
                        isDisconnectByUser);
                handleDisconnected(deviceID, isDisconnectByUser);

                if (isDisconnectByUser) {
                    WTStatisticsUtil.connectStatus(Contants.SERVER_BREAKLINK, 2, phoneBrand, phoneModel);
                }
                mSharedPreferencesUtil.putBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
                //连接断开，打开蓝牙
                BTManager.getInstance().bluetoothOn();
                CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 0);
                stopLauncherFloater();
                VoiceManager.getInstance().unRegisterWakeUp();
                break;
            //设备已连接
            case UCAR_DEVICE_CONNECTED:
                //TODO:
                // 1、隐藏进度条
                // 2、连接成功后，请断开与手机蓝牙的HFP与A2DP Profile
                // 3、系统保证不要在投屏连接期间自动发起蓝牙回连操作
                Log.i(TAG, "handleConnectStateChangedMessage() ucar device connected");
                BTManager.getInstance().bluetoothOff();

                if (IS_CARLINK_FRONT) {
                    // 如果在前台连接成功，需要设置允许获取焦点
                    UCarAdapter.getInstance().allowGainAudioFocus();
                } else {
                    // 如果在后台连接成功，需要主动通知手机端暂停推流
                    UCarAdapter.getInstance().pauseCast();
                }
                VoiceManager.getInstance().registerCarLinkWakeUp();
                break;
            //连接失败
            case UCAR_DEVICE_CONNECT_FAILED:
                // 连接失败，需要根据错误码引导用户确认手机端蓝牙是否开启，或者USB线是否插入正确
                handleConnectFailed(parameter);
                break;
            //投屏连接
            case UCAR_DEVICE_CAST_CONNECTED:
                Log.d(TAG, "handleConnectStateChangedMessage() cast connected");
                Log.d(TAG, "handleConnectStateChangedMessage() sIsServiceInitialized: " + sIsServiceInitialized);
                mIsCastSucceed = true;
                mIsPendingCheckExitProcess = true;
                setCastSurfaceVisibility(true);
                //关闭进度条
                mHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
                WTStatisticsUtil.connectStatus(Contants.SERVER_LINK, 2, phoneBrand, phoneModel);
                //保存连接状态到sp
                mSharedPreferencesUtil.putBoolean(Contants.SP_IS_CARLINK_CONNECT, true);
                CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 1);
                break;
            //投屏断开连接
            case UCAR_DEVICE_CAST_DISCONNECTED:
                Log.d(TAG, "handleConnectStateChangedMessage() cast disconnected");
                mIsCastSucceed = false;
                setCastSurfaceVisibility(false);
                WTStatisticsUtil.connectStatus(Contants.SERVER_BREAKLINK, 2, phoneBrand, phoneModel);
                mSharedPreferencesUtil.putBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
                CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 0);
                break;
            //回到车机桌面
            case UCAR_DEVICE_GO_TO_HOME:
                // UCar请求回到车机桌面
                Log.d(TAG, "handleConnectStateChangedMessage() 回到车机桌面");
                backToHome();
                break;
            default:
                break;
        }
    }

    private void handleDisconnected(String deviceID, boolean isDisconnectByUser) {
        if (mIsPendingCheckExitProcess) {
            mIsPendingCheckExitProcess = false;
            int connectType = UCarAdapter.getInstance().getConnectType(deviceID);

            // 当无线投屏时，非用户主动断开而出现投屏异常断开，需要等待重连，否则退出进程
            setCastSurfaceVisibility(false);
            boolean isWireless = connectType == CONNECT_TYPE_WIFI_P2P || connectType == CONNECT_TYPE_WIFI_SOFT_AP;
            if (!isDisconnectByUser && isWireless) {
                Log.i(TAG, "prepare for auto reconnection");
                // 自SDK V1.2.11以后版本，SDK内部将不再在异常断开场景下重新发广播和开启USB设备发现
                // 因此HMI开发者需要根据自身实际在合适的时机调用发广播和开启USB设备发现的逻辑
                mHandler.sendEmptyMessage(MSG_START_ADVERTISE_SCAN_DEVICE);
            } else {
                // 此默认处理不会在后台进行蓝牙广播与USB扫描，所以如果用户此时使用手机靠近车机或者用USB连接手机与车机，将是无反应的，
                // 后续用户主动点击ICCOA CarLink图标拉起activity后才能重新发起连接。这里也可以直接调用finish()退出activity，从而退出进程
                Log.i(TAG, "move the activity to the end of its task stack");
                finish();
                IS_CARLINK_FRONT = false;
            }
        }
        if (mIsProgressShown) {
            mHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
        }
    }

    /**
     * @param errorCode :
     *                  ERROR_INIT_FAILED                10001 SDK初始化失败
     *                  ERROR_INVALID_PARAMETER          10002 无效的参数
     *                  ERROR_WIRELESS_CONNECT_FAILED    10003 无线连接失败
     *                  ERROR_USB_CONNECT_FAILED         10004 USB连接失败
     *                  ERROR_START_CAST_FAILED          10005 投屏通道创建失败
     *                  ERROR_CONNECT_TIMEOUT            10006 连接超时
     *                  ERROR_USER_INTERVENTION_TIMEOUT  10007 用户确认超时
     *                  ERROR_CONNECT_INNER_FAILED       10008 内部错误
     */
    private void handleConnectFailed(int errorCode) {
        Log.i(TAG, "ucar connect failed, error code: " + errorCode);
        if (mDialog != null) {
            mDialog.show();
            return;
        }
        // 连接失败，请使用弹窗引导用户恢复连接
        String failedTitle = getString(R.string.connect_failed);
        String message = "\n" + getString(R.string.wireless_reconnect_method) + "\n" +
                getString(R.string.usb_reconnect_method) + "\n" +
                getString(R.string.need_user_intervention_method) + "\n" +
                getString(R.string.unsupport_wireless_method) + "\n" +
                getString(R.string.restart_car_method) + "\n";
        Log.i(TAG, "error message: " + message);
        if (mIsProgressShown) {
            mHandler.sendEmptyMessage(MSG_CLOSE_PROGRESS);
        }
        mDialog = UCarAlertUtil.makeDialog(this,
                R.layout.dialog_connect_failed_notice,
                0,
                failedTitle,
                message,
                getString(R.string.reconnect),
                getString(R.string.cancel),
                () -> {
                    // 如果用户点击弹窗的"重连"按钮则重新发起连接
                    mHandler.sendEmptyMessage(MSG_START_ADVERTISE_SCAN_DEVICE);
                    return false;
                },
                () -> {
                    finish();
                    IS_CARLINK_FRONT = false;
                    return false;
                });
        mDialog.show();
        Log.i(TAG, "handleConnectFailed() mDialog: " + mDialog + ", mDialog.isShowing: " + mDialog.isShowing());
    }

    private void handlePhoneStateInfoReceived(UCarCommon.PhoneStateInfo phoneStateInfo) {
        Log.d(TAG, "handlePhoneStateInfoReceived() phoneStateInfo: " + phoneStateInfo.toString());
        boolean isUseMic = phoneStateInfo.isUseMicrophone();
        mHandler.post(() -> {
            if (isUseMic != mPhoneStateInfo.isUseMicrophone()) {
                if (phoneStateInfo.isUseMicrophone()) {
                    //TODO:
                    // 请通知占用MIC的其它程序(如语音助手)立即释放MIC
                    Log.i(TAG, "ucar request use mic");
                } else {
                    // 请通知其它程序MIC已经被释放，它可以继续使用
                    Log.i(TAG, "showViewByKeyWord  ucar has released mic");
                    //mic释放后，启用讯飞
                    VoiceUtils.getInstance().stopOrResumeVr(true);
                }
                mPhoneStateInfo.setUseMicrophone(isUseMic);
            }

            UCarCommon.CallState modemCallState = phoneStateInfo.getModemCallState();
            if (modemCallState != mPhoneStateInfo.getModemCallState()) {
                // 请处理与本地Modem或者蓝牙电话的互斥
                if (modemCallState == UCarCommon.CallState.INCOMING
                        || modemCallState == UCarCommon.CallState.DIALING
                        || modemCallState == UCarCommon.CallState.ALERTING
                        || modemCallState == UCarCommon.CallState.ACTIVE
                        || modemCallState == UCarCommon.CallState.HOLDING
                ) {
                    // 如果UCar在后台，请恢复到前台显示
                    Log.i(TAG, "modem call state changed, modemCallState: " + modemCallState);

                    //carlink已投屏来电，手机互联不在前台且当前不是倒车档才能拉起投屏界面
                    if (!IS_CARLINK_FRONT && CarManager.getInstance().getCurrentGear() != UCarCommon.GearState.GEAR_REVERSE) {
                        Log.i(TAG, "modem call state changed, 不在前台 需要拉起");
                        Intent intent = new Intent(this, CarLinkMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                }
                //IDLE 空闲状态，也表示电话已经挂断
                else if (modemCallState == UCarCommon.CallState.IDLE) {
                    // 如果在车机原生HMI桌面有自定义通话弹窗，请取消显示
                    Log.i(TAG, "modem call state is idle");
                }
                mPhoneStateInfo.setModemCallState(modemCallState);
            }

            boolean isVoiceAssistantActive = phoneStateInfo.isVoiceAssistantActive();
            if (isVoiceAssistantActive != mPhoneStateInfo.isVoiceAssistantActive()) {
                if (isVoiceAssistantActive) {
                    //手机端语音助手被用户唤醒，需要屏蔽车端本地语音助手及车端免唤醒词等快捷指令
                    Log.i(TAG, "voice assistant state is active");
                } else {
                    //车厂可以通过UCarAdapter.sendVRCMD接口将车端部分免唤醒快捷指令转化为VRCmdType发给手机端处理
                    Log.i(TAG, "voice assistant state is idle");
                }
                mPhoneStateInfo.setVoiceAssistantActive(isVoiceAssistantActive);
            }


            boolean isVoipCallActive = phoneStateInfo.isVoipCall();
            if (isVoipCallActive != mPhoneStateInfo.isVoipCall()) {
                if (isVoipCallActive) {
                    // 如果UCar在后台，请恢复到前台显示
                    Log.i(TAG, "voip call state is active");
                } else {
                    // 如果在车机原生HMI桌面有自定义VOIP通话弹窗，请取消显示
                    Log.i(TAG, "voip call state is idle");
                }
                mPhoneStateInfo.setVoipCall(isVoipCallActive);
            }
        });
    }

    //显示投影的surface是否可见
    private void setCastSurfaceVisibility(boolean isShow) {
        Log.d(TAG, "setCastSurfaceVisibility() isShow: " + isShow);

        if (isShow) {
            //显示surface
            setCastSurface();
            //显示carlink的surfaceView
            castLayout.setVisibility(View.VISIBLE);
            castManager.setVisibility(View.GONE);
        } else {
            //不显示surface
            unsetCastSurface();
            //避免退出CarLink时，闪烁连接码的问题
            if (!mIsClose) {
                castManager.setVisibility(View.VISIBLE);
            }
            //隐藏carlink的surfaceView
            castLayout.setVisibility(View.GONE);
        }
    }

    //设置投影的surface可见
    //在surfaceHolderCallback的监听中，开启投屏
    private void setCastSurface() {
        if (mSurfaceHolderCallback == null) {
            Log.d(TAG, "create mSurfaceHolderCallback!!");
            //创建回调对象
            mSurfaceHolderCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d(TAG, "surfaceCreated()");
                    holder.setSizeFromLayout();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.i(TAG, "surfaceChanged() width: " + width + ", height: " + height);
                    if (holder != null
                            && holder.getSurface() != null
                            //surface有效
                            && holder.getSurface().isValid()) {
                        //startCast  开始投屏
                        UCarAdapter.getInstance().startCast(holder.getSurface(), width, height);
                    } else {
                        Log.i(TAG, "SurfaceHolder or Surface is null or invalid");
                        //通知手机，车机端投屏需要暂停
                        UCarAdapter.getInstance().pauseCast();
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d(TAG, "surfaceDestroyed()");
                    UCarAdapter.getInstance().pauseCast();
                }
            };

            Log.d(TAG, "get mSurfaceView!!");

            Log.d(TAG, "mSurfaceView: " + mSurfaceView + ", mSurfaceView.getHolder(): " + mSurfaceView.getHolder());
            if ((mSurfaceView != null) && (mSurfaceView.getHolder() != null)) {
                Log.d(TAG, "add surface holder callback!!");
                resizeSurfaceAccordingToCastSize();
                //注册回调对象
                mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
                //得到surface
                Surface surface = mSurfaceView.getHolder().getSurface();
                if (surface != null && surface.isValid()) {
                    //调用carlink SDK开始投影
                    UCarAdapter.getInstance().startCast(surface, mSurfaceView.getWidth(), mSurfaceView.getHeight());
                }
            }
        }
    }

    void unsetCastSurface() {
        Log.i(TAG, "unsetCastSurface()");
        if ((mSurfaceView != null) && (mSurfaceView.getHolder() != null)) {
            Log.i(TAG, "unsetCastSurface() remove surface holder callback");
            //移除回调对象
            mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
            mSurfaceHolderCallback = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        if (mIsCastSucceed) {
            stopLauncherFloater();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        IS_CARLINK_FRONT = true;

        //如果没有投影成功
        if (sIsServiceInitialized && !mIsCastSucceed) {
            //扫描设备
            Log.d(TAG, "onResume() start advertise and scan device");
            mHandler.sendEmptyMessage(MSG_START_ADVERTISE_SCAN_DEVICE);
        }
        //如果投影成功了
        else if (mIsCastSucceed) {
            Log.d(TAG, "onResume() resume surfaceView: " + mSurfaceView);
            setCastSurface();
        }
        //初始化carlink Sdk
        mHandler.postDelayed(this::initUCarSDK, START_CAST_DELAY);
        if (sIsServiceInitialized) {
            // 如果当前窗体获得视图焦点且允许SDK获取音频焦点则需要调用如下API
            // 因为当SDK的音频焦点被其它应用抢占后，除非收到新音频类型的音频流数据，否则SDK将不会主动抢占焦点
            // 故当SDK恢复到前台或者被用户点击使能时（如主副屏切换），必须要设置允许获取焦点，SDK收到手机端音频流时才可以自动获取焦点，否则会导致没有声音
            UCarAdapter.getInstance().allowGainAudioFocus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        IS_CARLINK_FRONT = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");

        IS_CARLINK_FRONT = false;
        //如果没有投影成功
        if (!mIsCastSucceed) {
            Log.d(TAG, "onStop() stop advertise and scan device");
            //扫描设备
            mHandler.sendEmptyMessage(MSG_STOP_ADVERTISE_SCAN_DEVICE);
            //没有投影成功，就直接关闭
            finish();
        }
        //投影成功
        else {
            new Thread(() -> {
                Log.i(TAG, "pause cast");
                UCarAdapter.getInstance().pauseCast();
            }).start();
            unsetCastSurface();
            //TODO: 857新增需求：2023/10/17  判断是否是通话中，启动悬浮窗
            int status = 0;
            if (mCallInfo != null) {
                UCarCommon.CallState callState = mCallInfo.callState;
                if (callState != UCarCommon.CallState.IDLE
                        && callState != UCarCommon.CallState.UNKNOWN_STATE
                ) {
                    status = 1;
                }
            }
            startLauncherFloater(status);
        }
    }

    private void startLauncherFloater(int status) {
        Log.i(TAG, "startLauncherFloater() status: "+status);
        //启动悬浮窗服务
        Intent intent = new Intent(this, LaunchFloatingService.class);
        startService(intent);
    }

    private void stopLauncherFloater() {
        Log.i(TAG, "stopLauncherFloater() ");
        //停止悬浮窗服务
        Intent intent = new Intent(this, LaunchFloatingService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        WTSkinManager.get().removeSkinChangedListener(mSkinChangedListener);
        mSharedPreferencesUtil.putBoolean(Contants.SP_IS_CARLINK_CONNECT, false);
        CommonUtil.setGlobalProp(getApplicationContext(), Contants.SYS_IS_CARLINK_CONNECT, 0);
        UCarAdapter.getInstance().unregisterCarConnectListener(mConnectCallback);
        UCarAdapter.getInstance().unregisterPhoneDataListener(mPhoneDataCallback);
        UCarAdapter.getInstance().unregisterCameraInfoListener(mCameraInfoCallback);
//        UCarAdapter.getInstance().unRegisterAudioRecorderListener(mAudioRecorderListener);
        UCarAdapter.getInstance().deInit();
        sIsServiceInitialized = false;
        stopLauncherFloater();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent()");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged() hasFocus: " + hasFocus);
        /*
         * Some cars force the display of the system bar, so you need
         * to get the real window size. In onCreate, onResume and other
         * earlier lifecycle callback functions of activity, the correct
         * display size cannot be obtained because the view has not been
         * attached to WMS
         */
        if (hasFocus) {
            Log.i(TAG, "onWindowFocusChanged() has focus");
        }
    }


    public byte[] getBluetoothMac() {
        byte[] macBytes = new byte[CAR_ID_SIZE];
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        @SuppressLint("HardwareIds") String mac = bluetoothManager.getAdapter().getAddress();
        Log.i(TAG, "car mac:" + mac + ", is connected before: " +
                (UCarAdapter.getPhoneIdByBtMac(mac, getApplicationContext()) != null ? "true" : " false"));
        if (mac != null) {
            String[] strArr = mac.split(":");
            for (int i = 0; i < strArr.length; i++) {
                int value = Integer.parseInt(strArr[i], 16);
                macBytes[i] = (byte) value;
            }
        } else {
            Log.e(TAG, "Temporarily use random numbers as IDs, only for testing");
            new Random().nextBytes(macBytes);
        }

        return macBytes;
    }

    private byte[] generateCarId() {
        // 目前使用蓝牙MAC地址作为车机的唯一标识符，如果无法获取，可以使用其它能够唯一标示车机的ID
        // 生成，但是需要初始化SDK时，设置不支持自动回连
        SharedPreferences prefs = this.getSharedPreferences(CAR_ID_PREFS, MODE_PRIVATE);
        String id = prefs.getString(CAR_ID_KEY, "");

        if (TextUtils.isEmpty(id)) {
            byte[] raw = getBluetoothMac();
            id = Base64.getEncoder().encodeToString(raw);
            prefs.edit().putString(CAR_ID_KEY, id).apply();
            Log.d(TAG, "generateCarId() raw id: " + Arrays.toString(raw));
        }
        Log.d(TAG, "generateCarId() id: " + id);

        return Base64.getDecoder().decode(id);
    }

    private void initDisplayMetrics() {
        mDisplayDensityDpi = AppConfig.getDpi();
        mFullDisplayWidth = AppConfig.getDisplayWidth();
        mFullDisplayHeight = AppConfig.getDisplayHeight();
        mExpectedDisplayWidth = AppConfig.getDisplayWidth();
        mExpectedDisplayHeight = AppConfig.getDisplayHeight()
                - AppConfig.getDockHeight()
                - AppConfig.getStatusBarHeight();
    }

    //初始化carLinkSdk
    private void initUCarSDK() {
        Log.d(TAG, "initUCarSDK() sIsServiceInitialized: " + sIsServiceInitialized);
        if (!sIsServiceInitialized) {
            initDisplayMetrics();
            byte[] mCarId = generateCarId();
            Log.d(TAG, "initUCarSDK() carID: " + Arrays.toString(mCarId));
            //UCarConfig的builder
            UCarConfig.Builder builder = new UCarConfig.Builder();
//            setSupportP2P boolean
//            无线是否支持P2P，如果也支持SoftAP，SDK将根据手机
//                    能力进行选择
//            setSupportSoftAP boolean
//            无线是否支持SoftAP，如果也支持P2P，SDK将根据手机
//                    能力进行选择
            Log.i(TAG, "initUCarSDK() call builder.setCarBrMac()");
            //UCar配置对象
            mUCarConfig = builder.setCarBrMac(mCarId)
                    .setDpi(mDisplayDensityDpi)
                    .setFps(DISPLAY_FPS)
                    .setScreenWidth(mFullDisplayWidth)
                    .setScreenHeight(mFullDisplayHeight)
                    .setVideoDisplayWidth(mExpectedDisplayWidth)
                    .setVideoDisplayHeight(mExpectedDisplayHeight)
                    .setSupportP2P(true)//WIFI+蓝牙
                    .setSupportCamera(true)
                    //设置使用自定义音频录制
                    .setUseCustomAudioRecord(true)
//                    .setSupportSoftAP(true)//热点连接
                    .setSupportStereoRecord(true)
                    .build();
            Log.i(TAG, "initUCarSDK() call UCarAdapter.getInstance().init()");
            //初始化carlink sdk
            boolean success = UCarAdapter.getInstance().init(getApplicationContext(),
                    //UCar配置对象
                    mUCarConfig,
                    new ICarInitCallback() {
                        @Override
                        public void onInitSuccess() {
                            Log.i(TAG, "initUCarSDK() onInitSuccess() ");
                            sIsServiceInitialized = true;
                            Log.i(TAG, "initUCarSDK() onInitSuccess() call registerCarEventListener()");
                            //注册汽车事件监听器
                            registerCarEventListener();
                            //发送MSG_START_ADVERTISE_SCAN_DEVICE消息，来启动蓝牙设备扫描
                            //发送扫描设备的蓝牙设备扫描。advertise这里不是广告，而是蓝牙设备扫描
                            mHandler.sendEmptyMessage(MSG_START_ADVERTISE_SCAN_DEVICE);
                        }

                        @Override
                        public void onInitFailed(int errorCode) {
                            Log.e(TAG, "initUCarSDK() onInitFailed() UCarAdapter.getInstance() init failed");
                            mHandler.post(() -> handleConnectFailed(errorCode));
                        }
                    });

            //carlink sdk初始化失败
            if (!success) {
                Log.e(TAG, "initUCarSDK() 初始化ucar sdk失败！！");
                finish();
                IS_CARLINK_FRONT = false;
            } else {
                Log.i(TAG, "initUCarSDK() 初始化ucar sdk成功！");
            }
        } else {
            Log.d(TAG, "initUCarSDK() 已初始化过，直接投屏");
        }
    }

    private void resizeSurfaceAccordingToCastSize() {
        ConstraintLayout.LayoutParams lp
                = new ConstraintLayout.LayoutParams(mExpectedDisplayWidth,
                mExpectedDisplayHeight);
        mSurfaceView.setLayoutParams(lp);
    }

    private void backToHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}