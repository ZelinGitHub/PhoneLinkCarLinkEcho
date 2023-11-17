package com.wt.phonelink.carlink;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.ucar.vehiclesdk.ICarAudioRecorderListener;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.ucar.vehiclesdk.recorder.AudioConfig;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CarlinkCustomAudioRecord implements ICarAudioRecorderListener {
    private static final String TAG = "AudioRecordDemo";
    private AudioRecord mAudioRecord;
    private AudioConfig mAudioConfig;
    private boolean mSupportStereoRecord = true;
    private int mAudioBufSize = 0;
    public static final int MAX_RETRY_COUNT = 15;
    //是否正在录音
    private volatile boolean mIsRecording = false;
    private static final long INTERVAL_RETRY = 10L;


    //初始化buffer
    private boolean initBuffer() {
        boolean result;
        //如果音频格式是CHANNEL_IN_STEREO
        if (AudioFormat.CHANNEL_IN_STEREO == mAudioConfig.getChannel()) {
            //得到最小的buffer大小
            mAudioBufSize = AudioRecord.getMinBufferSize(
                    mSupportStereoRecord ? mAudioConfig.getSampleRate() : mAudioConfig.getSampleRate() * 2,
                    mSupportStereoRecord ? mAudioConfig.getChannel() : AudioFormat.CHANNEL_IN_MONO,
                    mAudioConfig.getFormat()
            );
        } else {
            //得到最小的buffer大小
            mAudioBufSize = AudioRecord.getMinBufferSize(
                    mAudioConfig.getSampleRate(),
                    mAudioConfig.getChannel(),
                    mAudioConfig.getFormat()
            );
        }
        result = mAudioBufSize != AudioRecord.ERROR_BAD_VALUE;
        if (!result) {
            Log.e(TAG, "initBuffer error!");
        }
        return result;
    }


    //创建AudioRecord构造器
    private AudioRecord.Builder createAudioRecordBuilder() {
        //音频格式构造器
        AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
//        //如果音频格式是CHANNEL_IN_STEREO
//        if (AudioFormat.CHANNEL_IN_STEREO == mAudioConfig.getChannel()) {
//            //设置音频采样率
//            //设置channelMask
//            audioFormatBuilder
//                    .setSampleRate(
//                            mSupportStereoRecord ? mAudioConfig.getSampleRate() : mAudioConfig.getSampleRate() * 2
//                    )
//                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO);
////                            .setChannelMask(
////                            mSupportStereoRecord ? mAudioConfig.getChannel() : AudioFormat.CHANNEL_IN_MONO
////                    )
//        } else {
//            //设置音频采样率
//            //设置channelMask
//            audioFormatBuilder
//                    .setSampleRate(
//                            mAudioConfig.getSampleRate()
//                    )
//                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO);
////                    .setChannelMask(
////                            mAudioConfig.getChannel()
////                    );
//        }
        //音频格式。使用音频格式构造器，构造音频格式
        AudioFormat format =
                audioFormatBuilder
                        .setEncoding(mAudioConfig.getFormat())
                        .setChannelIndexMask(0x03) // 2ch：0x03,4ch:0xf,6ch:0x3f
                        .build();
        //AudioRecord构造器
        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioFormat(format)
//                .setAudioSource(mAudioConfig.getSource())
                //修改固定为mic类型
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setBufferSizeInBytes(mAudioBufSize);
        return builder;
    }

    //开始录音的回调
    @Override
    public void onStartRecorder(UCarCommon.AudioFormat format, boolean isCallActive) {
        Log.d(TAG, "startRecorder: ");
        //onStartRecorder为开始录制，需车厂自己创建AudioRecord进行录制并且通过sendMicRecordData传输音频数据到手机
        mAudioConfig = AudioConfig.getCarConfig(format);
        //初始化buffer
        initBuffer();
        // 车厂可以在此调整为自己所需的配置
        if (null == mAudioRecord) {
            //AudioRecord构造器
            AudioRecord.Builder builder = createAudioRecordBuilder();
            try {
                //使用Builder构造AudioRecord
                mAudioRecord = builder.build();
            } catch (UnsupportedOperationException e) {
                //再次创建，原因不明
                builder = createAudioRecordBuilder();
                try {
                    //使用Builder构造AudioRecord
                    mAudioRecord = builder.build();
                } catch (UnsupportedOperationException exception) {
                    Log.w(TAG, "initRecorder failed", exception);
                }
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    Log.d(TAG, "run: ");
                }
                Log.i(TAG, "car audio recorder thread start");
                int retryCount = MAX_RETRY_COUNT;
                mIsRecording = false;
                while (0 < retryCount--) {
                    //使用AudioRecord开始录音
                    mAudioRecord.startRecording();
                    if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                        try {
                            Thread.sleep(INTERVAL_RETRY);
                        } catch (IllegalArgumentException | InterruptedException e) {
                            Log.e(TAG, "sleep duration start recording error.", e);
                        }
                        continue;
                    }
                    //正在录音
                    mIsRecording = true;
                    break;
                }

                if (!mIsRecording) {
                    Log.e(TAG, "mic is occupied, exit audio record thread");
                }
                int size = mAudioBufSize / 2;
                short[] samples = new short[size];
                //如果正在录音
                while (mIsRecording) {
                    //使用AudioRecord读取数据
                    int bufferRead = mAudioRecord.read(samples, 0, size);
                    if (bufferRead > 0) {
                        Log.d(TAG, "custom recored sendData, bodyLen: " + bufferRead);
                        Calendar calendar = new GregorianCalendar();
                        int time = (int) (calendar.getTimeInMillis() / 1000);
                        //发送mic数据
                        UCarAdapter.getInstance().sendMicRecordData(bufferRead, samples, time);
                    }
                }
            }
        }).start();

        ///
    }

    //结束录音的回调
    @Override
    public void onStopRecorder() {
        this.mIsRecording = false;
        Log.d(TAG, "stopRecorder: ");
    }
}
