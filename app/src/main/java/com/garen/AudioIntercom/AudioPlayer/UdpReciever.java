package com.garen.AudioIntercom.AudioPlayer;

import android.util.Log;

import com.garen.AudioIntercom.AudioConfig.AudioConfig;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpReciever implements Runnable{
    private static String TAG = "UdpReciever";
    private static boolean isRecieving = false;
    private static DatagramSocket mRecieveUdp = null;
    private static int RecievePORT = 8888;

    public void startUdpRecieving(){

        // 创建 UDP socket
        try {
            mRecieveUdp = new DatagramSocket(RecievePORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

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
    }

    @Override
    public void run() {

        byte[] bytes = new byte[1024];
        FileOutputStream fos = null;

        // 2. new FileOutputStream (以 AUDIO_UDP_RECIEVE_PLAYER_FILENAME 为文件名)
        if(AudioConfig.IS_SAVE_AUDIODATA) {
            try {
                fos = new FileOutputStream(AudioConfig.AUDIO_SAVE_PATH + "/" + AudioConfig.AUDIO_UDP_RECIEVE_PLAYER_FILENAME);
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
        }
    }
}
