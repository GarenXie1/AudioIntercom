package com.garen.AudioIntercom.AudioRecorder;

import android.util.Log;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpSender implements Runnable{

    private static String TAG = "UdpSender";
    private boolean isUdpSending =false;
    private LinkedBlockingQueue<AudioData> mAudioRecordQueue;
    private AudioData recordData;
    private DatagramSocket udpSocket;
    private String mDstIP = "192.168.1.100";
    private int PORT = 10086;

    // 通过 构造函数 获取 Audio Record Queue 引用.
    public UdpSender(LinkedBlockingQueue<AudioData> queue){
        mAudioRecordQueue = queue;
    }

    // 通过 构造函数 获取 Audio Record Queue 引用.
    public UdpSender(LinkedBlockingQueue<AudioData> queue, String ip){
        mAudioRecordQueue = queue;
        mDstIP = ip;
    }

    public void startUdpSending(){

        try {
            udpSocket = new DatagramSocket();
        }catch (SocketException e){
            e.printStackTrace();
        }

        // New&&start Thread (把 UdpSender对象引用 this 作为构造方法的参数)
        new Thread(this).start();

        // 设置 isUdpSending
        isUdpSending = true;
    }

    public void stopUdpSending(){

        udpSocket.close();
    }

    @Override
    public void run() {

        FileOutputStream udpSendFos = null;

        if(AudioConfig.IS_SAVE_AUDIODATA) {
            try {
                udpSendFos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_UDP_SEND_RECORD_FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        while(isUdpSending){

            // UDP 发送 线程从 LinkedBlockingQueue  队列中取出 AudioRecordData类
            try {
                recordData = mAudioRecordQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 从 AudioRecordData类 获得数据
            byte[] data = recordData.getData();
            int len = recordData.getLen();
            InetAddress address = null;
            try {
                address = InetAddress.getByName(mDstIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if(AudioConfig.IS_SAVE_AUDIODATA) {
                try {
                    udpSendFos.write(data, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 构造 DatagramPacket 对象
            DatagramPacket dp = new DatagramPacket(data,len,address,PORT);
            Log.i(TAG,"UDP sending len --> " + len + "; mAudioRecordQueue size --> " + mAudioRecordQueue.size());

            // UDP 发送
            try {
                udpSocket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(AudioConfig.IS_SAVE_AUDIODATA) {
            if (udpSendFos != null) {
                try {
                    Log.i(TAG, "udpSendFos.close() . . .");
                    udpSendFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
