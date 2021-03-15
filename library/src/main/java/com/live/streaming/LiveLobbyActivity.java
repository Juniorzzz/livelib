package com.live.streaming;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.unity3d.player.UnityPlayerNativeActivityPico;

public class LiveLobbyActivity extends UnityPlayerNativeActivityPico {

    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:onCreate");
        InitMediaProjection();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(Util.LOG_TAG, "LiveLobbyActivity:onActivityResult requestCode:" + requestCode+" resultCode:"+resultCode);

        if(requestCode == Util.REQUEST_MEDIA_PROJECTION){

            MediaProjection mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            VirtualDisplay display = mMediaProjection.createVirtualDisplay("Live-VirtualDisplay",
                    1920, 1080, 1200*1200, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null,null, null);
            LiveClient.getInstance().SetVirtualDisplay(display);

            Intent intent = new Intent(this, RefreshActivity.class);
            startActivity(intent);

            LiveClient.getInstance().BindService(this);
        }
    }

    public void InitMediaProjection() {
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:InitMediaProjection");
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, Util.REQUEST_MEDIA_PROJECTION);
    }

}
