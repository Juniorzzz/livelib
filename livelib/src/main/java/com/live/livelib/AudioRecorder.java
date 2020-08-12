package com.live.livelib;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecorder {

    private AudioRecord audioRecord;
    private int bufferSizeInBytes;
    private boolean start = false;
    private int readSize = 0;

    private OnRecordLisener onRecordLisener;

    public AudioRecorder() {
        Log.i(Util.LOG_TAG, "AudioRecorder:AudioRecorder");
        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes
        );
    }

    public void setOnRecordLisener(OnRecordLisener onRecordLisener) {
        Log.i(Util.LOG_TAG, "AudioRecorder:setOnRecordLisener");

        this.onRecordLisener = onRecordLisener;
    }

    public void startRecord() {
        Log.i(Util.LOG_TAG, "AudioRecorder:startRecord");

        new Thread() {
            @Override
            public void run() {
                super.run();
                start = true;
                audioRecord.startRecording();

                byte[] audiodata = new byte[bufferSizeInBytes];
                while (start) {
                    readSize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
                    if (onRecordLisener != null) {
                        onRecordLisener.recordByte(audiodata, readSize);
                    }
                }

                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
            }
        }.start();
    }

    public void stopRecord() {
        start = false;
    }

    public interface OnRecordLisener {
        void recordByte(byte[] audioData, int readSize);
    }

    public boolean isStart() {
        return start;
    }
}
