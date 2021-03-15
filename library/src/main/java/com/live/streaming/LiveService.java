package com.live.streaming;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
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

    public class CustomBinder extends Binder {
        public LiveService getService() {
            return LiveService.this;
        }
    }

    private CustomBinder binder = new CustomBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onBind");

        return binder;
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
    }

    void SetVirtual(VirtualDisplay virtualDisplay) {
        Log.i(Util.LOG_TAG, "LiveService:SetVirtual");
        mPublisher.setVirtualDisplay(virtualDisplay);
    }

    public void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "LiveService:InitLive");

        mPublisher.setPreviewResolution(width, height);
        mPublisher.setOutputResolution(height, width); // 这里要和preview反过来

        mPublisher.initPublisher();
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "LiveService:StartLive:" + url);
        if(mPublisher != null){
            mPublisher.startPublish(url);
            mPublisher.startScreen();
        }
    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "LiveService:StopLive");
        if(mPublisher != null){
            mPublisher.stopPublish();
            mPublisher.stopScreen();
        }
    }

    public void WriteVideoStream(byte[] data, int width, int height){
        Log.i(Util.LOG_TAG, "LiveService:WriteVideoStream");
    }

    public void WriteAudioStream(byte[] data, int len){
        Log.i(Util.LOG_TAG, "LiveService:WriteAudioStream");
        if(mPublisher != null)
            mPublisher.setAudioData(data, len);
    }
}
