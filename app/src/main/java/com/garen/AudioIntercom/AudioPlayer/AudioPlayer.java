package com.garen.AudioIntercom.AudioPlayer;

import android.media.AudioTrack;
import android.util.Log;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;
import com.garen.AudioIntercom.AudioRecorder.AudioData;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioPlayer implements Runnable{
    private static LinkedBlockingQueue<AudioData> mAudioPlayerDataQueue = null;
    private static String TAG = "AudioPlayer";
    private AudioTrack mAudioTrack = null;
    private boolean isPlaying = false;

    // 通过 构造函数 获取 Audio Player Queue 引用.
    public AudioPlayer(LinkedBlockingQueue<AudioData> queue){
        mAudioPlayerDataQueue = queue;
    }

    public void startPlaying(){
        // 1. 获取 minimum 播放音频数据缓冲段大小
        int bufferSize = AudioTrack.getMinBufferSize(AudioConfig.SAMPLE_RATE,AudioConfig.AudioPlayerChannelConfig,AudioConfig.audioFormat);

        // 2. 初始化 AudioTrack 音频播放器
        mAudioTrack = new AudioTrack(AudioConfig.AudioPlayerStreamType, AudioConfig.SAMPLE_RATE, AudioConfig.AudioPlayerChannelConfig,
                                    AudioConfig.audioFormat, bufferSize, AudioConfig.AudioPlayerMode);

        // 3. 判断 AudioTrack instance 实例化对象是否初始化成功.
        if(mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED){
            Log.e(TAG, "init AudioTrack Failed.");
        }

        // 开始播放线程.
        new Thread(this).start();

        // 设置 isPlaying
        isPlaying = true;
    }

    public void stopPlaying(){

        // getPlayState --> Returns the playback state of the AudioTrack instance.
        if (mAudioTrack != null && mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
            if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {

                // 7. Flushes the audio data currently queued for playback
                mAudioTrack.flush();

                // 8. Stops playing the audio data.
                // (MODE_STREAM) audio will stop playing after the last buffer that was written has been played.
                mAudioTrack.stop();
            }

            // 9. Releases the native AudioTrack resources.
            mAudioTrack.release();
            mAudioTrack = null;
        }

        // 设置 isPlaying
        isPlaying = false;
    }


    @Override
    public void run() {
        FileOutputStream playFos = null;

        // 4. Starts playing an AudioTrack.
        if(mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            mAudioTrack.play();
        }


        if(AudioConfig.IS_SAVE_AUDIODATA) {
            try {
                playFos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_PLAYER_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        while (isPlaying){
            // 5. 从 队列缓存区 中取出 需要播放的音频数据.
            AudioData needPlayAudioData = null;
            try {
                // 如果队列为空，会导致阻塞在此.
                Log.i(TAG,"mAudioPlayerDataQueue size --> " + mAudioPlayerDataQueue.size());
                needPlayAudioData = mAudioPlayerDataQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 6. 写入 需要播放的音频数据 到 AudioTrack.
            byte[] data = needPlayAudioData.getData();
            int len = needPlayAudioData.getLen();
            mAudioTrack.write(data,0,len);

            if(AudioConfig.IS_SAVE_AUDIODATA) {
                try {
                    playFos.write(data,0,len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(AudioConfig.IS_SAVE_AUDIODATA) {
            if (playFos != null) {
                try {
                    Log.i(TAG, "playFos.close() . . .");
                    playFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
