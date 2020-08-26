package com.live.livelib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import static android.content.Context.BIND_AUTO_CREATE;

public class LiveClient {

    private ILiveInterface mService = null;
    static LiveClient _instance = null;

    public static LiveClient Instance()
    {
        if(_instance == null)
            _instance = new LiveClient();

        return _instance;
    }

    public void SetService(ILiveInterface service){

        Log.i(Util.LOG_TAG, "LiveClient:SetService");

        mService = service;
    }

    public boolean IsReady()
    {
        return mService != null;
    }

    public void InitLive(int width, int height, int fps, int bit, int sampleRate, int channel, int sampleBit){
        Log.i(Util.LOG_TAG, "LiveClient:InitLive");

        try {
            mService.InitLive(width, height, fps, bit, sampleRate, channel, sampleBit);
        } catch (RemoteException e) {
            Log.i(Util.LOG_TAG, "LiveClient:exception ");
            e.printStackTrace();
        }
    }

    public void StartLive(String url){
        Log.i(Util.LOG_TAG, "LiveClient:StartLive:" +url);

        try {
            mService.StartLive(url);
        } catch (RemoteException e) {
            Log.i(Util.LOG_TAG, "LiveClient:StartLive ");
            e.printStackTrace();
        }
    }

    public void StopLive(){
        Log.i(Util.LOG_TAG, "LiveClient:StopLive");

        try {
            mService.StopLive();
        } catch (RemoteException e) {
            Log.i(Util.LOG_TAG, "LiveClient:exception ");
            e.printStackTrace();
        }
    }
}
