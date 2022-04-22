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


public class AudioRecorder implements Runnable {

    public static final String TAG = "AudioRecorder";
    public AudioRecord mAudioRecord = null;
    private int AudioStatus = AudioRecord.STATE_UNINITIALIZED;
    private Thread mAudioRecordThread = null;
    private int audioBufferSize = 0;
    private boolean isRecording = false;

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

    @Override
    public void run() {

        byte[] audioData = new byte[audioBufferSize];
        int readCnt = 0;
        FileOutputStream fos = null;

        // Starts recording from the AudioRecord instance.
        mAudioRecord.startRecording();

        // 2. new FileOutputStream (以 AUDIO_RECORD_FILENAME 为文件名)
        try {
            fos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" +AudioConfig.AUDIO_RECORD_FILENAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // read audio data.
        while(isRecording){
            readCnt = mAudioRecord.read(audioData,0,audioData.length);
            Log.i(TAG,"readCnt --> " + readCnt);

            if(AudioConfig.IS_SAVE_AUDIODATA){
                // 将把 auido data 写入 文件中.
                try {
                    fos.write(audioData,0,readCnt);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(fos != null) {
            try {
                Log.i(TAG,"fos.close() . . .");
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
