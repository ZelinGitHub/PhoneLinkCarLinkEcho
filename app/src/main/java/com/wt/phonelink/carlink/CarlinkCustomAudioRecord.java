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

    //the sample rate expressed in Hertz.
    // 44100Hz is currently the only rate that is guaranteed to work on all devices,
    // but other rates such as 22050, 16000, and 11025 may work on some devices.
    private static final int SAMPLE_RATE_HZ = 16000;
    //声道数 CHANNEL_IN_STEREO是立体声。目前来看这个int值是12
    //describes the configuration of the audio channels.
    // See AudioFormat.CHANNEL_IN_MONO and AudioFormat.CHANNEL_IN_STEREO.
    // AudioFormat.CHANNEL_IN_MONO是单声道，AudioFormat.CHANNEL_IN_STEREO是立体声，立体声就是双声道。
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    //返回的音频数据的格式
    //the format in which the audio data is to be returned. 音频数据的返回格式。
    //See AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT, and AudioFormat.ENCODING_PCM_FLOAT.
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    //核心类，Android系统的录音机
    private AudioRecord mAudioRecord;
    //录音的缓存大小。录音缓存是byte数据。
    private int mAudioBufSize = 0;
    //是否正在录音
    private volatile boolean mIsRecording = false;

    private static String sPathPcm = "";
    private static String sPathWav = "";

    public void init(Context context) {
        sPathPcm = context.getFilesDir() + "/" + "abc.pcm";
        sPathWav = context.getFilesDir() + "/" + "abc.wav";
        Log.i(TAG, "init() sPathPcm: " + sPathPcm);
        Log.i(TAG, "init() sPathWav: " + sPathWav);
    }

    //开始录音的回调，被carlink sdk调用
    @Override
    public void onStartRecorder(UCarCommon.AudioFormat format, boolean isCallActive) {
        Log.d(TAG, "startRecorder()");
        createAudioRecord();
        startRecord();
        //开始录音，并获取录音数据
        new Thread(() -> {
            Log.i(TAG, "onStartRecorder() 在新线程获取数据");
            getAndSaveData();
        }).start();
    }


    //结束录音的回调
    //结束录音，将mIsRecording置为false，getData中的循环就会失效
    //被CarLink sdk调用
    @Override
    public void onStopRecorder() {
        Log.d(TAG, "stopRecorder()");
        reset();
//        convertPCM2WAV();
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

    private static void convertPCM2WAV() {
        PcmToWavUtil ptwUtil = new PcmToWavUtil(SAMPLE_RATE_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        ptwUtil.pcmToWav(sPathPcm, sPathWav, true);
    }

    private void createAudioRecord() {
        //onStartRecorder为开始录制，需车厂自己创建AudioRecord进行录制并且通过sendMicRecordData传输音频数据到手机
        // 车厂可以在此调整为自己所需的配置
        if (null == mAudioRecord) {
            //初始化录音的缓存大小
            boolean result = initBuffer();
            Log.d(TAG, "createAudioRecord() result: " + result);
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

    //初始化buffer
    //获取录音的缓存大小
    private boolean initBuffer() {
        boolean result;
        //如果音频格式是CHANNEL_IN_STEREO
        //录音的缓存大小
        mAudioBufSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
        );
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
        //音频格式。使用音频格式构造器，构造音频格式
        AudioFormat format =
                audioFormatBuilder
                        //设置采样率
                        .setSampleRate(SAMPLE_RATE_HZ)
                        //修改为立体声
                        .setChannelIndexMask(0x03) // 2ch：0x03,4ch:0xf,6ch:0x3f
                        //设置音频数据的返回格式
                        .setEncoding(AUDIO_FORMAT)
                        .build();
        //AudioRecord构造器
        return new AudioRecord.Builder()
                //修改固定为mic类型
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(format)
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
                getData(fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getAndSaveData() error: " + e.getLocalizedMessage());
        }
    }

    private void getData(FileOutputStream fos) throws IOException {
        if (mAudioRecord == null) {
            Log.e(TAG, "mAudioRecord is null! ");
            return;
        }
        //读取录音数据的大小。录音缓存是byte数据。
        //short是byte的两倍，所以转换成short数据，这里size要除以2
//        int size = mAudioBufSize / 2;
        int size = mAudioBufSize;
        //存储录音数据的数组。因为sendMicRecordData的参数是short数组，所以这里就传了short数组。
        //因为short是2字节，byte是1字节，所以size要除以2。这样才会和byte[mAudioBufSize]的容量相等。
        short[] audioData = new short[size];
//        byte[] audioData = new byte[size];
        //如果正在录音
        //会一直调用read方法读取数据
        while (mIsRecording) {
            //使用AudioRecord读取数据
            //如果把读取到的数据保存下来是一个PCM文件
            //本来这里audioRead要传byte数组，但是因为sendMicRecordData的参数是short数组，所以这里就传了short数组。
            int result = mAudioRecord.read(audioData, 0, size);
            if (result > 0) {
                sendToCarLink(audioData, result);
//                saveToLocal(fos, audioData);
            }
        }
    }


    private static void sendToCarLink(short[] audioData, int result) {
        Log.d(TAG, "custom record sendData, bodyLen: " + result);
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
