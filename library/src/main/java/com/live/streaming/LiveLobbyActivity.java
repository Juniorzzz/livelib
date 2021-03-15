//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.live.streaming;

import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplay.Callback;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.unity3d.player.UnityPlayerNativeActivityPico;

public class LiveLobbyActivity extends UnityPlayerNativeActivityPico {
    private MediaProjectionManager mediaProjectionManager;

    public LiveLobbyActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:onCreate");
        InitMediaProjection();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == 1002) {
            MediaProjection mMediaProjection = this.mediaProjectionManager.getMediaProjection(resultCode, data);
            VirtualDisplay display = mMediaProjection.createVirtualDisplay("Live-VirtualDisplay", 1920, 1080, 1440000, 1, (Surface) null, (Callback) null, (Handler) null);
            LiveClient.getInstance().SetVirtualDisplay(display);
            Intent intent = new Intent(this, RefreshActivity.class);
            startActivity(intent);
            LiveClient.getInstance().BindService(this, resultCode, data);
        }
    }

    public void InitMediaProjection() {
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:InitMediaProjection");
        mediaProjectionManager = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = this.mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, 1002);
    }
}
