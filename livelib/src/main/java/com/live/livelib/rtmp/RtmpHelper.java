package com.live.livelib.rtmp;

import android.text.TextUtils;
import android.util.Log;

import com.live.livelib.Util;

public class RtmpHelper {

    private OnConntionListener mOnConntionListener;

    public RtmpHelper() {
        System.loadLibrary("librtmp-lib");
    }

    public boolean isConnected = false;

    public void initLivePush(String url) {
        if (TextUtils.isEmpty(url)) return;
        Log.i(Util.LOG_TAG, "RtmpHelper:initLivePush:"+url);
        isConnected = false;
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
    public void pushAudioData(byte[] data, int len) {
        if (data == null) return;
        n_pushAudioData(data, len);
    }

    public void stop(){
        n_stop();
    }

    private void onConntecting() {
        Log.i(Util.LOG_TAG, "RtmpHelper:onConntecting:");

        isConnected = false;
        if (mOnConntionListener != null) {
            mOnConntionListener.onConntecting();
        }
    }

    private void onConntectSuccess() {
        Log.i(Util.LOG_TAG, "RtmpHelper:onConntectSuccess:");

        isConnected = true;
        if (mOnConntionListener != null) {
            mOnConntionListener.onConntectSuccess();
        }
    }

    private void onConntectFail(String msg) {
        Log.i(Util.LOG_TAG, "RtmpHelper:onConntectFail:"+msg );

        isConnected = false;
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
