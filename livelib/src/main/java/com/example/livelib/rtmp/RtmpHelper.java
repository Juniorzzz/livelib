package com.example.livelib.rtmp;

import android.text.TextUtils;
import android.util.Log;

public class RtmpHelper {

    private OnConntionListener mOnConntionListener;

    public RtmpHelper() {
        System.loadLibrary("librtmp-lib");
    }

    public void initLivePush(String url) {
        if (TextUtils.isEmpty(url)) return;
        Log.i("pest", "RtmpHelper:initLivePush:"+url);
        n_init(url);
    }

    public void pushSPSPPS(byte[] sps, byte[] pps) {
        if (sps == null || pps == null) return;
        n_pushSPSPPS(sps, sps.length, pps, pps.length);
    }

    public void pushVideoData(byte[] data, boolean keyFrame) {
        if (data == null) return;
        n_pushVideoData(data, data.length, keyFrame);
    }
    public void pushAudioData(byte[] data) {
        if (data == null) return;
        n_pushAudioData(data, data.length);
    }

    public void stop(){
        n_stop();
    }


    private void onConntecting() {
        Log.i("pest", "RtmpHelper:onConntecting:");

        if (mOnConntionListener != null) {
            mOnConntionListener.onConntecting();
        }
    }

    private void onConntectSuccess() {
        Log.i("pest", "RtmpHelper:onConntectSuccess:");

        if (mOnConntionListener != null) {
            mOnConntionListener.onConntectSuccess();
        }
    }

    private void onConntectFail(String msg) {
        Log.i("pest", "RtmpHelper:onConntectFail:"+msg );

        if (mOnConntionListener != null) {
            mOnConntionListener.onConntectFail(msg);
        }
    }


    public void setOnConntionListener(OnConntionListener onConntionListener) {
        this.mOnConntionListener = onConntionListener;
    }


    private native void n_init(String url);

    private native void n_pushSPSPPS(byte[] sps, int spsLen, byte[] pps, int ppsLen);

    private native void n_pushVideoData(byte[] data, int dataLen, boolean keyFrame);
    private native void n_pushAudioData(byte[] data, int dataLen);
    private native void n_stop();


}
