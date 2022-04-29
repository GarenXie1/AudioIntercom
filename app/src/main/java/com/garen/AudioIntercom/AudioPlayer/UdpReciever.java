package com.garen.AudioIntercom.AudioPlayer;

import android.util.Log;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;
import com.garen.AudioIntercom.AudioRecorder.AudioData;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpReciever implements Runnable{
    private static String TAG = "UdpReciever";
    private static boolean isRecieving = false;
    private static DatagramSocket mRecieveUdp = null;
    private static int RecievePORT = 10086;
    private LinkedBlockingQueue<AudioData> queue;
    private int QUEUE_MAX_COUNT = 100;
    private String mStopCommand = "STOP";

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

    private void sendUDPCommand(String cmd){

        // 发送给 127.0.0.1 内部地址
        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // 构造 发送数据的 packet
        DatagramPacket dp = new DatagramPacket(cmd.getBytes(),cmd.length(),address,RecievePORT);

        // 调用 UDP send 发送
        try {
            mRecieveUdp.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopUdpRecieving(){

        // 把 STOP UDP 命令 发送给 接收 UDP 线程.
        sendUDPCommand(mStopCommand);

        // 设置 isRecieving
        isRecieving = false;

        //关闭接收端
        mRecieveUdp.close();
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

        // 把 需要播放的音频 AudioData 引用放入 缓冲区队列 中.
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
                // blocks until a datagram is received.
                mRecieveUdp.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 解析 UDP 数据包.
            byte[] datas = dp.getData();
            int len = dp.getLength();

            if(new String(datas,0,len).equals(mStopCommand)){
                Log.i(TAG,"UDP recieve Thread will Stop , due to recieving UDP stop command.");
                break;
            }else {
                // 把 音频数据封装为 AudioData, 并添加到 缓冲区队列中.
                addDataIntoQueue(datas, len, discardFos);
            }

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

    public LinkedBlockingQueue<AudioData> getAudioPlayerDataQueue() {
        return queue;
    }
}
