package com.example.livelib;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;


import androidx.annotation.Nullable;

public class LiveActivity extends Activity{
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private int mediaProjectionCode;
    private Intent mediaPrjectionBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");
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

        this.mediaProjectionCode = resultCode;
        this.mediaPrjectionBundle = data;
        if(mediaProjectionManager == null)
        {
            Log.i(Util.LOG_TAG, "LiveActivity:onActivityResult mediaProjectionManager is null!!!");
            finish();
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(this.mediaProjectionCode, this.mediaPrjectionBundle);
        if(Live.Instance() == null){
            Log.i(Util.LOG_TAG, "LiveActivity:onActivityResult Live.Instance is null!!!");
            finish();
            return;
        }
        Live.Instance().setMediaProjection(mediaProjection);

        finish();
    }
}
