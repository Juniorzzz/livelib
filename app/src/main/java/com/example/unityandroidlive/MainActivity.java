package com.example.unityandroidlive;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.live.streaming.LiveClient;
import com.live.streaming.U2ALive;
import com.live.streaming.Util;

import net.ossrs.yasea.CustomScreenPublisher;
import net.ossrs.yasea.SrsCameraView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private Button btnPublish;
    private Button btnSwitchCamera;
    private Button btnRecord;
    private Button btnSwitchEncoder;
    private Button btnPause;

    private String recPath = Environment.getExternalStorageDirectory().getPath() + "/test.mp4";
    private SharedPreferences sp;


    private MediaProjectionManager mediaProjectionManager;

    private final String LOG_TAG = "LiveService";
    public final static int RC_CAMERA = 100;

    private boolean isPermissionGranted = false;

    public static Activity activity;

    String rtmpUrl = "rtmp://192.168.1.100:1935/live/rfBd56ti2SMtYvSgD5xAV0YU99zampta7Z7S575KLkIZ9PYk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainActivity:onCreate");
        super.onCreate(savedInstanceState);

        activity = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

//        InitMediaProjection();

        requestPermission();
        init();
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(Util.LOG_TAG, "LiveLobbyActivity:onActivityResult requestCode:" + requestCode+" resultCode:"+resultCode);

        if(requestCode == Util.REQUEST_MEDIA_PROJECTION){
            LiveClient.getInstance().BindService(this, resultCode, data);
        }
    }

    public void InitMediaProjection() {
        Log.i(Util.LOG_TAG, "LiveLobbyActivity:InitMediaProjection");
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, Util.REQUEST_MEDIA_PROJECTION);
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

    private static int val = 0;
    private void init() {
        Log.i(LOG_TAG, "MainActivity:init");

        sp = getSharedPreferences("Yasea", MODE_PRIVATE);
        rtmpUrl = sp.getString("rtmpUrl", rtmpUrl);

//        U2ALive.CreateVirtualDisplay();

        int densityDpi = Util.getDensityDpi(getApplicationContext());

        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(rtmpUrl);

//        U2A.InitPublisher(mMediaProjection, densityDpi);

        btnPublish = (Button) findViewById(R.id.publish);
        btnSwitchCamera = (Button) findViewById(R.id.swCam);
        btnRecord = (Button) findViewById(R.id.record);
        btnSwitchEncoder = (Button) findViewById(R.id.swEnc);
        btnPause = (Button) findViewById(R.id.pause);
        btnPause.setEnabled(false);
//        mCameraView = (SrsCameraView) findViewById(R.id.glsurfaceview_camera);

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPublish.getText().toString().contentEquals("publish")) {

                    if (val == 0){
                        val = 1;
                        U2ALive.CreateVirtualDisplay();
                        U2ALive.InitLive(1920,1080,60);
                    }

                    U2ALive.StartLive(rtmpUrl);
//                    mPublisher.startPublish(rtmpUrl);
//                    mPublisher.startCamera();

                    if (btnSwitchEncoder.getText().toString().contentEquals("soft encoder")) {
                        Toast.makeText(getApplicationContext(), "Use hard encoder", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Use soft encoder", Toast.LENGTH_SHORT).show();
                    }
                    btnPublish.setText("stop");
                    btnSwitchEncoder.setEnabled(false);
                    btnPause.setEnabled(true);
                } else if (btnPublish.getText().toString().contentEquals("stop")) {
                    U2ALive.StopLive();
//                    mPublisher.stopPublish();
//                    mPublisher.stopRecord();
                    btnPublish.setText("publish");
                    btnRecord.setText("record");
                    btnSwitchEncoder.setEnabled(true);
                    btnPause.setEnabled(false);
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnPause.getText().toString().equals("Pause")){
//                    mPublisher.pausePublish();
                    btnPause.setText("resume");
                }else{
//                    mPublisher.resumePublish();
                    btnPause.setText("Pause");
                }
            }
        });

        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPublisher.switchCameraFace((mPublisher.getCameraId() + 1) % Camera.getNumberOfCameras());
            }
        });
    }

    private void CreateVirtualDisplay() {

//        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);

//        ImageReader reader = ImageReader.newInstance(mWidth,mHeight, ImageFormat.JPEG, 3);
//        ImageReader reader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 3);
//        reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                Log.i("Live", "CreateVirtualDisplay:onImageAvailable");
//                Image img = reader.acquireLatestImage();
//                Image.Plane[] planes = img.getPlanes();
//                ByteBuffer buffer = planes[0].getBuffer();
//
//                byte[] rgbadata = new byte[mWidth * mHeight * 4];
//                buffer.get(rgbadata);
//                byte[] abgrdata = new byte[rgbadata.length];
//
//                int index = 0;
//                while (index < rgbadata.length) {
//
//                    if (index % 4 == 0) {
//                        abgrdata[index] = rgbadata[index+3];
//                    }else if (index % 4 == 1) {
//                        abgrdata[index] = rgbadata[index+1];
//                    }else if (index % 4 == 2) {
//                        abgrdata[index] = rgbadata[index-1];
//                    }else if (index % 4 == 3) {
//                        abgrdata[index] = rgbadata[index-3];
//                    }
//                    index ++;
//                }
//
//                U2A.WriteVideoFrame(abgrdata, mWidth, mHeight);
//                img.close();
//            }
//        }, null);
//
//        U2A.Publish(rtmpUrl);
    }
}
