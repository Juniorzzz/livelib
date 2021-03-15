package com.live.streaming;

import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayerNativeActivityPico;

public class LiveActivity extends UnityPlayerNativeActivityPico {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");

        LiveClient.getInstance().BindService(this);
    }
}
