package com.garen.AudioIntercom.AudioPlayer;

import android.util.Log;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;
import com.garen.AudioIntercom.AudioRecorder.AudioData;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpReciever implements Runnable{
    private static String TAG = "UdpReciever";
    private static boolean isRecieving = false;
    private static DatagramSocket mRecieveUdp = null;
    private static int RecievePORT = 8888;
    private LinkedBlockingQueue<AudioData> queue;
    private int QUEUE_MAX_COUNT = 100;

    public void startUdpRecieving(){

        // 创建 UDP socket
        try {
            mRecieveUdp = new DatagramSocket(RecievePORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // 初始化  数据缓冲区队列.
        queue = new LinkedBlockingQueue<AudioData>(QUEUE_MAX_COUNT);
        Log.i(TAG,"init recieve UDP queue remaining Size --> " + queue.remainingCapacity());
        Log.i(TAG,"init recieve UDP queue elements Size --> " + queue.size());

        // New&&start Thread (把 UdpReciever this 作为构造方法的参数)
        new Thread(this).start();

        // 设置 isRecieving
        isRecieving = true;

    }

    public void stopUdpRecieving(){

        // 设置 isRecieving
        isRecieving = false;

        //关闭接收端
        mRecieveUdp.close();

        // 把 队列的音频数据，全部写入文件中进行调试验证
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            FileOutputStream udpRecieveFos = null;
            AudioData needPlayData = null;

            try {
                udpRecieveFos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_PLAYER_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while(queue.size() != 0){
                try {
                    needPlayData = queue.take();
                    udpRecieveFos.write(needPlayData.getData(),0,needPlayData.getLen());
                    Log.i(TAG,"needPlayData len --> " + needPlayData.getLen() + "; queue size --> " + queue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (udpRecieveFos != null) {
                try {
                    Log.i(TAG, "udpRecieveFos.close() . . .");
                    udpRecieveFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void addDataIntoQueue(byte[] data , int len, FileOutputStream diacardFos){

        // new AudioData 类.
        // 需要把 data 数组 重新拷贝一份到 AudioData 中.
        AudioData audioData = new AudioData(data, len);

        // 如果队列是满的，则出列一个元素(即丢弃录取的音频的 前面部分数据), 来实现继续能 入队列操作.
        if(queue.remainingCapacity() ==0){

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

        byte[] bytes = new byte[1024];
        FileOutputStream fos = null;
        FileOutputStream discardFos = null;

        // 2. new FileOutputStream (以 AUDIO_UDP_RECIEVE_PLAYER_FILENAME 为文件名)
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            try {
                fos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_UDP_RECIEVE_PLAYER_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                discardFos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_DISCARD_PLAYER_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        while (isRecieving){

            // 创建一个 DatagramPacket 数据包，用于接收 UDP 数据
            DatagramPacket dp = new DatagramPacket(bytes,bytes.length);

            // 调用 DatagramSocket 对象的 receive 方法接收数据
            try {
                mRecieveUdp.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 解析 UDP 数据包.
            byte[] datas = dp.getData();
            int len = dp.getLength();

            // 把 音频数据封装为 AudioData, 并添加到 缓冲区队列中.
            addDataIntoQueue(datas,len,discardFos);


            // 把音频数据写入到文件中，来进一步验证.
            if(AudioConfig.IS_SAVE_AUDIODATA) {
                try {
                    fos.write(datas,0,len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 释放资源.
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            if (fos != null) {
                try {
                    Log.i(TAG, "fos.close() . . .");
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (discardFos != null) {
                try {
                    Log.i(TAG, "discardFos.close() . . .");
                    discardFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
