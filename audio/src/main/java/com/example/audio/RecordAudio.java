package com.example.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordAudio {

    private final int kSampleRate = 44100;
    private final int kChannelMode = AudioFormat.CHANNEL_IN_STEREO;
    private final int kEncodeFormat = AudioFormat.ENCODING_PCM_16BIT;

    private String Log_Tag = "LiveLib";
    AudioRecord mRecord = null;

    public void InitRecoder(){

        Log.i(Log_Tag, "RecordAudio:InitRecoder" );
        int minBufferSize = AudioRecord.getMinBufferSize(kSampleRate, kChannelMode,
                kEncodeFormat);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                kSampleRate, kChannelMode, kEncodeFormat, minBufferSize * 2);
    }

    public void Record(){
        Log.i(Log_Tag, "RecordAudio:Record" );
        mRecord.startRecording();
    }
}
