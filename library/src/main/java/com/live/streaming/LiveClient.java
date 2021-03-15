//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.live.streaming;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;

import com.live.streaming.LiveService.CustomBinder;

public class LiveClient {
    private LiveService mService = null;
    Activity currentActivity;
    VirtualDisplay virtualDisplay = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.i(Util.LOG_TAG, "LiveClient:ServiceConnection:onServiceConnected");
            CustomBinder customBinder = (CustomBinder) binder;
            mService = customBinder.getService();
            mService.SetVirtual(LiveClient.this.virtualDisplay);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(Util.LOG_TAG, "LiveClient:ServiceConnection:onServiceDisconnected");
            mService = null;
        }
    };
    private static LiveClient instance;
    private MediaProjectionManager mediaProjectionManager;

    public static LiveClient getInstance() {
        if (instance == null) {
            Log.i(Util.LOG_TAG, "LiveClient:getInstance new LiveClient");
            instance = new LiveClient();
        }

        return instance;
    }

    public LiveClient() {
        instance = this;
    }

    public void BindService(Activity activity) {
        Log.i(Util.LOG_TAG, "LiveClient:BindService  1");
        currentActivity = activity;
        Intent liveIntent = new Intent(activity, LiveService.class);
        activity.bindService(liveIntent, this.serviceConnection, 1);
    }

    public void BindService(Activity activity, int resultCode, Intent data) {
        Log.i(Util.LOG_TAG, "LiveClient:BindService  2");
        currentActivity = activity;
        Intent liveIntent = new Intent(activity, LiveService.class);
        liveIntent.putExtra("resultCode", resultCode);
        liveIntent.putExtra("resultData", data);
        activity.bindService(liveIntent, this.serviceConnection, 1);
    }

    public void UnbindService(Activity activity) {
        activity.unbindService(this.serviceConnection);
    }

    public void CreateVirtualDisplay() {
        Log.i(Util.LOG_TAG, "LiveClient:CreateVirtualDisplay");
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:CreateVirtualDisplay mService == null");
        }
    }

    public void SetVirtualDisplay(VirtualDisplay display) {
        Log.i(Util.LOG_TAG, "LiveClient:SetVirtualDisplay");
        virtualDisplay = display;
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:CreateVirtualDisplay mService == null");
        } else {
            mService.SetVirtual(display);
        }
    }

    public boolean IsReady() {
        return mService != null;
    }

    public void InitLive(int width, int height, int fps) {
        Log.i(Util.LOG_TAG, "LiveClient:InitLive");
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:InitLive mService == null");
        } else {
            Log.i(Util.LOG_TAG, "LiveClient:InitLive 1");

            try {
                Log.i(Util.LOG_TAG, "LiveClient:InitLive 2");
                mService.InitLive(width, height, fps);
                Log.i(Util.LOG_TAG, "LiveClient:InitLive 3");
            } catch (Exception var5) {
                Log.e(Util.LOG_TAG, var5.toString());
            }

        }
    }

    public void StartLive(String url) {
        Log.i(Util.LOG_TAG, "LiveClient:StartLive:" + url);
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:StartLive mService == null");
        } else {
            try {
                mService.StartLive(url);
            } catch (Exception var3) {
                Log.e(Util.LOG_TAG, var3.toString());
            }

        }
    }

    public void StopLive() {
        Log.i(Util.LOG_TAG, "LiveClient:StopLive");
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:StopLive mService == null");
        } else {
            try {
                mService.StopLive();
            } catch (Exception var2) {
                Log.e(Util.LOG_TAG, var2.toString());
            }

        }
    }

    public void WriteVideoStream(byte[] data, int width, int height) {
        Log.i(Util.LOG_TAG, "LiveClient:WriteVideoStream");
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:WriteVideoStream mService == null");
        } else {
            try {
                mService.WriteVideoStream(data, width, height);
            } catch (Exception var5) {
                Log.e(Util.LOG_TAG, var5.toString());
            }

        }
    }

    public void WriteAudioStream(byte[] data, int len) {
        Log.i(Util.LOG_TAG, "LiveClient:WriteAudioStream");
        if (mService == null) {
            Log.i(Util.LOG_TAG, "LiveClient:WriteAudioStream mService == null");
        } else {
            try {
                mService.WriteAudioStream(data, len);
            } catch (Exception var4) {
                Log.e(Util.LOG_TAG, var4.toString());
            }
        }
    }
}
