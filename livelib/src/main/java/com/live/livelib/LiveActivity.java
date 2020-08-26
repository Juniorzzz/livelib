package com.live.livelib;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.unity3d.player.UnityPlayerNativeActivityPico;
;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsScreenPublisher;
import net.ossrs.yasea.SrsScreenView;

//public class LiveActivity extends AppCompatActivity{
public class LiveActivity extends UnityPlayerNativeActivityPico{

    public final static int RC_CAMERA = 100;
    MediaProjectionManager mediaProjectionManager;
    private boolean isPermissionGranted = false;
    String rtmpUrl = "rtmp://192.168.1.100:1935/live/rfBd56ti2SMtYvSgD5xAV0YU99zampta7Z7S575KLkIZ9PYk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Util.LOG_TAG, "LiveActivity:onCreate");

        InitMediaProjection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(Util.LOG_TAG, "LiveActivity:onResume");
    }

    private void requestPermission() {
        Log.i(Util.LOG_TAG, "MainActivity:requestPermission");

        //1. 检查是否已经有该权限
        if (Build.VERSION.SDK_INT >= 23 && ( ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            //2. 权限没有开启，请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_CAMERA);
        } else {
            //权限已经开启，做相应事情
            isPermissionGranted = true;
            InitMediaProjection();
        }
    }

    //3. 接收申请成功或者失败回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限被用户同意,做相应的事情
                isPermissionGranted = true;
                InitMediaProjection();
            } else {
                //权限被用户拒绝，做相应的事情
                finish();
            }
        }
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

        if (mediaProjectionManager == null) {
            Log.i(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjectionManager is null!!!");
            return;
        }

        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

        int densityDpi = Util.getDensityDpi(getApplicationContext());
        U2A.InitPublisher(mediaProjection, densityDpi);
    }
}
