package com.garen.AudioIntercom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.garen.AudioIntercom.AudioRecorder.AudioRecorder;

public class MainActivity extends AppCompatActivity {

    private Button recordBtn;
    private int ret = 0;
    private static final String TAG = "AudioIntercom.MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 找到 R.id.RecordBtnId
        recordBtn = (Button)findViewById(R.id.RecordBtnId);

        // 设置 按键的单击事件的回调函数.
        recordBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AudioRecorder recorder = new AudioRecorder();
                ret = recorder.initRecorder();
                Log.i(TAG ,"init recording audio --> " + ret);
                if(ret == AudioRecorder.STATE_UNINITIALIZED){
                    Log.i(TAG ,"init recording audio Failed.");
                    return;
                }else {
                    Log.i(TAG ,"will start recording audio");
                    recorder.startRecording();
                    recorder.stopRecording();
                }
            }
        });
    }

}