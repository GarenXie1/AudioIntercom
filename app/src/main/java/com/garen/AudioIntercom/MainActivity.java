package com.garen.AudioIntercom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.garen.AudioIntercom.AudioPlayer.AudioPlayer;
import com.garen.AudioIntercom.AudioPlayer.UdpReciever;
import com.garen.AudioIntercom.AudioRecorder.AudioRecorder;
import com.garen.AudioIntercom.AudioRecorder.UdpSender;

public class MainActivity extends AppCompatActivity {

    private Button startRecordBtn,stopRecordBtn,stopRecieveUdpBtn;
    private EditText edit_ip;
    private int ret = 0;
    private static final String TAG = "AudioIntercom.MainActivity";
    private AudioRecorder recorder = null;
    private UdpSender udpSender = null;
    private UdpReciever udpReciever = null;
    private AudioPlayer audioPlayer = null;
    private String OtherIP = null;

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF) ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取 本地 IP 地址
        WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        String ip = null;
        if (wifiManager != null) {
            ip = intToIp(wifiManager.getDhcpInfo().ipAddress);
            Log.i(TAG ,"Local Ip address : " + ip);
        }

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
                if(OtherIP != null){
                    udpSender = new UdpSender(recorder.getAudioRecordDataQueue(),OtherIP);
                }else {
                    udpSender = new UdpSender(recorder.getAudioRecordDataQueue());
                }
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


        edit_ip = findViewById(R.id.ip_edit);
        edit_ip.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edit_ip.setSingleLine();


        edit_ip.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch (i) {
                    case EditorInfo.IME_ACTION_DONE:
                        OtherIP = edit_ip.getText().toString();
                        Log.i(TAG ,"对端的 Ip 地址: "+ OtherIP);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }

}