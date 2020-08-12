package com.live.livelib;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.unity3d.player.UnityPlayerNativeActivityPico;

import androidx.annotation.Nullable;

public class LiveActivity extends UnityPlayerNativeActivityPico{

    private ILiveInterface mService = null;
    MediaProjectionManager mediaProjectionManager;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            int count = Scheduler.Instance().testCount;
            Log.i(Util.LOG_TAG, "Client:ServiceConnection:onServiceConnected : "+count);
            mService = ILiveInterface.Stub.asInterface(service);

            try {
                mService.InitLive();
            } catch (RemoteException e) {
                Log.i(Util.LOG_TAG, "Client:CustomNetworkService:exception ");
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");

//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName(this, "com.live.livelib.LiveService"));

        //binding to remote service
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        Scheduler.Instance().currentActivity = this;
        InitMediaProjection();
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

        Scheduler.Instance().mediaHelper.InitMedia(mediaProjectionManager, resultCode, data);
    }
}
