package com.live.livelib;

import android.media.projection.MediaProjection;
import android.util.Log;

import com.live.livelib.rtmp.OnConntionListener;
import com.live.livelib.rtmp.RtmpHelper;

public class Live implements OnConntionListener, LiveEncoder.OnMediaInfoListener {

    private RtmpHelper rtmpHelper;
    private LiveEncoder encoder = new LiveEncoder();

    public void InitLive(int width, int height, int fps, int bit, int sampleRate, int channel, int sampleBit) {

        Log.i(Util.LOG_TAG, "Live:InitLive" + String.format("width:%d height:%d fps:%d rate:%d channel:%d bit:%d", width, height, fps, sampleRate, channel, sampleBit));

        encoder.initEncoder(width, height, fps, bit, sampleRate, channel, sampleBit);
        encoder.setOnMediaInfoListener(this);
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "Live:StartLive:" + url);
        ;
        if (rtmpHelper != null) {
            rtmpHelper.stop();
            rtmpHelper = null;
        }

        rtmpHelper = new RtmpHelper();
        rtmpHelper.initLivePush(url);
        rtmpHelper.setOnConntionListener(this);
    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "Live:StopLive");
        if (encoder != null) {
            encoder.stop();
            encoder = null;
        }

        rtmpHelper.stop();
    }

    public void WriteAudioStream(byte[] data, int len) {
        Log.i(Util.LOG_TAG, "Live:onEncodeSPSPPSInfo");

        if (rtmpHelper == null || !rtmpHelper.isConnected)
            return;

        if (encoder != null) {
            encoder.WriteAudioStream(data, len);
        }
        else{
            Log.e(Util.LOG_TAG, "Live:setMediaProjection encoder == null");
        }
    }

    @Override
    public void onConntecting() {
        Log.e(Util.LOG_TAG, "connecting...");
    }

    @Override
    public void onConntectSuccess() {
        Log.e(Util.LOG_TAG, "Live:onConntectSuccess...");
        encoder.start();
    }

    @Override
    public void onConntectFail(String msg) {
        Log.e(Util.LOG_TAG, "Live:onConntectFail  " + msg);
        StopLive();
    }

    @Override
    public void onMediaTime(int times) {

    }

    @Override
    public void onEncodeSPSPPSInfo(byte[] sps, byte[] pps) {
        Log.i(Util.LOG_TAG, "Live:onEncodeSPSPPSInfo");
        rtmpHelper.pushSPSPPS(sps, pps);
    }

    @Override
    public void onEncodeVideoDataInfo(byte[] data, boolean keyFrame) {
        Log.i(Util.LOG_TAG, "Live:onEncodeVideoDataInfo");
        rtmpHelper.pushVideoData(data, keyFrame);
    }

    @Override
    public void onEncodeAudioInfo(byte[] data) {
        Log.i(Util.LOG_TAG, "Live:onEncodeAudioInfo");
        rtmpHelper.pushAudioData(data);
    }
}
