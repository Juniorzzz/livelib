package com.live.livelib;

import android.media.projection.MediaProjection;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.CustomScreenPublisher;
import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsRecordHandler;
import net.ossrs.yasea.SrsScreenPublisher;
import net.ossrs.yasea.SrsScreenView;

public class U2A {

    public final static int RC_CAMERA = 100;

    private static int mWidth = 1920;
    private static int mHeight = 1080;

    private static CustomScreenPublisher mPublisher;

    public static void InitLive(int width, int height, int fps, int bit, int sampleRate, int channel, int sampleBit) {
        Log.i(Util.LOG_TAG, "U2A:InitLive");
        Scheduler.Instance().live.InitLive(width, height, fps, bit, sampleRate, channel, sampleBit);

//        LiveClient.Instance().InitLive(width, height, fps, bit, sampleRate, channel, sampleBit);
    }

    public static void StartLive(String url) {
        Log.i(Util.LOG_TAG, "U2A:StartLive:" +url);
        Scheduler.Instance().live.StartLive(url);
//        LiveClient.Instance().StartLive(url);
    }

    public static void StopLive() {
        Log.i(Util.LOG_TAG, "U2A:StopLive");
        Scheduler.Instance().live.StopLive();
//        LiveClient.Instance().StopLive();
    }

    public static void WriteAudioStream(byte[] data, int len){
        Log.i(Util.LOG_TAG, "U2A:WriteAudioStream");
        Scheduler.Instance().live.WriteAudioStream(data, len);
    }

    public static void InitPublisher(MediaProjection projection, int densityDpi){

        mPublisher = new CustomScreenPublisher();
        mPublisher.setEncodeHandler(new SrsEncodeHandler(null));
        mPublisher.setRtmpHandler(new RtmpHandler(null));
        mPublisher.setRecordHandler(new SrsRecordHandler(null));
        mPublisher.setPreviewResolution(mWidth, mHeight);
        mPublisher.setOutputResolution(mHeight, mWidth); // 这里要和preview反过来
        mPublisher.setVideoHDMode();
        mPublisher.setMediaDPI(densityDpi);
        mPublisher.setMediaProjection(projection);
    }

    public static void WriteVideoFrame(byte[] data, int width, int height){
        Log.i(Util.LOG_TAG, "U2A:WriteVideoFrame");
        mPublisher.setFrameData(data, mWidth, mHeight);
    }

    public static void Publish(String url){
        mPublisher.startPublish(url);
    }
}
