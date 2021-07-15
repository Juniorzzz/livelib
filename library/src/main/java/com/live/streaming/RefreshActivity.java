package com.live.streaming;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(Util.LOG_TAG, "RefreshActivity:onDestroy");
    }
}
