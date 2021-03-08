package com.live.streaming;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;

public class LiveClient {
    private ILiveInterface mService = null;
    Activity currentActivity;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(Util.LOG_TAG, "LiveClient:ServiceConnection:onServiceConnected");
            mService = ILiveInterface.Stub.asInterface(service);
//            CreateVirtualDisplay();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(Util.LOG_TAG, "LiveClient:ServiceConnection:onServiceDisconnected");
            mService = null;
        }
    };

    private static LiveClient instance;
    public static LiveClient getInstance(){
        if(instance == null){
            Log.i(Util.LOG_TAG, "LiveClient:getInstance new LiveClient");
            instance = new LiveClient();
        }
        return instance;
    }

    public LiveClient(){
        instance = this;
    }

    public void BindService(Activity activity){
        Log.i(Util.LOG_TAG, "LiveClient:BindService  1");
        currentActivity = activity;

        Intent liveIntent = new Intent(activity, LiveService.class);
        activity.bindService(liveIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private MediaProjectionManager mediaProjectionManager;
    public void BindService(Activity activity, int resultCode, Intent data ){

        Log.i(Util.LOG_TAG, "LiveClient:BindService  2");

        currentActivity = activity;

        Intent liveIntent = new Intent(activity, LiveService.class);
        liveIntent.putExtra("resultCode", resultCode);
        liveIntent.putExtra("resultData", data);
        activity.bindService(liveIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void UnbindService(Activity activity)
    {
        activity.unbindService(serviceConnection);
    }

    public void CreateVirtualDisplay(){
        Log.i(Util.LOG_TAG, "LiveClient:CreateVirtualDisplay");
        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:CreateVirtualDisplay mService == null");
            return;
        }

//        try {
//            mService.CreateVirtualDisplay();
//
//            Intent intent = new Intent(currentActivity, RefreshActivity.class);
//            currentActivity.startActivity(intent);
//
//        }catch (Exception e){
//            Log.e(Util.LOG_TAG, e.toString());
//        }
    }

    public boolean IsReady(){
        return mService != null;
    }

    public void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "LiveClient:InitLive");

        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:InitLive mService == null");
            return;
        }
        Log.i(Util.LOG_TAG, "LiveClient:InitLive 1");
        try {
            Log.i(Util.LOG_TAG, "LiveClient:InitLive 2");
            mService.InitLive(width, height, fps);
            Log.i(Util.LOG_TAG, "LiveClient:InitLive 3");
        }catch (Exception e){
            Log.e(Util.LOG_TAG, e.toString());
        }
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "LiveClient:StartLive:" + url);
        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:StartLive mService == null");
            return;
        }

        try {
            mService.StartLive(url);

            Intent intent = new Intent(currentActivity, RefreshActivity.class);
            currentActivity.startActivity(intent);

        }catch (Exception e){
            Log.e(Util.LOG_TAG, e.toString());
        }
    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "LiveClient:StopLive");
        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:StopLive mService == null");
            return;
        }

        try {
            mService.StopLive();
        }catch (Exception e){
            Log.e(Util.LOG_TAG, e.toString());
        }
    }

    public void WriteVideoStream(byte[] data, int width, int height) {
        Log.i(Util.LOG_TAG, "LiveClient:WriteVideoStream");

        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:WriteVideoStream mService == null");
            return;
        }

        try {
            mService.WriteVideoStream(data, width, height);
        }catch (Exception e){
            Log.e(Util.LOG_TAG, e.toString());
        }
    }

    public void WriteAudioStream(byte[] data, int len) {
        Log.i(Util.LOG_TAG, "LiveClient:WriteAudioStream");

        if(mService == null)
        {
            Log.i(Util.LOG_TAG, "LiveClient:WriteAudioStream mService == null");
            return;
        }

        try {
            mService.WriteAudioStream(data, len);
        }catch (Exception e){
            Log.e(Util.LOG_TAG, e.toString());
        }
    }
}
