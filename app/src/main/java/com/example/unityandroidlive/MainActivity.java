package com.example.unityandroidlive;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.unityandroidlive.camera.CameraEglSurfaceView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.livelib.Live;
import com.example.unityandroidlive.rtmp.encoder.PushEncode;

public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
//    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
        }, 5);

        findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LivePushActivity.class));
            }
        });

        PushEncode pushEncode = new PushEncode(this, 0);
        pushEncode.initEncoder(null, 2880, 1080,44100,2,16);

        Live live = new Live();
        live.InitLive(2880,1080,15,44100,2,1);
        live.StartLive("rtmp://192.168.1.100:1935/live/rfBd56ti2SMtYvSgD5xAV0YU99zampta7Z7S575KLkIZ9PYk");
    }


    public void camera(View view) {
        startActivity(new Intent(this, CameraActivity.class));
    }

    public void recode(View view) {
        startActivity(new Intent(this, RecodeActivity.class));
    }

}
