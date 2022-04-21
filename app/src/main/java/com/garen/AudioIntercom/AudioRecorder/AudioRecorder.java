package com.garen.AudioIntercom.AudioRecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;


public class AudioRecorder implements Runnable{

    public static final String TAG = "AudioRecorder";
    public AudioRecord mAudioRecord = null;
    private int AudioStatus = AudioRecord.STATE_UNINITIALIZED;
    private Thread mAudioRecordThread = null;

    public static int STATE_INITIALIZED = AudioRecord.STATE_INITIALIZED;
    public static int STATE_UNINITIALIZED = AudioRecord.STATE_UNINITIALIZED;


    @SuppressLint("MissingPermission")
    public int initRecorder(){
        int audioBufferSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLE_RATE,
                AudioConfig.AudioRecordChannelConfig, AudioConfig.audioFormat);

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
    }


    public void stopRecording(){
        // Stops recording.
        if(AudioStatus == AudioRecord.STATE_INITIALIZED){
            mAudioRecord.stop();
        }
    }

    @Override
    public void run() {

        // Starts recording from the AudioRecord instance.
        mAudioRecord.startRecording();


        byte[] audioData = new byte[1024];
        int readCnt = 0;
        // read audio data.
        while(true){
            readCnt = mAudioRecord.read(audioData,0,audioData.length);
            Log.i(TAG,"readCnt --> " + readCnt);
        }
    }

}
