package com.garen.AudioIntercom.AudioRecorder;

import java.util.Arrays;

public class AudioRecordData {

    private byte[] mData;
    private int mLen;


    public AudioRecordData(byte[] data, int len){
        mData = data;
        mLen = len;
    }

    public byte[] getData() {
        return mData;
    }

    public int getLen() {
        return mLen;
    }

    @Override
    public String toString() {
        return "AudioRecordData{" +
                "mData=" + Arrays.toString(mData) +
                ", mLen=" + mLen +
                '}';
    }
}
