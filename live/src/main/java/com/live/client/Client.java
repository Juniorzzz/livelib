package com.live.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.live.livelib.ILiveInterface;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;

public class Client {
    private ILiveInterface mService = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(Util.LOG_TAG, "Client:ServiceConnection:onServiceConnected");
            mService = ILiveInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void InitClient()
    {
        Log.d(Util.LOG_TAG,"Client:InitClient");

        if( mService != null ) return;

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, "com.vlp.aidllib.NetworkService"));

        //binding to remote service
        UnityPlayer.currentActivity.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
}
