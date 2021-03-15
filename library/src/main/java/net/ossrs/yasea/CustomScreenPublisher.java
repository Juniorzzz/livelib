package net.ossrs.yasea;

import android.hardware.Camera;
import android.hardware.display.VirtualDisplay;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.projection.MediaProjection;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;
import com.live.streaming.Util;
import com.seu.magicfilter.utils.MagicFilterType;

import java.io.File;

public class CustomScreenPublisher {

    private static AcousticEchoCanceler aec;
    private static AutomaticGainControl agc;
    private Thread aworker;
    private Thread vworker;

    private boolean sendVideoOnly = false;
    private boolean sendAudioOnly = false;
    private int videoFrameCount;
    private long lastTimeMillis;

    private SrsFlvMuxer mFlvMuxer;
    private SrsMp4Muxer mMp4Muxer;
    private SrsEncoder mEncoder;

    private Object lock=new Object();
    private boolean isRun = false;

    public void setFrameData(byte[] data, int width, int height) {
        Log.i("LiveLib", "CustomScreenPublisher:SetFrameData");

        if (!sendAudioOnly) {
            mEncoder.onGetRgbaFrame(data, width, height);
        }
    }

    public void setAudioData(byte[] data, int len){
        if(mEncoder == null) return;
            mEncoder.onGetPcmFrame(data, len);
    }

    public void initPublisher(){
        vworker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if(!isRun){

                        try {
                            // This is trivial...
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            break;
                        }

                        continue;
                    }

                    mEncoder.onGetScreenFrame();

                    try {
                        // This is trivial...
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });

        isRun = false;
        vworker.start();
    }

    public void startScreen(){

        mEncoder.startScreen();

        synchronized (lock){
            isRun = true;
        }
    }

    public void stopScreen(){
        Log.i("LiveLib", "stopScreen");

        synchronized (lock){
            isRun = false;
        }

        mEncoder.stopScreen();
    }

    public void startAudio() {
        SrsEncoder.aChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
    }

    public void stopAudio() {
        if (aworker != null) {
            aworker.interrupt();
            try {
                aworker.join();
            } catch (InterruptedException e) {
                aworker.interrupt();
            }
            aworker = null;
        }

        if (aec != null) {
            aec.setEnabled(false);
            aec.release();
            aec = null;
        }

        if (agc != null) {
            agc.setEnabled(false);
            agc.release();
            agc = null;
        }
    }

    public void startEncode() {
        if (!mEncoder.start()) {
            return;
        }

        startAudio();
    }

    public void stopEncode() {
        stopAudio();
        mEncoder.stop();
    }
    public void pauseEncode(){
        stopAudio();
    }
    private void resumeEncode() {
        startAudio();
    }

    public void startPublish(String rtmpUrl) {
        if (mFlvMuxer != null) {
            mFlvMuxer.start(rtmpUrl);
            mFlvMuxer.setVideoResolution(mEncoder.getOutputWidth(), mEncoder.getOutputHeight());
            startEncode();
        }
    }
    public void resumePublish(){
        if(mFlvMuxer != null) {
            mEncoder.resume();
            resumeEncode();
        }
    }

    public void stopPublish() {
        if (mFlvMuxer != null) {
            stopEncode();
            mFlvMuxer.stop();
        }
    }

    public void pausePublish(){
        if (mFlvMuxer != null) {
            mEncoder.pause();
            pauseEncode();
        }
    }
    public boolean startRecord(String recPath) {
        return mMp4Muxer != null && mMp4Muxer.record(new File(recPath));
    }

    public void stopRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.stop();
        }
    }

    public void pauseRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.pause();
        }
    }

    public void resumeRecord() {
        if (mMp4Muxer != null) {
            mMp4Muxer.resume();
        }
    }

    public boolean isAllFramesUploaded(){
        return mFlvMuxer.getVideoFrameCacheNumber().get() == 0;
    }

    public int getVideoFrameCacheCount(){
        if(mFlvMuxer != null) {
            return mFlvMuxer.getVideoFrameCacheNumber().get();
        }
        return 0;
    }

    public void switchToSoftEncoder() {
        mEncoder.switchToSoftEncoder();
    }

    public void switchToHardEncoder() {
        mEncoder.switchToHardEncoder();
    }

    public boolean isSoftEncoder() {
        return mEncoder.isSoftEncoder();
    }

    public void setPreviewResolution(int width, int height) {
        mEncoder.setPreviewResolution(width, height);
    }

    public void setOutputResolution(int width, int height) {
        if (width <= height) {
            mEncoder.setPortraitResolution(width, height);
        } else {
            mEncoder.setLandscapeResolution(width, height);
        }
    }

    public void setVideoHDMode() {
        mEncoder.setVideoHDMode();
    }

    public void setVideoSmoothMode() {
        mEncoder.setVideoSmoothMode();
    }

    public void setVirtualDisplay(VirtualDisplay display){
        mEncoder.setVirtualDisplay(display);
    }

    public void setRtmpHandler(RtmpHandler handler) {
        mFlvMuxer = new SrsFlvMuxer(handler);
        if (mEncoder != null) {
            mEncoder.setFlvMuxer(mFlvMuxer);
        }
    }

    public void setRecordHandler(SrsRecordHandler handler) {
        mMp4Muxer = new SrsMp4Muxer(handler);
        if (mEncoder != null) {
            mEncoder.setMp4Muxer(mMp4Muxer);
        }
    }

    public void setEncodeHandler(SrsEncodeHandler handler) {
        mEncoder = new SrsEncoder(handler);
        if (mFlvMuxer != null) {
            mEncoder.setFlvMuxer(mFlvMuxer);
        }
        if (mMp4Muxer != null) {
            mEncoder.setMp4Muxer(mMp4Muxer);
        }
    }

    public void setDPI(int dpi){
        if (mEncoder != null) {
            mEncoder.setDPI(dpi);
        }
    }
}
