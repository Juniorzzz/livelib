package com.example.livelib;

import android.util.Log;
import com.example.livelib.rtmp.OnConntionListener;
import com.example.livelib.rtmp.RtmpHelper;
import com.example.livelib.yuv.YuvHelper;
import com.example.livelib.yuv.YuvType;

public class Live implements OnConntionListener,LiveEncoder.OnMediaInfoListener {

    private RtmpHelper rtmpHelper;
    private LiveEncoder encoder;

    public void InitLive(int width, int height, int fps, int sampleRate, int channel, int sampleBit) {

        Log.i("pest", "Live:InitLive" + String.format("width:%d height:%d fps:%d rate:%d channel:%d bit:%d", width, height, fps, sampleRate, channel, sampleBit));

        encoder = new LiveEncoder();
        encoder.initEncoder(width, height, fps, sampleRate, channel, sampleBit);
        encoder.setOnMediaInfoListener(this);
    }

    public void StartLive(String url) {
        Log.i("pest", "Live:StartLive:"+url);
        if (rtmpHelper != null) {
            rtmpHelper.stop();
            rtmpHelper = null;
        }

        rtmpHelper = new RtmpHelper();
        rtmpHelper.initLivePush(url);
        rtmpHelper.setOnConntionListener(this);
    }

    public void StopLive() {
        Log.i("pest", "Live:StopLive");
        if (encoder != null){
            encoder.stop();
            encoder = null;
        }
    }

    public void WriteVideoStreamRGB(byte[] data, int width, int height){
        Log.i("pest", "Live:WriteVideoStreamRGB");

        if(rtmpHelper == null || !rtmpHelper.isConnected)
            return;

        if(encoder != null)
        {
            byte[] yuvData=new byte[width*height*3/2];
            YuvHelper.RgbaToI420(YuvType.RGB24_TO_I420,data,yuvData,width, height);
            encoder.WriteVideoStreamYUV(yuvData);
        }
    }

    public void WriteVideoStreamYUV(byte[] data) {
        Log.i("pest", "Live:WriteVideoStream");

        if(rtmpHelper == null || !rtmpHelper.isConnected)
            return;

        if(encoder != null)
            encoder.WriteVideoStreamYUV(data);
    }

    public void WriteAudioStream(byte[] data) {
        Log.i("pest", "Live:onEncodeSPSPPSInfo");

        if(rtmpHelper == null || !rtmpHelper.isConnected)
            return;

        if(encoder != null)
            encoder.WriteAudioStream(data);
    }

    @Override
    public void onConntecting() {
        Log.e("pest", "connecting...");
    }

    @Override
    public void onConntectSuccess() {
        Log.e("pest", "onConntectSuccess...");
        encoder.start();
    }
    @Override
    public void onConntectFail(String msg) {
        Log.e("pest", "onConntectFail  " + msg);
    }

    @Override
    public void onMediaTime(int times){

    }

    @Override
    public void onEncodeSPSPPSInfo(byte[] sps, byte[] pps){
        Log.i("pest", "Live:onEncodeSPSPPSInfo");
        rtmpHelper.pushSPSPPS(sps, pps);
    }

    @Override
    public void onEncodeVideoDataInfo(byte[] data, boolean keyFrame) {
        Log.i("pest", "Live:onEncodeVideoDataInfo");
        rtmpHelper.pushVideoData(data,keyFrame);
    }

    @Override
    public void onEncodeAudioInfo(byte[] data){
        Log.i("pest", "Live:onEncodeAudioInfo");
        rtmpHelper.pushAudioData(data);
    }


}
