package com.live.livelib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import android.media.projection.MediaProjection;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unity3d.player.UnityPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class LiveEncoder {

    private Surface videoInputSurface;
    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;
    private int videoWidth, videoHeight, videoFps, videoBit;
    private MediaCodec.BufferInfo mAudioBuffInfo;
    private MediaCodec mAudioEncodec;
    private int audioChannel, audioSampleRate, audioSampleBit;

    private VideoEncodecThread mVideoEncodecThread;
    private AudioEncodecThread mAudioEncodecThread;

    private OnMediaInfoListener onMediaInfoListener;

    private byte[] sps, pps;
    private boolean encodeStart, videoExit, audioExit;

    private AudioRecorder mAudioRecorder;

    public void start() {
        Log.i(Util.LOG_TAG, "LiveEncoder:start");

        mVideoEncodecThread = new VideoEncodecThread(new WeakReference<>(this));
        mVideoEncodecThread.start();

//        mAudioEncodecThread = new AudioEncodecThread(new WeakReference<>(this));
//        mAudioEncodecThread.start();

//        mAudioRecorder.startRecord();

        Intent intent = new Intent(Scheduler.Instance().currentActivity, RefreshActivity.class);
        Scheduler.Instance().currentActivity.startActivity(intent);
    }

    public void stop() {

        Log.i(Util.LOG_TAG, "LiveEncoder:stop");

        if (mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();
            mVideoEncodecThread = null;
        }

        if (mAudioEncodecThread != null) {
            mAudioEncodecThread.exit();
            mAudioEncodecThread = null;
        }
    }

    public void initEncoder(int width, int height, int fps, int bit, int sampleRate, int channel, int sampleBit) {

        Log.i(Util.LOG_TAG, "LiveEncoder:initEncoder");

        initLiveVideo(width, height, fps, bit);
//        initLiveAudio(channel, sampleRate, sampleBit);
//
//        initPcmRecoder();
    }

    public void initLiveVideo(int width, int height, int fps, int bit) {

        Log.i(Util.LOG_TAG, "LiveEncoder:initLiveVideo");

        videoWidth = width;
        videoHeight = height;
        videoFps = fps;
        videoBit = bit;

        MediaFormat mediaFormat = new MediaFormat();
        mVideoEncodec = createHardVideoMediaCodec(mediaFormat, videoWidth, videoHeight);
        mVideoEncodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        videoInputSurface = mVideoEncodec.createInputSurface();
        mVideoBuffInfo = new MediaCodec.BufferInfo();

        Scheduler.Instance().mediaHelper.CreateVirtualDisplay(videoWidth, videoHeight, videoInputSurface);
    }

    public void initLiveAudio(int channel, int sampleRate, int sampleBit) {
        Log.i(Util.LOG_TAG, "LiveEncoder:initLiveAudio");

        audioChannel = channel;
        audioSampleRate = sampleRate;
        audioSampleBit = sampleBit;

        try {
            mAudioEncodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channel);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 10);
            mAudioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mAudioBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            mAudioEncodec = null;
            mAudioBuffInfo = null;
        }
    }

    public MediaCodec createHardVideoMediaCodec(MediaFormat videoFormat, int width, int height) {
        Log.i(Util.LOG_TAG, "LiveEncoder:createHardVideoMediaCodec");

        videoFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC);
        videoFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        videoFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBit);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoFps);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline);
        // 当画面静止时,重复最后一帧，不影响界面显示
        videoFormat.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / 45);
        videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31);
        videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        videoFormat.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        MediaCodec result = null;
        try {
            result = MediaCodec.createEncoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            return null;
        }
        return result;
    }

    private void initPcmRecoder(){
        Log.i(Util.LOG_TAG, "LiveEncoder:initPcmRecoder");

        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.setOnRecordLisener(new AudioRecorder.OnRecordLisener() {
            @Override
            public void recordByte(byte[] audioData, int readSize) {
                if(encodeStart){
//                    putPcmData(audioData,readSize);
                }
            }
        });
    }

    public void putPcmData(byte[] buffer, int size) {

        Log.i(Util.LOG_TAG, "LiveEncoder:putPcmData");

        if (mAudioEncodecThread != null && !mAudioEncodecThread.isExit && buffer != null && size > 0) {
            int inputBufferIndex = mAudioEncodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer byteBuffer = mAudioEncodec.getInputBuffer(inputBufferIndex);
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size, audioSampleRate, audioChannel, audioSampleBit);
//                Log.e("zzz", "AudioTime = " + pts / 1000000.0f);
                mAudioEncodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
            }
        }
    }

    private long audioPts;

    //176400
    private long getAudioPts(int size, int sampleRate, int channel, int sampleBit) {
        audioPts += (long) (1.0 * size / (sampleRate * channel * (sampleBit / 8)) * 1000000.0);
        return audioPts;
    }

    public void WriteAudioStream(byte[] data, int len) {
        Log.i(Util.LOG_TAG, "LiveEncoder:WriteAudioStream");
        putPcmData(data, len);
    }

    static class VideoEncodecThread extends Thread {
        private WeakReference<LiveEncoder> encoderWeakReference;
        private boolean isExit;

        private long pts;

        private MediaCodec videoEncodec;
        private MediaCodec.BufferInfo videoBufferinfo;

        public VideoEncodecThread(WeakReference<LiveEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;

            videoEncodec = encoderWeakReference.get().mVideoEncodec;
            videoBufferinfo = encoderWeakReference.get().mVideoBuffInfo;
            pts = 0;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            videoEncodec.start();
            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    encoderWeakReference.get().videoExit = true;

                    if (encoderWeakReference.get().audioExit) {
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.END);
                        }
                    }
                    break;
                }

                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    ByteBuffer spsb = videoEncodec.getOutputFormat().getByteBuffer("csd-0");
                    encoderWeakReference.get().sps = new byte[spsb.remaining()];
                    spsb.get(encoderWeakReference.get().sps, 0, encoderWeakReference.get().sps.length);
//                    Log.e("pest", "sps: " + ByteUtil.bytesToHexSpaceString(encoderWeakReference.get().sps));

                    ByteBuffer ppsb = videoEncodec.getOutputFormat().getByteBuffer("csd-1");
                    encoderWeakReference.get().pps = new byte[ppsb.remaining()];
                    ppsb.get(encoderWeakReference.get().pps, 0, encoderWeakReference.get().pps.length);
//                    Log.e("pest", "pps: " + ByteUtil.bytesToHexSpaceString(encoderWeakReference.get().pps));


                    if (!encoderWeakReference.get().encodeStart) {
                        encoderWeakReference.get().encodeStart = true;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.START);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encoderWeakReference.get().encodeStart) {
                            SystemClock.sleep(10);
                            continue;
                        }
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;
//                        Log.e("pest", "VideoTime = " + videoBufferinfo.presentationTimeUs / 1000000.0f);

                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onEncodeSPSPPSInfo(encoderWeakReference.get().sps,
                                    encoderWeakReference.get().pps);
                        }

                        //写入数据
                        byte[] data = new byte[outputBuffer.remaining()];
                        outputBuffer.get(data, 0, data.length);
                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onEncodeVideoDataInfo(data,
                                    videoBufferinfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME);
                        }

                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    static class AudioEncodecThread extends Thread {
        private WeakReference<LiveEncoder> encoderWeakReference;
        private boolean isExit;

        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo audioBufferinfo;

        private long pts;


        public AudioEncodecThread(WeakReference<LiveEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;
            audioEncodec = encoderWeakReference.get().mAudioEncodec;
            audioBufferinfo = encoderWeakReference.get().mAudioBuffInfo;
            pts = 0;
        }


        @Override
        public void run() {
            super.run();
            isExit = false;
            audioEncodec.start();

            while (true) {
                if (isExit) {
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    encoderWeakReference.get().audioExit = true;

                    //如果video退出了
                    if (encoderWeakReference.get().videoExit) {
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.END);
                        }

                    }
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!encoderWeakReference.get().encodeStart) {
                        encoderWeakReference.get().encodeStart = true;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.START);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encoderWeakReference.get().encodeStart) {
                            SystemClock.sleep(10);
                            continue;
                        }

                        ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(audioBufferinfo.offset);
                        outputBuffer.limit(audioBufferinfo.offset + audioBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = audioBufferinfo.presentationTimeUs;
                        }
                        audioBufferinfo.presentationTimeUs = audioBufferinfo.presentationTimeUs - pts;

                        //写入数据
                        byte[] data = new byte[outputBuffer.remaining()];
                        outputBuffer.get(data, 0, data.length);
                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onEncodeAudioInfo(data);
                        }

                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                    }
                }
            }
        }
        public void exit() {
            isExit = true;
        }
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);

        void onEncodeSPSPPSInfo(byte[] sps, byte[] pps);

        void onEncodeVideoDataInfo(byte[] data, boolean keyFrame);

        void onEncodeAudioInfo(byte[] data);
    }

    private OnStatusChangeListener onStatusChangeListener;

    public void setOnStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        this.onStatusChangeListener = onStatusChangeListener;
    }

    public interface OnStatusChangeListener {
        void onStatusChange(STATUS status);

        enum STATUS {
            INIT,
            START,
            END
        }

    }
}
