package com.live.streaming;

import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.CustomScreenPublisher;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsRecordHandler;

public class LiveService extends Service {

    private CustomScreenPublisher mPublisher;
    Intent mResultData;
    int mResultCode;

    // 录屏
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    private Binder mBinder = new ILiveInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        public void setupMediaProjectionManager(){
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }

        public void setupMediaProjection(){
            mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        }

        public void CreateVirtualDisplay(){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:CreateVirtualDisplay");
            mPublisher.createVirtualDisplay();
        }

        public void InitLive(int width, int height, int fps) {
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:InitLive");

            mPublisher.setPreviewResolution(width, height);
            mPublisher.setOutputResolution(height, width); // 这里要和preview反过来

            setupMediaProjectionManager();
            setupMediaProjection();

            mPublisher.setupMediaProjection(mMediaProjection, Util.getDensityDpi(getApplicationContext()));

            mPublisher.initPublisher();
        }

        public void StartLive(String url) {
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StartLive:" + url);
            if(mPublisher != null){
                mPublisher.startPublish(url);
                mPublisher.startScreen();
            }
        }

        public void StopLive() {
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StopLive");
            if(mPublisher != null){
                mPublisher.stopPublish();
                mPublisher.stopScreen();
            }
        }

        public void WriteVideoStream(byte[] data, int width, int height){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:WriteVideoStream");
        }

        public void WriteAudioStream(byte[] data, int len){
            Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:WriteAudioStream");
            if(mPublisher != null)
                mPublisher.setAudioData(data, len);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onBind");

        Bundle bundle = intent.getExtras();
        if(intent.hasExtra("resultData") && intent.hasExtra("resultCode") && bundle != null)
        {
            mResultData = bundle.getParcelable("resultData");
            mResultCode = bundle.getInt("resultCode");
        }

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onUnbind");

        return super.onUnbind(intent);
    }

    //    @Override
    public void onCreate() {
        Log.i(Util.LOG_TAG, "LiveService:onCreate");
        super.onCreate();

        mPublisher = new CustomScreenPublisher();
        mPublisher.setEncodeHandler(new SrsEncodeHandler(null));
        mPublisher.setRtmpHandler(new RtmpHandler(null));
        mPublisher.setRecordHandler(new SrsRecordHandler(null));
        mPublisher.setVideoHDMode();
//        mPublisher.setVideoSmoothMode();
//        mPublisher.switchToHardEncoder();
    }
}
