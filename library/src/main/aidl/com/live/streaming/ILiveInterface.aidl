// ILiveInterface.aidl
package com.live.streaming;

// Declare any non-default types here with import statements

interface ILiveInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void CreateVirtualDisplay();
    void InitLive(int width, int height, int fps);
    void StartLive(String url);
    void StopLive();

    void WriteVideoStream(in byte[] data, int width, int height);
    void WriteAudioStream(in byte[] data, int len);
}
