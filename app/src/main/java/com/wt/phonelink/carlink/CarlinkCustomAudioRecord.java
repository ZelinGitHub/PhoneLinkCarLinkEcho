package com.wt.phonelink.carlink;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.ucar.vehiclesdk.ICarAudioRecorderListener;
import com.ucar.vehiclesdk.UCarAdapter;
import com.ucar.vehiclesdk.UCarCommon;
import com.ucar.vehiclesdk.recorder.AudioConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

//这个类的对象将被注册到UCarAdapter，在开始录音和结束录音时调用
public class CarlinkCustomAudioRecord implements ICarAudioRecorderListener {
    private static final String TAG = "PhoneLink/CarlinkCustomAudioRecord";
    //录音失败的最大重试次数
    public static final int MAX_RETRY_COUNT = 15;
    //录音失败后的重试间隔
    private static final long INTERVAL_RETRY = 10L;


    //核心类，Android系统的录音机
    private AudioRecord mAudioRecord;
    //录音的缓存大小。录音缓存是byte数据。
    private int mAudioBufSize = 0;
    //是否正在录音
    private volatile boolean mIsRecording = false;

    private static String sPathPcm = "";
    private static String sPathWav = "";

    private AudioConfig mAudioConfig;
    private boolean mSupportStereoRecord = true;
    private PcmToWavUtil mPtwUtil;

    public void init(Context context) {
        sPathPcm = context.getFilesDir() + "/" + "abc.pcm";
        sPathWav = context.getFilesDir() + "/" + "abc.wav";
        Log.i(TAG, "init() sPathPcm: " + sPathPcm);
        Log.i(TAG, "init() sPathWav: " + sPathWav);
    }

    //开始录音的回调，被carlink sdk调用
    @Override
    public void onStartRecorder(UCarCommon.AudioFormat format, boolean isCallActive) {
        Log.d(TAG, "startRecorder() format: " + format);
        //onStartRecorder为开始录制，需车厂自己创建AudioRecord进行录制并且通过sendMicRecordData传输音频数据到手机
        mAudioConfig = AudioConfig.getCarConfig(format);
        Log.d(TAG, "startRecorder() mAudioConfig: " + mAudioConfig);
        createAudioRecord();
        startRecord();
        //开始录音，并获取录音数据
        new Thread(() -> {
            Log.i(TAG, "onStartRecorder() 在新线程获取数据");
            getAndSendData();
//            getAndSaveData();
        }).start();
    }


    //结束录音的回调
    //结束录音，将mIsRecording置为false，getData中的循环就会失效
    //被CarLink sdk调用
    @Override
    public void onStopRecorder() {
        Log.d(TAG, "stopRecorder()");
        reset();
        convertPCM2WAV();
    }

    private void reset() {
        this.mIsRecording = false;
        Log.d(TAG, "reset()");
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    private void convertPCM2WAV() {
        mPtwUtil.pcmToWav(sPathPcm, sPathWav, true);
    }

    private void createAudioRecord() {
        //onStartRecorder为开始录制，需车厂自己创建AudioRecord进行录制并且通过sendMicRecordData传输音频数据到手机
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
                    Log.w(TAG, "createAudioRecord() exception: ", exception);
                }
            }
        }
    }


    private AudioRecord.Builder createAudioRecordBuilder() {
        Log.i(TAG, "createAudioRecordBuilder() mAudioConfig: " + mAudioConfig);
        int sampleRate;
        int channelMask;
        if (AudioFormat.CHANNEL_IN_STEREO == mAudioConfig.getChannel()) {
            Log.d(TAG, "createAudioRecordBuilder() AudioConfig.getChannel() is CHANNEL_IN_STEREO");
            if (mSupportStereoRecord) {
                sampleRate = mAudioConfig.getSampleRate();
                channelMask = mAudioConfig.getChannel();
            } else {
                sampleRate = mAudioConfig.getSampleRate() * 2;
                channelMask = AudioFormat.CHANNEL_IN_MONO;
            }
        } else {
            Log.d(TAG, "createAudioRecordBuilder() AudioConfig.getChannel() is not CHANNEL_IN_STEREO! ");
            sampleRate = mAudioConfig.getSampleRate();
            channelMask = mAudioConfig.getChannel();
        }
        int audioFormat = mAudioConfig.getFormat();
        channelMask = AudioFormat.CHANNEL_IN_STEREO;
//        sampleRate=16000;
        //初始化录音的缓存大小
        mAudioBufSize = AudioRecord.getMinBufferSize(sampleRate, channelMask, audioFormat);
        if (mAudioBufSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "createAudioRecord() initBuffer error!");
        }
        mPtwUtil = new PcmToWavUtil(sampleRate, channelMask, audioFormat);
        Log.i(TAG, "createAudioRecordBuilder() " +
                "sampleRate: " + sampleRate
                + ", channelMask: " + channelMask
                + " , audioFormat: " + audioFormat
        );
        AudioFormat.Builder audioFormatBuilder = new AudioFormat.Builder();
        audioFormatBuilder
                //1 设置采样率
                .setSampleRate(sampleRate)
                //2 设置通道配置
                .setChannelMask(channelMask)
                //3 设置输出音频格式
                .setEncoding(audioFormat);
        AudioFormat format = audioFormatBuilder.build();
        return new AudioRecord.Builder()
                //1 设置音源类型
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                //2 设置AudioFormat
                .setAudioFormat(format)
                //3 设置buffer
                .setBufferSizeInBytes(mAudioBufSize);

    }

    private void startRecord() {
        if (mAudioRecord == null) {
            Log.e(TAG, "mAudioRecord is null! ");
            return;
        }
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.d(TAG, "run: ");
        }
        Log.i(TAG, "car audio recorder thread start");
        int retryCount = MAX_RETRY_COUNT;
        //是否正在录音的标志
        mIsRecording = false;
        while (0 < retryCount--) {
            //使用AudioRecord开始录音
            mAudioRecord.startRecording();
            //录音状态改变，需要退出本次录音，重新开始录音
            if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                try {
                    Thread.sleep(INTERVAL_RETRY);
                } catch (IllegalArgumentException | InterruptedException e) {
                    Log.e(TAG, "sleep duration start recording error.", e);
                }
                //重新开始录音
                continue;
            }
            //正在录音
            mIsRecording = true;
            //录音结束，跳出循环
            break;
        }
        //多次尝试录音全部失败
        if (!mIsRecording) {
            Log.e(TAG, "mic is occupied, exit audio record thread");
        }
    }

    private void getAndSaveData() {
        FileOutputStream fos = null;
        try {
            try {
                File file = new File(sPathPcm);
                if (!file.exists()) {
                    boolean result = file.createNewFile();
                    Log.i(TAG, "getAndSaveData() result: " + result);
                }
                fos = new FileOutputStream(file);
                if (mAudioRecord == null) {
                    Log.e(TAG, "mAudioRecord is null! ");
                    return;
                }
                int size = mAudioBufSize;
                byte[] audioData = new byte[size];
                //如果正在录音
                //会一直调用read方法读取数据
                while (mIsRecording) {
                    //使用AudioRecord读取数据
                    //如果把读取到的数据保存下来是一个PCM文件
                    int result = mAudioRecord.read(audioData, 0, size);
                    if (result > 0) {
                        saveToLocal(fos, audioData);
                    }
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getAndSaveData() error: " + e.getLocalizedMessage());
        }
    }


    private void getAndSendData() {
        if (mAudioRecord == null) {
            Log.e(TAG, "mAudioRecord is null! ");
            return;
        }
        //读取录音数据的大小。录音缓存是byte数据。
        //short是byte的两倍，所以转换成short数据，这里size要除以2
        int size = mAudioBufSize / 2;
        //存储录音数据的数组。因为sendMicRecordData的参数是short数组，所以这里就传了short数组。
        //因为short是2字节，byte是1字节，所以size要除以2。这样才会和byte[mAudioBufSize]的容量相等。
        short[] audioData = new short[size];
        //如果正在录音
        //会一直调用read方法读取数据
        while (mIsRecording) {
            //使用AudioRecord读取数据
            //如果把读取到的数据保存下来是一个PCM文件
            //本来这里audioRead要传byte数组，但是因为sendMicRecordData的参数是short数组，所以这里就传了short数组。
            int result = mAudioRecord.read(audioData, 0, size);
            if (result > 0) {
                sendToCarLink(audioData, result);
            }
        }
    }


    private static void sendToCarLink(short[] audioData, int result) {
        Calendar calendar = new GregorianCalendar();
        int time = (int) (calendar.getTimeInMillis() / 1000);
        //发送mic数据。参数audioData是short类型。
        UCarAdapter.getInstance().sendMicRecordData(result, audioData, time);
    }

    private static void saveToLocal(FileOutputStream fos, byte[] audioData) throws IOException {
        if (fos != null) {
            fos.write(audioData);
        }
    }

    /**
     * 使用MediaPlayer播放文件，并且指定一个当播放完成后会触发的监听器
     */
    private void playWavWithMediaPlayer(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {
        Log.d(TAG, "playWavWithMediaPlayer()");
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "playWavWithMediaPlayer() e: " + e.getLocalizedMessage());
        }
    }

}
