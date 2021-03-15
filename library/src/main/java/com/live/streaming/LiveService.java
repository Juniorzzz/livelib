//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.live.streaming;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.github.faucamp.simplertmp.RtmpHandler;
import com.github.faucamp.simplertmp.RtmpHandler.RtmpListener;
import net.ossrs.yasea.CustomScreenPublisher;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsRecordHandler;
import net.ossrs.yasea.SrsEncodeHandler.SrsEncodeListener;
import net.ossrs.yasea.SrsRecordHandler.SrsRecordListener;

public class LiveService extends Service {
    private CustomScreenPublisher mPublisher;
    Intent mResultData;
    int mResultCode;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private LiveService.CustomBinder binder = new LiveService.CustomBinder();

    public LiveService() {
    }

    public IBinder onBind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onBind");
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        Log.i(Util.LOG_TAG, "LiveService:onUnbind");
        return super.onUnbind(intent);
    }

    public void onCreate() {
        Log.i(Util.LOG_TAG, "LiveService:onCreate");
        super.onCreate();
        mPublisher = new CustomScreenPublisher();
        mPublisher.setEncodeHandler(new SrsEncodeHandler((SrsEncodeListener)null));
        mPublisher.setRtmpHandler(new RtmpHandler((RtmpListener)null));
        mPublisher.setRecordHandler(new SrsRecordHandler((SrsRecordListener)null));
        mPublisher.setVideoHDMode();
    }

    public void setupMediaProjectionManager() {
        this.mMediaProjectionManager = (MediaProjectionManager)this.getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    public void setupMediaProjection() {
        this.mMediaProjection = this.mMediaProjectionManager.getMediaProjection(this.mResultCode, this.mResultData);
    }

    void SetVirtual(VirtualDisplay virtualDisplay) {
        Log.i(Util.LOG_TAG, "LiveService:SetVirtual");
        this.mPublisher.setVirtualDisplay(virtualDisplay);
    }

    public void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "LiveService:InitLive");
        mPublisher.setPreviewResolution(width, height);
        mPublisher.setOutputResolution(height, width);
        setupMediaProjectionManager();
        setupMediaProjection();
        mPublisher.setupMediaProjection(mMediaProjection, Util.getDensityDpi(getApplicationContext()));
        mPublisher.initPublisher();
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StartLive:" + url);
        if (mPublisher != null) {
            mPublisher.startPublish(url);
            mPublisher.startScreen();
        }

    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:StopLive");
        if (mPublisher != null) {
            mPublisher.stopPublish();
            mPublisher.stopScreen();
        }

    }

    public void WriteVideoStream(byte[] data, int width, int height) {
        Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:WriteVideoStream");
    }

    public void WriteAudioStream(byte[] data, int len) {
        Log.i(Util.LOG_TAG, "LiveService:ILiveInterface:WriteAudioStream");
        if (this.mPublisher != null) {
            this.mPublisher.setAudioData(data, len);
        }

    }

    public class CustomBinder extends Binder {
        public CustomBinder() {
        }

        public LiveService getService() {
            return LiveService.this;
        }
    }
}
