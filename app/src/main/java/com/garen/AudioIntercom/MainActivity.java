package com.garen.AudioIntercom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button recordBtn;
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
                Log.i(TAG ,"will start recording audio");
            }
        });
    }




}