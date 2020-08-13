package com.live.livelib;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RefreshActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "RefreshActivity:onCreate");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Util.LOG_TAG, "RefreshActivity:onResume");

        finish();
    }
}
