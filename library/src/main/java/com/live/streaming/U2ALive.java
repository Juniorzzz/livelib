package com.live.streaming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class U2ALive {
    private static U2ALive instance = new U2ALive();

    public static U2ALive Instance(){
        return instance;
    }

    public boolean IsReady(){
        Log.i(Util.LOG_TAG, "U2ALive:IsReady");
        return LiveClient.getInstance().IsReady();
    }

    public void RefreshScreen(Activity activity)
    {
        Log.i(Util.LOG_TAG, "U2ALive:RefreshScreen");
        Intent intent = new Intent(activity, RefreshActivity.class);
        activity.startActivity(intent);
    }

    public void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "U2ALive:InitLive");
        LiveClient.getInstance().InitLive(width, height, fps);
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "U2ALive:StartLive:" + url);
        LiveClient.getInstance().StartLive(url);
    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "U2ALive:StopLive:");
        LiveClient.getInstance().StopLive();
    }

    public void WriteVideoFrame(byte[] data, int width, int height){
        Log.i(Util.LOG_TAG, "U2A:WriteVideoFrame");
        LiveClient.getInstance().WriteVideoStream(data, width, height);
    }

    public void WriteAudioStream(byte[] data, int len){
        Log.i(Util.LOG_TAG, "U2A:WriteAudioStream");
        LiveClient.getInstance().WriteAudioStream(data, len);
    }
}
