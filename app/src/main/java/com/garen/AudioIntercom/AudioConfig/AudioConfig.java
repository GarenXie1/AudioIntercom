package com.garen.AudioIntercom.AudioConfig;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioConfig {
    public static final boolean IS_SAVE_AUDIODATA = true;
    public static final String AUDIO_SAVE_PATH = "/storage/emulated/0/audioFile";

    // Auido Record debug file.
    public static final String AUDIO_RECORD_FILENAME = "audio_record.pcm";
    public static final String AUDIO_DISCARD_RECORD_FILENAME = "discard_audio_record.pcm";
    public static final String AUDIO_UDP_SEND_RECORD_FILENAME = "udp_send_audio_record.pcm";

    // Auido Player debug file.
    public static final String AUDIO_PLAYER_FILENAME = "audio_player.pcm";
    public static final String AUDIO_UDP_RECIEVE_PLAYER_FILENAME = "udp_recieve_audio_player.pcm";


    public static final int SAMPLE_RATE = 8000;
    public static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    // Audio record config.
    public static final int AudioRecordSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public static final int AudioRecordChannelConfig = AudioFormat.CHANNEL_IN_MONO;



}
