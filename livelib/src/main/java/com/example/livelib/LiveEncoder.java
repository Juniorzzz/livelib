package com.example.livelib;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static android.content.ContentValues.TAG;

public class LiveEncoder {

    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;
    private int videoWidth, videoHeight, videoFps;

    private MediaCodec.BufferInfo mAudioBuffInfo;
    private MediaCodec mAudioEncodec;
    private int audioChannel, audioSampleRate, audioSampleBit;

    private VideoEncodecThread mVideoEncodecThread;
    private AudioEncodecThread mAudioEncodecThread;

    private OnMediaInfoListener onMediaInfoListener;

    final int TIMEOUT_USEC = 10000;
    long generateIndex = 0;

    private byte[] sps, pps;
    private boolean encodeStart, videoExit, audioExit;

    public void start() {
        mVideoEncodecThread = new VideoEncodecThread(new WeakReference<>(this));
        mVideoEncodecThread.start();

//        mAudioEncodecThread = new AudioEncodecThread(new WeakReference<>(this));
//        mAudioEncodecThread.start();
    }

    public void stop() {
        if (mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();
            mVideoEncodecThread = null;
        }

        if (mAudioEncodecThread != null) {
            mAudioEncodecThread.exit();
            mAudioEncodecThread = null;
        }
    }

    public void initEncoder(int width, int height, int fps, int sampleRate, int channel, int sampleBit) {
        initLiveVideo(width, height, fps);
        initLiveAudio(channel, sampleRate, sampleBit);
    }

    public void initLiveVideo(int width, int height, int fps) {
        videoWidth = width;
        videoHeight = height;
        videoFps = fps;
        try {
            mVideoEncodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            MediaFormat videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);//30帧
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);//RGBA
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            //设置压缩等级  默认是baseline
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
                }
            }

            mVideoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mVideoBuffInfo = null;
        }
    }

    public void initLiveAudio(int channel, int sampleRate, int sampleBit) {
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

    private void encodeYUV420SP(byte[] yuv420sp, byte[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = ((int) argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = ((int) argb[index] & 0xff0000) >> 16;
                G = ((int) argb[index] & 0xff00) >> 8;
                B = ((int) argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V

                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    private void encodeYUV420P(byte[] yuv420sp, byte[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + width * height / 4;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = ((int) argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = ((int) argb[index] & 0xff0000) >> 16;
                G = ((int) argb[index] & 0xff00) >> 8;
                B = ((int) argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                V = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128; // Previously U
                U = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128; // Previously V

                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[vIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv420sp[uIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }

                index++;
            }
        }
    }


    /**
     * RGB图片转YUV420数据
     * 宽、高不能为奇数
     *
     * @param pixels 图片像素集合
     * @param width  宽
     * @param height 高
     * @return
     */
    public void rgb2YCbCr420(byte[] yuv420, byte[] pixels, int width, int height) {
        int len = width * height;
        //yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
//        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //屏蔽ARGB的透明度值
                int rgb = pixels[i * width + j] & 0x00FFFFFF;
                //像素的颜色顺序为bgr，移位运算。
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                //套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                //调整
                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                //赋值
                yuv420[i * width + j] = (byte) y;
                yuv420[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                yuv420[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / videoFps;
    }

    public void WriteVideoStreamRGB(byte[] rgbData) {
        byte[] yuvData = new byte[videoWidth * videoHeight * 3 / 2];
        encodeYUV420SP(yuvData, rgbData, videoWidth, videoHeight);

        WriteVideoStreamYUV(yuvData);
    }

    public void WriteVideoStreamYUV(byte[] yuvData) {
        ByteBuffer[] buffers = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            buffers = mVideoEncodec.getInputBuffers();
        }

        int inputBufferIndex = mVideoEncodec.dequeueInputBuffer(TIMEOUT_USEC);
        if (inputBufferIndex >= 0) {
            long ptsUsec = computePresentationTime(generateIndex);

            //有效的空的缓存区
            ByteBuffer inputBuffer = null;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = buffers[inputBufferIndex];
            } else {
                inputBuffer = mVideoEncodec.getInputBuffer(inputBufferIndex);//inputBuffers[inputBufferIndex];
            }
            inputBuffer.clear();
            inputBuffer.put(yuvData);
            //将数据放到编码队列
//            mVideoEncodec.queueInputBuffer(inputBufferIndex, 0, yuvData.length, ptsUsec, 0);
            mVideoEncodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(), System.nanoTime() / 1000, 0);


            generateIndex++;
        } else {
            Log.i(TAG, "input buffer not available");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void WriteAudioStream(byte[] data) {

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

        @Override
        public void run() {

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
