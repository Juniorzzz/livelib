//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.ossrs.yasea;

import android.hardware.display.VirtualDisplay;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.projection.MediaProjection;
import android.util.Log;
import com.github.faucamp.simplertmp.RtmpHandler;
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
    private Object lock = new Object();
    private boolean isRun = false;

    public CustomScreenPublisher() {
    }

    public void setFrameData(byte[] data, int width, int height) {
        Log.i("LiveLib", "CustomScreenPublisher:SetFrameData");
        if (!this.sendAudioOnly) {
            this.mEncoder.onGetRgbaFrame(data, width, height);
        }

    }

    public void setAudioData(byte[] data, int len) {
        if (this.mEncoder != null) {
            this.mEncoder.onGetPcmFrame(data, len);
        }
    }

    public void initPublisher() {
        this.vworker = new Thread(new Runnable() {
            public void run() {
                while(true) {
                    if (!Thread.interrupted()) {
                        if (!CustomScreenPublisher.this.isRun) {
                            try {
                                Thread.sleep(20L);
                                continue;
                            } catch (InterruptedException var3) {
                            }
                        } else {
                            CustomScreenPublisher.this.mEncoder.onGetScreenFrame();

                            try {
                                Thread.sleep(20L);
                                continue;
                            } catch (InterruptedException var2) {
                            }
                        }
                    }

                    return;
                }
            }
        });
        this.isRun = false;
        this.vworker.start();
    }

    public void startScreen() {
        this.mEncoder.startScreen();
        synchronized(this.lock) {
            this.isRun = true;
        }
    }

    public void stopScreen() {
        Log.i("LiveLib", "stopScreen");
        synchronized(this.lock) {
            this.isRun = false;
        }

        this.mEncoder.stopScreen();
    }

    public void startAudio() {
        SrsEncoder.aChannelConfig = 12;
    }

    public void stopAudio() {
        if (this.aworker != null) {
            this.aworker.interrupt();

            try {
                this.aworker.join();
            } catch (InterruptedException var2) {
                this.aworker.interrupt();
            }

            this.aworker = null;
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
        if (this.mEncoder.start()) {
            this.startAudio();
        }
    }

    public void stopEncode() {
        this.stopAudio();
        this.mEncoder.stop();
    }

    public void pauseEncode() {
        this.stopAudio();
    }

    private void resumeEncode() {
        this.startAudio();
    }

    public void startPublish(String rtmpUrl) {
        if (this.mFlvMuxer != null) {
            this.mFlvMuxer.start(rtmpUrl);
            this.mFlvMuxer.setVideoResolution(this.mEncoder.getOutputWidth(), this.mEncoder.getOutputHeight());
            this.startEncode();
        }

    }

    public void resumePublish() {
        if (this.mFlvMuxer != null) {
            this.mEncoder.resume();
            this.resumeEncode();
        }

    }

    public void stopPublish() {
        if (this.mFlvMuxer != null) {
            this.stopEncode();
            this.mFlvMuxer.stop();
        }

    }

    public void pausePublish() {
        if (this.mFlvMuxer != null) {
            this.mEncoder.pause();
            this.pauseEncode();
        }

    }

    public boolean startRecord(String recPath) {
        return this.mMp4Muxer != null && this.mMp4Muxer.record(new File(recPath));
    }

    public void stopRecord() {
        if (this.mMp4Muxer != null) {
            this.mMp4Muxer.stop();
        }

    }

    public void pauseRecord() {
        if (this.mMp4Muxer != null) {
            this.mMp4Muxer.pause();
        }

    }

    public void resumeRecord() {
        if (this.mMp4Muxer != null) {
            this.mMp4Muxer.resume();
        }

    }

    public boolean isAllFramesUploaded() {
        return this.mFlvMuxer.getVideoFrameCacheNumber().get() == 0;
    }

    public int getVideoFrameCacheCount() {
        return this.mFlvMuxer != null ? this.mFlvMuxer.getVideoFrameCacheNumber().get() : 0;
    }

    public void switchToSoftEncoder() {
        this.mEncoder.switchToSoftEncoder();
    }

    public void switchToHardEncoder() {
        this.mEncoder.switchToHardEncoder();
    }

    public boolean isSoftEncoder() {
        return this.mEncoder.isSoftEncoder();
    }

    public void setPreviewResolution(int width, int height) {
        this.mEncoder.setPreviewResolution(width, height);
    }

    public void setOutputResolution(int width, int height) {
        if (width <= height) {
            this.mEncoder.setPortraitResolution(width, height);
        } else {
            this.mEncoder.setLandscapeResolution(width, height);
        }

    }

    public void setVideoHDMode() {
        this.mEncoder.setVideoHDMode();
    }

    public void setVideoSmoothMode() {
        this.mEncoder.setVideoSmoothMode();
    }

    public void setVirtualDisplay(VirtualDisplay display) {
        this.mEncoder.setVirtualDisplay(display);
    }

    public void setRtmpHandler(RtmpHandler handler) {
        this.mFlvMuxer = new SrsFlvMuxer(handler);
        if (this.mEncoder != null) {
            this.mEncoder.setFlvMuxer(this.mFlvMuxer);
        }

    }

    public void setRecordHandler(SrsRecordHandler handler) {
        this.mMp4Muxer = new SrsMp4Muxer(handler);
        if (this.mEncoder != null) {
            this.mEncoder.setMp4Muxer(this.mMp4Muxer);
        }

    }

    public void setEncodeHandler(SrsEncodeHandler handler) {
        this.mEncoder = new SrsEncoder(handler);
        if (this.mFlvMuxer != null) {
            this.mEncoder.setFlvMuxer(this.mFlvMuxer);
        }

        if (this.mMp4Muxer != null) {
            this.mEncoder.setMp4Muxer(this.mMp4Muxer);
        }

    }

    public void setupMediaProjection(MediaProjection projection, int dpi) {
        if (this.mEncoder != null) {
            this.mEncoder.setupMediaProjection(projection, dpi);
        }

    }
}
