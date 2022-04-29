package com.garen.AudioIntercom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.garen.AudioIntercom.AudioPlayer.AudioPlayer;
import com.garen.AudioIntercom.AudioPlayer.UdpReciever;
import com.garen.AudioIntercom.AudioRecorder.AudioRecorder;
import com.garen.AudioIntercom.AudioRecorder.UdpSender;

public class MainActivity extends AppCompatActivity {

    private Button startRecordBtn,stopRecordBtn,stopRecieveUdpBtn;
    private int ret = 0;
    private static final String TAG = "AudioIntercom.MainActivity";
    private AudioRecorder recorder = null;
    private UdpSender udpSender = null;
    private UdpReciever udpReciever = null;
    private AudioPlayer audioPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 找到 R.id.StartrecordBtn
        startRecordBtn = (Button)findViewById(R.id.StartRecordBtnId);

        // 设置 按键的单击事件的回调函数.
        startRecordBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                recorder = new AudioRecorder();
                ret = recorder.initRecorder();
                Log.i(TAG ,"init recording audio --> " + ret);
                if(ret == AudioRecorder.STATE_UNINITIALIZED){
                    Log.i(TAG ,"init recording audio Failed.");
                    return;
                }else {
                    Log.i(TAG ,"will start recording audio");
                    recorder.startRecording();
                }

                // 启动 UDP发送线程，来发送 录音数据.
                udpSender = new UdpSender(recorder.getAudioRecordDataQueue());
                udpSender.startUdpSending();

                // 启动 UDP 接收线程，用于 接收音频数据
                udpReciever = new UdpReciever();
                udpReciever.startUdpRecieving();

                // 启动 Auido Player 播放线程, 用于播放音频
                audioPlayer = new AudioPlayer(udpReciever.getAudioPlayerDataQueue());
                audioPlayer.startPlaying();
            }
        });

        stopRecordBtn = (Button) findViewById(R.id.StopRecordBtnId);
        stopRecordBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG ,"will stop recording audio");
                recorder.stopRecording();
            }
        });

        stopRecieveUdpBtn = (Button) findViewById(R.id.StopRecieveBtnId);
        stopRecieveUdpBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i(TAG ,"will stop recieving UDP audio data.");
                udpReciever.stopUdpRecieving();

                Log.i(TAG ,"will stop playing audio data.");
                audioPlayer.stopPlaying();
            }
        });



    }

}