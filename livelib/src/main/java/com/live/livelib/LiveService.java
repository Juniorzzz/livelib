package com.live.livelib;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.live.livelib.ILiveInterface;
import com.unity3d.player.UnityPlayer;

public class LiveService extends Service {

    static LiveService service;
    private Binder mBinder = new CustomBinder();
    private int mediaProjectionCode;
    private Intent mediaPrjectionBundle;

//    @Override
//    public void onCreate(){
//        Log.i(Util.LOG_TAG, "LiveService:onCreate");
//        super.onCreate();
//
////        Intent intent = new Intent( UnityPlayer.currentActivity.getApplicationContext(), RefreshActivity.class);
////        UnityPlayer.currentActivity.startActivity(intent);
//    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onBind : ");

        createMediaProjection();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.i(Util.LOG_TAG, "LiveService:onDestroy -- ");
        super.onDestroy();

    }

    public void setMediaProject(MediaProjection mediaProjection) {
        Log.i(Util.LOG_TAG, "LiveService:setMediaProject");


    }

    public MediaProjection createMediaProjection(){
        return ((MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(mediaProjectionCode, mediaPrjectionBundle);
    }


    public class CustomBinder extends Binder implements ILiveInterface {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        public void InitLive(){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:InitLive");
        }

        public void InitLive(int width, int height, int fps, int bit, int sampleRate, int channel, int sampleBit){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:InitLive");
            Scheduler.Instance().live.InitLive(width, height, fps, bit, sampleRate, channel, sampleBit);

        }

        public void StartLive(String url){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StartLive:" +url);
            Scheduler.Instance().live.StartLive(url);
        }

        public void StopLive(){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StopLive");
            Scheduler.Instance().live.StopLive();
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        public LiveService getService(){
            return LiveService.this;
        }
    }
}
