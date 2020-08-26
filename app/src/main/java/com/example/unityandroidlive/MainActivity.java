package com.example.unityandroidlive;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;


import com.github.faucamp.simplertmp.RtmpHandler;
import com.live.livelib.Scheduler;
import com.live.livelib.U2A;
import com.live.livelib.Util;

import net.ossrs.yasea.CustomScreenPublisher;
import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;
import net.ossrs.yasea.SrsScreenPublisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int mResultCode;
    private Intent mResultData;

    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;

    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;
    private Surface videoInputSurface;
    public static int vBitrate = 1200 * 1024;  // 1200 kbps
    public static final int VFPS = 24;

    private final String LOG_TAG = "Live";
    public final static int RC_CAMERA = 100;

    private int mWidth = 1920;
    private int mHeight = 1080;

    //    private SrsScreenPublisher mPublisher;
    private CustomScreenPublisher mPublisher;
    private SrsCameraView mCameraView;

    private boolean isPermissionGranted = false;

    String rtmpUrl = "rtmp://192.168.1.100:1935/live/rfBd56ti2SMtYvSgD5xAV0YU99zampta7Z7S575KLkIZ9PYk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainActivity:onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        requestPermission();
    }

    public void InitMediaProjection() {
        Log.i(Util.LOG_TAG, "MainActivity:InitMediaProjection");

        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(Util.LOG_TAG, "MainActivity:onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);

        if (mMediaProjectionManager == null) {
            Log.i(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjectionManager is null!!!");
            return;
        }

        mResultCode = resultCode;
        mResultData = data;

        init(mWidth, mHeight, resultCode, data);
        CreateVirtualDisplay();

    }

    private void requestPermission() {
        Log.i(LOG_TAG, "MainActivity:requestPermission");

        //1. 检查是否已经有该权限
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            //2. 权限没有开启，请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_CAMERA);
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

    private void init(final int videoWidth, final int videoHeight, int resultCode, Intent data) {
        Log.i(LOG_TAG, "MainActivity:init");

        if (mMediaProjectionManager == null) {
            Log.i(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjectionManager is null!!!");
            return;
        }

        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);

        if (mMediaProjection == null) {
            Log.e(Util.LOG_TAG, "MediaHelper:CreateVirtualDisplay mediaProjection == null !!!");
            return;
        }

        int densityDpi = Util.getDensityDpi(getApplicationContext());
//
//        mCameraView = findViewById(R.id.glsurfaceview_camera);
////        mPublisher = new CustomScreenPublisher(mCameraView);
//        mPublisher = new CustomScreenPublisher();
//        mPublisher.setEncodeHandler(new SrsEncodeHandler(null));
//        mPublisher.setRtmpHandler(new RtmpHandler(null));
//        mPublisher.setRecordHandler(new SrsRecordHandler(null));
//        mPublisher.setPreviewResolution(mWidth, mHeight);
//        mPublisher.setOutputResolution(mHeight, mWidth); // 这里要和preview反过来
//        mPublisher.setVideoHDMode();
//        mPublisher.setMediaDPI(densityDpi);
//        mPublisher.setMediaProjection(mMediaProjection);

        U2A.InitPublisher(mMediaProjection, densityDpi);


    }

    private void CreateVirtualDisplay() {

//        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);

//        ImageReader reader = ImageReader.newInstance(mWidth,mHeight, ImageFormat.JPEG, 3);
        ImageReader reader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 3);
        reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i("Live", "CreateVirtualDisplay:onImageAvailable");
                Image img = reader.acquireLatestImage();
                Image.Plane[] planes = img.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();

                byte[] rgbadata = new byte[mWidth * mHeight * 4];
                buffer.get(rgbadata);
                byte[] abgrdata = new byte[rgbadata.length];

                int index = 0;
                while (index < rgbadata.length) {

                    if (index % 4 == 0) {
                        abgrdata[index] = rgbadata[index+3];
                    }else if (index % 4 == 1) {
                        abgrdata[index] = rgbadata[index+1];
                    }else if (index % 4 == 2) {
                        abgrdata[index] = rgbadata[index-1];
                    }else if (index % 4 == 3) {
                        abgrdata[index] = rgbadata[index-3];
                    }
                    index ++;
                }

//                mPublisher.setFrameData(abgrdata, mWidth, mHeight);
                U2A.WriteVideoFrame(abgrdata, mWidth, mHeight);
                img.close();
            }
        }, null);

//        int densityDpi = Util.getDensityDpi(getApplicationContext());
//        mMediaProjection.createVirtualDisplay("Live-VirtualDisplay", mWidth, mHeight, densityDpi,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, reader.getSurface(), null, null);

        U2A.Publish(rtmpUrl);

//        mPublisher.startPublish(rtmpUrl);
    }
}
