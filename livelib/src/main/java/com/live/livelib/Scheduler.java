package com.live.livelib;
import android.app.Activity;
import android.util.Log;

public class Scheduler {

    private static Scheduler _instance = null;
    public static Scheduler Instance(){
        if(_instance == null){
            Log.i(Util.LOG_TAG, "Scheduler is null, new Scheduler");
            _instance = new Scheduler();
        }

        return _instance;
    }

    public Live live = new Live();
    public MediaHelper mediaHelper = new MediaHelper();
    public Activity currentActivity;

    public int testCount = 0;

}
