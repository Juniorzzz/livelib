package com.example.unityandroidlive.encodec;

import android.content.Context;
import android.util.Log;

public class VideoEncodeRecode extends BaseMediaEncoder {

    public VideoEncodeRecode(Context context, int textureId) {
        super(context);
        Log.e("pest","id = "+textureId);
        setRender(new VideoEncodeRender(context,textureId));
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

}
