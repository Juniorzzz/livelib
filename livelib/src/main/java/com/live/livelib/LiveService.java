package com.live.livelib;

import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.live.livelib.ILiveInterface;

public class LiveService extends Service {

    private Binder mBinder = new ILiveInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        public void InitLive(){

        }
    };

    @Override
    public void onCreate(){
        Log.i(Util.LOG_TAG, "LiveService:onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {

        int count = Scheduler.Instance().testCount;
        Log.i(Util.LOG_TAG, "LiveService:onBind : " + count);

        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.i(Util.LOG_TAG, "LiveService:onDestroy -- ");
        super.onDestroy();

    }
}
