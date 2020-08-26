package com.example.unityandroidlive;

import com.github.faucamp.simplertmp.RtmpHandler;

import java.io.IOException;
import java.net.SocketException;

public class CustomRtmpHandler implements RtmpHandler.RtmpListener {

    @Override
    public void onRtmpConnecting(String msg){

    }

    @Override
    public void onRtmpConnected(String msg){

    }

    @Override
    public void onRtmpVideoStreaming(){

    }

    @Override
    public void onRtmpAudioStreaming(){

    }

    @Override
    public void onRtmpStopped(){

    }

    @Override
    public void onRtmpDisconnected(){

    }

    @Override
    public void onRtmpVideoFpsChanged(double fps){

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate){

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate){

    }

    @Override
    public void onRtmpSocketException(SocketException e){

    }

    @Override
    public void onRtmpIOException(IOException e){

    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e){

    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e){

    }

}
