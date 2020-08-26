package net.ossrs.yasea;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;

import com.seu.magicfilter.base.gpuimage.GPUImageFilter;
import com.seu.magicfilter.utils.MagicFilterType;
import com.seu.magicfilter.utils.OpenGLUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class SrsScreenView {

    private SrsScreenView.PreviewCallback mPrevCb;
    private SurfaceTexture surfaceTexture;


    public SurfaceTexture getTexture(){
        return surfaceTexture;
    }

    public void onSurfaceCreated(){

        surfaceTexture = new SurfaceTexture(2);

        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                surfaceTexture.updateTexImage();
            }
        });
    }

    public void setPreviewCallback(SrsScreenView.PreviewCallback cb) {
        mPrevCb = cb;
    }

    public void startRecordScreen(){

    }

    public void stopRecordScreen(){

    }

    public interface PreviewCallback {

        void onGetRgbaFrame(byte[] data, int width, int height);
    }
}
