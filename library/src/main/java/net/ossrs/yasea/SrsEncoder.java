//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.ossrs.yasea;

import android.graphics.Rect;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplay.Callback;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import com.live.streaming.Util;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class SrsEncoder {
    private static final String TAG = "SrsEncoder";
    public static final String VCODEC = "video/avc";
    public static final String ACODEC = "audio/mp4a-latm";
    public static String x264Preset = "veryfast";
    public static int vPrevWidth = 1920;
    public static int vPrevHeight = 1080;
    public static int vPortraitWidth = 1080;
    public static int vPortraitHeight = 1920;
    public static int vLandscapeWidth = 1920;
    public static int vLandscapeHeight = 1080;
    public static int vOutWidth = 1080;
    public static int vOutHeight = 1920;
    public static int vBitrate = 1228800;
    public static final int VFPS = 60;
    public static final int VGOP = 60;
    public static final int ASAMPLERATE = 44100;
    public static int aChannelConfig = 12;
    public static final int ABITRATE = 65536;
    private SrsEncodeHandler mHandler;
    private SrsFlvMuxer flvMuxer;
    private SrsMp4Muxer mp4Muxer;
    private MediaCodecInfo vmci;
    private MediaCodec vencoder;
    private MediaCodec aencoder;
    private MediaCodec screenEncodec;
    private BufferInfo mVideoBuffInfo;
    private boolean networkWeakTriggered = false;
    private boolean useSoftEncoder = false;
    private boolean canSoftEncode = false;
    private long mPresentTimeUs;
    private long mPausetime;
    private int mVideoColorFormat;
    private VirtualDisplay mVirtualDisplay;
    Surface mVideoInputSurface;
    MediaProjection mMediaProjection;
    private int videoDpi;
    private int videoFlvTrack;
    private int videoMp4Track;
    private int audioFlvTrack;
    private int audioMp4Track;

    public SrsEncoder(SrsEncodeHandler handler) {
        this.mHandler = handler;
        this.mVideoColorFormat = this.chooseVideoEncoder();
    }

    public void setFlvMuxer(SrsFlvMuxer flvMuxer) {
        this.flvMuxer = flvMuxer;
    }

    public void setMp4Muxer(SrsMp4Muxer mp4Muxer) {
        this.mp4Muxer = mp4Muxer;
    }

    public boolean start() {
        if (this.flvMuxer != null && this.mp4Muxer != null) {
            this.mPresentTimeUs = System.nanoTime() / 1000L;
            if (!this.useSoftEncoder && (vOutWidth % 32 != 0 || vOutHeight % 32 != 0) && this.vmci.getName().contains("MTK")) {
            }

            this.setEncoderResolution(vOutWidth, vOutHeight);
            this.setEncoderFps(60);
            this.setEncoderGop(60);
            this.setEncoderBitrate(vBitrate);
            this.setEncoderPreset(x264Preset);
            if (this.useSoftEncoder) {
                this.canSoftEncode = this.openSoftEncoder();
                if (!this.canSoftEncode) {
                    return false;
                }
            }

            try {
                this.aencoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
            } catch (IOException var5) {
                Log.e("SrsEncoder", "create aencoder failed.");
                var5.printStackTrace();
                return false;
            }

            int ach = aChannelConfig == 12 ? 2 : 1;
            MediaFormat audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, ach);
            audioFormat.setInteger("bitrate", 65536);
            audioFormat.setInteger("max-input-size", 0);
            this.aencoder.configure(audioFormat, (Surface)null, (MediaCrypto)null, 1);
            this.audioFlvTrack = this.flvMuxer.addTrack(audioFormat);
            this.audioMp4Track = this.mp4Muxer.addTrack(audioFormat);

            try {
                this.vencoder = MediaCodec.createByCodecName(this.vmci.getName());
            } catch (IOException var4) {
                Log.e("SrsEncoder", "create vencoder failed.");
                var4.printStackTrace();
                return false;
            }

            MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", vOutWidth, vOutHeight);
            videoFormat.setInteger("color-format", this.mVideoColorFormat);
            videoFormat.setInteger("max-input-size", 0);
            videoFormat.setInteger("bitrate", vBitrate);
            videoFormat.setInteger("frame-rate", 60);
            videoFormat.setInteger("i-frame-interval", 1);
            this.vencoder.configure(videoFormat, (Surface)null, (MediaCrypto)null, 1);
            this.videoFlvTrack = this.flvMuxer.addTrack(videoFormat);
            this.videoMp4Track = this.mp4Muxer.addTrack(videoFormat);
            this.vencoder.start();
            this.aencoder.start();
            return true;
        } else {
            return false;
        }
    }

    public void createSurface() {
        Log.i(Util.LOG_TAG, "LiveEncoder:createSurface");
        MediaFormat mediaFormat = new MediaFormat();
        this.screenEncodec = this.createHardVideoMediaCodec(mediaFormat, vPrevWidth, vPrevHeight);
        this.screenEncodec.configure(mediaFormat, (Surface)null, (MediaCrypto)null, 1);
        this.mVideoInputSurface = this.screenEncodec.createInputSurface();
    }

    public void setupMediaProjection(MediaProjection projection, int dpi) {
        Log.i(Util.LOG_TAG, "LiveEncoder:setupMediaProjection");
        this.mMediaProjection = projection;
        this.videoDpi = dpi;
    }

    public void createVirtualDisplay(Surface surface) {
        Log.i(Util.LOG_TAG, "LiveEncoder:createVirtualDisplay");
        if (this.mVirtualDisplay == null) {
            if (this.mMediaProjection == null) {
                Log.i(Util.LOG_TAG, "LiveEncoder:createVirtualDisplay:mMediaProjection == null");
            }

            if (surface == null) {
                Log.i(Util.LOG_TAG, "LiveEncoder:createVirtualDisplay:surface == null");
            }

            this.mVirtualDisplay = this.mMediaProjection.createVirtualDisplay("Live-VirtualDisplay", vPrevWidth, vPrevHeight, this.videoDpi, 1, surface, (Callback)null, (Handler)null);
        }
    }

    public void startScreen() {
        Log.i(Util.LOG_TAG, "LiveEncoder:startScreen");
        if (this.mVideoInputSurface == null) {
            this.createSurface();
        }

        if (this.mVirtualDisplay == null) {
            this.createVirtualDisplay(this.mVideoInputSurface);
        } else {
            this.mVirtualDisplay.setSurface(this.mVideoInputSurface);
        }

        this.screenEncodec.start();
    }

    public void stopScreen() {
        Log.i(Util.LOG_TAG, "LiveEncoder:stopScreen");
        this.screenEncodec.stop();
        if (this.mVideoInputSurface != null) {
            this.mVideoInputSurface.release();
            this.mVideoInputSurface = null;
        }

    }

    public MediaCodec createHardVideoMediaCodec(MediaFormat videoFormat, int width, int height) {
        Log.i(Util.LOG_TAG, "LiveEncoder:createHardVideoMediaCodec");
        videoFormat.setString("mime", "video/avc");
        videoFormat.setInteger("width", width);
        videoFormat.setInteger("height", height);
        videoFormat.setInteger("bitrate", vBitrate);
        videoFormat.setInteger("frame-rate", 60);
        videoFormat.setInteger("color-format", 2130708361);
        videoFormat.setInteger("repeat-previous-frame-after", 20000000);
        videoFormat.setInteger("i-frame-interval", 1);
        videoFormat.setInteger("channel-count", 1);
        MediaCodec result = null;

        try {
            result = MediaCodec.createEncoderByType(videoFormat.getString("mime"));
            return result;
        } catch (IOException var6) {
            return null;
        }
    }

    public void pause() {
        this.mPausetime = System.nanoTime() / 1000L;
    }

    public void resume() {
        long resumeTime = System.nanoTime() / 1000L - this.mPausetime;
        this.mPresentTimeUs += resumeTime;
        this.mPausetime = 0L;
    }

    public void stop() {
        if (this.useSoftEncoder) {
            this.closeSoftEncoder();
            this.canSoftEncode = false;
        }

        if (this.aencoder != null) {
            Log.i("SrsEncoder", "stop aencoder");

            try {
                this.aencoder.stop();
            } catch (IllegalStateException var3) {
                var3.printStackTrace();
            }

            this.aencoder.release();
            this.aencoder = null;
        }

        if (this.vencoder != null) {
            Log.i("SrsEncoder", "stop vencoder");

            try {
                this.vencoder.stop();
            } catch (IllegalStateException var2) {
                var2.printStackTrace();
            }

            this.vencoder.release();
            this.vencoder = null;
        }

    }

    public void switchToSoftEncoder() {
        this.useSoftEncoder = true;
    }

    public void switchToHardEncoder() {
        this.useSoftEncoder = false;
    }

    public boolean isSoftEncoder() {
        return this.useSoftEncoder;
    }

    public boolean canHardEncode() {
        return this.vencoder != null;
    }

    public boolean canSoftEncode() {
        return this.canSoftEncode;
    }

    public boolean isEnabled() {
        return this.canHardEncode() || this.canSoftEncode();
    }

    public void setPreviewResolution(int width, int height) {
        vPrevWidth = width;
        vPrevHeight = height;
    }

    public void setPortraitResolution(int width, int height) {
        vOutWidth = width;
        vOutHeight = height;
        vPortraitWidth = width;
        vPortraitHeight = height;
        vLandscapeWidth = height;
        vLandscapeHeight = width;
    }

    public void setLandscapeResolution(int width, int height) {
        vOutWidth = width;
        vOutHeight = height;
        vLandscapeWidth = width;
        vLandscapeHeight = height;
        vPortraitWidth = height;
        vPortraitHeight = width;
    }

    public void setVideoHDMode() {
        vBitrate = 1228800;
        x264Preset = "veryfast";
    }

    public void setVideoSmoothMode() {
        vBitrate = 512000;
        x264Preset = "superfast";
    }

    public void setVirtualDisplay(VirtualDisplay display) {
        this.mVirtualDisplay = display;
    }

    public int getPreviewWidth() {
        return vPrevWidth;
    }

    public int getPreviewHeight() {
        return vPrevHeight;
    }

    public int getOutputWidth() {
        return vOutWidth;
    }

    public int getOutputHeight() {
        return vOutHeight;
    }

    public void setScreenOrientation(int orientation) {
        if (orientation == 1) {
            vOutWidth = vPortraitWidth;
            vOutHeight = vPortraitHeight;
        } else if (orientation == 2) {
            vOutWidth = vLandscapeWidth;
            vOutHeight = vLandscapeHeight;
        }

        if (!this.useSoftEncoder && (vOutWidth % 32 != 0 || vOutHeight % 32 != 0) && this.vmci.getName().contains("MTK")) {
        }

        this.setEncoderResolution(vOutWidth, vOutHeight);
    }

    private void onProcessedYuvFrame(byte[] yuvFrame, long pts) {
        ByteBuffer[] inBuffers = this.vencoder.getInputBuffers();
        ByteBuffer[] outBuffers = this.vencoder.getOutputBuffers();
        int inBufferIndex = this.vencoder.dequeueInputBuffer(-1L);
        if (inBufferIndex >= 0) {
            ByteBuffer bb = inBuffers[inBufferIndex];
            bb.clear();
            bb.put(yuvFrame, 0, yuvFrame.length);
            this.vencoder.queueInputBuffer(inBufferIndex, 0, yuvFrame.length, pts, 0);
        }

        while(true) {
            BufferInfo vebi = new BufferInfo();
            int outBufferIndex = this.vencoder.dequeueOutputBuffer(vebi, 0L);
            if (outBufferIndex < 0) {
                return;
            }

            ByteBuffer bb = outBuffers[outBufferIndex];
            this.onEncodedAnnexbFrame(bb, vebi);
            this.vencoder.releaseOutputBuffer(outBufferIndex, false);
        }
    }

    private void onProcessedScreenFrame(long pts) {
        ByteBuffer[] outBuffers = this.screenEncodec.getOutputBuffers();

        while(true) {
            this.mVideoBuffInfo = new BufferInfo();
            int outBufferIndex = this.screenEncodec.dequeueOutputBuffer(this.mVideoBuffInfo, 0L);
            if (outBufferIndex < 0) {
                return;
            }

            this.mVideoBuffInfo.presentationTimeUs = pts;
            ByteBuffer bb = outBuffers[outBufferIndex];
            this.onEncodedAnnexbFrame(bb, this.mVideoBuffInfo);
            this.screenEncodec.releaseOutputBuffer(outBufferIndex, false);
        }
    }

    private void onSoftEncodedData(byte[] es, long pts, boolean isKeyFrame) {
        ByteBuffer bb = ByteBuffer.wrap(es);
        BufferInfo vebi = new BufferInfo();
        vebi.offset = 0;
        vebi.size = es.length;
        vebi.presentationTimeUs = pts;
        vebi.flags = isKeyFrame ? 1 : 0;
        this.onEncodedAnnexbFrame(bb, vebi);
    }

    private void onEncodedAnnexbFrame(ByteBuffer es, BufferInfo bi) {
        this.mp4Muxer.writeSampleData(this.videoMp4Track, es.duplicate(), bi);
        this.flvMuxer.writeSampleData(this.videoFlvTrack, es, bi);
    }

    private void onEncodedAacFrame(ByteBuffer es, BufferInfo bi) {
        this.mp4Muxer.writeSampleData(this.audioMp4Track, es.duplicate(), bi);
        this.flvMuxer.writeSampleData(this.audioFlvTrack, es, bi);
    }

    public void onGetPcmFrame(byte[] data, int size) {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            ByteBuffer[] inBuffers = this.aencoder.getInputBuffers();
            ByteBuffer[] outBuffers = this.aencoder.getOutputBuffers();
            int inBufferIndex = this.aencoder.dequeueInputBuffer(-1L);
            if (inBufferIndex >= 0) {
                ByteBuffer bb = inBuffers[inBufferIndex];
                bb.clear();
                bb.put(data, 0, size);
                long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
                this.aencoder.queueInputBuffer(inBufferIndex, 0, size, pts, 0);
            }

            while(true) {
                BufferInfo aebi = new BufferInfo();
                int outBufferIndex = this.aencoder.dequeueOutputBuffer(aebi, 0L);
                if (outBufferIndex < 0) {
                    break;
                }

                ByteBuffer bb = outBuffers[outBufferIndex];
                this.onEncodedAacFrame(bb, aebi);
                this.aencoder.releaseOutputBuffer(outBufferIndex, false);
            }
        }

    }

    public void onGetScreenFrame() {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
            this.onProcessedScreenFrame(pts);
        }

        if (this.networkWeakTriggered) {
            this.networkWeakTriggered = false;
            this.mHandler.notifyNetworkResume();
        }

    }

    public void onGetRgbaFrame(byte[] data, int width, int height) {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
            if (this.useSoftEncoder) {
                this.swRgbaFrame(data, width, height, pts);
            } else {
                byte[] processedData = this.hwRgbaFrame(data, width, height);
                if (processedData != null) {
                    this.onProcessedYuvFrame(processedData, pts);
                } else {
                    this.mHandler.notifyEncodeIllegalArgumentException(new IllegalArgumentException("libyuv failure"));
                }
            }

            if (this.networkWeakTriggered) {
                this.networkWeakTriggered = false;
                this.mHandler.notifyNetworkResume();
            }
        } else {
            this.mHandler.notifyNetworkWeak();
            this.networkWeakTriggered = true;
        }

    }

    public void onGetYuvNV21Frame(byte[] data, int width, int height, Rect boundingBox) {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
            if (this.useSoftEncoder) {
                throw new UnsupportedOperationException("Not implemented");
            }

            byte[] processedData = this.hwYUVNV21FrameScaled(data, width, height, boundingBox);
            if (processedData != null) {
                this.onProcessedYuvFrame(processedData, pts);
            } else {
                this.mHandler.notifyEncodeIllegalArgumentException(new IllegalArgumentException("libyuv failure"));
            }

            if (this.networkWeakTriggered) {
                this.networkWeakTriggered = false;
                this.mHandler.notifyNetworkResume();
            }
        } else {
            this.mHandler.notifyNetworkWeak();
            this.networkWeakTriggered = true;
        }

    }

    public void onGetArgbFrame(int[] data, int width, int height, Rect boundingBox) {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
            if (this.useSoftEncoder) {
                throw new UnsupportedOperationException("Not implemented");
            }

            byte[] processedData = this.hwArgbFrameScaled(data, width, height, boundingBox);
            if (processedData != null) {
                this.onProcessedYuvFrame(processedData, pts);
            } else {
                this.mHandler.notifyEncodeIllegalArgumentException(new IllegalArgumentException("libyuv failure"));
            }

            if (this.networkWeakTriggered) {
                this.networkWeakTriggered = false;
                this.mHandler.notifyNetworkResume();
            }
        } else {
            this.mHandler.notifyNetworkWeak();
            this.networkWeakTriggered = true;
        }

    }

    public void onGetArgbFrame(int[] data, int width, int height) {
        AtomicInteger videoFrameCacheNumber = this.flvMuxer.getVideoFrameCacheNumber();
        if (videoFrameCacheNumber != null && videoFrameCacheNumber.get() < 60) {
            long pts = System.nanoTime() / 1000L - this.mPresentTimeUs;
            if (this.useSoftEncoder) {
                throw new UnsupportedOperationException("Not implemented");
            }

            byte[] processedData = this.hwArgbFrame(data, width, height);
            if (processedData != null) {
                this.onProcessedYuvFrame(processedData, pts);
            } else {
                this.mHandler.notifyEncodeIllegalArgumentException(new IllegalArgumentException("libyuv failure"));
            }

            if (this.networkWeakTriggered) {
                this.networkWeakTriggered = false;
                this.mHandler.notifyNetworkResume();
            }
        } else {
            this.mHandler.notifyNetworkWeak();
            this.networkWeakTriggered = true;
        }

    }

    private byte[] hwRgbaFrame(byte[] data, int width, int height) {
        switch(this.mVideoColorFormat) {
            case 19:
                return this.RGBAToI420(data, width, height, true, 180);
            case 21:
                return this.RGBAToNV12(data, width, height, true, 180);
            default:
                throw new IllegalStateException("Unsupported color format!");
        }
    }

    private byte[] hwYUVNV21FrameScaled(byte[] data, int width, int height, Rect boundingBox) {
        switch(this.mVideoColorFormat) {
            case 19:
                return this.NV21ToI420Scaled(data, width, height, true, 180, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());
            case 21:
                return this.NV21ToNV12Scaled(data, width, height, true, 180, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());
            default:
                throw new IllegalStateException("Unsupported color format!");
        }
    }

    private byte[] hwArgbFrameScaled(int[] data, int width, int height, Rect boundingBox) {
        switch(this.mVideoColorFormat) {
            case 19:
                return this.ARGBToI420Scaled(data, width, height, false, 0, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());
            case 21:
                return this.ARGBToNV12Scaled(data, width, height, false, 0, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());
            default:
                throw new IllegalStateException("Unsupported color format!");
        }
    }

    private byte[] hwArgbFrame(int[] data, int inputWidth, int inputHeight) {
        switch(this.mVideoColorFormat) {
            case 19:
                return this.ARGBToI420(data, inputWidth, inputHeight, false, 0);
            case 21:
                return this.ARGBToNV12(data, inputWidth, inputHeight, false, 0);
            default:
                throw new IllegalStateException("Unsupported color format!");
        }
    }

    private void swRgbaFrame(byte[] data, int width, int height, long pts) {
        Log.i("LiveLib", "SrsEncoder:swRgbaFrame");
        this.RGBASoftEncode(data, width, height, true, 180, pts);
    }

    public AudioRecord chooseAudioRecord() {
        AudioRecord mic = new AudioRecord(0, 44100, 12, 2, this.getPcmBufferSize() * 4);
        aChannelConfig = 12;
        return mic;
    }

    private int getPcmBufferSize() {
        int pcmBufSize = AudioRecord.getMinBufferSize(44100, 12, 2) + 8191;
        return pcmBufSize - pcmBufSize % 8192;
    }

    private MediaCodecInfo chooseVideoEncoder(String name) {
        int nbCodecs = MediaCodecList.getCodecCount();

        for(int i = 0; i < nbCodecs; ++i) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (mci.isEncoder()) {
                String[] types = mci.getSupportedTypes();

                for(int j = 0; j < types.length; ++j) {
                    if (types[j].equalsIgnoreCase("video/avc")) {
                        Log.i("SrsEncoder", String.format("vencoder %s types: %s", mci.getName(), types[j]));
                        if (name == null) {
                            return mci;
                        }

                        if (mci.getName().contains(name)) {
                            return mci;
                        }
                    }
                }
            }
        }

        return null;
    }

    private int chooseVideoEncoder() {
        this.vmci = this.chooseVideoEncoder((String)null);
        int matchedColorFormat = 0;
        CodecCapabilities cc = this.vmci.getCapabilitiesForType("video/avc");

        int i;
        for(i = 0; i < cc.colorFormats.length; ++i) {
            int cf = cc.colorFormats[i];
            Log.i("SrsEncoder", String.format("vencoder %s supports color fomart 0x%x(%d)", this.vmci.getName(), cf, cf));
            if (cf >= 19 && cf <= 21 && cf > matchedColorFormat) {
                matchedColorFormat = cf;
            }
        }

        for(i = 0; i < cc.profileLevels.length; ++i) {
            CodecProfileLevel pl = cc.profileLevels[i];
            Log.i("SrsEncoder", String.format("vencoder %s support profile %d, level %d", this.vmci.getName(), pl.profile, pl.level));
        }

        Log.i("SrsEncoder", String.format("vencoder %s choose color format 0x%x(%d)", this.vmci.getName(), matchedColorFormat, matchedColorFormat));
        return matchedColorFormat;
    }

    private native void setEncoderResolution(int var1, int var2);

    private native void setEncoderFps(int var1);

    private native void setEncoderGop(int var1);

    private native void setEncoderBitrate(int var1);

    private native void setEncoderPreset(String var1);

    private native byte[] RGBAToI420(byte[] var1, int var2, int var3, boolean var4, int var5);

    private native byte[] RGBAToNV12(byte[] var1, int var2, int var3, boolean var4, int var5);

    private native byte[] ARGBToI420Scaled(int[] var1, int var2, int var3, boolean var4, int var5, int var6, int var7, int var8, int var9);

    private native byte[] ARGBToNV12Scaled(int[] var1, int var2, int var3, boolean var4, int var5, int var6, int var7, int var8, int var9);

    private native byte[] ARGBToI420(int[] var1, int var2, int var3, boolean var4, int var5);

    private native byte[] ARGBToNV12(int[] var1, int var2, int var3, boolean var4, int var5);

    private native byte[] NV21ToNV12Scaled(byte[] var1, int var2, int var3, boolean var4, int var5, int var6, int var7, int var8, int var9);

    private native byte[] NV21ToI420Scaled(byte[] var1, int var2, int var3, boolean var4, int var5, int var6, int var7, int var8, int var9);

    private native int RGBASoftEncode(byte[] var1, int var2, int var3, boolean var4, int var5, long var6);

    private native boolean openSoftEncoder();

    private native void closeSoftEncoder();

    static {
        System.loadLibrary("yuv");
        System.loadLibrary("enc");
    }
}
