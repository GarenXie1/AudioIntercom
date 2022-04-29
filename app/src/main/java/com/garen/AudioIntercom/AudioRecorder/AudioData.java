package com.garen.AudioIntercom.AudioRecorder;

import java.util.Arrays;

public class AudioData {

    private byte[] mData;
    private int mLen;


    public AudioData(byte[] data, int len){
        mLen = len;
        mData = new byte[mLen];

        //需要把 data 数组 重新拷贝一份到 mData 中.
        System.arraycopy(data,0,mData,0,mLen);
    }

    public byte[] getData() {
        return mData;
    }

    public int getLen() {
        return mLen;
    }

    @Override
    public String toString() {
        return "AudioData{" +
                "mData=" + Arrays.toString(mData) +
                ", mLen=" + mLen +
                '}';
    }
}
