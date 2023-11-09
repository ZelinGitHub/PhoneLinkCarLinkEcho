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
    private volatile boolean mIsRecording = false;
    private static final long INTERVAL_RETRY = 10L;
    private boolean initBuffer() {
        boolean result;
        if (AudioFormat.CHANNEL_IN_STEREO == mAudioConfig.getChannel()) {
            mAudioBufSize = AudioRecord.getMinBufferSize(
                    mSupportStereoRecord ? mAudioConfig.getSampleRate() : mAudioConfig.getSampleRate() * 2,
                    mSupportStereoRecord ? mAudioConfig.getChannel() : AudioFormat.CHANNEL_IN_MONO,
                    mAudioConfig.getFormat());
        } else {
            mAudioBufSize = AudioRecord.getMinBufferSize(
                    mAudioConfig.getSampleRate(),
                    mAudioConfig.getChannel(),
                    mAudioConfig.getFormat());
        }
        result = mAudioBufSize != AudioRecord.ERROR_BAD_VALUE;
        if (!result) {
            Log.e(TAG, "initBuffer error!");
        }
        return result;
    }
    private AudioRecord.Builder createAudioRecordBuilder() {
        AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
        if (AudioFormat.CHANNEL_IN_STEREO == mAudioConfig.getChannel()) {
            audioFormatBuilder.setSampleRate(mSupportStereoRecord ? mAudioConfig.getSampleRate() : mAudioConfig.getSampleRate() * 2)
                    .setChannelMask(mSupportStereoRecord ? mAudioConfig.getChannel() : AudioFormat.CHANNEL_IN_MONO);
        } else {
            audioFormatBuilder.setSampleRate(mAudioConfig.getSampleRate())
                    .setChannelMask(mAudioConfig.getChannel());
        }
        AudioFormat format = audioFormatBuilder.setEncoding(mAudioConfig.getFormat())
                .build();
        AudioRecord.Builder builder = new AudioRecord.Builder()
                .setAudioFormat(format)
//                .setAudioSource(mAudioConfig.getSource())
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setBufferSizeInBytes(mAudioBufSize);
        return builder;
    }

    @Override
    public void onStartRecorder(UCarCommon.AudioFormat format, boolean isCallActive) {
        Log.d(TAG, "startRecorder: ");
        //onStartRecorder为开始录制，需车厂自己创建AudioRecord进行录制并且通过sendMicRecordData传输音频数据到手机
        mAudioConfig = AudioConfig.getCarConfig(format);
        initBuffer();
        // 车厂可以在此调整为自己所需的配置
        if (null == mAudioRecord) {
            AudioRecord.Builder builder = createAudioRecordBuilder();
            try {
                mAudioRecord = builder.build();
            } catch (UnsupportedOperationException e) {
                builder = createAudioRecordBuilder();
                try {
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
                    mAudioRecord.startRecording();
                    if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                        try {
                            Thread.sleep(INTERVAL_RETRY);
                        } catch (IllegalArgumentException | InterruptedException  e) {
                            Log.e(TAG, "sleep duration start recording error.", e);
                        }
                        continue;
                    }
                    mIsRecording = true;
                    break;
                }

                if (!mIsRecording) {
                    Log.e(TAG, "mic is occupied, exit audio record thread");
                }
                int size = mAudioBufSize / 2;
                short[] samples = new short[size];
                while (mIsRecording) {
                    int bufferRead = mAudioRecord.read(samples, 0, size);
                    if (bufferRead > 0) {
                        Log.d(TAG, "custom recored sendData, bodyLen: " + bufferRead);
                        Calendar calendar = new GregorianCalendar();
                        int time = (int) (calendar.getTimeInMillis() / 1000);
                        UCarAdapter.getInstance().sendMicRecordData(bufferRead, samples, time);
                    }
                }
            }
        }).start();

        ///
    }

    @Override
    public void onStopRecorder() {
        this.mIsRecording = false;
        Log.d(TAG, "stopRecorder: ");
    }
}
