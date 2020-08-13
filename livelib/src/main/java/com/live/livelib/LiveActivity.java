package com.live.livelib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.unity3d.player.UnityPlayerNativeActivityPico;

import androidx.annotation.Nullable;

public class LiveActivity extends UnityPlayerNativeActivityPico{

    MediaProjectionManager mediaProjectionManager;
    LiveService liveService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(Util.LOG_TAG, "Client:ServiceConnection:onServiceConnected");

            LiveService.CustomBinder binder = (LiveService.CustomBinder)service;
            liveService = binder.getService();


//            LiveClient.Instance().SetService(ILiveInterface.Stub.asInterface(service));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(Util.LOG_TAG, "Client:ServiceConnection:onServiceDisconnected");
            LiveClient.Instance().SetService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, "com.live.livelib.LiveService"));
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        InitMediaProjection();

//        Scheduler.Instance().currentActivity = this;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Util.LOG_TAG, "LiveActivity:onResume");
    }

    public void InitMediaProjection() {
        Log.i(Util.LOG_TAG, "LiveActivity:InitMediaProjection");

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(Util.LOG_TAG, "LiveActivity:onActivityResult requestCode:" + requestCode+" resultCode:"+resultCode);

        if (mediaProjectionManager == null) {
            Log.i(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjectionManager is null!!!");
            return;
        }

        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        liveService.setMediaProject(mediaProjection);

//        Scheduler.Instance().mediaHelper.InitMedia(mediaProjection);
    }
}
