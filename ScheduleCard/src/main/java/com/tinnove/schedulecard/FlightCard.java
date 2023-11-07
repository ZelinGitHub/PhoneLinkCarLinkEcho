package com.tinnove.schedulecard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tinnove.hicarclient.HiCarManager;
import com.tinnove.hicarclient.HiCarServiceListener;
import com.tinnove.hicarclient.PhoneLinkConnectListener;

public class FlightCard extends FrameLayout {
    private final ImageView iv_icon;
    private final TextView tv_title;
    private final TextView tv_flight_title;
    private final TextView tv_flight_start_time;
    private final TextView tv_flight_content;
    private final TextView tv_no_schedule;
    private final TextView tv_connect;
    private final Button btn_connect;
    private static final String TAG = "WTPhoneLink/FlightCard";

    public FlightCard(@NonNull Context context) {
        this(context, null);
    }

    public FlightCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlightCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FlightCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //attachToRoot为true会将控件添加到父控件，不需要再调用addView
        View view = layoutInflater.inflate(R.layout.layout_flight_card, this, true);
        iv_icon = view.findViewById(R.id.iv_icon);
        tv_title = view.findViewById(R.id.tv_title);
        tv_flight_title = view.findViewById(R.id.tv_flight_title);
        tv_flight_start_time = view.findViewById(R.id.tv_flight_start_time);
        tv_flight_content = view.findViewById(R.id.tv_flight_content);
        tv_no_schedule = view.findViewById(R.id.tv_no_schedule);
        tv_connect = view.findViewById(R.id.tv_connect);
        btn_connect = view.findViewById(R.id.btn_connect);
        //
        btn_connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhoneLinkActivity(context);
            }
        });
        HiCarManager.getInstance().init(context);
        HiCarManager.getInstance().registerHiCarListener(new HiCarServiceListener() {
            @Override
            public void onDataReceive(String key, int dataType, byte[] data) {
                Log.i(TAG, "onDataReceive() key: " + key + ", dataType: " + dataType);
                processData(dataType, data);
            }

            @Override
            public void onDeviceChange(String key, int event, int errorcode) {
                Log.i(TAG, "onDeviceChange() key: " + key + ", event: " + event + ", errorCode: " + errorcode);
                post(new Runnable() {
                    @Override
                    public void run() {
                        //已连接
                        if (event == 101) {
                            updateConnectedUI();
                        }
                        //未连接
                        if (event == 102) {
                            updateDisconnectedUI();
                        }
                    }
                });
            }
        });
        try {
            HiCarManager.getInstance().bindHiCarService();
            HiCarManager.getInstance().registerPhoneLinkConnectListener(new PhoneLinkConnectListener() {
                @Override
                public void onServiceConnected() {
                    Log.i(TAG,"onServiceConnected() ");
                    try {
                        if(HiCarManager.getInstance().isConnected()){
                            updateConnectedUI();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processData(int dataType, byte[] data) {
        switch (dataType) {
            //音频元数据
            case Contants.DATA_TYPE_META_MEDIA:
                try {
                    String content = new String(data);
                    Log.i(TAG, "onDataReceive() content: " + content);
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String mediaDataStr = jsonObject.getString("MediaData");
                    Log.i(TAG, "onDataReceive() mediaDataStr: " + mediaDataStr);
                    //jsonObject2
                    org.json.JSONObject jsonObject2 = new org.json.JSONObject(mediaDataStr);
//                        String AlbumArtURL = jsonObject2.getString("AlbumArtURL");
                    String AlbumName = jsonObject2.getString("AlbumName");
                    String AppName = jsonObject2.getString("AppName");
                    String Artist = jsonObject2.getString("Artist");
                    int ElapsedTime = jsonObject2.getInt("ElapsedTime");
                    boolean IsAudioFromCar = jsonObject2.getBoolean("IsAudioFromCar");
                    String Name = jsonObject2.getString("Name");
                    String AppPackageName = jsonObject2.getString("AppPackageName");
                    int Status = jsonObject2.getInt("Status");
                    int TotalTime = jsonObject2.getInt("TotalTime");
                    //MediaData
                    MediaData mediaData = new MediaData();
//                        mediaData.setAlbumArtURL(AlbumArtURL);
                    mediaData.setAlbumName(AlbumName);
                    mediaData.setAppName(AppName);
                    mediaData.setArtist(Artist);
                    mediaData.setElapsedTime(ElapsedTime);
                    mediaData.setAudioFromCar(IsAudioFromCar);
                    mediaData.setName(Name);
                    mediaData.setAppPackageName(AppPackageName);
                    mediaData.setStatus(Status);
                    mediaData.setTotalTime(TotalTime);
                    Log.i(TAG, "onDataReceive() mediaData: " + mediaData);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            case Contants.DATA_TYPE_META_SCENE:
                try {
                    String content = new String(data);
                    Log.i(TAG, "onDataReceive() content: " + content);
                    org.json.JSONObject jsonObject = new org.json.JSONObject(content);
                    String sceneDataStr = jsonObject.getString("ScenceData");
                    Log.i(TAG, "onDataReceive() sceneDataStr: " + sceneDataStr);
                    //jsonObject2
                    org.json.JSONObject jsonObject2 = new org.json.JSONObject(sceneDataStr);
                    //场景类元数据的类型，包括如下 1 表示日历， 2 表示天气， 3 表示情景智能， 4 表示IoT 控制。
                    int Type = jsonObject2.getInt("Type");
                    int Status = jsonObject2.getInt("Status");
                    int Id = jsonObject2.getInt("Id");
//                        String Title = jsonObject2.getString("Title");
                    String AppPackage = jsonObject2.getString("AppPackage");
                    String MainText = jsonObject2.getString("MainText");
                    String SubText = jsonObject2.getString("SubText");
                    String OptText = jsonObject2.getString("OptText");
                    //ScenceData
                    ScenceData scenceData = new ScenceData();
                    scenceData.setType(Type);
                    scenceData.setStatus(Status);
                    scenceData.setId(Id);
//                        scenceData.setTitle(Title);
                    scenceData.setAppPackage(AppPackage);
                    scenceData.setMainText(MainText);
                    scenceData.setSubText(SubText);
                    scenceData.setOptText(OptText);
                    Log.i(TAG, "onDataReceive() scenceData: " + scenceData);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            switch (Type) {
                                case 1:
                                    updateCalendarUI(scenceData);
                                    break;
                                case 2:
                                    updateWeatherUI(scenceData);
                                    break;
                                case 3:
                                    updateSituationalIntelligenceUI(scenceData);
                                    break;
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
        }
    }


    private void updateDisconnectedUI() {
        Log.i(TAG, "updateDisconnectedUI()");
        iv_icon.setImageResource(R.drawable.ic_desktop_shoujihulian_nor);
        tv_title.setText("手机互联");
        tv_connect.setVisibility(View.VISIBLE);
        btn_connect.setVisibility(View.VISIBLE);
        tv_no_schedule.setVisibility(View.GONE);
        tv_flight_title.setVisibility(View.GONE);
        tv_flight_start_time.setVisibility(View.GONE);
        tv_flight_content.setVisibility(View.GONE);
    }

    private void updateConnectedUI() {
        Log.i(TAG, "updateConnectedUI()");
        iv_icon.setImageResource(R.drawable.ic_desktop_hicar_nor);
        tv_title.setText("HUAWEI Hicar");
        tv_connect.setVisibility(View.GONE);
        btn_connect.setVisibility(View.GONE);
        tv_no_schedule.setVisibility(View.VISIBLE);
        tv_flight_title.setVisibility(View.GONE);
        tv_flight_start_time.setVisibility(View.GONE);
        tv_flight_content.setVisibility(View.GONE);
    }

    private void updateWeatherUI(ScenceData scenceData) {
        Log.i(TAG, "updateWeatherUI()");
    }

    private void updateCalendarUI(ScenceData scenceData) {
        Log.i(TAG, "updateCalendarUI()");
    }

    private void updateSituationalIntelligenceUI(ScenceData scenceData) {
        Log.i(TAG, "updateSituationalIntelligenceUI()");
        iv_icon.setImageResource(R.drawable.ic_desktop_hicar_nor);
        tv_title.setText("HUAWEI Hicar");
        tv_connect.setVisibility(View.GONE);
        btn_connect.setVisibility(View.GONE);
        tv_no_schedule.setVisibility(View.GONE);
        tv_flight_title.setVisibility(View.VISIBLE);
        tv_flight_start_time.setVisibility(View.VISIBLE);
        tv_flight_content.setVisibility(View.VISIBLE);
        tv_flight_title.setText(scenceData.getOptText());
        tv_flight_start_time.setText(scenceData.getMainText());
        String subText = scenceData.getSubText();
        String subText2 = subText.replace("\n", " ");
        tv_flight_content.setText(subText2);
    }

    public void startPhoneLinkActivity(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.wt.phonelink", "com.wt.phonelink.MainActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
