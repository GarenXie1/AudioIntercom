package com.garen.AudioIntercom.AudioConfig;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioConfig {

    public static final int SAMPLE_RATE = 8000;
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    // Audio record config.
    public static final int AudioRecordSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public static final int AudioRecordChannelConfig = AudioFormat.CHANNEL_IN_MONO;



}
