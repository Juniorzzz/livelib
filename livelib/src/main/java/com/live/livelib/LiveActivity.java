package com.live.livelib;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerNativeActivityPico;

import androidx.annotation.Nullable;

public class LiveActivity extends UnityPlayerNativeActivityPico{

    MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");

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
