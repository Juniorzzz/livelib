package com.live.livelib;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.unity3d.player.UnityPlayer;

public class MediaHelper {


    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;

    private int mediaProjectionCode;
    private Intent mediaPrjectionBundle;

    public void InitMedia(MediaProjectionManager manager, int resultCode, @Nullable Intent data){
        Log.i(Util.LOG_TAG, "MediaHelper:InitMedia");
        mediaProjectionManager = manager;
        mediaProjectionCode = resultCode;
        mediaPrjectionBundle = data;
    }

    public void CreateVirtualDisplay(int videoWidth, int videoHeight, Surface surface){

        if(mediaProjectionManager == null)
        {
            Log.i(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjectionManager is null!!!");
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(mediaProjectionCode, mediaPrjectionBundle);

        if(mediaProjection == null)
        {
            Log.e(Util.LOG_TAG,"MediaHelper:CreateVirtualDisplay mediaProjection == null !!!");
            return;
        }

        int densityDpi = Util.getDensityDpi(UnityPlayer.currentActivity.getApplicationContext());
        mediaProjection.createVirtualDisplay("Live-VirtualDisplay", videoWidth, videoHeight, densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);
    }
}
