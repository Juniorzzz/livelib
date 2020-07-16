package com.example.livelib.yuv;

public class YuvHelper {
    static {
        System.loadLibrary("libyuv-lib");
    }

    public static native int RgbaToI420(int type,byte[] rgba,byte[] yuv,int width,int height);
}
