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
        Log.i("pest", "LiveActivity:onCreate");
        InitMediaProjection();
    }

    public void InitMediaProjection() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("pest", "LiveActivity:onActivityResult");

        this.mediaProjectionCode = resultCode;
        this.mediaPrjectionBundle = data;
        mediaProjection = mediaProjectionManager.getMediaProjection(this.mediaProjectionCode, this.mediaPrjectionBundle);
        Live.Instance().setMediaProjection(mediaProjection);

        finish();
    }
}
