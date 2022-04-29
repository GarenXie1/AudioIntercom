package com.garen.AudioIntercom.AudioRecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.util.Log;
import android.view.Window;

import androidx.core.app.ActivityCompat;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


public class AudioRecorder implements Runnable {

    public static final String TAG = "AudioRecorder";
    public AudioRecord mAudioRecord = null;
    private int AudioStatus = AudioRecord.STATE_UNINITIALIZED;
    private Thread mAudioRecordThread = null;
    private int audioBufferSize = 0;
    private boolean isRecording = false;
    private int QUEUE_MAX_COUNT = 100;
    private LinkedBlockingQueue<AudioData> queue;

    public static int STATE_INITIALIZED = AudioRecord.STATE_INITIALIZED;
    public static int STATE_UNINITIALIZED = AudioRecord.STATE_UNINITIALIZED;

    public AudioRecorder(){
        if(AudioConfig.IS_SAVE_AUDIODATA){
            // 1. 先判断 AUDIO_SAVE_PATH 目录是否存在，否，则新建目录.
            File audioFileDir = new File(AudioConfig.AUDIO_SAVE_PATH );
            if(!audioFileDir.exists()){
                Log.i(TAG,AudioConfig.AUDIO_SAVE_PATH + " Not Exits.");
                boolean ret = audioFileDir.mkdir();
                if(ret){
                    Log.i(TAG,AudioConfig.AUDIO_SAVE_PATH + " create done.");
                }else{
                    Log.i(TAG,AudioConfig.AUDIO_SAVE_PATH + " create Failed.");
                }
            }
        }

        // 初始化  数据缓冲区队列.
        queue = new LinkedBlockingQueue<AudioData>(QUEUE_MAX_COUNT);
        Log.i(TAG,"init queue remaining Size --> " + queue.remainingCapacity());
        Log.i(TAG,"init queue elements Size --> " + queue.size());
    }

    @SuppressLint("MissingPermission")
    public int initRecorder() {
        audioBufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLE_RATE,
                AudioConfig.AudioRecordChannelConfig, AudioConfig.audioFormat);
        Log.i(TAG,"getMinBufferSize --> " + audioBufferSize);

        // new AudioRecord instance.
        mAudioRecord = new AudioRecord(AudioConfig.AudioRecordSource, AudioConfig.SAMPLE_RATE, AudioConfig.AudioRecordChannelConfig, AudioConfig.audioFormat, audioBufferSize);
        AudioStatus = mAudioRecord.getState();
        if(AudioStatus == AudioRecord.STATE_INITIALIZED){
            Log.i(TAG,"new AudioRecord Successfully.");
        }else if(AudioStatus == AudioRecord.STATE_UNINITIALIZED){
            Log.i(TAG,"new AudioRecord Failed.");
        }
        return AudioStatus;
    }


    public void startRecording() {
        // New Thread (把 AudioRecorder对象 作为构造方法的参数)
        mAudioRecordThread = new Thread(this);

        // start Record Thread. (Will call run())
        mAudioRecordThread.start();

        // set isRecording Flag
        isRecording = true;
    }


    public void stopRecording(){
        // Stops recording.
        if(AudioStatus == AudioRecord.STATE_INITIALIZED){
            mAudioRecord.stop();
        }

        // set isRecording Flag
        isRecording = false;
    }

    private void addRecordDataIntoQueue(byte[] data , int len, FileOutputStream diacardFos){

        // new AudioData 类.
        // 需要把 data 数组 重新拷贝一份到 AudioData 中.
        AudioData audioData = new AudioData(data, len);

        // 如果队列是满的，则出列一个元素(即丢弃录取的音频的 前面部分数据), 来实现继续能 入队列操作.
        if(queue.remainingCapacity() ==0){

            //
            AudioData diacardData = null;
            try {
                diacardData = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.e(TAG,"Queue is Full ,So discard the first audio data !!!!!");

            if(AudioConfig.IS_SAVE_AUDIODATA){
                try {
                    diacardFos.write(diacardData.getData(),0,diacardData.getLen());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 把 AudioData 引用放入 缓冲区队列 中.
        try {
            queue.put(audioData);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        byte[] audioData = new byte[audioBufferSize];
        int readCnt = 0;
        FileOutputStream fos = null;
        FileOutputStream diacardFos = null;


        // Starts recording from the AudioRecord instance.
        mAudioRecord.startRecording();

        // 2. new FileOutputStream (以 AUDIO_RECORD_FILENAME 为文件名)
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            try {
                fos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_RECORD_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                diacardFos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_DISCARD_RECORD_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // read audio data.
        while(isRecording){
            readCnt = mAudioRecord.read(audioData,0,audioData.length);
            //Log.i(TAG,"readCnt --> " + readCnt);

            // 把 音频数据封装为 AudioData, 并添加到 缓冲区队列中.
            addRecordDataIntoQueue(audioData,readCnt,diacardFos);

            if(AudioConfig.IS_SAVE_AUDIODATA){
                // 将把 auido data 写入 文件中.
                try {
                    fos.write(audioData,0,readCnt);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 当录音结果后，释放资源.
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            if (fos != null) {
                try {
                    Log.i(TAG, "fos.close() . . .");
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (diacardFos != null) {
                try {
                    Log.i(TAG, "diacardFos.close() . . .");
                    diacardFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public LinkedBlockingQueue<AudioData> getAudioRecordDataQueue() {
        return queue;
    }
}
