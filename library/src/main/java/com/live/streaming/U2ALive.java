package com.live.streaming;

import android.util.Log;

public class U2ALive {

    public static boolean IsReady(){
        Log.i(Util.LOG_TAG, "U2ALive:IsReady");
        return LiveClient.getInstance().IsReady();
    }

    public static void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "U2ALive:InitLive");
        LiveClient.getInstance().InitLive(width, height, fps);
    }

    public static void StartLive(String url) {
        Log.i(Util.LOG_TAG, "U2ALive:StartLive:" + url);
        LiveClient.getInstance().StartLive(url);
    }

    public static void StopLive() {
        Log.i(Util.LOG_TAG, "U2ALive:StopLive:");
        LiveClient.getInstance().StopLive();
    }

    public static void WriteVideoFrame(byte[] data, int width, int height){
        Log.i(Util.LOG_TAG, "U2A:WriteVideoFrame");
        LiveClient.getInstance().WriteVideoStream(data, width, height);
    }

    public static void WriteAudioStream(byte[] data, int len){
        Log.i(Util.LOG_TAG, "U2A:WriteAudioStream");
        LiveClient.getInstance().WriteAudioStream(data, len);
    }
}
