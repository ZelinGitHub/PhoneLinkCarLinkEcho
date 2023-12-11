package com.wt.phonelink;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


import static com.incall.apps.hicar.servicesdk.contants.Constants.ACTION_WT_LINK_CONTROL;
import static com.incall.apps.hicar.servicesdk.contants.Constants.PERMISSION_WT_LINK_CONTROL;
import static com.incall.apps.hicar.servicesdk.contants.Constants.STATE_WINDOW_MINIMIZE;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.incall.apps.hicar.servicesdk.contants.CallState;
import com.incall.apps.hicar.servicesdk.contants.Constants;
import com.incall.apps.hicar.servicesdk.contants.HiCarCallStatusChangeEvent;
import com.incall.apps.hicar.servicesdk.contants.HicarSelfVoiceChangeEvent;
import com.incall.apps.hicar.servicesdk.contants.LaunchPhoneLinkEvent;
import com.incall.apps.hicar.servicesdk.servicesimpl.audio.HcAudioManager;
import com.incall.apps.hicar.servicesdk.utils.SharedPreferencesUtil;
import com.tinnove.wecarspeech.clientsdk.impl.IClientType;
import com.tinnove.wecarspeech.clientsdk.impl.SpeechClientMgr;
import com.tinnove.wecarspeech.clientsdk.interfaces.IPlayStateCallback;
import com.tinnove.wecarspeech.clientsdk.interfaces.ISpeechClient;
import com.tinnove.wecarspeech.clientsdk.interfaces.OnClientReadyCallback;
import com.tinnove.wecarspeech.clientsdk.model.WakeUpEvent;
import com.tinnove.wecarspeech.clientsdk.model.WakeUpScene;
import com.tinnove.wecarspeech.clientsdk.semantic.command.CommandAnnotation;
import com.tinnove.wecarspeech.clientsdk.semantic.command.TextList;
import com.tinnove.wecarspeech.clientsdk.semantic.executor.CommandAbilityExecutor;
import com.wt.phonelink.carlink.CarLinkMainActivity;
import com.wt.phonelink.hicar.HiCarMainActivity;
import com.wt.phonelink.utils.CommonUtil;
import com.wt.phonelink.utils.VoiceUtils;
import com.wt.phonelink.utils.WTStatisticsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;


import java.util.List;


/**
 * 1、手机互联app打开指令：
 * 支持用户通过唤醒语音助手，发出打开的指令，打开“手机互联”应用。识别到相关打开相关的语音指令，则帮助用户打开手机互联应用至前台(具体页面展示逻辑见2.4.2部分)。相关交互逻辑遵循语音打开应用的逻辑。
 * 2、手机互联app关闭指令：
 * 支持用户通过唤醒语音助手，发出关闭的指令，关闭“手机互联”应用。识别到相关关闭相关的语音指令，连接状态不断开。
 * 手机盒子连接时，收到关闭指令，则变为小窗状态。
 * hicar/carlink连接时，收到关闭指令，则则帮助用户关闭手机互联应用至后台
 * 相关交互逻辑遵循语音关闭应用的逻辑。语音侧指令打开/关闭某应用的逻辑见下图：
 * <p>
 * 3、hicar/carlink 手机语音助手支持免唤醒词唤醒
 * 注册免唤醒词：小艺小艺/小爱同学/小布小布/小V小V，支持用户通过语音指令打开互联产品及唤醒对应的手机助手。
 * a.免唤醒指令“小爱同学/小布小布/小V小V”，carlink已连接对应的手机，则调用carlink页面至前台，并唤醒手机语音助手；hicar/carlink/手机盒子未连接，则调取carlink连接页面至前台，并TTS提醒“请先连接手机”；
 * b.免唤醒指令“小艺小艺”，hicar已连接，则调用hicar页面至前台，并唤醒手机语音助手；hicar/carlink/手机盒子均未连接，则调取hicar连接页面至前台，并TTS提醒“请先连接手机”；
 * c.通过手机互联已连接某手机，但是与免唤醒词不匹配，或者已经通过手机盒子连接手机，则车机不响应相关指令。
 * i.小爱同学：对应小米、红米系手机
 * ii.小布小布：对应OPPO手机
 * iii.小V小V：对应VIVO手机
 * iv.小艺小艺：对应华为系手机
 * 注意：车机上在手机语音助手唤醒期间，车机语音助手不响应语音指令；车机语音助手唤醒期间，手机语音助手不响应语音指令。
 */
public class VoiceManager extends CommandAbilityExecutor {
    private static final String TAG = "WTPhoneLink/VoiceManager";
    private Application myApplication;
    //语音客户端
    ISpeechClient mSpeechClient;
    private static VoiceManager voiceManager;
    private SharedPreferencesUtil sp;
    private String sceneId = null;
    private final OnClientReadyCallback mOnClientReadyCallback = new OnClientReadyCallback() {
        //onClientReady 函数会在 client 端和语音 server 建立绑定完成初始化后回调
        // 端上可在onClientReady 中进行 client 和技能等注册。
        @Override
        public void onClientReady() {
            Log.i(TAG, "onClientReady() thread:" + Thread.currentThread().getName());
            //在此处切换到主线程
            // 注册client和skill
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Log.i(TAG, "onClientReady() thread: " + Thread.currentThread().getName());
                //注册客户端
                registerClient();
                //订阅命令
                subscribeCommand();
            });
        }

        //onInitFailed 函数是在 client 和语音 server 建立绑定失败的情况下回调，端上在收到
        //onInitFailed 后需要重新调用初始化接口重新发起绑定初始化操作。
        @Override
        public void onInitFailed(int errCode, String msg) {
            Log.i(TAG, "onInitFailed() errCode: " + errCode + ", msg: " + msg);
            //重新调用初始化接口重新发起绑定初始化操作
            init(myApplication);
        }

        //onClientDisconnect 函数会在 client 和语音 server 间的绑定断开（断开可能是因为语音
        //server 异常退出或者 service 绑定不稳定）的时候调用， client 端收到 onClientDisconnect
        //回调后需要重新发起初始化操作建立和语音 server 的连接。
        @Override
        public void onClientDisconnect() {
            Log.i(TAG, "onClientDisconnect()");
            //重新发起初始化操作建立和语音server的连接
            init(myApplication);
        }
    };


    public static VoiceManager getInstance() {
        if (voiceManager == null) {
            voiceManager = new VoiceManager();
        }
        return voiceManager;
    }

    //梧桐语音初始化
    public void init(Application app) {
        Log.i(TAG, "init()");
        myApplication = app;
        sp = SharedPreferencesUtil.getInstance(MyApplication.getContext());
        //初始化之前先反初始化 析构
        deInit();
        //创建一个handlerThread和它使用的handler
        //初始化语音客户端管理器，传入一个客户端准备监听对象
        SpeechClientMgr.getInstance().init(myApplication, mOnClientReadyCallback);
        EventBus.getDefault().register(this);
    }

    //反初始化
    private void deInit() {
        unregisterClient();
        unSubscribeCommand();
        mSpeechClient = null;
        EventBus.getDefault().unregister(this);
    }


    //注册梧桐语音client
    private void registerClient() {
        Log.i(TAG, "registerClient()");
        if (mSpeechClient == null) {
            //使用语音客户端管理器注册客户端，保存到mSpeechClient
            mSpeechClient = SpeechClientMgr.getInstance().registerClient(
                    myApplication, "7E48C1F80FCCB5DA6F21195891FE24BC", "手机互联", "phonelink", IClientType.TYPE_DEFAULT, true);
        }
    }

    //注销客户端准备监听对象
    private void unregisterClient() {
        Log.i(TAG, "unregisterClient()");
        //使用语音客户端管理器注销客户端准备监听对象
        SpeechClientMgr.getInstance().unregisterClientInitCallback(mOnClientReadyCallback);
    }

    //订阅语义解析器
    public void subscribeCommand() {
        Log.i(TAG, "subscribeCommand() mSpeechClient: " + mSpeechClient);
        if (mSpeechClient != null) {
            //使用语音客户端订阅语义解析器
            mSpeechClient.subscribeCommand(this);
        }
    }

    //退订语义解析器
    public void unSubscribeCommand() {
        Log.i(TAG, "unSubscribeCommand() mSpeechClient: " + mSpeechClient);
        if (mSpeechClient != null) {
            //使用语音客户端退订语义解析器
            mSpeechClient.unSubscribeCommand(this);
        }
    }

    //场景唤醒词是App进入特定业务场景才注册的唤醒词，退出场景则注销场景唤醒词。
    public void registerHiCarWakeUp() {
        Log.i(TAG, "registerHiCarWakeUp()");
        boolean isForeground = CommonUtil.isActivityRunning(MyApplication.getContext(), "com.wt.phonelink");
        Log.i(TAG, "registerHiCarWakeUp() isActivityRunning: " + isForeground);
        if (!isForeground) {
            return;
        }

        boolean isHicarConnected = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        Log.i(TAG, "registerHiCarWakeUp() isHicarConnected: " + isHicarConnected);
        if (isHicarConnected) {
            WakeUpScene mainScene = new WakeUpScene();
            //注册场景内唤醒词
            //讯飞语音唤醒后调用
            mainScene.addWakeupEvent(new WakeUpEvent("小艺小艺", (taskId, indication, semantic, doaDirection) -> {
                Log.i(TAG, "registerHiCarWakeUp() onWakeup() indication: " + indication + ", semantic: " + semantic);
                //唤醒华为手机语音助手
                wakeUpHuawei(null, null);
            }));
            if (mSpeechClient != null) {
                sceneId = mSpeechClient.registerWakeupScene(mainScene);
                Log.i(TAG, "registerHiCarWakeUp() sceneId: " + sceneId);
            }
        }
    }

    public void registerCarLinkWakeUp() {
        boolean isForeground = CommonUtil.isActivityRunning(MyApplication.getContext(), "com.wt.phonelink");
        Log.i(TAG, "registerCarLinkWakeUp() isActivityRunning: " + isForeground);
        if (!isForeground) {
            return;
        }
        boolean isCarLinkConnected = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        Log.i(TAG, "registerCarLinkWakeUp() isCarLinkConnected: " + isCarLinkConnected);
        if (isCarLinkConnected) {
            WakeUpScene mainScene = new WakeUpScene();
            //注册场景内唤醒词
            //讯飞语音唤醒后调用
            mainScene.addWakeupEvent(new WakeUpEvent("小布小布", (taskId, indication, semantic, doaDirection) -> {
                Log.i(TAG, "registerCarLinkWakeUp() onWakeup() indication: " + indication + " semantic: " + semantic);
                //唤醒carlink手机语音助手
                launchOam("小布小布");
            }));
            mainScene.addWakeupEvent(new WakeUpEvent("小V小V", (taskId, indication, semantic, doaDirection) -> {
                Log.i(TAG, "registerCarLinkWakeUp() onWakeup() indication: " + indication + ", semantic: " + semantic);
                launchOam("小V小V");
            }));
            mainScene.addWakeupEvent(new WakeUpEvent("小爱同学", (taskId, indication, semantic, doaDirection) -> {
                Log.i(TAG, "registerCarLinkWakeUp() onWakeup() indication: " + indication + ", semantic: " + semantic);
                launchOam("小爱同学");
            }));
            if (mSpeechClient != null) {
                sceneId = mSpeechClient.registerWakeupScene(mainScene);
                Log.i(TAG, "registerCarLinkWakeUp() sceneId: " + sceneId);
            }
        }
    }

    public void unRegisterWakeUp() {
        Log.i(TAG, "unRegisterWakeUp()");
        if (mSpeechClient != null && !TextUtils.isEmpty(sceneId)) {
            Log.i(TAG, "unRegisterWakeUp() sceneId: " + sceneId);
            mSpeechClient.unregisterWakeupScene(sceneId);
            sceneId = null;
        }
        //恢复讯飞识别结果
        releaseRecordStatus();
    }


    //这个是通过 语音sdk映射到这个方法来的
    // 你看我之前发的那个资料里面，在C236文件夹里面应该有一份是语音相关的文档，你可以看看
    // 有 不懂的可以请教下北京语音研发史琨茸
    //唤醒打开carLink
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.XiaoMi")
    public void wakeUpXiaoMi(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpXiaoMi param:  " + param + ", textList: " + textList);
        launchOam("小爱同学");
    }

    //唤醒打开carLink
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.OPPO")
    public void wakeUpOPPO(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpOPPO param:  " + param + ", textList: " + textList);
        launchOam("小布小布");
    }

    //唤醒打开carLink
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.Vivo")
    public void wakeUpVivo(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpVivo param: " + param + ", textList: " + textList);
        launchOam("小V小V");
    }

    /*
    唤醒华为手机语音助手，停止梧桐的讯飞语音助手
     */
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.Huawei")
    public void wakeUpHuawei(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpHuawei() param: " + param + ", textList: " + textList);
        //得到sp保存的hiCar和carLink是否连接的状态
        //sp保存的wtBox是否连接的状态
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        //sp保存的hiCar是否连接的状态
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        //sp保存的carLink是否连接的状态
        boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        //carLink已连接
        if (isCarLinkConnect) {
            Log.e(TAG, "wakeUpHuawei() current CarLink isConnected ");
            return;
        }
        //wtBox已连接
        if (isWTBoxConnect) {
            Log.e(TAG, "wakeUpHuawei() current wtBox isConnected ");
            return;
        }
        //hiCar未连接，播放语音
        if (!isHiCarConnect) {
            Log.e(TAG, "wakeUpHuawei() current hiCar isNotConnected ");
            return;
        }
        //停止讯飞语音识别。讯飞和手机互联是竞争关系。讯飞就是梧桐系统语音。
        Log.i(TAG, "wakeUpHuawei()  applyRecordStatus()");
        boolean recordStatus = mSpeechClient.applyRecordStatus();
        Log.i(TAG, "wakeUpHuawei()  recordStatus: " + recordStatus);
        //如果hiCar没有在前台
        if (!Constants.IS_HICAR_FRONT) {
            startHicarMainActivity();
        }
        //6s后唤醒讯飞语音。
        releaseRecordStatusDelay();
        //唤醒华为手机语音助手，这是华为手机的语音助手，不是梧桐系统的
        HcAudioManager.getInstance().wakeUpHiCarByVoice();
    }

    /*
唤醒CarLink手机语音助手，停止梧桐的讯飞语音助手
 */
    public void launchOam(String keyword) {
        Log.i(TAG, "launchOam() keyword: " + keyword);
        //得到wtBox、hiCar、carLink的连接状态
        //sp保存的wtBox是否连接的状态
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        //sp保存的hiCar是否连接的状态
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        //sp保存的carLink是否连接的状态
        boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        //如果梧桐box已连接
        if (isWTBoxConnect) {
            Log.e(TAG, "launchOam() current wtBox isConnected ");
            return;
        }
        //如果hiCar已连接
        if (isHiCarConnect) {
            Log.e(TAG, "launchOam() current HiCar isConnected ");
            return;
        }
        //如果carLink未连接
        if (!isCarLinkConnect) {
            Log.e(TAG, "launchOam() current CarLink isNotConnected! ");
            return;
        }
        Log.i(TAG, "launchOam()  applyRecordStatus()");
        //停止讯飞语音识别 讯飞和手机互联是竞争关系
        boolean recordStatus = mSpeechClient.applyRecordStatus();
        Log.i(TAG, "launchOam() recordStatus: " + recordStatus);
        //6s后唤醒讯飞语音
        releaseRecordStatusDelay();
        //如果carLink不在前台，就打开carLink主Activity
        if (!Constants.IS_CARLINK_FRONT) {
            startCarlinkMainActivity();
        }
        //唤醒carLink手机语音助手，这个是手机语音助手，不是梧桐自己的系统语音
        VoiceUtils.getInstance().awakenCarLinkVoiceAssistant(keyword);
    }

    //讯飞语音打开手机互联
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.Open")
    public void wakeUpOpen(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpOpen() param: " + param + ", textList: " + textList);
        //更新hiCar、carLink、wtBox是否连接的状态
        //sp保存的wtBox是否连接的状态
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        //sp保存的hiCar是否连接的状态
        boolean isHiCarConnect = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        //sp保存的carLink是否连接的状态
        boolean isCarLinkConnect = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        //打开手机互联
        //小桐畅联正在连接
        if (isWTBoxConnect) {
            Log.i(TAG, "wakeUpOpen() IS_WTBOX_FRONT: " + Constants.IS_WTBOX_FRONT);
            if (!Constants.IS_WTBOX_FRONT) {
                playTts("好的，没问题");
            } else {
                playTts("手机互联已经打开了");
            }
            //启动wtBox主activity
            // adb shell am start -n com.tinnove.link.client/.QrCodeActivity
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.tinnove.link.client", "com.tinnove.link.client.QrCodeActivity");
            intent.setComponent(componentName);
            intent.putExtra("pkg", "com.wt.phonelink");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                myApplication.startActivity(intent);
            } catch (Exception e) {
                Log.e("跳转手机盒子发生异常！！", e.toString());
                Toast.makeText(myApplication, "没有安装手机盒子！", Toast.LENGTH_SHORT).show();
            }
            //唤醒wtBox手机语音助手，手机盒子目前没有功能呢。
        }
        //hiCar正在连接
        else if (isHiCarConnect) {
            Log.i(TAG, "wakeUpOpen() hiCar IS_FRONT: " + Constants.IS_HICAR_FRONT);
            //如果hiCar没有在前台
            if (!Constants.IS_HICAR_FRONT) {
                playTts("好的，没问题");
                //启动hiCar主activity
                startHicarMainActivity();
            } else {
                playTts("手机互联已经打开了");
            }
            //唤醒hiCar手机语音助手
            HcAudioManager.getInstance().wakeUpHiCarByVoice();
        }
        //carLink正在连接
        else if (isCarLinkConnect) {
            Log.i(TAG, "wakeUpOpen() IS_CARLINK_FRONT: " + Constants.IS_CARLINK_FRONT);
            //如果carLink没有在前台
            if (!Constants.IS_CARLINK_FRONT) {
                playTts("好的，没问题");
                startCarlinkMainActivity();
            } else {
                playTts("手机互联已经打开了");
            }
            //唤醒carLink手机语音助手
            VoiceUtils.getInstance().awakenCarLinkVoiceAssistant("打开手机互联");
        }
        //wt盒子、hiCar、carLink都没有连接
        else {
            Log.i(TAG, "wakeUpOpen() IS_PHONE_LINK_FRONT: " + Constants.IS_PHONE_LINK_FRONT);
            if (!Constants.IS_PHONE_LINK_FRONT) {
                playTts("好的，没问题");
            } else {
                playTts("手机互联已经打开了");
            }
            startMainActivity();
            //未连接的时候tts播报后，进入连接或者选择界面，并调用埋点首页曝光
            WTStatisticsUtil.showHomePage(2);
        }
    }


    //讯飞语音关闭手机互联
    @CommandAnnotation(method = "Command://Tinnove.PhoneLink.Close")
    public void wakeUpClose(JSONObject param, List<TextList> textList) {
        Log.i(TAG, "wakeUpClose()  param: " + param + ", textList: " + textList);
        //sp保存的wtBox是否连接的状态
        boolean isWTBoxConnect = sp.getBoolean(Constants.SP_IS_WTBOX_CONNECT);
        //回到桌面就行
        Log.i(TAG, "wakeUpClose()  call toLauncher()");
        startAppListActivity();
        Log.i(TAG, "wakeUpClose()  " +
                "IS_CARLINK_FRONT: " + Constants.IS_CARLINK_FRONT
                + ", IS_HICAR_FRONT: " + Constants.IS_HICAR_FRONT
                + ", IS_WTBOX_FRONT: " + Constants.IS_WTBOX_FRONT
                + ", IS_PHONE_LINK_FRONT: " + Constants.IS_PHONE_LINK_FRONT
        );
        if (!Constants.IS_CARLINK_FRONT
                && !Constants.IS_HICAR_FRONT
                && !Constants.IS_WTBOX_FRONT
                && !Constants.IS_PHONE_LINK_FRONT
        ) {
            playTts("手机互联已经关闭了");
        }
        Log.i(TAG, "wakeUpClose()  isWTBoxConnect: " + isWTBoxConnect);
        //如果梧桐box已连接
        if (isWTBoxConnect) {
            Intent intent = new Intent();
            intent.setAction(ACTION_WT_LINK_CONTROL);
            intent.putExtra("state", STATE_WINDOW_MINIMIZE);
            myApplication.sendBroadcast(intent, PERMISSION_WT_LINK_CONTROL);
        }
    }


    //播放tts，content是要播放的内容
    public void playTts(String content) {
        //使用语音客户端播放tts
        mSpeechClient.playTTS(content, new IPlayStateCallback() {
            @Override
            public void onPlayBegin() {
                Log.i(TAG, "onPlayBegin() ");
            }

            @Override
            public void onPlayPause() {
                Log.i(TAG, "onPlayPause() ");
            }

            @Override
            public void onPlayResume() {
                Log.i(TAG, "onPlayResume() ");
            }

            @Override
            public void onProgress(int i, int i1) {
                Log.i(TAG, "onProgress() ");
            }

            @Override
            public void onPlayCompleted() {
                Log.i(TAG, "onPlayCompleted() ");
            }

            @Override
            public void onPlayInterrupted(int i) {
                Log.i(TAG, "onPlayInterrupted() ");
            }

            @Override
            public void onPlayEnd() {
                Log.i(TAG, "onPlayEnd() ");
            }

            @Override
            public void onPlayError() {
                Log.i(TAG, "onPlayError() ");
            }

            @Override
            public void onStatusChange(int i, int i1) {
                Log.i(TAG, "onStatusChange() ");
            }
        });
    }


    private void releaseRecordStatusDelay() {
        Log.i(TAG, "releaseRecordStatusDelay()");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (mSpeechClient != null) {
                //恢复讯飞语音识别
                boolean recordStatus = mSpeechClient.releaseRecordStatus();
                Log.i(TAG, "releaseRecordStatusDelay recordStatus: " + recordStatus);
            }
        }, 6000);
    }

    public void releaseRecordStatus() {
        Log.i(TAG, "releaseRecordStatus() 恢复讯飞语音识别结果");
        if (mSpeechClient != null) {
            //恢复讯飞语音识别结果
            mSpeechClient.releaseRecordStatus();
            Log.i(TAG, "releaseRecordStatus() 恢复讯飞语音识别结果完成");
        } else {
            Log.e(TAG, "releaseRecordStatus() mSpeechClient is null! ");
        }
    }

    //     * 来/去电过程中  禁止讯飞语音识别，结束后释放
    //     * @param callState 当前电话状态
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHicarCallStatusChangeEvent(HiCarCallStatusChangeEvent callStateEvent) {
        //得到通话状态
        int callState = callStateEvent.getCallState();
        Log.i(TAG, "onHicarCallStatusChangeEvent()  callState: " + callState);
        //响铃和通话中
        if (callState == CallState.RINGING || callState == CallState.CALLING) {
            //停止讯飞语音识别
            mSpeechClient.applyRecordStatus();
            Log.i(TAG, "onHicarCallStatusChangeEvent() 停止讯飞语音识别");
        }
        //空闲
        else if (callState == CallState.IDLE) {
            //恢复讯飞语音识别结果
            mSpeechClient.releaseRecordStatus();
            Log.i(TAG, "onHicarCallStatusChangeEvent()  恢复讯飞语音识别结果");
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void stopSpeechSR(HicarSelfVoiceChangeEvent selfVoiceChangeEvent) {
        Log.i(TAG, "stopSpeechSR() 停止正在播报的语音状态 selfVoiceChangeEvent: " + selfVoiceChangeEvent);
        if (mSpeechClient != null) {
            //停止语音识别状态
            mSpeechClient.stopSR();
            Log.i(TAG, "stopSpeechSR() 停止正在播报的语音状态完成");
        }
    }


    //todo 方控语音按键需求
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaunchPhoneLinkEvent(LaunchPhoneLinkEvent statusEvent) {
        Log.i(TAG, "onLaunchPhoneLinkEvent() statusEvent: " + statusEvent);
        //判断是否在前台
        boolean isForeground = CommonUtil.isActivityRunning(MyApplication.getContext(), "com.wt.phonelink");
        Log.i(TAG, "onLaunchPhoneLinkEvent() isForeground: " + isForeground);
        //如果没有在前台，什么都不做
        if (!isForeground) {
            Log.e(TAG, "onLaunchPhoneLinkEvent() PhoneLink is not foreground, do nothing! ");
            return;
        }
        //如果hicar已经连接，唤醒hicar手机语音助手，停止梧桐讯飞语音助手
        boolean isHicarConnected = sp.getBoolean(Constants.SP_IS_HICAR_CONNECT);
        Log.i(TAG, "onLaunchPhoneLinkEvent() isHicarConnected: " + isHicarConnected);
        if (isHicarConnected) {
            VoiceManager.getInstance().wakeUpHuawei(null, null);
            return;
        }
        //如果carlink已经连接，唤醒carlink手机语音助手，停止梧桐讯飞语音助手
        boolean isCarLinkConnected = sp.getBoolean(Constants.SP_IS_CARLINK_CONNECT);
        Log.i(TAG, "onLaunchPhoneLinkEvent() isCarLinkConnected: " + isCarLinkConnected);
        if (isCarLinkConnected) {
            String phoneBrand = sp.getString(Constants.SP_PHONE_BRAND).toUpperCase();
            Log.i(TAG, "onLaunchPhoneLinkEvent() phoneBrand: " + phoneBrand);
            switch (phoneBrand) {
                case "OPPO":
                case "REALME":
                case "ONEPLUS":
                    launchOam("小布小布");
                    break;
                case "VIVO":
                case "IQOO":
                    launchOam("小V小V");
                    break;
                case "MI":
                case "XIAOMI":
                case "REDMI":
                    launchOam("小爱同学");
                    break;
                default:
                    break;
            }
        }
    }


    //进入桌面
    public void startAppListActivity() {
        Log.i(TAG, "startAppListActivity()");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tinnove.launcher", "com.tinnove.applist.AppListActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        myApplication.startActivity(intent);
    }

    private void startHicarMainActivity() {
        Log.d(TAG, "startHicarMainActivity()");
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, HiCarMainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (context != null) {
            context.startActivity(intent);
        }
    }

    private void startCarlinkMainActivity() {
        Log.d(TAG, "startCarlinkMainActivity()");
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, CarLinkMainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (context != null) {
            context.startActivity(intent);
        }
    }

    private void startMainActivity() {
        Log.d(TAG, "startMainActivity()");
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (context != null) {
            context.startActivity(intent);
        }
    }

}
